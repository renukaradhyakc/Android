package com.thelinkphone.app.service;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.telephony.TelephonyManager;
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
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.FirebaseApp;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.thelinkphone.app.adapter.LinkPreviewAdapter;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;

public class IncomingCallPopupService extends Service {

    private static final String TAG = "IncomingCallPopupService";
    private static final long ANIMATION_DURATION = 500;
    private static final int NOTIFICATION_ID = 2025;
    private static final String CHANNEL_ID = "incoming_call_popup_channel";

    private WindowManager windowManager;
    private View popupView;
    private WindowManager.LayoutParams params;
    private boolean isVisible = false;
    private ListenerRegistration linkListener;
    private LinkPreviewAdapter linkAdapter;
    private String phoneNumber;

    public static final String CALL_MODE = "CALL_MODE";
    public static final String USERNAME = "USERNAME";
    public static final String IS_CALLALINK_USER = "IS_CALLALINK_USER";
    public static final String IS_WITHIN_SCHEDULE = "IS_WITHIN_SCHEDULE";
    public static final String IS_A_CONTACT = "IS_A_CONTACT";
    public static final String PHONE_NUMBER = "PHONE_NUMBER";

    /** Entry point to show popup safely from background **/
    public static void showPopup(Context context, int callMode, String username,
            boolean isCallALinkUser, boolean isWithinSchedule, boolean isAContact) {
        Intent intent = new Intent(context, IncomingCallPopupService.class);
        intent.putExtra(CALL_MODE, callMode);
        intent.putExtra(USERNAME, username);
        intent.putExtra(IS_CALLALINK_USER, isCallALinkUser);
        intent.putExtra(IS_WITHIN_SCHEDULE, isWithinSchedule);
        intent.putExtra(IS_A_CONTACT, isAContact);
        intent.putExtra(PHONE_NUMBER, CallManager.getInstance().getPhoneCall());
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        ensureFirebaseInitialized();
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
        phoneNumber = intent.getStringExtra(PHONE_NUMBER);

        Log.d(TAG, "Received popup data -> username: " + username +
                ", callMode: " + callMode +
                ", isCallALinkUser: " + isCallALinkUser +
                ", isWithinSchedule: " + isWithinSchedule +
                ", isAContact: " + isAContact +
                ", phoneNumber: " + phoneNumber);

        // Show popup if not already visible
        if (!isVisible) {
            showPopupLayout(callMode, username, isCallALinkUser, isWithinSchedule, isAContact);
        } else if (phoneNumber != null && !phoneNumber.isEmpty() && linkListener == null) {
            Log.d(TAG, "Setting up listener for already visible popup");
            setupLinkListener();

        }
        return START_STICKY;
    }

    @SuppressLint("ClickableViewAccessibility")
    private void showPopupLayout(int mode, String username,
            boolean isCallALinkUser, boolean isWithinSchedule, boolean isAContact) {
        try {
            if (popupView != null && popupView.isAttachedToWindow()) {
                Log.w(TAG, "Popup already attached, skipping duplicate addView");
                return;
            }

            windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
            Context themedContext = new ContextThemeWrapper(this, R.style.AppTheme);
            LayoutInflater inflater = LayoutInflater.from(themedContext);
            popupView = inflater.inflate(R.layout.activity_card, null);

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
                    PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.CENTER;

            // --- 2. Set Initial State (Invisible, Scaled, OFF-SCREEN RIGHT) BEFORE Adding
            // ---

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
            setupLinkListener();

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
                    } catch (IllegalArgumentException e) {
                    }
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

                // --- B) Animate View Properties (Alpha and Scale) using ViewPropertyAnimator
                // ---
                popupView.animate()
                        .alpha(1f)
                        .scaleX(1f)
                        .scaleY(1f)
                        .setDuration(400)
                        .setInterpolator(new android.view.animation.DecelerateInterpolator())
                        .start();

            }, 10); // 10ms delay ensures addView is fully processed

        } catch (Exception e) {
            Log.e(TAG, "Error showing popup: " + e.getMessage(), e);
            stopSelf();
        }
    }

    private void hidePopup(final View view, final WindowManager windowManager,
            final WindowManager.LayoutParams params) {
        if (view == null || windowManager == null)
            return;

        // Clear Firestore data when hiding popup
        if (phoneNumber != null) {
            // Use centralized phone number normalization utility
            String cleanNumber = OtherUtils.normalizePhoneNumber(phoneNumber);
            String sessionId = "call_" + cleanNumber;

            FirebaseFirestore.getInstance().collection("call_links").document(sessionId).collection("links")
                    .get().addOnSuccessListener(querySnapshot -> {
                        com.google.firebase.firestore.WriteBatch batch = FirebaseFirestore.getInstance().batch();
                        for (com.google.firebase.firestore.DocumentSnapshot doc : querySnapshot.getDocuments()) {
                            batch.delete(doc.getReference());
                        }
                        batch.commit();
                    });
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
                } catch (IllegalArgumentException ignored) {
                }
            }
        });

        animator.addListener(new android.animation.AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(android.animation.Animator animation) {
                try {
                    windowManager.removeView(view);
                } catch (IllegalArgumentException ignored) {
                }
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
    private void makeDraggable(final View view, final WindowManager.LayoutParams params,
            final WindowManager windowManager) {
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

                                if (tapDistanceX < TAP_MOVEMENT_THRESHOLD * 2
                                        && tapDistanceY < TAP_MOVEMENT_THRESHOLD * 2) {
                                    Log.d(TAG, "✅ DOUBLE TAP DETECTED - Launching ActivityCall");

                                    try {
                                        Intent intent = new Intent(IncomingCallPopupService.this,
                                                com.thelinkphone.app.ActivityCall.class);
                                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                        startActivity(intent);

                                        new Handler().postDelayed(() -> {
                                            hidePopup(view, windowManager, params);
                                        }, 100);

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
                                android.animation.ValueAnimator animator = android.animation.ValueAnimator
                                        .ofFloat(view.getAlpha(), 0.0f);
                                animator.setDuration(800);
                                final int direction = (int) Math.signum(finalDeltaX);

                                animator.addUpdateListener(
                                        new android.animation.ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(android.animation.ValueAnimator animation) {
                                                float animatedValue = (Float) animation.getAnimatedValue();
                                                view.setAlpha(animatedValue);

                                                params.x = params.x + (int) (screenWidth * 0.025 * direction);
                                                try {
                                                    windowManager.updateViewLayout(view, params);
                                                } catch (IllegalArgumentException e) {
                                                }
                                            }
                                        });

                                animator.addListener(new android.animation.AnimatorListenerAdapter() {
                                    @Override
                                    public void onAnimationEnd(android.animation.Animator animation) {
                                        try {
                                            windowManager.removeView(view);
                                            isVisible = false;
                                            stopSelf();
                                        } catch (IllegalArgumentException e) {
                                        }
                                    }
                                });
                                animator.start();

                            } else {
                                Log.d(TAG, "Swipe cancelled - snapping back");
                                android.animation.ValueAnimator xAnimator = android.animation.ValueAnimator
                                        .ofInt(params.x, initialX);
                                android.animation.ValueAnimator alphaAnimator = android.animation.ValueAnimator
                                        .ofFloat(view.getAlpha(), 1.0f);

                                xAnimator.setDuration(400);
                                alphaAnimator.setDuration(400);

                                xAnimator.addUpdateListener(
                                        new android.animation.ValueAnimator.AnimatorUpdateListener() {
                                            @Override
                                            public void onAnimationUpdate(android.animation.ValueAnimator animation) {
                                                params.x = (Integer) animation.getAnimatedValue();
                                                try {
                                                    windowManager.updateViewLayout(view, params);
                                                } catch (IllegalArgumentException e) {
                                                }
                                            }
                                        });

                                alphaAnimator.addUpdateListener(
                                        new android.animation.ValueAnimator.AnimatorUpdateListener() {
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

    private void applyTheme(int mode, String username,
            boolean isCallALinkUser, boolean isWithinSchedule, boolean isAContact) {

        CardView mainCard = popupView.findViewById(R.id.main_card);
        View cardInnerLayout = popupView.findViewById(R.id.card_inner_layout);
        TextView userName = popupView.findViewById(R.id.text_name);
        TextView branding = popupView.findViewById(R.id.text_branding);
        TextView modeText = popupView.findViewById(R.id.model_label);
        TextView avatarText = popupView.findViewById(R.id.text_avatar);

        if (username != null && !username.isEmpty()) {
            userName.setText(username);
        } else {
            userName.setText(R.string.unknown_caller);
        }

        // Add null safety check before accessing username.length()
        if (username != null && username.length() > 0) {
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
                break;

            case MyShare.CALL_SETTING_PHONELINK_SCHEDULED:
                mainCard.setCardBackgroundColor(ContextCompat.getColor(this, R.color.callalink_background));
                if (cardInnerLayout != null)
                    cardInnerLayout.setBackgroundResource(R.drawable.modern_gradient_bg);
                userName.setTextColor(ContextCompat.getColor(this, R.color.callalink_textcolor));
                branding.setTextColor(ContextCompat.getColor(this, R.color.callalink_textcolor));
                modeText.setText(R.string.callalink_mode_label);
                modeText.setTextColor(ContextCompat.getColor(this, R.color.callalink_textcolor));
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
                    NotificationManager.IMPORTANCE_LOW);
            channel.setDescription("Shows incoming call popup overlays");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null)
                manager.createNotificationChannel(channel);
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

    private void setupLinkListener() {
        Log.d(TAG, "=== setupLinkListener START ===");
        Log.d(TAG, "Caller phoneNumber received: " + phoneNumber);

        if (phoneNumber == null || phoneNumber.isEmpty()) {
            Log.e(TAG, "Caller phone number not available - EXITING");
            return;
        }

        /**
         * FIRESTORE SESSION ARCHITECTURE (CORRECTED):
         * - We listen to OUR OWN session (recipient's session)
         * - The caller's ActivityCall saves links to OUR session
         * - This creates bidirectional link sharing:
         * * Caller saves to "call_<our number>" → we receive here
         * * We save to "call_<caller number>" → caller receives
         */

        // Get OUR phone number (the recipient)
        String ourPhoneNumber = getOwnPhoneNumber();

        if (ourPhoneNumber == null || ourPhoneNumber.isEmpty()) {
            Log.e(TAG, "Could not retrieve our own phone number - EXITING");
            return;
        }

        Log.d(TAG, "Our phone number: " + ourPhoneNumber);

        // Use centralized phone number normalization utility
        String ourNumber = OtherUtils.normalizePhoneNumber(ourPhoneNumber);
        String callerNumber = OtherUtils.normalizePhoneNumber(phoneNumber);

        Log.d(TAG, "Normalized - Our number: " + ourNumber + ", Caller number: " + callerNumber);

        RecyclerView linksRecycler = popupView.findViewById(R.id.links_recycler);
        if (linksRecycler == null) {
            Log.e(TAG, "links_recycler not found!");
            return;
        }

        linkAdapter = new LinkPreviewAdapter();
        linkAdapter.clearLinks();

        // Load locally stored links first
        loadLocalLinks();

        linksRecycler.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        linksRecycler.setAdapter(linkAdapter);

        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Hide RecyclerView initially if no local links
        if (linkAdapter.getItemCount() == 0) {
            linksRecycler.setVisibility(View.GONE);
            Log.d(TAG, "No local links, hiding RecyclerView");
        }

        // Listen to OUR session (where the caller stores links for us)
        String ourSession = "call_" + ourNumber;
        Log.d(TAG, "=== LISTENING TO OUR SESSION: " + ourSession + " ===");

        linkListener = db.collection("call_links")
                .document(ourSession)
                .collection("links")
                .addSnapshotListener((snapshots, error) -> {
                    Log.d(TAG, "=== OUR SESSION LISTENER TRIGGERED ===");
                    if (error != null) {
                        Log.e(TAG, "Our session listener error: " + error.getMessage());
                        return;
                    }

                    if (snapshots != null && !snapshots.isEmpty()) {
                        Log.d(TAG, "Found " + snapshots.size() + " documents in our session");
                        for (com.google.firebase.firestore.DocumentChange dc : snapshots.getDocumentChanges()) {
                            if (dc.getType() == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                                String link = dc.getDocument().getString("link");
                                if (link != null) {
                                    linksRecycler.setVisibility(View.VISIBLE);
                                    linkAdapter.addLink(link);
                                    Log.d(TAG, "✅ Link received from caller in our session: " + link);

                                    if (popupView != null && popupView.getVisibility() != View.VISIBLE) {
                                        popupView.setVisibility(View.VISIBLE);
                                        popupView.setAlpha(1f);
                                        isVisible = true;
                                    }
                                }
                            }
                        }
                    } else {
                        Log.d(TAG, "No documents in our session yet");
                    }
                });

        Log.d(TAG, "=== setupLinkListener COMPLETE ===");
    }

    /**
     * Retrieves our own phone number (the recipient's number).
     * Uses the phone number from login API response stored in SharedPreferences.
     *
     * @return Our phone number, or null if unavailable
     */
    @SuppressLint("HardwareIds")
    private String getOwnPhoneNumber() {
        // Get phone number from login API (stored in SharedPreferences)
        // This is saved by LoginActivity.saveToken() after successful login
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        String phoneNumber = prefs.getString("auth_phone", null);

        if (phoneNumber != null && !phoneNumber.isEmpty()) {
            Log.d(TAG, "Retrieved our number from login API: " + phoneNumber);
            return phoneNumber;
        }

        Log.w(TAG, "Could not retrieve our phone number from SharedPreferences");
        Log.w(TAG, "Make sure user is logged in and phone_number is returned from login API");
        return null;
    }

    private void loadLocalLinks() {
        android.content.SharedPreferences prefs = getSharedPreferences("call_links", MODE_PRIVATE);
        java.util.Set<String> storedLinks = prefs.getStringSet("shared_links", new java.util.HashSet<>());

        RecyclerView linksRecycler = popupView.findViewById(R.id.links_recycler);
        if (!storedLinks.isEmpty()) {
            linksRecycler.setVisibility(View.VISIBLE);
            for (String link : storedLinks) {
                linkAdapter.addLink(link);
                Log.d(TAG, "Local link loaded: " + link);
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "Service destroyed — hiding popup if visible");
        if (linkListener != null)
            linkListener.remove();
        if (linkAdapter != null)
            linkAdapter.clearLinks();
        if (isVisible) {
            hidePopup(popupView, windowManager, params);
        }
    }

    private boolean ensureFirebaseInitialized() {
        try {
            if (FirebaseApp.getApps(this).isEmpty()) {
                FirebaseApp.initializeApp(this);
                Log.d(TAG, "Firebase initialized safely");
            }
            return true;
        } catch (Exception e) {
            Log.e(TAG, "Firebase init failed", e);
            return false;
        }
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
