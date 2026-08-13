package com.thelinkphone.app;

import android.app.Application;
import android.content.Context;
import android.util.Log;

import com.google.firebase.FirebaseApp;
import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesConfiguration;
import com.revenuecat.purchases.LogLevel;
import com.thelinkphone.app.BuildConfig;
import com.thelinkphone.app.utils.AnalyticsHelper;

public class MyAppClass extends Application {

    private static final String REVENUECAT_API_KEY = "goog_QkslWhIJlNRCDNKKLfalUpxWjZV";

    public static Context myContext;
    @Override
    public void onCreate() {
        super.onCreate();
        myContext = this;

        FirebaseApp.initializeApp(this);
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(true);

        final Thread.UncaughtExceptionHandler defaultHandler = Thread.getDefaultUncaughtExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler((thread, throwable) -> {
            try {
                FirebaseCrashlytics.getInstance().log("Uncaught exception on thread: " + thread.getName());
                FirebaseCrashlytics.getInstance().recordException(throwable);
            } catch (Exception e) {
                Log.e("MyAppClass", "Failed to record crash to Crashlytics", e);
            }
            if (defaultHandler != null) {
                defaultHandler.uncaughtException(thread, throwable); // let Crashlytics' own handler still run
            }
        });

        AnalyticsHelper.init(this);

        // Configure RevenueCat
        PurchasesConfiguration.Builder builder = new PurchasesConfiguration.Builder(this, REVENUECAT_API_KEY);

        // Enable debug logs in development
        if (BuildConfig.DEBUG) {
            builder.diagnosticsEnabled(true);
        }

        Purchases.configure(builder.build());
        Purchases.setLogLevel(LogLevel.DEBUG); // Remove in production
    }
}
