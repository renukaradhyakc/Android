package com.thelinkphone.app.service;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
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
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.MyShare;

public class BlockedPopupService extends Service {

    private static final String TAG = "BlockedPopupService";
    private static final long AUTO_DISMISS_MS = 10000;
    private static final int NOTIFICATION_ID = 2025;
    private static final String CHANNEL_ID = "blocked_call_popup_channel";

    private WindowManager windowManager;
    private View popupView;
    private WindowManager.LayoutParams params;
    private boolean isVisible = false;

    public static final String USERNAME = "USERNAME";

    public static void showPopup(Context context, String username) {
        Intent intent = new Intent(context, BlockedPopupService.class);
        intent.putExtra(USERNAME, username);
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

        String username = intent.getStringExtra(USERNAME);

        Log.d(TAG, "Received popup data -> username: " + username);


        // Show popup if not already visible
        if (!isVisible) {
            showPopupLayout(username);
        }

        return START_STICKY;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showPopupLayout(String username) {
        try {
            if (popupView != null && popupView.isAttachedToWindow()) {
                Log.w(TAG, "Popup already attached, skipping duplicate addView");
                return;
            }

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Context themedContext = new ContextThemeWrapper(this, R.style.AppTheme);
            LayoutInflater inflater = LayoutInflater.from(themedContext);
            popupView = inflater.inflate(R.layout.activity_card_3, null);

            if (popupView == null) {
                Log.e(TAG, "FATAL: Failed to inflate R.layout.activity_card. Exiting.");
                stopSelf();
                return;
            }

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
            params.gravity = Gravity.TOP;

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
        // Define a threshold for horizontal glide to trigger dismissal
        final int SWIPE_THRESHOLD_X = (int) (windowManager.getDefaultDisplay().getWidth() * 0.10); // 30% of screen width

        view.setOnTouchListener(new View.OnTouchListener() {
            private int initialX, initialY;
            private float initialTouchX, initialTouchY;
            private int screenWidth, screenHeight;
            private boolean isBeingSwiped = false; // Flag to track if a swipe is in progress

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
                        // Reset swipe flag
                        isBeingSwiped = false;
                        initialX = params.x;
                        initialY = params.y;
                        initialTouchX = event.getRawX();
                        initialTouchY = event.getRawY();
                        return true;

                    case MotionEvent.ACTION_MOVE:
                        int deltaX = (int) (event.getRawX() - initialTouchX);
                        int deltaY = (int) (event.getRawY() - initialTouchY);

                        // Check if horizontal movement significantly exceeds vertical movement
                        if (Math.abs(deltaX) > Math.abs(deltaY) * 2) {
                            isBeingSwiped = true;
                            // For a horizontal glide, we primarily update X and update the alpha
                            params.x = initialX + deltaX;

                            // Calculate alpha based on horizontal displacement
                            // Alpha should decrease as deltaX approaches SWIPE_THRESHOLD_X
                            float displacementRatio = Math.min(1.0f, (float) Math.abs(deltaX) / SWIPE_THRESHOLD_X);
                            float alpha = 1.0f - displacementRatio;
                            view.setAlpha(alpha); // Change the view's opacity

                            // Do not clamp the X position when swiping for dismissal
                            windowManager.updateViewLayout(view, params);
                            return true;
                        } else if (!isBeingSwiped) {
                            // Standard dragging logic (if not actively swiping for dismissal)
                            params.x = initialX + deltaX;
                            params.y = initialY + deltaY;

                            // Get popup's width & height
                            int popupWidth = view.getWidth();
                            int popupHeight = view.getHeight();

                            // Clamp to prevent half disappearing
                            int minX = -screenWidth / 2 + popupWidth / 2;
                            int maxX = screenWidth / 2 - popupWidth / 2;
                            int minY = -screenHeight / 2 + popupHeight / 2;
                            int maxY = screenHeight / 2 - popupHeight / 2;

                            params.x = Math.max(minX, Math.min(params.x, maxX));
                            params.y = Math.max(minY, Math.min(params.y, maxY));

                            windowManager.updateViewLayout(view, params);
                            return true;
                        }
                        return true; // Continue to return true if dragging/swiping

                    case MotionEvent.ACTION_UP:
                        int finalDeltaX = (int) (event.getRawX() - initialTouchX);

                        if (isBeingSwiped) {
                            if (Math.abs(finalDeltaX) >= SWIPE_THRESHOLD_X) {
                                // **Trigger Fade Out and Dismissal (SLOWER)**
                                android.animation.ValueAnimator animator = android.animation.ValueAnimator.ofFloat(view.getAlpha(), 0.0f);
                                // 💡 Increased duration for slower fade-out
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
                                        } catch (IllegalArgumentException e) {}
                                    }
                                });
                                animator.start();

                            } else {
                                // **Snap Back (Cancel Dismissal) (SLOWER)**

                                android.animation.ValueAnimator xAnimator = android.animation.ValueAnimator.ofInt(params.x, initialX);
                                android.animation.ValueAnimator alphaAnimator = android.animation.ValueAnimator.ofFloat(view.getAlpha(), 1.0f);

                                // 💡 Increased duration for slower snap back
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
                                        // Ensure full opacity is set explicitly after the animation finishes
                                        view.setAlpha(1.0f);
                                    }
                                });

                                xAnimator.start();
                                alphaAnimator.start();
                            }
                            isBeingSwiped = false;
                            return true;
                        }

                        // Standard drag release (if not swiping)
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
        if (isVisible){
            hidePopup(popupView, windowManager, params);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}