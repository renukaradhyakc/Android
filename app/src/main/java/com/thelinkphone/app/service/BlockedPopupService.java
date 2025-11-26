package com.thelinkphone.app.service;

import static com.thelinkphone.app.service.IncomingCallPopupService.CALL_MODE;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.TextView;


import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.PagerSnapHelper;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.thelinkphone.app.R;
import com.thelinkphone.app.adapter.AdapterContextAndContent;
import com.thelinkphone.app.model.ContextAndContent;
import com.thelinkphone.app.utils.ContextCarouselManager;
import com.thelinkphone.app.utils.MyShare;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BlockedPopupService extends Service {

    private static final String TAG = "BlockedPopupService";
    private static final long AUTO_DISMISS_MS = 10000;
    private static final int NOTIFICATION_ID = 2025;
    private static final String CHANNEL_ID = "blocked_call_popup_channel";
    private ContextCarouselManager carouselManager;
    private WindowManager windowManager;
    private View popupView;
    private WindowManager.LayoutParams params;
    private boolean isVisible = false;

    public static final String USERNAME = "USERNAME";
    public static final String IS_MANUALLY_BLOCKED = "IS_MANUALLY_BLOCKED";
    public static final String PHONE_NUMBER="PHONE_NUMBER";
    private String phoneNumber;

    public static void showPopup(Context context,int callMode,String username,boolean isManuallyBlocked,String phoneNumber) {
        Log.d(TAG, "Static showPopup called with username: " + username + ", callMode: " + callMode + ", isManuallyBlocked: " + isManuallyBlocked);
        Intent intent = new Intent(context, BlockedPopupService.class);
        intent.putExtra(USERNAME, username);
        intent.putExtra(CALL_MODE, callMode);
        intent.putExtra(IS_MANUALLY_BLOCKED,isManuallyBlocked);
        intent.putExtra(PHONE_NUMBER,phoneNumber);
        try {
            ContextCompat.startForegroundService(context, intent);
            Log.d(TAG, "Foreground service started successfully");
        } catch (Exception e) {
            Log.e(TAG, "Error starting service: " + e.getMessage(), e);
        }
    }


    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "onCreate called");
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, buildNotification());
    }

    @SuppressLint("InflateParams")
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand called");
        if (intent == null) {
            Log.e(TAG, "Intent is null, stopping service");
            stopSelf();
            return START_NOT_STICKY;
        }

        String username = intent.getStringExtra(USERNAME);
        boolean isManuallyBlocked = intent.getBooleanExtra(IS_MANUALLY_BLOCKED, false);
        int callMode = intent.getIntExtra(CALL_MODE, MyShare.CALL_SETTING_UNRESTRICTED);
        this.phoneNumber=intent.getStringExtra(PHONE_NUMBER);
        Log.d(TAG, "Received popup data -> username: " + username +
                ", callMode: " + callMode +
                ", isManuallyBlocked: " + isManuallyBlocked);


        // Show popup if not already visible
        if (!isVisible) {
            Log.d(TAG, "Popup not visible, showing layout");
            showPopupLayout(callMode, username, isManuallyBlocked,phoneNumber);
        } else {
            Log.w(TAG, "Popup already visible, skipping");
        }

        return START_STICKY;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showPopupLayout(int mode,String username,boolean isManuallyBlocked,String phoneNumber) {
        try {
            Log.d(TAG, "showPopupLayout started for username: " + username);
            if (popupView != null && popupView.isAttachedToWindow()) {
                Log.w(TAG, "Popup already attached, skipping duplicate addView");
                return;
            }

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Context themedContext = new ContextThemeWrapper(this, R.style.AppTheme);
            LayoutInflater inflater = LayoutInflater.from(themedContext);
//            popupView = inflater.inflate(R.layout.context_and_content_blocked, null);

            try {
                popupView = inflater.inflate(R.layout.context_and_content_blocked, null);
                Log.d(TAG, "Successfully inflated activity_card_3");
            } catch (Exception e) {
                Log.w(TAG, "activity_card_3 not found, using activity_card as fallback");
                popupView = inflater.inflate(R.layout.activity_card_3, null);
            }

            if (popupView == null) {
                Log.e(TAG, "FATAL: Failed to inflate layout. Exiting.");
                stopSelf();
                return;
            }

            setupStoryCarousel(popupView,phoneNumber);

            int layoutFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                    : WindowManager.LayoutParams.TYPE_PHONE;

            params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.WRAP_CONTENT,
                    layoutFlag,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
                            | WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL
                            | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN
                            | WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
                            | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                            | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON,
                    PixelFormat.TRANSLUCENT
            );
            params.gravity = Gravity.CENTER;

            //Set Initial State (Invisible, Scaled, OFF-SCREEN RIGHT) BEFORE Adding

            popupView.setVisibility(View.INVISIBLE);
            popupView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

            // Calculate screen width for starting position
            android.graphics.Point size = new android.graphics.Point();
            windowManager.getDefaultDisplay().getSize(size);
            int screenWidth = size.x;

            //Set the Window position (params.x) off-screen right
            params.x = screenWidth;

            // Set starting animation properties for the view (within the window)
            popupView.setAlpha(0f);
            popupView.setTranslationX(0f); // TranslationX is relative to the new off-screen window
            popupView.setTranslationY(0f);
            popupView.setScaleX(0.9f);
            popupView.setScaleY(0.9f);

            Log.d(TAG, "Initial state set. Alpha: 0.0, Params.X: " + params.x + ", Scale: 0.9");

            // Add view to WindowManager
            windowManager.addView(popupView, params);
            isVisible = true;
            Log.d(TAG, "View added to WindowManager successfully");

            makeDraggable(popupView, params, windowManager);
            applyBlockedTheme(mode,username,isManuallyBlocked,phoneNumber);


            ImageButton buttonClose = popupView.findViewById(R.id.button_close);
            if (buttonClose != null) {
                buttonClose.setOnClickListener(v -> {
                    Log.d(TAG, "Close button clicked");
                    hidePopup(popupView, windowManager, params);
                });
            } else {
                Log.w(TAG, "Close button not found in layout");
            }

            //Guaranteed Animation Start with Delay (Dual Animator)
            new Handler().postDelayed(() -> {

                Log.d(TAG, "Starting animation sequence");
                popupView.setVisibility(View.VISIBLE);

                android.animation.ValueAnimator xAnimator = android.animation.ValueAnimator.ofInt(screenWidth, 0);
                xAnimator.setDuration(400);
                xAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator());

                xAnimator.addUpdateListener(animation -> {
                    params.x = (Integer) animation.getAnimatedValue();
                    try {
                        // Update the window position on every frame
                        windowManager.updateViewLayout(popupView, params);
                    } catch (IllegalArgumentException e) {
                        Log.e(TAG, "Error updating view layout: " + e.getMessage());
                    }
                });

                xAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationStart(android.animation.Animator animation) {
                        Log.d(TAG, "Slide animation started");
                    }
                    @Override
                    public void onAnimationEnd(android.animation.Animator animation) {
                        Log.d(TAG, "Slide animation completed");
                    }
                });
                xAnimator.start();

                // Animate View Properties (Alpha and Scale) using ViewPropertyAnimator
                popupView.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(400)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();

            }, 10); // 10ms delay ensures addView is fully processed


            // Auto-dismiss after 30s
            new Handler().postDelayed(() -> {
                Log.d(TAG,"Auto-dissmis timer triggered");
                if (isVisible) hidePopup(popupView, windowManager, params);
            }, 30000);

        } catch (Exception e) {
            Log.e(TAG, "Error showing popup: " + e.getMessage(), e);
            e.printStackTrace();
            stopSelf();
        }
    }

    private void applyBlockedTheme(int mode,String username,boolean isManuallyBlocked,String phoneNumber) {
        try {
            Log.d(TAG, "Applying blocked theme for: " + username + ", mode: " + mode + ", manuallyBlocked: " + isManuallyBlocked);

            TextView userNameView = popupView.findViewById(R.id.text_name);
            if (userNameView != null) {
                userNameView.setSelected(true);

                String displayNameToShow = (username != null && !username.isEmpty()) ? username : "Unknown Caller";
                String numberToShow = (phoneNumber != null && !phoneNumber.isEmpty()) ? phoneNumber : "Unknown Number";

                String combinedText = displayNameToShow + "   •   " + numberToShow + "   •   ";

                StringBuilder marqueeBuilder = new StringBuilder();
                for (int i = 0; i < 10; i++) {
                    marqueeBuilder.append(combinedText);
                }

                userNameView.setText(marqueeBuilder.toString());
            }


            // Set branding
            TextView brandingView = popupView.findViewById(R.id.text_branding);
            if (brandingView != null) {
                brandingView.setTextColor(ContextCompat.getColor(this, android.R.color.white));
            }

            // Set mode label
            TextView modeText = popupView.findViewById(R.id.model_label);
            if (modeText != null) {
                if (mode == MyShare.CALL_SETTING_UNRESTRICTED) {
                    modeText.setText(R.string.unrestricted_mode_label);
                } else {
                    modeText.setText(R.string.callalink_mode_label);
                }
            }

            // Set block reason badge
            TextView reasonForBlock = popupView.findViewById(R.id.block_reason);
            if (reasonForBlock != null) {
                if (isManuallyBlocked) {
                    reasonForBlock.setText("Manually Blocked");
                } else {
                    reasonForBlock.setText("Outside-Schedule-Call");
                }
                Log.d(TAG, "Block reason set to: " + (isManuallyBlocked ? "Manually Blocked" : "Outside-Schedule-Call"));
            }

            // Set avatar
            TextView avatarText = popupView.findViewById(R.id.text_avatar);
            if (avatarText != null) {
                if (username != null && username.length() > 0) {
                    // Get the first character that is a letter or digit
                    String firstChar = "";
                    for (int i = 0; i < username.length(); i++) {
                        char c = username.charAt(i);
                        if (Character.isLetterOrDigit(c)) {
                            firstChar = String.valueOf(Character.toUpperCase(c));
                            break;
                        }
                    }
                    if (!firstChar.isEmpty()) {
                        avatarText.setText(firstChar);
                        Log.d(TAG, "Avatar text set to: " + firstChar);
                    } else {
                        avatarText.setText("?");
                        Log.d(TAG, "Avatar text set to default: ?");
                    }
                } else {
                    avatarText.setText("?");
                    Log.d(TAG, "Avatar text set to default: ?");
                }
                Log.d(TAG, "Blocked theme applied successfully");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error applying blocked theme: " + e.getMessage(), e);
        }
    }


    private void hidePopup(final View view, final WindowManager windowManager, final WindowManager.LayoutParams params) {
        if (view == null || windowManager == null) {
            Log.w(TAG, "Cannot hide popup - view or windowManager is null");
            return;
        }
        Log.d(TAG, "Hiding popup");

        if (carouselManager != null) {
            carouselManager.stopAutoScroll();
            Log.d(TAG, "Carousel auto-scroll stopped before hide animation");
        }

        // Get screen width (for horizontal movement)
        android.graphics.Point size = new android.graphics.Point();
        windowManager.getDefaultDisplay().getSize(size);
        final int screenWidth = size.x;

        // 💡 Fade-out with side movement (same as in makeDraggable)
        android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(view.getAlpha(), 0.0f);
        animator.setDuration(800);
        final int direction = 1;

        animator.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(android.animation.ValueAnimator animation) {
                float animatedValue = (Float) animation.getAnimatedValue();
                view.setAlpha(animatedValue);

                // Move slightly off-screen gradually
                params.x = params.x + (int) (screenWidth * 0.025 * direction);
                try {
                    windowManager.updateViewLayout(view, params);
                } catch (IllegalArgumentException ignored) {}
            }
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                try {
                    windowManager.removeView(view);
                    isVisible = false;
                    popupView=null;
                    Log.d(TAG, "Popup removed from WindowManager");
                    stopSelf(); // Stop the service after hiding
                } catch (IllegalArgumentException e) {
                    Log.e(TAG, "Error removing view: " + e.getMessage());
                }
            }
        });

        animator.start();
    }



    public static void hidePopup(Context context) {
        if (context == null) {
            Log.e(TAG, "Cannot hide popup — context is null");
            return;
        }

        try {
            Intent intent = new Intent(context.getApplicationContext(), BlockedPopupService.class);
            context.getApplicationContext().stopService(intent);
            Log.d(TAG, "Popup hide requested");
        } catch (Exception e) {
            Log.e(TAG, "Error hiding popup: " + e.getMessage());
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void makeDraggable(final View view, final WindowManager.LayoutParams params, final WindowManager windowManager) {

        final int SWIPE_THRESHOLD_X = (int) (windowManager.getDefaultDisplay().getWidth() * 0.10); // 30% of screen width

        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private int screenWidth, screenHeight;
            private boolean isBeingSwiped = false;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (screenWidth == 0 || screenHeight == 0) {
                    android.graphics.Point size = new android.graphics.Point();
                    windowManager.getDefaultDisplay().getSize(size);
                    screenWidth = size.x;
                    screenHeight = size.y;
                }

                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:

                        isBeingSwiped = false;
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);

                        if (Math.abs(deltaX) > Math.abs(deltaY) * 2) {
                            isBeingSwiped = true;

                            params.x = initialX + deltaX;

                            float displacementRatio = Math.min(1.0f, (float) Math.abs(deltaX) / SWIPE_THRESHOLD_X);
                            float alpha = 1.0f - displacementRatio;
                            view.setAlpha(alpha); // Change the view's opacity


                            windowManager.updateViewLayout(view, params);
                            return true;
                        } else if (!isBeingSwiped) {

                            params.x = initialX + deltaX;
                            params.y = initialY + deltaY;


                            int popupWidth = view.getWidth();
                            int popupHeight = view.getHeight();

                            int minX = -screenWidth / 2 + popupWidth / 2;
                            int maxX = screenWidth / 2 - popupWidth / 2;
                            int minY = -screenHeight / 2 + popupHeight / 2;
                            int maxY = screenHeight / 2 - popupHeight / 2;

                            params.x = Math.max(minX, Math.min(params.x, maxX));
                            params.y = Math.max(minY, Math.min(params.y, maxY));

                            windowManager.updateViewLayout(view, params);
                            return true;
                        }
                        return true;

                    case MotionEvent.ACTION_UP:
                        int finalDeltaX = (int) (event.getRawX() - initialTouchX);

                        if (isBeingSwiped) {
                            if (Math.abs(finalDeltaX) >= SWIPE_THRESHOLD_X) {
                                android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(view.getAlpha(), 0.0f);
                                animator.setDuration(800);
                                final int direction = (int) Math.signum(finalDeltaX);

                                animator.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(android.animation.ValueAnimator animation) {
                                        float animatedValue = (Float) animation.getAnimatedValue();
                                        view.setAlpha(animatedValue);

                                        // Move off-screen slower while fading out
                                        params.x = params.x + (int) (screenWidth * 0.025 * direction); // Adjusted multiplier
                                        try {
                                            windowManager.updateViewLayout(view, params);
                                        } catch (IllegalArgumentException e) {}
                                    }
                                });
                                animator.addListener(new android.animation.AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(android.animation.Animator animation) {
                                        try {
                                            windowManager.removeView(view);
                                            isVisible = false;
                                            stopSelf();
                                        } catch (IllegalArgumentException e) {}
                                    }
                                });
                                animator.start();

                            } else {
                                android.animation.ValueAnimator xAnimator = android.animation.ValueAnimator.ofInt(params.x, initialX);
                                android.animation.ValueAnimator alphaAnimator = android.animation.ValueAnimator.ofFloat(view.getAlpha(), 1.0f);

                                xAnimator.setDuration(400);
                                alphaAnimator.setDuration(400);

                                xAnimator.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(android.animation.ValueAnimator animation) {
                                        params.x = (Integer) animation.getAnimatedValue();
                                        try {
                                            windowManager.updateViewLayout(view, params);
                                        } catch (IllegalArgumentException e) {}
                                    }
                                });

                                alphaAnimator.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
                                    @Override
                                    public void onAnimationUpdate(android.animation.ValueAnimator animation) {
                                        view.setAlpha((Float) animation.getAnimatedValue());
                                    }
                                });

                                alphaAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(android.animation.Animator animation) {
                                        view.setAlpha(1.0f);
                                    }
                                });

                                xAnimator.start();
                                alphaAnimator.start();
                            }
                            isBeingSwiped = false;
                            return true;
                        }

                        return true;
                }
                return false;
            }
        });
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "Blocked Call Popup",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("Shows blocked call popup overlays");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) manager.createNotificationChannel(channel);
        }
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Blocked call notification")
                .setContentText("Displaying blocked call overlay")
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setOngoing(true)
                .build();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed — hiding popup if visible");

        if (carouselManager != null) {
            carouselManager.release();
            carouselManager = null;
        }

        if (isVisible && popupView!=null){
            hidePopup(popupView, windowManager, params);
        }

        removeCallData(phoneNumber);
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void setupStoryCarousel(View popupView, String phoneNumber) {
        RecyclerView storiesRecycler = popupView.findViewById(R.id.stories_container);
        if (storiesRecycler == null) {
            Log.e(TAG, "RecyclerView stories_container not found in layout");
            return;
        }

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.e(TAG, "❌ Phone number is null or empty, cannot fetch stories");
            storiesRecycler.setVisibility(View.GONE);
            return;
        }

        Log.d(TAG, "🔍 Fetching call session for phone number: " + phoneNumber);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // Query for active call session matching the phone number
        db.collection("call_sessions")
                .whereEqualTo("myNumber", phoneNumber)
                .limit(1)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (querySnapshot.isEmpty()) {
                        Log.w(TAG, "⚠️ No active call session found for: " + phoneNumber);
                        storiesRecycler.setVisibility(View.GONE);
                        return;
                    }

                    // Get the first (most recent) document
                    DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                    String callSessionId = document.getId();
                    Map<String, Object> data = document.getData();

                    Log.d(TAG, "✅ Found call session: " + callSessionId);

                    if (data == null) {
                        Log.e(TAG, "❌ Document data is null");
                        storiesRecycler.setVisibility(View.GONE);
                        return;
                    }

                    // Extract call session metadata
                    String callerNumber = data.containsKey("phoneNumber") ? data.get("phoneNumber").toString() : phoneNumber;
                    long timestamp = data.containsKey("timestamp") ? (long) data.get("timestamp") : System.currentTimeMillis();
                    boolean active = "ongoing".equals(data.get("status"));

                    Log.d(TAG, "📞 Call metadata - Caller: " + callerNumber + ", Timestamp: " + timestamp + ", Active: " + active);

                    // Check if context_and_content exists
                    if (!data.containsKey("context_and_content")) {
                        Log.w(TAG, "⚠️ No context_and_content field found in document");
                        storiesRecycler.setVisibility(View.GONE);
                        return;
                    }

                    // Parse the context_and_content map
                    Object contextObj = data.get("context_and_content");
                    if (!(contextObj instanceof Map)) {
                        Log.e(TAG, "❌ context_and_content is not a Map");
                        storiesRecycler.setVisibility(View.GONE);
                        return;
                    }

                    Map<String, Object> contextMap = (Map<String, Object>) contextObj;
                    Log.d(TAG, "📦 Found " + contextMap.size() + " stories in context_and_content");

                    List<ContextAndContent> storyList = new ArrayList<>();

                    // Parse each story entry
                    for (Map.Entry<String, Object> entry : contextMap.entrySet()) {
                        try {
                            String key = entry.getKey();
                            Object value = entry.getValue();

                            if (!(value instanceof Map)) {
                                Log.w(TAG, "⚠️ Skipping entry " + key + " - not a Map");
                                continue;
                            }

                            Map<String, Object> storyData = (Map<String, Object>) value;

                            // Extract story fields
                            String title = storyData.containsKey("title") ? storyData.get("title").toString() : "Untitled";
                            String description = storyData.containsKey("description") ? storyData.get("description").toString() : "";
                            String url = storyData.containsKey("url") ? storyData.get("url").toString() : "";
                            String imageUrl = storyData.containsKey("imageUrl") ? storyData.get("imageUrl").toString() : "";

                            // Parse numeric ID from key (e.g., "1", "2", "3")
                            int id;
                            try {
                                id = Integer.parseInt(key);
                            } catch (NumberFormatException e) {
                                id = storyList.size() + 1; // Fallback to incremental ID
                                Log.w(TAG, "⚠️ Could not parse ID from key '" + key + "', using: " + id);
                            }

                            // Create ContextAndContent object
                            ContextAndContent story = new ContextAndContent(
                                    callSessionId,
                                    callerNumber,
                                    "", // receiverNumber - not available in current schema
                                    timestamp,
                                    active,
                                    id,
                                    title,
                                    imageUrl,
                                    url
                            );

                            storyList.add(story);
                            Log.d(TAG, "✅ Added story #" + id + ": " + title + " (" + url + ")");

                        } catch (Exception e) {
                            Log.e(TAG, "⚠️ Error parsing story entry: " + e.getMessage(), e);
                        }
                    }

                    // Sort stories by ID
                    java.util.Collections.sort(storyList, new java.util.Comparator<ContextAndContent>() {
                        @Override
                        public int compare(ContextAndContent o1, ContextAndContent o2) {
                            return Integer.compare(o1.getId(), o2.getId());
                        }
                    });

                    if (storyList.isEmpty()) {
                        Log.w(TAG, "⚠️ No valid stories parsed from Firestore");
                        storiesRecycler.setVisibility(View.GONE);
                        return;
                    }

                    // Setup RecyclerView with carousel manager
                    AdapterContextAndContent adapter = new AdapterContextAndContent(this,storyList);
                    storiesRecycler.setAdapter(adapter);
                    storiesRecycler.setVisibility(View.VISIBLE);
                    carouselManager = new ContextCarouselManager(this, storiesRecycler, storyList);
                    carouselManager.startAutoScroll();

                    Log.d(TAG, "🎉 Successfully loaded " + storyList.size() + " stories from Firestore");

                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Firestore query failed: " + e.getMessage(), e);
                    storiesRecycler.setVisibility(View.GONE);
                });
    }


    private void removeCallData(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isEmpty()) return;

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Delete call session document
        db.collection("call_sessions")
                .whereEqualTo("phoneNumber", phoneNumber)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    for (DocumentSnapshot doc : querySnapshot) {
                        doc.getReference().delete();
                        Log.d(TAG, "✅ Deleted call session: " + doc.getId());
                    }
                })
                .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to delete call session: " + e.getMessage(), e));
    }
}