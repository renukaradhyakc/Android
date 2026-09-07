package com.thelinkphone.app.utils;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

/**
 * Instrumentation for the incoming-call pipeline: screening -> permission API ->
 * contact lookup -> popup/notification -> user action.
 *
 * One instance per incoming call. Create it fresh at the top of onScreenCall(),
 * carry it through the async callbacks for that call, discard it when the call ends.
 * Do NOT reuse a single static instance across calls -- callId/timing state would leak
 * between calls that overlap or race.
 */
public class CallAnalyticsHelper {

    // TODO: move to a BuildConfig field populated from a git-ignored gradle.properties
    // value, not a hardcoded literal in source control.
    private static final String PEPPER = "callalink_v1_9f2b";
    private static FirebaseAnalytics analytics;
    private static FirebaseCrashlytics crashlytics;

    private final String callId;
    private final long callStartMs;
    private String hashedNumber;

    public static void init(Context context) {
        if (analytics == null) analytics = FirebaseAnalytics.getInstance(context);
        if (crashlytics == null) crashlytics = FirebaseCrashlytics.getInstance();
    }

    public CallAnalyticsHelper() {
        this.callId = UUID.randomUUID().toString();
        this.callStartMs = System.currentTimeMillis();
    }

    public String getCallId() {
        return callId;
    }

    // ---------- Stage 1: screening started ----------

    public void logScreeningStarted(@Nullable String rawPhoneNumber) {
        hashedNumber = hashNumber(rawPhoneNumber);
        setCrashlyticsKeys();

        Bundle b = base();
        log("call_screening_started", b);
    }

    // ---------- Stage 2: manual block check ----------

    public void logBlockCheckResult(boolean isBlocked) {
        Bundle b = base();
        b.putBoolean("is_blocked", isBlocked);
        log("call_block_check_result", b);
    }

    // ---------- Stage 3: /api/call-permission ----------

    public void logPermissionApiResult(long latencyMs, int httpStatus,
                                       boolean allowed, boolean success) {
        Bundle b = base();
        b.putLong("latency_ms", latencyMs);
        b.putInt("http_status", httpStatus);
        b.putBoolean("allowed", allowed);
        b.putBoolean("success", success);
        log("call_permission_api_result", b);

        // Slow calls are exactly what ate your foreground-service exemption window earlier
        // this session -- worth a non-fatal breadcrumb if it's getting close to the ~10s cliff.
        if (success && latencyMs > 6000) {
            recordNonFatal("Slow call-permission API response",
                    "latency_ms=" + latencyMs + " call_id=" + callId);
        }
    }

    // ---------- Stage 4: contact lookup ----------

    public void logContactLookupResult(boolean found, long lookupMs) {
        Bundle b = base();
        b.putBoolean("found", found);
        b.putLong("lookup_ms", lookupMs);
        log("call_contact_lookup_result", b);
    }

    // ---------- Stage 5: popup attempt / result (per call site) ----------

    /** source: "launchActivityCall" or "onAddCall" */
    public void logPopupAttempt(@NonNull String source, boolean phoneNumberPresent,
                                boolean overlayPermissionGranted) {
        Bundle b = base();
        b.putString("source", source);
        b.putBoolean("phone_number_present", phoneNumberPresent);
        b.putBoolean("overlay_permission", overlayPermissionGranted);
        log("call_popup_attempt", b);

        if (!phoneNumberPresent) {
            // exactly the bug we found today -- launchActivityCall() passing null phoneNumber
            recordNonFatal("Popup attempt missing phone number",
                    "source=" + source + " call_id=" + callId);
        }
    }

    /** result: "shown" | "already_visible" | "skipped_no_overlay" | "fg_service_denied" */
    public void logPopupResult(@NonNull String source, @NonNull String result) {
        Bundle b = base();
        b.putString("source", source);
        b.putString("result", result);
        log("call_popup_result", b);

        if (result.equals("skipped_no_overlay") || result.equals("fg_service_denied")) {
            recordNonFatal("Popup did not display",
                    "source=" + source + " result=" + result + " call_id=" + callId);
        }
    }

    // ---------- Stage 6: notification ----------

    public void logNotificationShown() {
        log("call_notification_shown", base());
    }

    // ---------- Stage 7: user action ----------

    /** action: "answered" | "missed" | "rejected" */
    public void logUserAction(@NonNull String action) {
        Bundle b = base();
        b.putString("action", action);
        b.putLong("time_to_action_ms", System.currentTimeMillis() - callStartMs);
        log("call_user_action", b);
    }

    // ---------- Stage 8: screening completed ----------

    public void logScreeningCompleted() {
        Bundle b = base();
        b.putLong("total_duration_ms", System.currentTimeMillis() - callStartMs);
        log("call_screening_completed", b);
    }

    // ---------- internals ----------

    private Bundle base() {
        Bundle b = new Bundle();
        b.putString("call_id", callId);
        if (hashedNumber != null) b.putString("caller_hash", hashedNumber);
        return b;
    }

    private void setCrashlyticsKeys() {
        if (crashlytics == null) return;
        crashlytics.setCustomKey("call_id", callId);
        if (hashedNumber != null) crashlytics.setCustomKey("caller_hash", hashedNumber);
    }

    private void log(String eventName, Bundle params) {
        if (analytics != null) analytics.logEvent(eventName, params);
    }

    private void recordNonFatal(String message, String detail) {
        if (crashlytics == null) return;
        crashlytics.log(detail);
        crashlytics.recordException(new RuntimeException(message));
    }

    /** SHA-256(number + pepper), truncated. Deterministic -- same number always hashes the same,
     *  so you can count repeat callers without ever storing/sending the raw number. */
    private static String hashNumber(@Nullable String rawPhoneNumber) {
        if (rawPhoneNumber == null) return null;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest((rawPhoneNumber + PEPPER).getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8; i++) { // 16 hex chars is plenty of entropy for this use
                sb.append(String.format("%02x", hash[i]));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException | java.io.UnsupportedEncodingException e) {
            return null;
        }
    }

    /** Debug-only display mask, e.g. "98*****210". Never send this to analytics --
     *  it's for local logcat/Crashlytics logs meant for developer eyes only. */
    public static String maskForDebugLog(@Nullable String rawPhoneNumber) {
        if (rawPhoneNumber == null || rawPhoneNumber.length() < 6) return "****";
        int len = rawPhoneNumber.length();
        return rawPhoneNumber.substring(0, 2) + "*****" + rawPhoneNumber.substring(len - 3);
    }
}