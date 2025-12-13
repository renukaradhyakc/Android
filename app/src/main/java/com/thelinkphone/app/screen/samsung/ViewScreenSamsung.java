package com.thelinkphone.app.screen.samsung;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.screen.ActionScreenResult;
import com.thelinkphone.app.screen.BaseScreen;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewScreenSamsung extends BaseScreen {
    private final ViewAddCallGalaxy viewAddCallGalaxy;
    private final ViewCallGalaxy viewCallGalaxy;

    public ViewScreenSamsung(Context context) {
        super(context);
        android.util.Log.d("CallScreen", "Using ViewScreenSamsung layout");
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = (widthScreen * 38) / 100;
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.addRule(14);
        layoutParams.setMargins(0, MyShare.getSizeNotification(context) + ((widthScreen * 5) / 100), 0, 0);
        addView(this.tvName, layoutParams);
        LayoutParams layoutParams2 = new LayoutParams(-2, -2);
        layoutParams2.addRule(14);
        layoutParams2.addRule(3, this.tvName.getId());
        layoutParams2.setMargins(0, 0, 0, widthScreen / 30);
        addView(this.tvStatus, layoutParams2);
        
        android.widget.TextView btnAddMessage = new android.widget.TextView(context);
        btnAddMessage.setId(999888);
        btnAddMessage.setText("Add Message");
        btnAddMessage.setTextColor(Color.WHITE);
        btnAddMessage.setTextSize(16);
        btnAddMessage.setGravity(17);
        btnAddMessage.setPadding(widthScreen / 10, widthScreen / 40, widthScreen / 10, widthScreen / 40);
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(Color.parseColor("#4D000000"));
        drawable.setCornerRadius(widthScreen / 15);
        drawable.setStroke(2, Color.parseColor("#80FFFFFF"));
        btnAddMessage.setBackground(drawable);
        btnAddMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewScreenSamsung.this.actionScreenResult.onAddMessage();
            }
        });
        LayoutParams btnParams = new LayoutParams(-2, -2);
        btnParams.addRule(14);
        btnParams.addRule(3, ViewScreenSamsung.this.tvStatus.getId());
        btnParams.setMargins(0, widthScreen / 50, 0, 0);
        addView(btnAddMessage, btnParams);
        
        LayoutParams layoutParams3 = new LayoutParams(i, i);
        layoutParams3.addRule(14);
        layoutParams3.addRule(3, btnAddMessage.getId());
        addView(this.imAvatar, layoutParams3);
        this.imAvatar.addStroke(widthScreen / 200, -1);
        ViewAddCallGalaxy viewAddCallGalaxy = new ViewAddCallGalaxy(context);
        this.viewAddCallGalaxy = viewAddCallGalaxy;
        viewAddCallGalaxy.setVisibility(View.GONE);
        viewAddCallGalaxy.setAddCallGalaxyResult(new ActionAcceptResult() { 
            @Override 
            public void onAccept() {
                ViewScreenSamsung.this.actionScreenResult.onAccept();
                ViewScreenSamsung.this.viewAddCallGalaxy.onRemove();
                ViewScreenSamsung.this.viewCallGalaxy.onShow();
            }

            @Override 
            public void onReject() {
                ViewScreenSamsung.this.actionScreenResult.onReject();
            }
        });
        LayoutParams layoutParams4 = new LayoutParams(-1, getResources().getDisplayMetrics().heightPixels / 2);
        layoutParams4.addRule(12);
        addView(viewAddCallGalaxy, layoutParams4);
        ViewCallGalaxy viewCallGalaxy = new ViewCallGalaxy(context);
        this.viewCallGalaxy = viewCallGalaxy;
        addView(viewCallGalaxy, -1, -1);
    }

    @Override 
    public void setActionScreenResult(ActionScreenResult actionScreenResult) {
        super.setActionScreenResult(actionScreenResult);
        this.viewCallGalaxy.setActionScreenResult(actionScreenResult);
    }

    @Override 
    public void callRinging() {
        this.viewAddCallGalaxy.setVisibility(View.VISIBLE);
        this.viewCallGalaxy.onStart();
    }

    @Override 
    public void callStarted() {
        this.viewAddCallGalaxy.onRemove();
        this.viewCallGalaxy.onShow();
    }

    @Override 
    public void initOutgoingCallUI() {
        this.viewAddCallGalaxy.onRemove();
        this.viewCallGalaxy.onShow();
    }

    @Override 
    public void showPhoneAccountPicker() {
        this.viewCallGalaxy.onPadClick();
    }

    @Override 
    public void updateViewMode() {
        this.viewCallGalaxy.updateUI(this.isMute, this.isSpeaker, this.isHold, this.isRec);
    }
}
