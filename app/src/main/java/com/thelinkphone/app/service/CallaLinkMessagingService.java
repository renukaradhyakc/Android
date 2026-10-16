package com.thelinkphone.app.service;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.thelinkphone.app.ActivityHome;
import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.DeviceRegistrar;
import com.thelinkphone.app.utils.NotificationChannels;

import java.util.Map;

public class CallaLinkMessagingService extends FirebaseMessagingService {

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        DeviceRegistrar.register(getApplicationContext(), token);
    }

    /**
     * Runs only while the app is in the foreground. In the background or when killed,
     * Android shows the notification itself from the "notification" part of the message.
     */
    @SuppressLint("MissingPermission")
    @Override
    public void onMessageReceived(@NonNull RemoteMessage message) {
        super.onMessageReceived(message);

        Map<String, String> data = message.getData();
        RemoteMessage.Notification notification = message.getNotification();

        String title = (notification != null && notification.getTitle() != null) ? notification.getTitle() : data.get("title");
        String body = (notification != null && notification.getBody() != null) ? notification.getBody() : data.get("body");

        if (title == null && body == null) {
            return;
        }

        if (Build.VERSION.SDK_INT >= 33
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        NotificationChannels.createAll(this);

        String category = data.get("category");
        String channelId = (category != null && !category.isEmpty()) ? category : "bookings";
        String uuid = data.get("notification_id");

        Intent open = new Intent(this, ActivityHome.class);
        open.setAction(ActivityHome.ACTION_OPEN_NOTIFICATION);
        open.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        for (Map.Entry<String, String> entry : data.entrySet()) {
            open.putExtra(entry.getKey(), entry.getValue());
        }

        PendingIntent pendingIntent = PendingIntent.getActivity(
                this,
                uuid != null ? uuid.hashCode() : 0,
                open,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
        );

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(body)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(body))
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);

        // same tag as the server uses, so one notification never shows twice
        NotificationManagerCompat.from(this).notify(uuid, 0, builder.build());
    }
}