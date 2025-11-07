package com.thelinkphone.app.custom;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewModeRecent extends RelativeLayout {
    private boolean isAll;
    private ModeResult modeResult;
    private final boolean theme;
    private final TextW tvAll;
    private final TextW tvMiss;
    private final ImageView vRun;

    
    public interface ModeResult {
        void onMode(boolean z);
    }

    public void setModeResult(ModeResult modeResult) {
        this.modeResult = modeResult;
    }

    public ViewModeRecent(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 60;
        int i2 = (widthScreen * 19) / 100;
        boolean theme = MyShare.getTheme(context);
        this.theme = theme;
        this.isAll = true;
        LayoutTransition layoutTransition = new LayoutTransition();
        layoutTransition.setDuration(400L);
        layoutTransition.enableTransitionType(4);
        setLayoutTransition(layoutTransition);
        TextW textW = new TextW(context);
        this.tvAll = textW;
        textW.setId(600);
        textW.setupText(400, 2.9f);
        textW.setGravity(1);
        textW.setPadding(0, i, 0, i);
        textW.setText(R.string.all);
        textW.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewModeRecent.this.m92xa24faa79(view);
            }
        });
        TextW textW2 = new TextW(context);
        this.tvMiss = textW2;
        textW2.setId(601);
        textW2.setupText(400, 2.9f);
        textW2.setGravity(1);
        textW2.setPadding(0, i, 0, i);
        textW2.setText(R.string.missed_call);
        textW2.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewModeRecent.this.m93x2f3cc198(view);
            }
        });
        int i3 = widthScreen / 200;
        ImageView imageView = new ImageView(context);
        this.vRun = imageView;
        imageView.setPadding(i3, i3, i3, i3);
        LayoutParams layoutParams = new LayoutParams(i2, -1);
        layoutParams.addRule(6, textW.getId());
        layoutParams.addRule(8, textW.getId());
        layoutParams.addRule(18, textW.getId());
        addView(imageView, layoutParams);
        addView(textW, i2, -2);
        LayoutParams layoutParams2 = new LayoutParams(i2, -2);
        layoutParams2.addRule(17, textW.getId());
        addView(textW2, layoutParams2);
        if (theme) {
            float f = widthScreen;
            setBackground(OtherUtils.bgIcon(Color.parseColor("#DCDCDC"), f / 50.0f));
            imageView.setImageDrawable(OtherUtils.bgIcon(-1, f / 60.0f));
            textW.setTextColor(-16777216);
            textW2.setTextColor(-16777216);
        } else {
            float f2 = widthScreen;
            setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), f2 / 50.0f));
            imageView.setImageDrawable(OtherUtils.bgIcon(Color.parseColor("#B8B8B8"), f2 / 60.0f));
        }
        updateLayout();
    }

    
    
    public  void m92xa24faa79(View view) {
        onAllClick();
    }

    
    
    public  void m93x2f3cc198(View view) {
        onMissClick();
    }

    private void onAllClick() {
        if (this.isAll) {
            return;
        }
        this.isAll = true;
        updateLayout();
        this.modeResult.onMode(true);
    }

    private void onMissClick() {
        if (this.isAll) {
            this.isAll = false;
            updateLayout();
            this.modeResult.onMode(false);
        }
    }

    private void updateLayout() {
        LayoutParams layoutParams = (LayoutParams) this.vRun.getLayoutParams();
        if (this.isAll) {
            layoutParams.removeRule(19);
            layoutParams.addRule(18, this.tvAll.getId());
            if (!this.theme) {
                this.tvAll.setTextColor(Color.parseColor("#2C2C2C"));
                this.tvMiss.setTextColor(-1);
            }
        } else {
            layoutParams.removeRule(18);
            layoutParams.addRule(19, this.tvMiss.getId());
            if (!this.theme) {
                this.tvMiss.setTextColor(Color.parseColor("#2C2C2C"));
                this.tvAll.setTextColor(-1);
            }
        }
        this.vRun.setLayoutParams(layoutParams);
    }
}
