
package com.thelinkphone.app.utils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.thelinkphone.app.AccessManager;
import com.thelinkphone.app.LoginActivity;

import java.util.concurrent.atomic.AtomicBoolean;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public final class AppBridge {

    private static final String TAG = "AppBridge";
    private static final long LOGOUT_WATCHDOG_MS = 12_000L;

    private final Context appContext;
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private final AtomicBoolean logoutStarted =
            new AtomicBoolean(false);

    private final AtomicBoolean finishing =
            new AtomicBoolean(false);

    private final Runnable watchdog = this::finishLogout;

    public AppBridge(Context context) {
        this.appContext = context.getApplicationContext();
    }

    @JavascriptInterface
    public void onLogout() {
        if (!logoutStarted.compareAndSet(false, true)) {
            return;
        }

        mainHandler.post(() -> {
            Log.d(TAG, "WebView requested native logout");

            mainHandler.postDelayed(watchdog, LOGOUT_WATCHDOG_MS);

            try {
                DeviceRegistrar.unregister(
                        appContext,
                        this::revokeServerToken
                );
            } catch (Exception e) {
                Log.e(TAG, "Unable to start device unregistration", e);
                revokeServerToken();
            }
        });
    }

    private void revokeServerToken() {
        SharedPreferences prefs =
                appContext.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);
        String token = prefs.getString("auth_token", null);

        if (token == null || token.trim().isEmpty()) {
            finishLogout();
            return;
        }

        token = token.trim();
        String auth = token.startsWith("Bearer ") ? token : "Bearer " + token;

        try {
            ApiClient.getClient().create(ApiService.class)
                    .logout(auth)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            finishLogout();
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            finishLogout();
                        }
                    });
        } catch (Exception e) {
            Log.e(TAG, "Unable to revoke the server token", e);
            finishLogout();
        }
    }

    private void finishLogout() {
        mainHandler.removeCallbacks(watchdog);

        if (!finishing.compareAndSet(false, true)) {
            return;
        }

        mainHandler.post(() -> {
            try {
                WebSession.reset();

                AccessManager.clearAllOnLogout(appContext);

                SessionManager.getInstance(appContext)
                        .clearAuthSession();

                Intent loginIntent =
                        new Intent(appContext, LoginActivity.class);

                loginIntent.addFlags(
                        Intent.FLAG_ACTIVITY_NEW_TASK
                                | Intent.FLAG_ACTIVITY_CLEAR_TOP
                );

                appContext.startActivity(loginIntent);

                Log.d(TAG, "Native logout completed");

            } catch (Exception e) {
                Log.e(TAG, "Failed to complete native logout", e);

            } finally {
                finishing.set(false);
                logoutStarted.set(false);
            }
        });
    }
}
