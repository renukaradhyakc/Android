package com.thelinkphone.app;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.telephony.TelephonyManager;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.thelinkphone.app.screen.ActionScreenResult;
import com.thelinkphone.app.screen.BaseScreen;
import com.thelinkphone.app.screen.ios.ViewScreenIos;
import com.thelinkphone.app.screen.ios2.ViewScreenIOS2;
import com.thelinkphone.app.screen.mate.ViewScreenMate;
import com.thelinkphone.app.screen.other.ViewScreenOther;
import com.thelinkphone.app.screen.pixel.ViewScreenPixel;
import com.thelinkphone.app.screen.samsung.ViewScreenSamsung;
import com.thelinkphone.app.service.CallManager;
import com.thelinkphone.app.service.CallManagerListener;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;

public class ActivityCall extends AppCompatActivity {
    private BaseScreen baseScreen;
    private Sensor mSensor;
    private SensorManager mSensorManager;
    private PowerManager pm;
    private int status;
    private PowerManager.WakeLock wOff;
    private PowerManager.WakeLock wOn;
    private final CallManagerListener listener = new CallManagerListener() {
        @Override
        public void onStateChanged(int i) {
            ActivityCall.this.status = i;
            ActivityCall.this.baseScreen.updateStatus(i);
        }
    };
    private final SensorEventListener registerListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }

        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (ActivityCall.this.isDestroyed() || ActivityCall.this.isFinishing()
                    || ActivityCall.this.isChangingConfigurations()) {
                return;
            }
            if (sensorEvent.values[0] == 0.0f) {
                ActivityCall.this.turnOff();
            } else {
                ActivityCall.this.turnOn();
            }
        }
    };

    @SuppressLint("WrongConstant")
    public static Intent makeIntent(Context context) {
        Intent intent = new Intent(context, ActivityCall.class);
        intent.setFlags(272760832);
        return intent;
    }

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        int style = MyShare.getStyle(this);
        if (style == 0) {
            this.baseScreen = new ViewScreenIos(this);
        } else if (style == 1) {
            this.baseScreen = new ViewScreenPixel(this);
        } else if (style == 2) {
            this.baseScreen = new ViewScreenSamsung(this);
        } else if (style == 3) {
            this.baseScreen = new ViewScreenMate(this);
        } else if (style == 4) {
            this.baseScreen = new ViewScreenIOS2(this);
        } else {
            this.baseScreen = new ViewScreenOther(this);
        }
        setContentView(this.baseScreen);
        this.baseScreen.setActionScreenResult(new ActionScreenResult() {
            @Override
            public void onAccept() {
                CallManager.getInstance().accept();
            }

            @Override
            public void onReject() {
                android.util.Log.d("ActivityCall", "End call button clicked");
                CallManager.getInstance().reject();
            }

            @Override
            public void onHold() {
                android.util.Log.d("ActivityCall", "Hold button clicked");
                ActivityCall.this.baseScreen.isHold = CallManager.getInstance().hold();
                ActivityCall.this.baseScreen.updateViewMode();
            }

            @Override
            public void onRecorder() {
                android.util.Log.d("ActivityCall", "Record button clicked");
                String[] strArr;
                if (Build.VERSION.SDK_INT >= 29) {
                    strArr = new String[] { "android.permission.READ_EXTERNAL_STORAGE",
                            "android.permission.RECORD_AUDIO" };
                } else {
                    strArr = new String[] { "android.permission.READ_EXTERNAL_STORAGE",
                            "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO" };
                }
                int length = strArr.length;
                boolean z = false;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        z = true;
                        break;
                    }
                    if (!OtherUtils.checkPer(ActivityCall.this, strArr[i])) {
                        break;
                    }
                    i++;
                }
                if (z) {
                    ActivityCall.this.onRecorder();
                } else {
                    ActivityCompat.requestPermissions(ActivityCall.this, strArr, 1);
                }
            }

            @Override
            public void onMute() {
                android.util.Log.d("ActivityCall", "Mute button clicked");
                ActivityCall.this.baseScreen.isMute = CallManager.getInstance().muteSpeaker();
                ActivityCall.this.baseScreen.updateViewMode();
            }

            @Override
            public void onSpeaker() {
                android.util.Log.d("ActivityCall", "Speaker button clicked");
                ActivityCall.this.baseScreen.isSpeaker = CallManager.getInstance().switchSpeaker();
                ActivityCall.this.baseScreen.updateViewMode();
            }

            @Override
            public void onOpenContact() {
                ActivityCall.this.baseScreen.onShowContact();
            }

            @Override
            public void onPadClick(String str) {
                CallManager.getInstance().onKeyPad(str);
            }

            @Override
            public void onAddMessage() {
                ActivityCall.this.showAddMessageBottomSheet();
            }
        });
        this.baseScreen.updateStatus(CallManager.getInstance().getState());
        CallManager.getInstance().addListener(this.listener);
        updateFlags();
        startSensor();
    }

    public void onRecorder() {
        android.util.Log.d("ActivityCall", "onRecorder called, current isRec: " + this.baseScreen.isRec);
        if (this.baseScreen.isRec) {
            android.util.Log.d("ActivityCall", "Recording already in progress, ignoring");
            return;
        }
        this.baseScreen.isRec = true;
        CallManager.getInstance().startRecorder();
        this.baseScreen.updateViewMode();
    }

    @Override
    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        String[] strArr2;
        super.onRequestPermissionsResult(i, strArr, iArr);
        if (Build.VERSION.SDK_INT >= 29) {
            strArr2 = new String[] { "android.permission.READ_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO" };
        } else {
            strArr2 = new String[] { "android.permission.READ_EXTERNAL_STORAGE",
                    "android.permission.WRITE_EXTERNAL_STORAGE", "android.permission.RECORD_AUDIO" };
        }
        int length = strArr2.length;
        boolean z = false;
        int i2 = 0;
        while (true) {
            if (i2 >= length) {
                z = true;
                break;
            } else if (!OtherUtils.checkPer(this, strArr2[i2])) {
                break;
            } else {
                i2++;
            }
        }
        if (z) {
            onRecorder();
        }
    }

    private void updateFlags() {
        int i;
        KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext()
                .getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
            i = 512;
        } else {
            i = 6816384;
        }
        getWindow().getDecorView().setSystemUiVisibility(1280);
        getWindow().addFlags(i);
        getWindow().setNavigationBarColor(0);
        getWindow().setStatusBarColor(0);
        SensorManager sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        this.mSensorManager = sensorManager;
        if (sensorManager != null) {
            this.mSensor = sensorManager.getDefaultSensor(8);
        }
        this.pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        turnOn();
    }

    public void turnOn() {
        PowerManager powerManager = this.pm;
        if (powerManager != null) {
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock newWakeLock = powerManager.newWakeLock(26, "tag");
            this.wOn = newWakeLock;
            newWakeLock.acquire(5000L);
        }
    }

    public void turnOff() {
        PowerManager powerManager = this.pm;
        if (powerManager != null) {
            @SuppressLint("InvalidWakeLockTag")
            PowerManager.WakeLock newWakeLock = powerManager.newWakeLock(32, "tag");
            this.wOff = newWakeLock;
            // Add 10 minute timeout to prevent battery drain if not released properly
            newWakeLock.acquire(10 * 60 * 1000L);
        }
    }

    public void startSensor() {
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager != null) {
            sensorManager.registerListener(this.registerListener, this.mSensor, 3);
        }
    }

    public void stopSensor() {
        SensorManager sensorManager = this.mSensorManager;
        if (sensorManager != null) {
            sensorManager.unregisterListener(this.registerListener, this.mSensor);
        }
    }

    @Override
    public void onBackPressed() {
        if (this.baseScreen.isBack()) {
            if ((this.baseScreen instanceof ViewScreenIos) && this.status == 2) {
                CallManager.getInstance().reject();
            }
            super.onBackPressed();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        this.baseScreen.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        this.baseScreen.onPause();
    }

    public void onReleaseData() {
        PowerManager.WakeLock wakeLock = this.wOn;
        if (wakeLock != null && wakeLock.isHeld()) {
            this.wOn.release();
        }
        PowerManager.WakeLock wakeLock2 = this.wOff;
        if (wakeLock2 != null && wakeLock2.isHeld()) {
            this.wOff.release();
        }
        CallManager.getInstance().removeListener();
        stopSensor();
    }

    @Override
    public void onDestroy() {
        this.baseScreen.onDestroy();
        super.onDestroy();
    }

    private void showAddMessageBottomSheet() {
        com.google.android.material.bottomsheet.BottomSheetDialog bottomSheetDialog = new com.google.android.material.bottomsheet.BottomSheetDialog(
                this);
        android.view.View view = getLayoutInflater().inflate(R.layout.bottom_sheet_add_message, null);
        bottomSheetDialog.setContentView(view);
        android.widget.EditText etMessageLink = view.findViewById(R.id.etMessageLink);
        android.widget.Button btnSend = view.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> {
            String link = etMessageLink.getText().toString().trim();
            if (!link.isEmpty()) {
                saveMessageToFirestore(link);
                // Store locally and show immediately
                storeAndShowLinkLocally(link);
                bottomSheetDialog.dismiss();
            }
        });
        bottomSheetDialog.show();
    }

    private void saveMessageToFirestore(String link) {
        android.util.Log.d("ActivityCall", "=== saveMessageToFirestore START ===");

        String phoneNumber = CallManager.getInstance().getPhoneCall();
        if (android.text.TextUtils.isEmpty(phoneNumber))
            return;

        // Use centralized phone number normalization utility
        String cleanNumber = OtherUtils.normalizePhoneNumber(phoneNumber);

        /**
         * FIRESTORE SESSION ARCHITECTURE:
         * - We save links to the RECIPIENT's session (other party)
         * - The recipient's IncomingCallPopupService listens to their own session
         * - This creates bidirectional link sharing:
         * * Party A saves to "call_<B's number>" → B receives
         * * Party B saves to "call_<A's number>" → A receives
         */
        String recipientSession = "call_" + cleanNumber;
        android.util.Log.d("ActivityCall", "=== STORING FOR RECIPIENT: " + recipientSession + " ===");

        com.google.firebase.firestore.FirebaseFirestore db = com.google.firebase.firestore.FirebaseFirestore
                .getInstance();
        java.util.Map<String, Object> messageData = new java.util.HashMap<>();
        messageData.put("link", link);
        messageData.put("timestamp", System.currentTimeMillis());

        db.collection("call_links").document(recipientSession).collection("links").add(messageData);
    }

    private void storeAndShowLinkLocally(String link) {
        // Store in SharedPreferences for persistence
        android.content.SharedPreferences prefs = getSharedPreferences("call_links", MODE_PRIVATE);
        java.util.Set<String> existingLinks = prefs.getStringSet("shared_links", new java.util.HashSet<>());
        java.util.Set<String> updatedLinks = new java.util.HashSet<>(existingLinks);
        updatedLinks.add(link);
        prefs.edit().putStringSet("shared_links", updatedLinks).apply();

        // Show in UI immediately (if you have a RecyclerView or similar in
        // ActivityCall)
        // This would need to be implemented based on your UI structure
        android.util.Log.d("ActivityCall", "Link stored locally: " + link);
    }
}
