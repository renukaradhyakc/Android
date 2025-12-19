package com.thelinkphone.app;

import android.app.Application;
import android.content.Context;

import com.revenuecat.purchases.Purchases;
import com.revenuecat.purchases.PurchasesConfiguration;
import com.revenuecat.purchases.LogLevel;
import com.revenuecat.purchases.api.BuildConfig;

public class MyAppClass extends Application {

    private static final String REVENUECAT_API_KEY = "goog_QkslWhIJlNRCDNKKLfalUpxWjZV";

    public static Context myContext;
    @Override
    public void onCreate() {
        super.onCreate();
        myContext = this;

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
