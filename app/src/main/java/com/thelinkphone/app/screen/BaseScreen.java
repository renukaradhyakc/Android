package com.thelinkphone.app.screen;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.view.View;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.thelinkphone.app.ActivityCall;
import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.AvatarPeople;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.service.CallManager;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.ReadContact;

public abstract class BaseScreen extends RelativeLayout {
    public ActionScreenResult actionScreenResult;
    public AppCompatActivity activityCall;
    private final BackgroundManager backgroundManager;
    public int callDuration;
    private final Handler handler;
    public final AvatarPeople imAvatar;
    public boolean isEndCall;
    public boolean isHold;
    public boolean isMute;
    public boolean isRec;
    public boolean isSpeaker;
    private final Runnable runnable;
    public final TextW tvName;
    public final TextW tvStatus;
    private ViewAllContact viewAllContact;

    public abstract void callRinging();

    public abstract void callStarted();

    public abstract void initOutgoingCallUI();

    public abstract void showPhoneAccountPicker();

    public abstract void updateViewMode();

    public BaseScreen(Context context) {
        super(context);
        this.runnable = new Runnable() {
            @Override
            public void run() {
                if (BaseScreen.this.isEndCall) {
                    return;
                }
                BaseScreen.this.callDuration = CallManager.getInstance().getTimeCall();
                BaseScreen.this.tvStatus.setText(OtherUtils.longToDur(BaseScreen.this.callDuration));
                BaseScreen.this.handler.postDelayed(this, 1000L);
            }
        };
        setBackgroundColor(-16777216);
        if (context instanceof AppCompatActivity) {
            this.activityCall = (AppCompatActivity) context;
        }
        this.backgroundManager = new BackgroundManager(this);
        View view = new View(context);
        view.setBackgroundColor(Color.parseColor("#30000000"));
        addView(view, -1, -1);
        AvatarPeople avatarPeople = new AvatarPeople(context);
        this.imAvatar = avatarPeople;
        avatarPeople.setId(100);
        TextW textW = new TextW(context);
        this.tvName = textW;
        textW.setId(6666654);
        textW.setupText(600, 6.3f);
        textW.setTextColor(-1);
        TextW textW2 = new TextW(context);
        this.tvStatus = textW2;
        textW2.setId(6666655);
        textW2.setupText(400, 3.8f);
        textW2.setTextColor(-1);
        textW2.setText(R.string.app_name);
        setDataCallInfo();
        this.handler = new Handler();
    }

    public void setDataCallInfo() {
        String phoneNumber = CallManager.getInstance().getPhoneCall();
        android.util.Log.d("BaseScreen", "Setting caller info for: " + phoneNumber);

        String[] contactData = ReadContact.getNamePhoto(getContext(), phoneNumber);
        String displayName = (contactData != null && !contactData[0].isEmpty())
                ? contactData[0]
                : MyShare.getCallerName(getContext());

        if (displayName == null || displayName.isEmpty()) {
            displayName = getContext().getString(R.string.unknown_caller);
        }

        setCallerName(displayName);
    }

    public void setActionScreenResult(ActionScreenResult actionScreenResult) {
        this.actionScreenResult = actionScreenResult;
    }

    public void updateStatus(int i) {
        if (i != 1) {
            if (i == 2) {
                this.tvStatus.setText(R.string.is_calling);
                callRinging();
                return;
            } else if (i == 4) {
                this.isEndCall = false;
                this.handler.removeCallbacks(this.runnable);
                this.handler.post(this.runnable);
                callStarted();
                return;
            } else if (i == 7) {
                AppCompatActivity appCompatActivity = this.activityCall;
                if (appCompatActivity instanceof ActivityCall) {
                    ((ActivityCall) appCompatActivity).onReleaseData();
                }
                endCall();
                this.isEndCall = true;
                return;
            } else if (i == 8) {
                showPhoneAccountPicker();
                return;
            } else if (i != 9) {
                return;
            }
        }
        AppCompatActivity appCompatActivity2 = this.activityCall;
        if (appCompatActivity2 instanceof ActivityCall) {
            ((ActivityCall) appCompatActivity2).startSensor();
        }
        this.tvStatus.setText(R.string.dialing);
        initOutgoingCallUI();
    }

    public void onShowContact() {
        if (this.viewAllContact == null) {
            this.viewAllContact = new ViewAllContact(getContext());
        }
        addView(this.viewAllContact, -1, -1);
        this.viewAllContact.onShow();
    }

    public boolean isBack() {
        ViewAllContact viewAllContact = this.viewAllContact;
        if (viewAllContact == null || indexOfChild(viewAllContact) == -1) {
            return true;
        }
        this.viewAllContact.onHide();
        return false;
    }

    private void endCall() {
        AppCompatActivity appCompatActivity = this.activityCall;
        if (appCompatActivity == null) {
            return;
        }

        if (appCompatActivity instanceof ActivityCall) {
            ((ActivityCall) appCompatActivity).stopSensor();
        }
        if (this.isEndCall) {
            this.activityCall.finishAndRemoveTask();
        } else if (this.callDuration > 0) {
            this.activityCall.runOnUiThread(new Runnable() {
                @Override
                public final void run() {
                    BaseScreen.this.m181x6b07399e();
                }
            });
        } else {
            this.tvStatus.setText(R.string.call_ended);
            this.activityCall.finish();
        }
    }



    public  void m181x6b07399e() {
        this.tvStatus.setText(OtherUtils.longToDur(this.callDuration) + " (" + getContext().getString(R.string.call_ended) + ")");
        new Handler().postDelayed(new Runnable() {
            @Override
            public final void run() {
                BaseScreen.this.m180x50ebbaff();
            }
        }, 1500L);
    }

    public void setCallerName(String name) {
        if (tvName != null && name != null && !name.isEmpty()) {
            tvName.setText(name);
        }
    }

    public  void m180x50ebbaff() {
        this.activityCall.finishAndRemoveTask();
    }

    public void onResume() {
        this.backgroundManager.onResume();
    }

    public void onPause() {
        this.backgroundManager.onPause();
    }

    public void onDestroy() {
        this.backgroundManager.onDestroy();
    }
}
