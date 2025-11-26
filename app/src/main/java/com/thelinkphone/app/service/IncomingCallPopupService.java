    package com.thelinkphone.app.service;

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
    import android.widget.LinearLayout;
    import android.widget.TextView;

    import androidx.cardview.widget.CardView;
    import androidx.core.app.NotificationCompat;
    import androidx.core.content.ContextCompat;
    import androidx.recyclerview.widget.LinearLayoutManager;
    import androidx.recyclerview.widget.PagerSnapHelper;
    import androidx.recyclerview.widget.RecyclerView;

    import com.google.firebase.firestore.DocumentReference;
    import com.google.firebase.firestore.DocumentSnapshot;
    import com.google.firebase.firestore.FieldPath;
    import com.google.firebase.firestore.FirebaseFirestore;
    import com.google.firebase.firestore.Query;
    import com.google.firebase.firestore.QueryDocumentSnapshot;
    import com.thelinkphone.app.R;
    import com.thelinkphone.app.adapter.AdapterContextAndContent;
    import com.thelinkphone.app.model.ContextAndContent;
    import com.thelinkphone.app.utils.ContextCarouselManager;
    import com.thelinkphone.app.utils.MyShare;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Map;

    public class IncomingCallPopupService extends Service {

        private static final String TAG = "IncomingCallPopupService";
        private static final long ANIMATION_DURATION = 500;
        private static final int NOTIFICATION_ID = 2025;
        private static final String CHANNEL_ID = "incoming_call_popup_channel";

        private WindowManager windowManager;
        private View popupView;
        private WindowManager.LayoutParams params;
        private boolean isVisible = false;
        private ContextCarouselManager carouselManager;
        public static final String CALL_MODE = "CALL_MODE";
        public static final String USERNAME = "USERNAME";
        public static final String IS_CALLALINK_USER = "IS_CALLALINK_USER";
        public static final String IS_WITHIN_SCHEDULE = "IS_WITHIN_SCHEDULE";
        public static final String IS_A_CONTACT = "IS_A_CONTACT";
        public static final String PHONE_NUMBER="PHONE_NUMBER";
        private String phoneNumber;

        /** Entry point to show popup safely from background **/
        public static void showPopup(Context context, int callMode, String username,
                                     boolean isCallALinkUser, boolean isWithinSchedule, boolean isAContact, String phoneNumber) {
            Intent intent = new Intent(context, IncomingCallPopupService.class);
            intent.putExtra(CALL_MODE, callMode);
            intent.putExtra(USERNAME, username);
            intent.putExtra(IS_CALLALINK_USER, isCallALinkUser);
            intent.putExtra(IS_WITHIN_SCHEDULE, isWithinSchedule);
            intent.putExtra(IS_A_CONTACT, isAContact);
            intent.putExtra(PHONE_NUMBER,phoneNumber);
            ContextCompat.startForegroundService(context, intent);
        }

        @Override
        public void onCreate() {
            super.onCreate();
            createNotificationChannel();
            startForeground(NOTIFICATION_ID, buildNotification());
        }

        @SuppressLint("InflateParams")
        @Override
        public int onStartCommand(Intent intent, int flags, int startId) {
            Log.d(TAG, "Popup service started");

            if (intent == null) {
                stopSelf();
                return START_NOT_STICKY;
            }

            int callMode = intent.getIntExtra(CALL_MODE, MyShare.CALL_SETTING_UNRESTRICTED);
            String username = intent.getStringExtra(USERNAME);
            boolean isCallALinkUser = intent.getBooleanExtra(IS_CALLALINK_USER, false);
            boolean isWithinSchedule = intent.getBooleanExtra(IS_WITHIN_SCHEDULE, false);
            boolean isAContact = intent.getBooleanExtra(IS_A_CONTACT, false);
            this.phoneNumber=intent.getStringExtra(PHONE_NUMBER);

            Log.d(TAG, "Received popup data -> username: " + username +
                    ", callMode: " + callMode +
                    ", isCallALinkUser: " + isCallALinkUser +
                    ", isWithinSchedule: " + isWithinSchedule +
                    ", isAContact: " + isAContact +
                    ", phoneNumber: "+phoneNumber);


            // Show popup if not already visible
            if (!isVisible) {
                showPopupLayout(callMode, username, isCallALinkUser, isWithinSchedule, isAContact, phoneNumber);
            }

            return START_STICKY;
        }

        @SuppressLint("ClickableViewAccessibility")
        private void showPopupLayout(int mode, String username,
                                     boolean isCallALinkUser, boolean isWithinSchedule, boolean isAContact, String phoneNumber) {
            try {
                if (popupView != null && popupView.isAttachedToWindow()) {
                    Log.w(TAG, "Popup already attached, skipping duplicate addView");
                    return;
                }

                windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
                Context themedContext = new ContextThemeWrapper(this, R.style.AppTheme);
                LayoutInflater inflater = LayoutInflater.from(themedContext);
                popupView = inflater.inflate(R.layout.context_and_content_unrestricted_mode, null);

                setupStoryCarousel(popupView,phoneNumber);

                // --- 1. Initialization and Null Check ---
                if (popupView == null) {
                    Log.e(TAG, "FATAL: Failed to inflate R.layout.activity_card. Exiting.");
                    stopSelf();
                    return;
                }

                int layoutFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
                        : WindowManager.LayoutParams.TYPE_PHONE;

                // Ensure params is initialized (Fixes previous IllegalArgumentException)
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

                // --- 2. Set Initial State (Invisible, Scaled, OFF-SCREEN RIGHT) BEFORE Adding ---

                // Explicitly hide the view before adding it
                popupView.setVisibility(View.INVISIBLE);
                popupView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

                // Calculate screen width for starting position
                android.graphics.Point size = new android.graphics.Point();
                windowManager.getDefaultDisplay().getSize(size);
                int screenWidth = size.x;

                // **** CRITICAL FIX: Set the Window position (params.x) off-screen right ****
                params.x = screenWidth;

                // Set starting animation properties for the view (within the window)
                popupView.setAlpha(0f);
                popupView.setTranslationX(0f); // TranslationX is relative to the new off-screen window
                popupView.setTranslationY(0f);
                popupView.setScaleX(0.9f);
                popupView.setScaleY(0.9f);

                Log.d(TAG, String.format("DEBUG 2: Initial State set. Alpha: 0.0, Params.X: %d, Scale: 0.9", params.x));

                // --- 3. View Addition ---
                windowManager.addView(popupView, params);
                isVisible = true;
                Log.d(TAG, "DEBUG 3: View added to WindowManager.");

                makeDraggable(popupView, params, windowManager);
                applyTheme(mode, username, isCallALinkUser, isWithinSchedule, isAContact);

                ImageButton buttonClose = popupView.findViewById(R.id.button_close);
                buttonClose.setOnClickListener(v -> hidePopup(popupView, windowManager, params));

                // --- 4. Guaranteed Animation Start with Delay (Dual Animator) ---
                new Handler().postDelayed(() -> {

                    // Re-set VISIBLE: This is the trigger.
                    popupView.setVisibility(View.VISIBLE);
                    Log.d(TAG, "DEBUG 4A: Visibility set to VISIBLE. Starting animation sequence.");

                    // --- A) Animate Window Position (Horizontal Slide) using ValueAnimator ---
                    android.animation.ValueAnimator xAnimator = android.animation.ValueAnimator.ofInt(screenWidth, 0);
                    xAnimator.setDuration(400);
                    xAnimator.setInterpolator(new android.view.animation.DecelerateInterpolator());

                    xAnimator.addUpdateListener(animation -> {
                        params.x = (Integer) animation.getAnimatedValue();
                        try {
                            // Update the window position on every frame
                            windowManager.updateViewLayout(popupView, params);
                        } catch (IllegalArgumentException e) {}
                    });

                    xAnimator.addListener(new android.animation.AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationStart(android.animation.Animator animation) {
                            Log.d(TAG, "DEBUG 4B: Animation officially started (Sliding X).");
                        }
                        @Override
                        public void onAnimationEnd(android.animation.Animator animation) {
                            Log.d(TAG, "DEBUG 4C: Animation completed (Sliding X).");
                        }
                    });
                    xAnimator.start();

                    // --- B) Animate View Properties (Alpha and Scale) using ViewPropertyAnimator ---
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
                    if (isVisible) hidePopup(popupView, windowManager, params);
                }, 30000);

            } catch (Exception e) {
                Log.e(TAG, "Error showing popup: " + e.getMessage(), e);
                stopSelf();
            }
        }


        private void hidePopup(final View view, final WindowManager windowManager, final WindowManager.LayoutParams params) {
            if (view == null || windowManager == null) return;

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
            animator.setDuration(800); // same slow fade-out duration
            final int direction = 1; // you can randomize or fix (1 = right, -1 = left)

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
                        isVisible=false;
                        popupView = null;
                        Log.d(TAG, "Popup removed and carousel stopped.");
                    } catch (IllegalArgumentException ignored) {}
                }
            });

            animator.start();
        }



        public static void hidePopup(Context context) {
            if (context == null) {
                android.util.Log.e("IncomingCallPopupService", "Cannot hide popup — context is null");
                return;
            }

            try {
                Intent intent = new Intent(context.getApplicationContext(), IncomingCallPopupService.class);
                context.getApplicationContext().stopService(intent);
                android.util.Log.d("IncomingCallPopupService", "Popup hide requested");
            } catch (Exception e) {
                android.util.Log.e("IncomingCallPopupService", "Error hiding popup: " + e.getMessage());
            }
        }


        @SuppressLint("ClickableViewAccessibility")
        private void makeDraggable(final View view, final WindowManager.LayoutParams params, final WindowManager windowManager) {
            final int SWIPE_THRESHOLD_X = (int) (windowManager.getDefaultDisplay().getWidth() * 0.10);

            view.setOnTouchListener(new View.OnTouchListener() {
                private static final long DOUBLE_TAP_TIMEOUT = 300;
                private static final int TAP_MOVEMENT_THRESHOLD = 20;
                private int initialX, initialY;
                private float initialTouchX, initialTouchY;
                private int screenWidth, screenHeight;
                private boolean isBeingSwiped = false;
                private boolean isDragging = false;
                private long firstTapTime = 0;
                private float firstTapX = 0;
                private float firstTapY = 0;

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
                            isDragging = false;
                            initialX = params.x;
                            initialY = params.y;
                            initialTouchX = event.getRawX();
                            initialTouchY = event.getRawY();

                            Log.d(TAG, "ACTION_DOWN detected at: " + initialTouchX + ", " + initialTouchY);
                            return true;

                        case MotionEvent.ACTION_MOVE:
                            int deltaX = (int) (event.getRawX() - initialTouchX);
                            int deltaY = (int) (event.getRawY() - initialTouchY);

                            if (Math.abs(deltaX) > TAP_MOVEMENT_THRESHOLD || Math.abs(deltaY) > TAP_MOVEMENT_THRESHOLD) {
                                isDragging = true;
                            }

                            if (!isDragging) {
                                return true;
                            }

                            if (Math.abs(deltaX) > Math.abs(deltaY) * 2 && Math.abs(deltaX) > TAP_MOVEMENT_THRESHOLD) {
                                isBeingSwiped = true;
                                params.x = initialX + deltaX;

                                float displacementRatio = Math.min(1.0f, (float) Math.abs(deltaX) / SWIPE_THRESHOLD_X);
                                float alpha = 1.0f - displacementRatio;
                                view.setAlpha(alpha);

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
                            long currentTime = System.currentTimeMillis();
                            float upX = event.getRawX();
                            float upY = event.getRawY();

                            float totalMovementX = Math.abs(upX - initialTouchX);
                            float totalMovementY = Math.abs(upY - initialTouchY);

                            Log.d(TAG, "ACTION_UP - Movement: X=" + totalMovementX + ", Y=" + totalMovementY +
                                    ", isDragging=" + isDragging + ", isBeingSwiped=" + isBeingSwiped);

                            if (!isDragging && !isBeingSwiped &&
                                    totalMovementX < TAP_MOVEMENT_THRESHOLD && totalMovementY < TAP_MOVEMENT_THRESHOLD) {

                                Log.d(TAG, "Valid tap detected");

                                if (firstTapTime > 0 && (currentTime - firstTapTime) < DOUBLE_TAP_TIMEOUT) {
                                    float tapDistanceX = Math.abs(upX - firstTapX);
                                    float tapDistanceY = Math.abs(upY - firstTapY);

                                    Log.d(TAG, "Checking double-tap: time=" + (currentTime - firstTapTime) +
                                            "ms, distance: X=" + tapDistanceX + ", Y=" + tapDistanceY);

                                    if (tapDistanceX < TAP_MOVEMENT_THRESHOLD * 2 && tapDistanceY < TAP_MOVEMENT_THRESHOLD * 2) {
                                        Log.d(TAG, "✅ DOUBLE TAP DETECTED - Launching ActivityCall");

                                        try {
                                            Intent intent = new Intent(IncomingCallPopupService.this, com.thelinkphone.app.ActivityCall.class);
                                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                            startActivity(intent);

    //                                        new Handler().postDelayed(() -> {
    //                                            hidePopup(view, windowManager, params);
    //                                        }, 100);

                                        } catch (Exception e) {
                                            Log.e(TAG, "❌ Error launching ActivityCall on double tap", e);
                                        }

                                        firstTapTime = 0;
                                        firstTapX = 0;
                                        firstTapY = 0;
                                        return true;
                                    }
                                }

                                Log.d(TAG, "First tap recorded, waiting for second tap...");
                                firstTapTime = currentTime;
                                firstTapX = upX;
                                firstTapY = upY;
                                return true;
                            }

                            firstTapTime = 0;
                            firstTapX = 0;
                            firstTapY = 0;

                            if (isBeingSwiped) {
                                int finalDeltaX = (int) (upX - initialTouchX);

                                if (Math.abs(finalDeltaX) >= SWIPE_THRESHOLD_X) {
                                    Log.d(TAG, "Swipe threshold reached - dismissing popup");
                                    android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(view.getAlpha(), 0.0f);
                                    animator.setDuration(800);
                                    final int direction = (int) Math.signum(finalDeltaX);

                                    animator.addUpdateListener(new android.animation.ValueAnimator.AnimatorUpdateListener() {
                                        @Override
                                        public void onAnimationUpdate(android.animation.ValueAnimator animation) {
                                            float animatedValue = (Float) animation.getAnimatedValue();
                                            view.setAlpha(animatedValue);

                                            params.x = params.x + (int) (screenWidth * 0.025 * direction);
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
                                    Log.d(TAG, "Swipe cancelled - snapping back");
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

        private void applyTheme( int mode,String username,
                                 boolean isCallALinkUser, boolean isWithinSchedule, boolean isAContact) {

            CardView mainCard = popupView.findViewById(R.id.main_card);
            View cardInnerLayout = popupView.findViewById(R.id.card_inner_layout);
            TextView userName = popupView.findViewById(R.id.text_name);
            TextView branding = popupView.findViewById(R.id.text_branding);
            TextView modeText = popupView.findViewById(R.id.model_label);
            TextView avatarText = popupView.findViewById(R.id.text_avatar);
    //        View divider=popupView.findViewById(R.id.divider);

            if (username != null && !username.isEmpty()) {
                userName.setText(username);
            } else {
                userName.setText(R.string.unknown_caller);
            }

            if (username.length() > 0) {
                avatarText.setText(String.valueOf(Character.toUpperCase(username.charAt(0))));
            }

            setupBadge(R.id.contact_badge, R.id.contact_badge_view, R.id.contact_badge_textview,
                    isAContact, this.getString(R.string.badge_in_contacts));

            setupBadge(R.id.callalink_badge, R.id.callalink_badge_view, R.id.callalink_badge_textview,
                    isCallALinkUser, this.getString(R.string.badge_callalink_user));

            setupBadge(R.id.outside_schedule_badge, R.id.outside_schedule_badge_view, R.id.outside_schedule_badge_textview,
                    isWithinSchedule, this.getString(R.string.badge_inside_schedule));

            switch (mode) {
                case MyShare.CALL_SETTING_UNRESTRICTED:
                    mainCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.blue));
                    if (cardInnerLayout != null)
                        cardInnerLayout.setBackgroundResource(R.drawable.palette);
                    userName.setTextColor(ContextCompat.getColor(this, R.color.unrestricted_textcolor));
                    branding.setTextColor(ContextCompat.getColor(this, R.color.unrestricted_textcolor));
                    modeText.setText(R.string.unrestricted_mode_label);
                    modeText.setTextColor(ContextCompat.getColor(this,R.color.unrestricted_textcolor));
                    if (carouselManager != null) {
                        carouselManager.setAvatarNameColor(
                                ContextCompat.getColor(this, R.color.unrestricted_textcolor)
                        );
                    }
                    break;

                case MyShare.CALL_SETTING_PHONELINK_SCHEDULED:
                    mainCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.callalink_background));
                    if (cardInnerLayout != null)
                        cardInnerLayout.setBackgroundResource(R.drawable.modern_gradient_bg);
                    userName.setTextColor(ContextCompat.getColor(this, R.color.unrestricted_textcolor));
                    branding.setTextColor(ContextCompat.getColor(this, R.color.unrestricted_textcolor));
                    modeText.setText(R.string.callalink_mode_label);
                    modeText.setTextColor(ContextCompat.getColor(this,R.color.unrestricted_textcolor));
    //                divider.setBackgroundColor(ContextCompat.getColor(this,R.color.callalink_textcolor));
                    break;
            }
        }

        private void setupBadge(int layoutId, int dotId, int textId, boolean condition, String label) {
            LinearLayout badgeLayout = popupView.findViewById(layoutId);
            View dotView = popupView.findViewById(dotId);
            TextView textView = popupView.findViewById(textId);

            if (condition) {
                badgeLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.bg_badge_green));
                dotView.setBackground(ContextCompat.getDrawable(this, R.drawable.dot_green));
                textView.setText(label);
                textView.setTextColor(ContextCompat.getColor(this, R.color.green));
            }
        }


        private void createNotificationChannel() {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        CHANNEL_ID,
                        "Incoming Call Popup",
                        NotificationManager.IMPORTANCE_LOW
                );
                channel.setDescription("Shows incoming call popup overlays");
                NotificationManager manager = getSystemService(NotificationManager.class);
                if (manager != null) manager.createNotificationChannel(channel);
            }
        }

        private Notification buildNotification() {
            return new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setContentTitle("Incoming call popup active")
                    .setContentText("Displaying call overlay")
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

        public static void removeCallData(String myNumber) {
            if (myNumber == null || myNumber.isEmpty()) return;

            String phoneNumber = myNumber.startsWith("+91") ? myNumber.substring(3) : myNumber;
            FirebaseFirestore db = FirebaseFirestore.getInstance();

            // Delete call session document
            db.collection("call_sessions")
                    .whereEqualTo("myNumber", phoneNumber)
                    .get()
                    .addOnSuccessListener(querySnapshot -> {
                        if (querySnapshot.isEmpty()) {
                            Log.w(TAG, "⚠️ No matching call_sessions found to delete for " + phoneNumber);
                            return;
                        }

                        for (DocumentSnapshot doc : querySnapshot) {
                            DocumentReference ref = doc.getReference();
                            ref.delete()
                                    .addOnSuccessListener(aVoid -> Log.d(TAG, "✅ Deleted Firestore call session: " + ref.getId()))
                                    .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to delete document " + ref.getId(), e));
                        }
                    })
                    .addOnFailureListener(e -> Log.e(TAG, "❌ Failed to delete call session: " + e.getMessage(), e));
        }

    }