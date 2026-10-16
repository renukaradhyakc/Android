package com.thelinkphone.app.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
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

    private static String lastSyncAuth = null;
    private static long lastSyncAt = 0;

    private DeviceRegistrar() {
    }

    /** Call from ActivityHome.onResume() and after a successful login. Cheap: throttled to once per 10 minutes per login. */
    public static void syncIfLoggedIn(Context context) {
        final Context app = context.getApplicationContext();
        String auth = authHeader(app);

        if (auth == null) {
            return;
        }

        long now = SystemClock.elapsedRealtime();
        if (auth.equals(lastSyncAuth) && now - lastSyncAt < RESYNC_MS) {
            return;
        }

        lastSyncAuth = auth;
        lastSyncAt = now;

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                lastSyncAuth = null;
                Log.w(TAG, "could not read the FCM token", task.getException());
                return;
            }
            register(app, task.getResult());
        });
    }

    public static void register(Context context, String fcmToken) {
        final Context app = context.getApplicationContext();
        String auth = authHeader(app);

        if (auth == null || fcmToken == null || fcmToken.isEmpty()) {
            return;
        }

        ApiClient.getClient().create(ApiService.class)
                .registerDevice(auth, "android", fcmToken, deviceId(app), appVersion(app))
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            Log.d(TAG, "device registered");
                        } else {
                            lastSyncAuth = null;
                            Log.w(TAG, "register failed: HTTP " + response.code());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        lastSyncAuth = null;
                        Log.w(TAG, "register failed: " + t.getMessage());
                    }
                });
    }

    /** Call on logout BEFORE the login token is cleared. Always runs "then", even when the server cannot be reached. */
    public static void unregister(Context context, Runnable then) {
        final Context app = context.getApplicationContext();
        final String auth = authHeader(app);
        lastSyncAuth = null;

        if (auth == null) {
            run(then);
            return;
        }

        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (!task.isSuccessful() || task.getResult() == null) {
                run(then);
                return;
            }

            ApiClient.getClient().create(ApiService.class)
                    .unregisterDevice(auth, task.getResult())
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