package com.thelinkphone.app.screen.other;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;
import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.screen.ActionScreenResult;
import com.thelinkphone.app.screen.BaseScreen;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewScreenOther extends BaseScreen {
    private final ViewAddCallOther viewAddCallOther;
    private final ViewCallOther viewCallOther;

    public ViewScreenOther(Context context) {
        super(context);
        android.util.Log.d("CallScreen", "Using ViewScreenOther layout");
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = (widthScreen * 38) / 100;
        LayoutParams layoutParams = new LayoutParams(i, i);
        layoutParams.addRule(14);
        layoutParams.setMargins(0, MyShare.getSizeNotification(context) + (i / 3), 0, 0);
        addView(this.imAvatar, layoutParams);
        this.imAvatar.addStroke(widthScreen / 200, Color.parseColor("#90ffffff"));
        LayoutParams layoutParams2 = new LayoutParams(-2, -2);
        layoutParams2.addRule(14);
        layoutParams2.addRule(3, this.imAvatar.getId());
        int i2 = widthScreen / 100;
        layoutParams2.setMargins(0, i2, 0, i2);
        addView(this.tvName, layoutParams2);
        LayoutParams layoutParams3 = new LayoutParams(-2, -2);
        layoutParams3.addRule(14);
        layoutParams3.addRule(3, this.tvName.getId());
        addView(this.tvStatus, layoutParams3);
        
        android.widget.TextView btnAddMessage = new android.widget.TextView(context);
        btnAddMessage.setText("Add Message [OTHER LAYOUT]");
        btnAddMessage.setTextColor(Color.WHITE);
        btnAddMessage.setTextSize(16);
        btnAddMessage.setGravity(17);
        int padding = widthScreen / 25;
        btnAddMessage.setPadding(padding * 2, padding, padding * 2, padding);
        btnAddMessage.setMinHeight(widthScreen / 12);
        android.graphics.drawable.GradientDrawable drawable = new android.graphics.drawable.GradientDrawable();
        drawable.setColor(Color.parseColor("#50000000"));
        drawable.setCornerRadius(widthScreen / 12);
        drawable.setStroke(3, Color.parseColor("#AAFFFFFF"));
        btnAddMessage.setBackground(drawable);
        btnAddMessage.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ViewScreenOther.this.actionScreenResult.onAddMessage();
            }
        });
        LayoutParams btnParams = new LayoutParams(-2, -2);
        btnParams.addRule(14);
        btnParams.addRule(3, this.tvStatus.getId());
        btnParams.setMargins(0, widthScreen / 20, 0, 0);
        addView(btnAddMessage, btnParams);        ViewAddCallOther viewAddCallOther = new ViewAddCallOther(context);
        this.viewAddCallOther = viewAddCallOther;
        viewAddCallOther.setVisibility(View.GONE);
        LayoutParams layoutParams4 = new LayoutParams(-1, widthScreen / 2);
        layoutParams4.addRule(12);
        layoutParams4.setMargins(0, 0, 0, MyShare.getSizeNavigation(context));
        addView(viewAddCallOther, layoutParams4);
        viewAddCallOther.setActionScreenResult(new ActionAcceptResult() { 
            @Override 
            public void onAccept() {
                ViewScreenOther.this.actionScreenResult.onAccept();
                ViewScreenOther.this.viewAddCallOther.onRemove();
                ViewScreenOther.this.viewCallOther.onShow();
            }

            @Override 
            public void onReject() {
                ViewScreenOther.this.actionScreenResult.onReject();
            }
        });
        ViewCallOther viewCallOther = new ViewCallOther(context);
        this.viewCallOther = viewCallOther;
        addView(viewCallOther, -1, -1);
    }

    @Override 
    public void setActionScreenResult(ActionScreenResult actionScreenResult) {
        super.setActionScreenResult(actionScreenResult);
        this.viewCallOther.setActionScreenResult(actionScreenResult);
    }

    @Override 
    public void callRinging() {
        this.viewAddCallOther.setVisibility(View.VISIBLE);
        this.viewCallOther.onStart();
    }

    @Override 
    public void callStarted() {
        this.viewAddCallOther.onRemove();
        this.viewCallOther.onShow();
    }

    @Override 
    public void initOutgoingCallUI() {
        this.viewAddCallOther.onRemove();
        this.viewCallOther.onShow();
    }

    @Override 
    public void showPhoneAccountPicker() {
        this.viewCallOther.onPadClick();
    }

    @Override 
    public void updateViewMode() {
        this.viewCallOther.updateUI(this.isMute, this.isSpeaker, this.isHold, this.isRec);
    }
}
