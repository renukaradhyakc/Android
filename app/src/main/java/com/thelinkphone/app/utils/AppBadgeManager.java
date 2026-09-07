package com.thelinkphone.app.utils;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.thelinkphone.app.R;

public class AppBadgeManager {

    private static final String CHANNEL_ID = "unread_calls_badge";
    private static final String PREFS_NAME = "badge_prefs";
    private static final String KEY_UNREAD_COUNT = "unread_missed_blocked_count";
    private static final int NOTIFICATION_ID = 9001;

    public static void createChannel(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager manager = context.getSystemService(NotificationManager.class);
            if (manager == null) return;

            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Missed & Blocked Calls",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setShowBadge(true);
            channel.setDescription("Shows unread missed and blocked call count on the app icon");
            manager.createNotificationChannel(channel);
        }
    }

    public static void increment(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        int count = prefs.getInt(KEY_UNREAD_COUNT, 0) + 1;
        prefs.edit().putInt(KEY_UNREAD_COUNT, count).apply();
        postNotification(context, count);
    }

    public static void clear(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putInt(KEY_UNREAD_COUNT, 0).apply();
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID);
    }

    public static int getCount(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getInt(KEY_UNREAD_COUNT, 0);
    }

    private static void postNotification(Context context, int count) {
        String contentText = count == 1
                ? "1 missed or blocked call"
                : count + " missed or blocked calls";

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("CallaLink")
                .setContentText(contentText)
                .setNumber(count)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOnlyAlertOnce(true)
                .setAutoCancel(false);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && context.checkSelfPermission(android.Manifest.permission.POST_NOTIFICATIONS)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, builder.build());
    }
}