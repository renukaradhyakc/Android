
package com.thelinkphone.app.utils;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.webkit.JavascriptInterface;

import com.thelinkphone.app.LoginActivity;

import java.util.concurrent.atomic.AtomicBoolean;

public final class AppBridge {

    private static final String TAG = "AppBridge";

    private final Context appContext;
    private final Handler mainHandler =
            new Handler(Looper.getMainLooper());

    private final AtomicBoolean logoutStarted =
            new AtomicBoolean(false);

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

            try {
                // Unregister before clearing the authentication token.
                DeviceRegistrar.unregister(
                        appContext,
                        this::finishLogout
                );
            } catch (Exception e) {
                Log.e(TAG, "Unable to start device unregistration", e);
                finishLogout();
            }
        });
    }

    private void finishLogout() {
        mainHandler.post(() -> {
            try {
                // Clears saved credentials and cached session.
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
                logoutStarted.set(false);
            }
        });
    }
}
