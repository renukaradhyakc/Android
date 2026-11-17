package com.thelinkphone.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.provider.Settings;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class DeviceRegistrar {

    private static final String TAG = "DeviceRegistrar";
    private static final String PREFS = "app_prefs";
    private static final String TOKEN_KEY = "auth_token";
    private static final long RESYNC_MS = 10 * 60 * 1000L;
    private static final long[] RETRY_DELAYS_MS = {30_000L, 120_000L, 600_000L};
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());
    private static String lastSyncAuth = null;
    private static long lastSyncAt = 0;
    private static int retryCount = 0;
    private DeviceRegistrar() {
    }

    /** Call from ActivityHome.onResume() and after a successful login. Cheap: throttled to once per 10 minutes per login. */
    public static void syncIfLoggedIn(Context context) {
        final Context app = context.getApplicationContext();
        String auth = authHeader(app);

        Log.d(TAG, "syncIfLoggedIn called");

        if (auth == null) {
            Log.d(TAG, "No authentication token; skipping registration");
            return;
        }

        Log.d(TAG, "Authenticated user detected");

        long now = SystemClock.elapsedRealtime();

        Log.d(TAG, "Checking registration throttle");

        if (auth.equals(lastSyncAuth) && now - lastSyncAt < RESYNC_MS) {
            Log.d(TAG, "Registration skipped due to throttle");
            return;
        }

        Log.d(TAG, "Starting FCM token retrieval");

        lastSyncAuth = auth;
        lastSyncAt = now;

        final long tokenRequestStart = SystemClock.elapsedRealtime();

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            long duration = SystemClock.elapsedRealtime() - tokenRequestStart;

            Log.d(TAG, "FCM token request completed in " + duration + " ms");

            if (!task.isSuccessful() || task.getResult() == null) {
                lastSyncAuth = null;
                Log.w(TAG, "FCM TOKEN RETRIEVAL FAILED", task.getException());
                scheduleRetry(app);
                return;
            }

            Log.d(TAG, "FCM TOKEN RETRIEVED SUCCESSFULLY");

            register(app, task.getResult());
        });
    }

    public static void register(Context context, String fcmToken) {
        final Context app = context.getApplicationContext();
        String auth = authHeader(app);

        if (auth == null || fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        Log.d(TAG, "Calling backend device registration API");

        ApiClient.getClient().create(ApiService.class)
                .registerDevice(auth, "android", fcmToken, deviceId(app), appVersion(app))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        Log.d(TAG, "Backend registration response: " + response.code());
                        if (response.isSuccessful()) {
                            retryCount = 0;
                            Log.d(TAG, "device registered");
                        } else {
                            lastSyncAuth = null;
                            Log.w(TAG, "register failed: HTTP " + response.code());
                            if (response.code() >= 500) {
                                scheduleRetry(app);
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        lastSyncAuth = null;
                        Log.e(TAG, "Backend registration network failure", t);
                        scheduleRetry(app);
                    }
                });
    }

    /** Call on logout BEFORE the login token is cleared. Always runs "then", even when the server cannot be reached. */
    public static void unregister(Context context, Runnable then) {
        final Context app = context.getApplicationContext();
        final String auth = authHeader(app);
        final String deviceId = deviceId(app);
        lastSyncAuth = null;
        HANDLER.removeCallbacksAndMessages(null);
        retryCount = 0;

        if (auth == null) {
            run(then);
            return;
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            String token = (task.isSuccessful() && task.getResult() != null) ? task.getResult() : "none";

            ApiClient.getClient().create(ApiService.class)
                    .unregisterDevice(auth, token, deviceId)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            run(then);
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            run(then);
                        }
                    });
        });
    }

    private static void scheduleRetry(final Context app) {
        Log.w(TAG, "Scheduling retry. Current retry count: " + retryCount);
        if (retryCount >= RETRY_DELAYS_MS.length) {
            return; // give up for now; the next onResume / login tries again
        }

        long delay = RETRY_DELAYS_MS[retryCount++];
        Log.w(TAG, "Next retry delay: " + (delay / 1000) + " seconds");
        HANDLER.postDelayed(() -> {
            Log.d(TAG, "Executing scheduled registration retry");
            syncIfLoggedIn(app);
        }, delay);
    }

    private static void run(Runnable then) {
        if (then != null) {
            then.run();
        }
    }

    private static String authHeader(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String token = prefs.getString(TOKEN_KEY, null);

        if (token == null || token.trim().isEmpty()) {
            return null;
        }

        token = token.trim();

        return token.startsWith("Bearer ") ? token : "Bearer " + token;
    }

    private static String deviceId(Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    private static String appVersion(Context context) {
        try {
            PackageInfo info = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return info.versionName;
        } catch (Exception e) {
            return null;
        }
    }
}