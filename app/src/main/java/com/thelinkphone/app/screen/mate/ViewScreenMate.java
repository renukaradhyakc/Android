package com.thelinkphone.app.screen.mate;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;

import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.screen.ActionScreenResult;
import com.thelinkphone.app.screen.BaseScreen;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewScreenMate extends BaseScreen {
    private final ViewAddCallMate viewAddCallMate;
    private final ViewCallMate viewCallMate;

    public ViewScreenMate(Context context) {
        super(context);
        android.util.Log.d("CallScreen", "Using ViewScreenMate layout");
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 70;
        int i2 = (widthScreen * 5) / 12;
        View view = new View(context);
        view.setId(65465);
        int i3 = i2 / 2;
        LayoutParams layoutParams = new LayoutParams(i3, i3);
        layoutParams.addRule(13);
        addView(view, layoutParams);
        LayoutParams layoutParams2 = new LayoutParams(-2, -2);
        layoutParams2.addRule(14);
        layoutParams2.addRule(2, view.getId());
        addView(this.tvStatus, layoutParams2);
        
        android.widget.TextView btnAddMessage = new android.widget.TextView(context);
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
                ViewScreenMate.this.actionScreenResult.onAddMessage();
            }
        });
        LayoutParams btnParams = new LayoutParams(-2, -2);
        btnParams.addRule(14);
        btnParams.addRule(2, ViewScreenMate.this.tvStatus.getId());
        btnParams.setMargins(0, i, 0, 0);
        addView(btnAddMessage, btnParams);
        
        LayoutParams layoutParams3 = new LayoutParams(-2, -2);
        layoutParams3.addRule(14);
        layoutParams3.addRule(2, this.tvStatus.getId());
        layoutParams3.setMargins(i, i, i, i);
        addView(this.tvName, layoutParams3);
        LayoutParams layoutParams4 = new LayoutParams(i2, i2);
        layoutParams4.addRule(14);
        layoutParams4.addRule(2, this.tvName.getId());
        addView(this.imAvatar, layoutParams4);
        this.imAvatar.addStroke(widthScreen / 200, -16777216);
        int i4 = (widthScreen * 19) / 100;
        ViewAddCallMate viewAddCallMate = new ViewAddCallMate(context);
        this.viewAddCallMate = viewAddCallMate;
        viewAddCallMate.setVisibility(View.GONE);
        viewAddCallMate.setItfAddCall(new ActionAcceptResult() { 
            @Override 
            public void onAccept() {
                ViewScreenMate.this.actionScreenResult.onAccept();
                ViewScreenMate.this.viewAddCallMate.onRemove();
                ViewScreenMate.this.viewCallMate.onShow();
            }

            @Override 
            public void onReject() {
                ViewScreenMate.this.actionScreenResult.onReject();
            }
        });
        LayoutParams layoutParams5 = new LayoutParams(-1, i4);
        layoutParams5.addRule(12);
        int i5 = i4 / 2;
        layoutParams5.setMargins(i5, 0, i5, i4);
        addView(viewAddCallMate, layoutParams5);
        ViewCallMate viewCallMate = new ViewCallMate(context);
        this.viewCallMate = viewCallMate;
        addView(viewCallMate, -1, -1);
    }

    @Override 
    public void setActionScreenResult(ActionScreenResult actionScreenResult) {
        super.setActionScreenResult(actionScreenResult);
        this.viewCallMate.setActionScreenResult(actionScreenResult);
    }

    @Override 
    public void callRinging() {
        this.viewAddCallMate.setVisibility(View.VISIBLE);
        this.viewCallMate.onStart();
    }

    @Override 
    public void callStarted() {
        this.viewAddCallMate.onRemove();
        this.viewCallMate.onShow();
    }

    @Override 
    public void initOutgoingCallUI() {
        this.viewAddCallMate.onRemove();
        this.viewCallMate.onShow();
    }

    @Override 
    public void showPhoneAccountPicker() {
        this.viewCallMate.onPadClick();
    }

    @Override 
    public void updateViewMode() {
        this.viewCallMate.updateUI(this.isMute, this.isSpeaker, this.isHold, this.isRec);
    }
}
