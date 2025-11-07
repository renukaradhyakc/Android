package com.thelinkphone.app.screen.pixel;

import android.content.Context;
import android.view.View;

import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.screen.ActionScreenResult;
import com.thelinkphone.app.screen.BaseScreen;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewScreenPixel extends BaseScreen {
    private final ViewCallPixel viewCall;
    private final ViewAdd viewInComing;

    public ViewScreenPixel(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 25;
        int i2 = (widthScreen * 5) / 12;
        View view = new View(context);
        view.setId(65465);
        int i3 = i2 / 5;
        LayoutParams layoutParams = new LayoutParams(i3, i3);
        layoutParams.addRule(13);
        addView(view, layoutParams);
        LayoutParams layoutParams2 = new LayoutParams(i2, i2);
        layoutParams2.addRule(14);
        layoutParams2.addRule(8, view.getId());
        addView(this.imAvatar, layoutParams2);
        LayoutParams layoutParams3 = new LayoutParams(-2, -2);
        layoutParams3.addRule(14);
        layoutParams3.addRule(2, this.imAvatar.getId());
        layoutParams3.setMargins(i, i, i, i * 3);
        addView(this.tvStatus, layoutParams3);
        LayoutParams layoutParams4 = new LayoutParams(-2, -2);
        layoutParams4.addRule(14);
        layoutParams4.addRule(2, this.tvStatus.getId());
       // addView(this.tvName, layoutParams4);
        ViewAdd viewAdd = new ViewAdd(getContext());
        this.viewInComing = viewAdd;
        viewAdd.setVisibility(View.GONE);
        viewAdd.setItfAddCall(new ActionAcceptResult() { 
            @Override 
            public void onAccept() {
                ViewScreenPixel.this.actionScreenResult.onAccept();
                ViewScreenPixel.this.viewInComing.onRemove();
                ViewScreenPixel.this.viewCall.onShow();
            }

            @Override 
            public void onReject() {
                ViewScreenPixel.this.actionScreenResult.onReject();
            }
        });
        LayoutParams layoutParams5 = new LayoutParams(-1, ((int) ((widthScreen * 19.7d) / 100.0d)) * 3);
        layoutParams5.addRule(12);
        layoutParams5.setMargins(0, 0, 0, MyShare.getSizeNavigation(context) + (widthScreen / 100));
        addView(viewAdd, layoutParams5);
        ViewCallPixel viewCallPixel = new ViewCallPixel(context);
        this.viewCall = viewCallPixel;
        LayoutParams layoutParams6 = new LayoutParams(-1, -2);
        layoutParams6.addRule(12);
        addView(viewCallPixel, layoutParams6);
    }

    @Override 
    public void setActionScreenResult(ActionScreenResult actionScreenResult) {
        super.setActionScreenResult(actionScreenResult);
        this.viewCall.setActionScreenResult(actionScreenResult);
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
