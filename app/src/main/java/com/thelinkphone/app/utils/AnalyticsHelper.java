package com.thelinkphone.app.utils;

import android.content.Context;
import android.os.Bundle;
import com.google.firebase.analytics.FirebaseAnalytics;

public class AnalyticsHelper {
    private static FirebaseAnalytics analytics;
    public static boolean reachedHome = false;

    public static void init(Context context) {
        if (analytics == null) {
            analytics = FirebaseAnalytics.getInstance(context);
        }
    }

    public static void logEvent(String eventName) {
        if (analytics != null) analytics.logEvent(eventName, null);
    }

    public static void logEvent(String eventName, Bundle params) {
        if (analytics != null) analytics.logEvent(eventName, params);
    }
}