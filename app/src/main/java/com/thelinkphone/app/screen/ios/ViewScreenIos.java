package com.thelinkphone.app.screen.ios;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.screen.ActionScreenResult;
import com.thelinkphone.app.screen.BaseScreen;
import com.thelinkphone.app.service.CallManager;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.ReadContact;


public class ViewScreenIos extends BaseScreen {
    private final ViewCall viewCall;
    private final ViewInComingIOS viewInComing;

    public ViewScreenIos(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 25;
        int i2 = (widthScreen * 18) / 100;
        LayoutParams layoutParams = new LayoutParams(i2, i2);
        layoutParams.addRule(21);
        int i3 = (widthScreen * 13) / 100;
        layoutParams.setMargins(0, MyShare.getSizeNotification(context) + i3, i, 0);
        addView(this.imAvatar, layoutParams);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setId(456456);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        linearLayout.setGravity(16);
        linearLayout.setPadding(i, 0, i, 0);
        LayoutParams layoutParams2 = new LayoutParams(-1, i2);
        layoutParams2.setMargins(0, MyShare.getSizeNotification(context) + i3, 0, 0);
        layoutParams2.addRule(16, this.imAvatar.getId());
        addView(linearLayout, layoutParams2);
        // Add the stylish TheLinkPhone branding (privacy-focused)
        linearLayout.addView(this.tvName, -1, -2);
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
        layoutParams3.setMargins(0, widthScreen / 200, 0, 0);
        linearLayout.addView(this.tvStatus, layoutParams3);
        ViewInComingIOS viewInComingIOS = new ViewInComingIOS(context);
        this.viewInComing = viewInComingIOS;
        viewInComingIOS.setViewRoot(this);
        viewInComingIOS.setVisibility(View.GONE);
        viewInComingIOS.setActionScreenResult(new ActionAcceptResult() { 
            @Override 
            public void onReject() {
            }

            @Override 
            public void onAccept() {
                ViewScreenIos.this.actionScreenResult.onAccept();
                ViewScreenIos.this.viewInComing.onRemove();
                ViewScreenIos.this.viewCall.onShow();
            }
        });
        LayoutParams layoutParams4 = new LayoutParams(-1, (int) ((widthScreen * 21.2f) / 100.0f));
        layoutParams4.addRule(12);
        int i4 = widthScreen / 8;
        layoutParams4.setMargins(i4, 0, i4, MyShare.getSizeNavigation(context) + (widthScreen / 7));
        addView(viewInComingIOS, layoutParams4);
        ViewCall viewCall = new ViewCall(context);
        this.viewCall = viewCall;
        LayoutParams layoutParams5 = new LayoutParams(-1, -1);
        layoutParams5.addRule(3, linearLayout.getId());
        addView(viewCall, layoutParams5);
    }

    @Override 
    public void setActionScreenResult(ActionScreenResult actionScreenResult) {
        super.setActionScreenResult(actionScreenResult);
        this.viewCall.setActionScreenResult(actionScreenResult);
    }

    @Override 
    public void setDataCallInfo() {
        // Privacy-focused: Use the BaseScreen's privacy-focused branding
        android.util.Log.d("ViewScreenIos", "Using privacy-focused call info from BaseScreen");
        
        // Call the parent method which sets up privacy-focused branding
        super.setDataCallInfo();
        
        // Additional iOS-specific privacy styling
        try {
            // Ensure avatar is completely hidden for privacy
            this.imAvatar.setVisibility(View.GONE);
            
            // Center align both name and status for better visual balance
            this.tvName.setGravity(android.view.Gravity.CENTER);
            this.tvStatus.setGravity(android.view.Gravity.CENTER);
            
            // Add a subtle gradient effect to the branding text (iOS style)
            this.tvName.setTextColor(android.graphics.Color.WHITE);
            
            android.util.Log.d("ViewScreenIos", "iOS-specific privacy styling applied");
        } catch (Exception e) {
            android.util.Log.e("ViewScreenIos", "Error applying iOS privacy styling: " + e.getMessage());
        }
    }

    @Override 
    public void callRinging() {
        this.viewInComing.setVisibility(View.VISIBLE);
        this.viewCall.onStart();
    }

    @Override 
    public void callStarted() {
        this.viewInComing.onRemove();
        this.viewCall.onShow();
    }

    @Override 
    public void initOutgoingCallUI() {
        this.viewInComing.onRemove();
        this.viewCall.onShow();
    }

    @Override 
    public void showPhoneAccountPicker() {
        this.viewCall.onPadClick();
    }

    @Override 
    public void updateViewMode() {
        this.viewCall.updateUI(this.isMute, this.isSpeaker, this.isHold, this.isRec);
    }
}
