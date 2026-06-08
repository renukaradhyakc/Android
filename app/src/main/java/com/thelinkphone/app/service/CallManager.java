package com.thelinkphone.app.service;

import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Build;
import android.telecom.Call;
import android.telecom.InCallService;
import android.util.Log;

import com.thelinkphone.app.utils.MyShare;

import java.util.List;


public class CallManager {
    private static CallManager callManager;
    private static InCallService staticInCallService; // Keep a static reference to prevent loss
    private Call call;
    private final Call.Callback callback = new Call.Callback() {
        @Override
        public void onStateChanged(Call call, int i) {
            super.onStateChanged(call, i);
            if (CallManager.this.listener != null) {
                CallManager.this.listener.onStateChanged(i);
            }
        }

        @Override
        public void onDetailsChanged(Call call, Call.Details details) {
            super.onDetailsChanged(call, details);
        }

        @Override
        public void onConferenceableCallsChanged(Call call, List<Call> list) {
            super.onConferenceableCallsChanged(call, list);
        }
    };
    private InCallService inCallService;
    private CallManagerListener listener;
    private AudioManager mAudioManager;
    private MyRecorder myRecorder;

    public static CallManager getInstance() {
        if (callManager == null) {
            callManager = new CallManager();
        }
        return callManager;
    }

    public void onAddCall(Call call, InCallService inCallService) {
        Log.d("CallManager", "Adding call to CallManager");
        this.inCallService = inCallService;
        staticInCallService = inCallService; // Store static reference to prevent loss
        this.mAudioManager = (AudioManager) inCallService.getSystemService("audio");

        if (this.mAudioManager == null) {
            Log.e("CallManager", "Failed to get AudioManager");
        } else {
            Log.d("CallManager", "AudioManager initialized successfully");
        }

        Call call2 = this.call;
        if (call2 != null) {
            call2.unregisterCallback(this.callback);
            this.call.disconnect();
        }
        this.call = call;
        call.registerCallback(this.callback);

        try {
            this.myRecorder = new MyRecorder(inCallService, getPhoneCall(), getState());
            Log.d("CallManager", "MyRecorder initialized successfully");
        } catch (Exception e) {
            Log.e("CallManager", "Error initializing MyRecorder: " + e.getMessage());
            e.printStackTrace();
        }
        int state = getState(call);
        Log.d("CallManager", "Call state detected: " + state + " (1=DIALING, 2=RINGING, 4=ACTIVE)");        if (state == Call.STATE_RINGING) {
            Log.d("CallManager", "Incoming call detected — triggering popup");

            int callMode = 0;
            String username = getPhoneCall(); // caller number
            boolean isCallalinkUser = false;
            boolean isWithinSchedule = true;
            boolean isAContact = false;

            try {
                IncomingCallPopupService.showPopup(
                        inCallService,
                        callMode,
                        username,
                        isCallalinkUser,
                        isWithinSchedule,
                        isAContact
                );
            } catch (Exception e) {
                Log.e("CallManager", "Error launching IncomingCallPopupService: " + e.getMessage());
                e.printStackTrace();
            }
        }
        
        // Also trigger popup for outgoing calls
//        if (state == Call.STATE_DIALING || state == 9) {
//            Log.d("CallManager", "Outgoing/Active call detected — triggering popup");
//
//            int callMode = 0;
//            String username = getPhoneCall(); // callee number
//            boolean isCallalinkUser = false;
//            boolean isWithinSchedule = true;
//            boolean isAContact = false;
//
//            try {
//                IncomingCallPopupService.showPopup(
//                        inCallService,
//                        callMode,
//                        username,
//                        isCallalinkUser,
//                        isWithinSchedule,
//                        isAContact
//                );
//            } catch (Exception e) {
//                Log.e("CallManager", "Error launching IncomingCallPopupService for outgoing call: " + e.getMessage());
//                e.printStackTrace();
//            }
//
//        }
    }
    public void onRemoveCall(Call call) {
        Log.d("CallManager", "Removing call from CallManager");
        Call call2 = this.call;
        if (call2 == call) {
            int callState = getState(call);
            Log.d("CallManager", "Call state during removal: " + callState);

            call2.unregisterCallback(this.callback);
            this.call = null;
            Log.d("CallManager", "Call reference cleared");

            // Only clear service references when call is actually ended (DISCONNECTED)
            if (callState == 7) { // Call.STATE_DISCONNECTED
                Log.d("CallManager", "Call disconnected, clearing service references");

                try {
                    IncomingCallPopupService.hidePopup(call.getDetails().getHandle() != null
                            ? inCallService : staticInCallService);
                } catch (Exception e) {
                    Log.e("CallManager", "Failed to hide popup: " + e.getMessage());
                }
                Context context = (inCallService != null) ? inCallService : staticInCallService;
                if (context != null) {
                    MyShare.clearCallInfo(context);
                }

                this.inCallService = null;
                this.mAudioManager = null;

            } else {
                Log.d("CallManager", "Call removed but not disconnected, keeping service references");
            }
        }

        MyRecorder myRecorder = this.myRecorder;
        if (myRecorder != null) {
            myRecorder.stopRecord();
            this.myRecorder = null;
        }
    }

    public void accept() {
        Call call = this.call;
        if (call != null) {
            call.answer(0);
        }
    }

    public void reject() {
        if (this.call != null) {
            try {
                int currentState = getState();
                Log.d("CallManager", "Rejecting/ending call, current state: " + currentState);

                if (currentState == 2) { // Call.STATE_RINGING
                    this.call.reject(false, null);
                    Log.d("CallManager", "Rejecting incoming call");

                    String username = getPhoneCall();
                    int callMode=0;
                    boolean ismanuallyblocked=false;
                    String phoneNumber = "";

                    BlockedPopupService.showPopup(
                            inCallService,
                            callMode,
                            username,
                            ismanuallyblocked,
                            phoneNumber
                    );
                } else {
                    this.call.disconnect();
                    Log.d("CallManager", "Disconnecting active call");
                }
            } catch (Exception e) {
                Log.e("CallManager", "Error rejecting/ending call: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e("CallManager", "Call is null, cannot reject/end - trying to get active calls from service");

            // Try to get active calls from the InCallService if available
            if (ensureServiceConnection() && this.inCallService != null) {
                try {
                    List<Call> calls = this.inCallService.getCalls();
                    if (calls != null && !calls.isEmpty()) {
                        Call activeCall = calls.get(0); // Get the first active call
                        Log.d("CallManager", "Found active call from service, attempting to disconnect");
                        activeCall.disconnect();
                    } else {
                        Log.e("CallManager", "No active calls found in service");
                    }
                } catch (Exception e) {
                    Log.e("CallManager", "Error getting calls from service: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    public boolean hold() {
        if (this.call == null) {
            Log.e("CallManager", "Call is null, cannot hold/unhold - trying to get active calls from service");

            // Try to get active calls from the InCallService if available
            if (ensureServiceConnection() && this.inCallService != null) {
                try {
                    List<Call> calls = this.inCallService.getCalls();
                    if (calls != null && !calls.isEmpty()) {
                        Call activeCall = calls.get(0); // Get the first active call
                        this.call = activeCall; // Restore the call reference
                        Log.d("CallManager", "Restored call reference from service");
                    } else {
                        Log.e("CallManager", "No active calls found in service for hold");
                        return false;
                    }
                } catch (Exception e) {
                    Log.e("CallManager", "Error getting calls from service for hold: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            } else {
                return false;
            }
        }

        try {
            int currentState = getState();
            boolean isOnHold = currentState == 3; // Call.STATE_HOLDING
            Log.d("CallManager", "Current call state: " + currentState + ", isOnHold: " + isOnHold);

            if (isOnHold) {
                this.call.unhold();
                Log.d("CallManager", "Unholding call");
            } else {
                this.call.hold();
                Log.d("CallManager", "Holding call");
            }
            return !isOnHold;
        } catch (Exception e) {
            Log.e("CallManager", "Error toggling hold: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean switchSpeaker() {
        // Try to re-establish service connection if needed
        if (!ensureServiceConnection()) {
            Log.e("CallManager", "Cannot establish service connection for speaker");
            return false;
        }

        AudioManager audioManager = this.mAudioManager;
        if (audioManager == null || this.inCallService == null) {
            Log.e("CallManager", "AudioManager or InCallService is null after connection attempt");
            return false;
        }

        boolean isSpeakerphoneOn = audioManager.isSpeakerphoneOn();
        Log.d("CallManager", "Current speaker state: " + isSpeakerphoneOn);
        try {
            if (!isSpeakerphoneOn) {
                this.inCallService.setAudioRoute(8); // ROUTE_SPEAKER
                Log.d("CallManager", "Setting audio route to speaker");
            } else {
                this.inCallService.setAudioRoute(1); // ROUTE_EARPIECE
                Log.d("CallManager", "Setting audio route to earpiece");
            }
        } catch (Exception e) {
            Log.e("CallManager", "Error switching speaker: " + e.getMessage());
            e.printStackTrace();
        }
        return !isSpeakerphoneOn;
    }

    public boolean muteSpeaker() {
        // Try to re-establish service connection if needed
        if (!ensureServiceConnection()) {
            Log.e("CallManager", "Cannot establish service connection for mute");
            return false;
        }

        AudioManager audioManager = this.mAudioManager;
        if (audioManager == null || this.inCallService == null) {
            Log.e("CallManager", "AudioManager or InCallService is null for mute after connection attempt");
            return false;
        }
        try {
            audioManager.setMode(AudioManager.MODE_IN_CALL);
            boolean isMicrophoneMute = this.mAudioManager.isMicrophoneMute();
            Log.d("CallManager", "Current mute state: " + isMicrophoneMute);

            InCallService inCallService = this.inCallService;
            if (inCallService != null) {
                inCallService.setMuted(!isMicrophoneMute);
                Log.d("CallManager", "Setting mute to: " + !isMicrophoneMute);
            }
            return !isMicrophoneMute;
        } catch (Exception e) {
            Log.e("CallManager", "Error toggling mute: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public void startRecorder() {
        MyRecorder myRecorder = this.myRecorder;
        if (myRecorder != null) {
            try {
                Log.d("CallManager", "Starting call recording");
                myRecorder.startRecord();
            } catch (Exception e) {
                Log.e("CallManager", "Error starting recording: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            Log.e("CallManager", "MyRecorder is null, cannot start recording");
        }
    }

    public void onKeyPad(String str) {
        Call call = this.call;
        if (call == null) {
            return;
        }
        call.playDtmfTone(str.charAt(0));
        this.call.stopDtmfTone();
    }

    public int getState() {
        Call call = this.call;
        if (call == null || call.getDetails() == null) {
            return -1;
        }
        if (Build.VERSION.SDK_INT >= 31) {
            return this.call.getDetails().getState();
        }
        return this.call.getState();
    }

    public String getPhoneCall() {
        String decode;
        try {
            Call call = this.call;
            return (call == null || call.getDetails() == null || (decode = Uri.decode(this.call.getDetails().getHandle().toString())) == null || !decode.startsWith("tel:")) ? "" : decode.substring(decode.indexOf("tel:") + 4);
        } catch (Exception unused) {
            return "";
        }
    }

    public int getTimeCall() {
        Call call = this.call;
        if (call == null || call.getDetails().getConnectTimeMillis() == 0) {
            return 0;
        }
        return (int) ((System.currentTimeMillis() - this.call.getDetails().getConnectTimeMillis()) / 1000);
    }

    public void addListener(CallManagerListener callManagerListener) {
        this.listener = callManagerListener;
    }

    public void removeListener() {
        this.listener = null;
    }

    public static int getState(Call call) {
        if (call == null) {
            return -1;
        }
        if (Build.VERSION.SDK_INT >= 31) {
            return call.getDetails().getState();
        }
        return call.getState();
    }

    private boolean ensureServiceConnection() {
        if (this.inCallService != null && this.mAudioManager != null) {
            return true;
        }

        Log.w("CallManager", "Service connection lost, attempting to re-establish from static reference");

        // Try to restore from static reference
        if (staticInCallService != null) {
            try {
                this.inCallService = staticInCallService;
                this.mAudioManager = (AudioManager) staticInCallService.getSystemService("audio");

                if (this.mAudioManager != null) {
                    Log.d("CallManager", "Service connection restored from static reference");
                    return true;
                } else {
                    Log.e("CallManager", "Failed to get AudioManager from static service reference");
                }
            } catch (Exception e) {
                Log.e("CallManager", "Error restoring service connection: " + e.getMessage());
                e.printStackTrace();
            }
        }

        Log.e("CallManager", "Cannot re-establish service connection");
        return false;
    }

    public static void clearStaticServiceReference() {
        Log.d("CallManager", "Clearing static service reference");
        staticInCallService = null;
    }
}
