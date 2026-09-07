package com.thelinkphone.app.service;

import android.app.KeyguardManager;
import android.content.ActivityNotFoundException;
import android.os.Build;
import android.os.PowerManager;
import android.telecom.Call;
import android.telecom.InCallService;
import android.util.Log;

import com.google.firebase.crashlytics.FirebaseCrashlytics;
import com.thelinkphone.app.ActivityCall;
import com.thelinkphone.app.utils.AppBadgeManager;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.ReadContact;
import com.thelinkphone.app.utils.SpamProtectionManager;
import java.util.ArrayList;


public class CallService extends InCallService {
    private static final String TAG = "CallService";
    private SpamProtectionManager spamProtectionManager;

    private final Call.Callback callListener = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int i) {
            super.onStateChanged(call, i);
            CallService.this.callNotificationManager.setupNotification(false);

            // Handle missed calls immediately for badge clearing
            if (i == Call.STATE_DISCONNECTED) {
//                handleCallDisconnected(call);
                handleMissedCallForBadge(call);
            }
        }
    };
    private MyNotificationManager callNotificationManager;

    @Override
    public void onCreate() {
        super.onCreate();
        this.callNotificationManager = new MyNotificationManager(this);
        this.spamProtectionManager = new SpamProtectionManager(this);
        Log.d(TAG, "CallService created with privacy protection enabled");
    }

    @Override
    public void onCallAdded(Call call) {
        super.onCallAdded(call);
        CallManager.getInstance().onAddCall(call, this);
        call.registerCallback(this.callListener);

        // Automatically enable privacy protection based on call settings
        if (spamProtectionManager != null) {
            spamProtectionManager.applyProtectionBasedOnSettings();
        }

        boolean isDeviceLocked = ((KeyguardManager) getSystemService("keyguard")).isDeviceLocked();
        if (!((PowerManager) getSystemService("power")).isInteractive() || isOutGoing(call) || isDeviceLocked) {
            try {
                this.callNotificationManager.setupNotification(false);
                startActivity(ActivityCall.makeIntent(this));
                return;
            } catch (ActivityNotFoundException e) {
                FirebaseCrashlytics.getInstance().recordException(e);
                this.callNotificationManager.setupNotification(true);
                return;
            }
        }
        this.callNotificationManager.setupNotification(true);
    }

    @Override
    public void onCallRemoved(Call call) {
        super.onCallRemoved(call);
        CallManager.getInstance().onRemoveCall(call);
        this.callNotificationManager.cancelNotification();

        // Delete call log if in phonelink scheduled mode
//        deleteCallLogIfNeeded(call);

        // Privacy protection automatically managed by call settings - no need to disable
    }

    private void deleteCallLogIfNeeded(Call call) {
        try {
            // Log current call setting
            String currentSetting = MyShare.getCallSettingName(this);
            Log.d(TAG, "Call ended - Current setting: " + currentSetting);

            // Check if call setting is phonelink scheduled
            if (MyShare.isCallSettingPhonelinkScheduled(this)) {
                // Get the phone number from the call
                String phoneNumber = null;
                if (call.getDetails() != null && call.getDetails().getHandle() != null) {
                    phoneNumber = call.getDetails().getHandle().getSchemeSpecificPart();
                }

                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    Log.d(TAG, "Phonelink Scheduled mode - Scheduling call log deletion for number: " + phoneNumber);
                    final String finalPhoneNumber = phoneNumber;

                    // Use single background thread operation to avoid ANR
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                // Small delay to ensure call log is written
                                Thread.sleep(1000);

                                // Clear missed call badge and delete log in background
                                ReadContact.clearMissedCallBadge(CallService.this, finalPhoneNumber);
                                ReadContact.removeMostRecentCallLog(CallService.this, finalPhoneNumber);

                                Log.d(TAG, "Call log deletion and badge clearing completed for: " + finalPhoneNumber);
                            } catch (Exception e) {
                                Log.e(TAG, "Error in background call log deletion: " + e.getMessage());
                            }
                        }
                    }).start();
                } else {
                    Log.w(TAG, "Could not extract phone number from call details for log deletion");
                }
            } else {
                Log.d(TAG, "Unrestricted mode - Call log preserved");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error in call log deletion process: " + e.getMessage(), e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        FirebaseCrashlytics.getInstance().log("service_destroyed | CallService.onDestroy() called");
        this.callNotificationManager.cancelNotification();

        // Privacy protection is managed by call settings

        // Clear the static reference to prevent memory leaks
        CallManager.clearStaticServiceReference();
    }

    private boolean isOutGoing(Call call) {
        if (Build.VERSION.SDK_INT >= 29) {
            return call.getDetails().getCallDirection() == 1;
        }
        ArrayList arrayList = new ArrayList();
        arrayList.add(9);
        arrayList.add(1);
        arrayList.add(8);
        return arrayList.contains(Integer.valueOf(CallManager.getState(call)));
    }

    private void handleCallDisconnected(Call call) {
        try {
            if (MyShare.isCallSettingPhonelinkScheduled(this)) {
                // Get call details
                String phoneNumber = null;
                if (call.getDetails() != null && call.getDetails().getHandle() != null) {
                    phoneNumber = call.getDetails().getHandle().getSchemeSpecificPart();
                }

                // Check if this was a missed call
                if (phoneNumber != null && !phoneNumber.isEmpty()) {
                    int callState = CallManager.getState(call);
                    boolean wasMissedCall = (callState == Call.STATE_DISCONNECTED &&
                                           !wasCallAnswered(call));

                    if (wasMissedCall) {
                        Log.d(TAG, "Detected missed call in Phonelink Scheduled mode - scheduling badge clearing for: " + phoneNumber);

                        // Clear missed call badge in background to avoid ANR
                        final String finalPhoneNumber = phoneNumber;
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    Thread.sleep(200); // Short delay
                                    ReadContact.clearMissedCallBadge(CallService.this, finalPhoneNumber);
                                    Log.d(TAG, "Missed call badge cleared in background for: " + finalPhoneNumber);
                                } catch (Exception e) {
                                    Log.e(TAG, "Error clearing missed call badge in background: " + e.getMessage());
                                }
                            }
                        }).start();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error handling call disconnection: " + e.getMessage());
        }
    }

    private boolean wasCallAnswered(Call call) {
        try {
            // Check if call was ever in active or holding state
            return false; // For now, assume all disconnected calls are missed calls
        } catch (Exception e) {
            return false;
        }
    }

    private void handleMissedCallForBadge(Call call) {
        try {
            int disconnectCode = call.getDetails().getDisconnectCause().getCode();
            if (disconnectCode == android.telecom.DisconnectCause.MISSED) {
                AppBadgeManager.increment(this);
                Log.d(TAG, "Missed call detected via DisconnectCause.MISSED - badge incremented");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error checking disconnect cause for badge: " + e.getMessage());
        }
    }
}
