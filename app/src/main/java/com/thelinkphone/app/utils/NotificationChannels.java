package com.thelinkphone.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

public final class NotificationChannels {

    private NotificationChannels() {
    }

    public static void createAll(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }

        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm == null) {
            return;
        }

        add(nm, "bookings", "Bookings", NotificationManager.IMPORTANCE_DEFAULT);
        add(nm, "reminders", "Reminders", NotificationManager.IMPORTANCE_HIGH);
        add(nm, "event_changes", "Event changes", NotificationManager.IMPORTANCE_DEFAULT);
        add(nm, "location", "Location sharing", NotificationManager.IMPORTANCE_HIGH);
        add(nm, "phone_schedules", "Phone schedules", NotificationManager.IMPORTANCE_DEFAULT);
        add(nm, "loyalty", "Loyalty", NotificationManager.IMPORTANCE_LOW);
        add(nm, "subscription", "Subscription", NotificationManager.IMPORTANCE_DEFAULT);
        add(nm, "security", "Security", NotificationManager.IMPORTANCE_HIGH);
        add(nm, "announcements", "Announcements", NotificationManager.IMPORTANCE_LOW);
    }

    private static void add(NotificationManager nm, String id, String name, int importance) {
        nm.createNotificationChannel(new NotificationChannel(id, name, importance));
    }
}