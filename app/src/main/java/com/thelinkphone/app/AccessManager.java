package com.thelinkphone.app;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import androidx.annotation.NonNull;

import com.revenuecat.purchases.CustomerInfo;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.interfaces.LogInCallback;

import com.thelinkphone.app.model.TrialStatusResponse;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.utils.TrialStartDialog;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class AccessManager {

    private static final String TAG = "AccessManager";
    private static final String PREFS = "app_prefs";
    private static final String TRIAL_ACTIVE_KEY = "trial_active";
    private static final String ENTITLEMENT_ID = "CallALink Premium";

    private static final String CACHE_PREFS = "access_cache";
    private static final String KEY_ACCESS_GRANTED = "access_granted";
    private static final String KEY_LAST_CHECK = "last_check_timestamp";
    private static final String KEY_SUBSCRIPTION_ACTIVE = "subscription_active";
    private static final String KEY_TRIAL_ACTIVE = "trial_active_cache";
    private static final long CACHE_TTL_MS = 6 * 60 * 60 * 1000;
    private static AccessCache memoryCache = null;
    private static long lastMemoryCacheUpdate = 0;

    private AccessManager() {}

    public interface AccessCallback {
        void onAccessGranted();
        void onAccessDenied(String reason);
    }

    public interface SubscriptionResult {
        void onResult(boolean isSubscribed);
    }

    public static void checkAccess(
            Activity activity,
            String email,
            AccessCallback callback
    ) {
        if (activity == null || email == null || email.trim().isEmpty()) {
            Log.e(TAG, "Invalid parameters for access check");
            callback.onAccessDenied("INVALID_PARAMETERS");
            return;
        }

        AccessCache cached = getMemoryCache(activity);
        if (cached != null && cached.isValid()) {
            Log.d(TAG, "Using in-memory cache for access check");
            handleCachedResult(cached, callback);
            return;
        }

        cached = getPersistentCache(activity);
        if (cached != null && cached.isValid()) {
            Log.d(TAG, "Using persistent cache for access check");
            updateMemoryCache(cached);
            handleCachedResult(cached, callback);
            return;
        }

        Log.d(TAG, "Cache miss - performing network access check");
        performNetworkAccessCheck(activity, email, callback);
    }

    public static void forceRefresh(
            Activity activity,
            String email,
            AccessCallback callback
    ) {
        Log.d(TAG, "Force refresh requested - bypassing cache");
        invalidateCache(activity);
        performNetworkAccessCheck(activity, email, callback);
    }

    private static void checkSubscription(
            Activity activity,
            String email,
            SubscriptionResult result
    ) {
        String currentUserId = Purchases.getSharedInstance().getAppUserID();

        if (email.equals(currentUserId)) {
            Purchases.getSharedInstance().getCustomerInfo(
                    new com.revenuecat.purchases.interfaces.ReceiveCustomerInfoCallback() {
                        @Override
                        public void onReceived(@NonNull CustomerInfo customerInfo) {
                            boolean active = isEntitlementActive(customerInfo);
                            result.onResult(active);
                        }

                        @Override
                        public void onError(@NonNull com.revenuecat.purchases.PurchasesError error) {
                            Log.e(TAG, "RevenueCat customer info failed: " + error.getMessage());
                            result.onResult(false);
                        }
                    }
            );
        } else {
            Purchases.getSharedInstance().logIn(email, new LogInCallback() {
                @Override
                public void onReceived(@NonNull CustomerInfo customerInfo, boolean created) {
                    boolean active = isEntitlementActive(customerInfo);
                    result.onResult(active);
                }

                @Override
                public void onError(@NonNull com.revenuecat.purchases.PurchasesError error) {
                    Log.e(TAG, "RevenueCat login failed: " + error.getMessage());
                    result.onResult(false);
                }
            });
        }
    }

    private static void performNetworkAccessCheck(
            Activity activity,
            String email,
            AccessCallback callback
    ) {
        ApiService apiService = ApiClient.getClient().create(ApiService.class);
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);

        Call<TrialStatusResponse> call = apiService.getTrialStatus(email);

        call.enqueue(new Callback<TrialStatusResponse>() {
            @Override
            public void onResponse(Call<TrialStatusResponse> call, Response<TrialStatusResponse> response) {
                if (!response.isSuccessful() || response.body() == null) {
                    Log.e(TAG, "Trial status failed → access denied");
                    callback.onAccessDenied("API_ERROR");
                    return;
                }

                TrialStatusResponse trial = response.body();

                // DETAILED LOGGING
                Log.d(TAG, "======== TRIAL STATUS RESPONSE ========");
                Log.d(TAG, "trial.isActive(): " + trial.isActive());
                Log.d(TAG, "trial.hasTrialRecord(): " + trial.hasTrialRecord());
                Log.d(TAG, "trial.isConsumed(): " + trial.isConsumed());
                Log.d(TAG, "=====================================");

                prefs.edit().putBoolean(TRIAL_ACTIVE_KEY, trial.isActive()).apply();

                checkSubscription(activity, email, isSubscribed -> {
                    Log.d(TAG, "======== SUBSCRIPTION CHECK ========");
                    Log.d(TAG, "Subscription active: " + isSubscribed);
                    Log.d(TAG, "===================================");
                    // GRANT ACCESS IF:
                    // 1. User has active subscription
                    if (isSubscribed) {
                        Log.d(TAG, "Subscription active → access granted");
                        saveCache(activity, new AccessCache(true, true, trial.isActive(), System.currentTimeMillis()));
                        callback.onAccessGranted();
                        return;
                    }

                    // 2. Trial is currently active
                    if (trial.isActive()) {
                        Log.d(TAG, "Trial active → access granted");
                        saveCache(activity, new AccessCache(true, false, true, System.currentTimeMillis()));
                        callback.onAccessGranted();
                        return;
                    }

                    // 3. User has NEVER started trial → SHOW DIALOG
                    if (!trial.hasTrialRecord()) {
                        Log.d(TAG, "======== NO TRIAL RECORD BRANCH ========");
                        Log.d(TAG, "Trial never started - showing dialog");
                        // DO NOT save cache or grant access yet - wait for user decision
                        showTrialDialogAndWait(activity, email, prefs, callback);
                        Log.d(TAG, "======================================");
                        return;
                    }

                    // DENY ACCESS IF:
                    // Trial has been consumed and no subscription
                    if (trial.isConsumed()) {
                        Log.d(TAG, "Trial consumed & no subscription → access denied");
                        saveCache(activity, new AccessCache(false, false, false, System.currentTimeMillis()));
                        callback.onAccessDenied("TRIAL_CONSUMED");
                        return;
                    }

                    // Fallback deny
                    saveCache(activity, new AccessCache(false, false, false, System.currentTimeMillis()));
                    callback.onAccessDenied("NO_ENTITLEMENT");
                });
            }

            @Override
            public void onFailure(Call<TrialStatusResponse> call, Throwable t) {
                Log.e(TAG, "Trial check error → access denied", t);
                callback.onAccessDenied("NETWORK_ERROR");
            }
        });
    }

    private static boolean isEntitlementActive(CustomerInfo customerInfo) {
        return customerInfo.getEntitlements().get(ENTITLEMENT_ID) != null &&
                customerInfo.getEntitlements().get(ENTITLEMENT_ID).isActive();
    }

    private static void showTrialDialogAndWait(Activity activity, String email,
                                               SharedPreferences prefs, AccessCallback callback) {
        TrialStartDialog dialog = new TrialStartDialog(activity, email,
                new TrialStartDialog.TrialDialogListener() {
                    @Override
                    public void onTrialStarted() {
                        Log.d(TAG, "Trial started by user - granting access");
                        prefs.edit().putBoolean(TRIAL_ACTIVE_KEY, true).apply();
                        // Save cache with access granted (trial now active)
                        saveCache(activity, new AccessCache(true, false, true, System.currentTimeMillis()));
                        // Grant access to home
                        callback.onAccessGranted();
                    }

                    @Override
                    public void onMaybeLater() {
                        Log.d(TAG, "User postponed trial start - denying access");
                        // DO NOT save cache - user hasn't made final decision
                        // This allows them to press back and see dialog again
                        // Deny access -> goes to paywall with back button enabled
                        callback.onAccessDenied("TRIAL_POSTPONED");
                    }
                });
        dialog.show();
    }

    private static AccessCache getMemoryCache(Activity activity) {
        if (memoryCache != null &&
                System.currentTimeMillis() - lastMemoryCacheUpdate < CACHE_TTL_MS) {
            return memoryCache;
        }
        return null;
    }

    private static AccessCache getPersistentCache(Activity activity) {
        SharedPreferences cache = activity.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE);
        long lastCheck = cache.getLong(KEY_LAST_CHECK, 0);

        if (System.currentTimeMillis() - lastCheck < CACHE_TTL_MS) {
            return new AccessCache(
                    cache.getBoolean(KEY_ACCESS_GRANTED, false),
                    cache.getBoolean(KEY_SUBSCRIPTION_ACTIVE, false),
                    cache.getBoolean(KEY_TRIAL_ACTIVE, false),
                    lastCheck
            );
        }
        return null;
    }

    private static void saveCache(Activity activity, AccessCache cache) {
        SharedPreferences.Editor editor = activity.getSharedPreferences(
                CACHE_PREFS, Context.MODE_PRIVATE).edit();
        editor.putBoolean(KEY_ACCESS_GRANTED, cache.accessGranted);
        editor.putBoolean(KEY_SUBSCRIPTION_ACTIVE, cache.subscriptionActive);
        editor.putBoolean(KEY_TRIAL_ACTIVE, cache.trialActive);
        editor.putLong(KEY_LAST_CHECK, cache.timestamp);
        editor.apply();

        updateMemoryCache(cache);

        Log.d(TAG, "Cache updated - Access: " + cache.accessGranted +
                ", Sub: " + cache.subscriptionActive +
                ", Trial: " + cache.trialActive);
    }

    private static void updateMemoryCache(AccessCache cache) {
        memoryCache = cache;
        lastMemoryCacheUpdate = System.currentTimeMillis();
    }

    private static void handleCachedResult(AccessCache cache, AccessCallback callback) {
        if (cache.accessGranted) {
            callback.onAccessGranted();
        } else {
            callback.onAccessDenied("CACHED_DENIAL");
        }
    }

    public static void invalidateCache(Activity activity) {
        Log.d(TAG, "Invalidating all access caches");
        memoryCache = null;
        lastMemoryCacheUpdate = 0;

        activity.getSharedPreferences(CACHE_PREFS, Context.MODE_PRIVATE)
                .edit()
                .clear()
                .apply();
    }

    private static class AccessCache {
        final boolean accessGranted;
        final boolean subscriptionActive;
        final boolean trialActive;
        final long timestamp;

        AccessCache(boolean accessGranted, boolean subscriptionActive,
                    boolean trialActive, long timestamp) {
            this.accessGranted = accessGranted;
            this.subscriptionActive = subscriptionActive;
            this.trialActive = trialActive;
            this.timestamp = timestamp;
        }

        boolean isValid() {
            return System.currentTimeMillis() - timestamp < CACHE_TTL_MS;
        }
    }
}