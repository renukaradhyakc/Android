package com.thelinkphone.app.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class OutgoingCallService extends Service {
    private static final String TAG = "OutgoingCallService";
    private static final String CHANNEL_ID = "outgoing_call_channel";

    private String myNumber;
    private String callSessionId;

    private FirebaseFirestore firestore;
    private DocumentReference callDocRef;
    private Handler handler = new Handler();
    private Runnable timestampUpdater;

    @Override
    public void onCreate() {
        super.onCreate();
        try {
            FirebaseApp.initializeApp(this);
            Log.d(TAG, "Firebase initialized successfully");
        } catch (Exception e) {
            Log.d(TAG, "Firebase already initialized: " + e.getMessage());
        }
        createNotificationChannel();
        startForeground(1, buildNotification("Starting call session..."));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            Log.e(TAG, "Intent is null, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        myNumber = intent.getStringExtra("myNumber");
        callSessionId = intent.getStringExtra("callSessionId");

        if (callSessionId == null || callSessionId.isEmpty()) {
            Log.e(TAG, "callSessionId is null or empty, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        Log.d(TAG, "Outgoing call started with sessionId=" + callSessionId + " phone=" + myNumber);

        if (firestore == null) {
            Log.w(TAG, "⚠️ Firestore was null, initializing now...");
            try {
                FirebaseApp.initializeApp(this);
            } catch (Exception e) {
                Log.d(TAG, "Firebase already initialized: " + e.getMessage());
            }
            firestore = FirebaseFirestore.getInstance();
        }

        // Verify Firestore is not null before proceeding
        if (firestore == null) {
            Log.e(TAG, "❌ CRITICAL: Firestore is still null after initialization!");
            stopSelf();
            return START_NOT_STICKY;
        }

        // Create Firestore document
        callDocRef = firestore.collection("call_sessions").document(myNumber);

        Map<String, Object> callData = new HashMap<>();
        callData.put("myNumber", myNumber);
        callData.put("timestamp", System.currentTimeMillis());
        callData.put("status", "ongoing");
        callData.put("callSessionId", callSessionId);

        Map<String, Map<String, String>> storyList = createStoryList();
        callData.put("context_and_content", storyList);

        Log.d(TAG, "📦 Prepared " + storyList.size() + " stories for Firestore");

        Log.d(TAG, "⬆️ Uploading to Firestore collection: call_sessions, document: " + myNumber);

        callDocRef.set(callData)
                .addOnSuccessListener(aVoid -> {Log.d(TAG, "✅ Firestore: Session created");
                                                Log.d(TAG, "✅ Firestore: Session created successfully for " + callSessionId);
                                                Log.d(TAG, "✅ All " + storyList.size() + " stories uploaded!");
                                                updateNotification("Call session active - " + storyList.size() + " stories");
                })
                .addOnFailureListener(e -> {Log.e(TAG, "❌ Firestore error: " + e.getMessage(),e);
                                            Log.e(TAG, "Error type: " + e.getClass().getName());
                                            updateNotification("Failed to create session");
                });
        return START_STICKY;
    }


    private Notification buildNotification(String contentText) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Outgoing Call Session")
                .setContentText(contentText)
                .setSmallIcon(android.R.drawable.sym_call_outgoing)
                .setOngoing(true)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Outgoing Call Tracking",
                    NotificationManager.IMPORTANCE_LOW
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "OutgoingCallService destroyed");

        handler.removeCallbacks(timestampUpdater);

        if (callDocRef != null) {
            Map<String, Object> update = new HashMap<>();
            update.put("status", "ended");
            update.put("endedAt", System.currentTimeMillis());
            callDocRef.update(update)
                    .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Firestore: Session ended"))
                    .addOnFailureListener(e -> Log.e(TAG, "❌ Firestore update error: " + e.getMessage()));
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void updateNotification(String contentText) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.notify(1, buildNotification(contentText));
        }
    }

    private Map<String, Map<String, String>> createStoryList() {
        Map<String, Map<String, String>> storyList = new HashMap<>();

        // Story 1: X (Twitter)
//        Map<String, String> story1 = new HashMap<>();
//        story1.put("title", "X");
//        story1.put("url", "https://x.com/wojakcodes/status/1898356531822748053");
//        story1.put("description", "Social media update");
//        storyList.put("1", story1);

        // Story 2: Pinterest
//        Map<String, String> story2 = new HashMap<>();
//        story2.put("title", "Pinterest");
//        story2.put("url", "https://in.pinterest.com/pin/20547742047210017/");
//        story2.put("description", "Design inspiration");
//        storyList.put("2", story2);

        // Story 3: MyDrive
        Map<String, String> story3 = new HashMap<>();
        story3.put("title", "MyDrive");
        story3.put("url", "https://docs.google.com/document/d/1A1toZSN0EOEWv1BkwVmv6PsH0vR8N4ERc60J7uKDIzw");
        story3.put("description", "Google Drive document");
        storyList.put("3", story3);

        // Story 4: TOI (Times of India)
        Map<String, String> story4 = new HashMap<>();
        story4.put("title", "TOI");
        story4.put("url", "https://timesofindia.indiatimes.com/");
        story4.put("description", "Latest news");
        storyList.put("4", story4);

        // Story 5: Android Developer
        Map<String, String> story5 = new HashMap<>();
        story5.put("title", "Android");
        story5.put("url", "https://developer.android.com");
        story5.put("description", "Development resources");
        storyList.put("5", story5);

        // Story 6: CallALink
        Map<String, String> story6 = new HashMap<>();
        story6.put("title", "CallALink");
        story6.put("url", "https://www.freepik.com/free-ai-image/anime-night-sky-illustration_249236808.htm");
        story6.put("description", "Night sky illustration");
        storyList.put("6", story6);


//        // Story 7: TheLinkPhone
        Map<String, String> story7 = new HashMap<>();
        story7.put("title", "TheLinkPhone");
        story7.put("url", "https://www.freepik.com/free-ai-image/japanese-samurai-rain_417436943.htm");
        story7.put("description", "Samurai artwork");
        storyList.put("7", story7);

        Log.d(TAG, "✅ Created all 7 stories:");
        for (Map.Entry<String, Map<String, String>> entry : storyList.entrySet()) {
            Log.d(TAG, "   Story " + entry.getKey() + ": " + entry.getValue().get("title"));
        }

        return storyList;
    }
}
