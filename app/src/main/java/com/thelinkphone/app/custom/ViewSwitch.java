package com.thelinkphone.app.custom;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;


public class ViewSwitch extends RelativeLayout {
    private boolean status;
    private StatusResult statusResult;
    private View vBgOff;
    private View vBgOn;
    private View vThumb;


    public interface StatusResult {
        void onSwitchResult(boolean z);
    }

    public ViewSwitch(Context context) {
        super(context);
        init();
    }

    public ViewSwitch(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        init();
    }

    public ViewSwitch(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
        init();
    }

    public void setStatusResult(StatusResult statusResult) {
        this.statusResult = statusResult;
    }

    private void init() {
        View view = new View(getContext());
        this.vThumb = view;
        view.setBackgroundResource(R.drawable.ic_thumb_switch);
        View view2 = new View(getContext());
        this.vBgOff = view2;
        view2.setBackgroundResource(R.drawable.ic_bg_switch_off);
        View view3 = new View(getContext());
        this.vBgOn = view3;
        view3.setBackgroundResource(R.drawable.ic_bg_switch_on);
        addView(this.vBgOn, new LayoutParams(-1, -1));
        addView(this.vBgOff, new LayoutParams(-1, -1));
        setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view4) {
                ViewSwitch.this.onClick(view4);
            }
        });
        addView(this.vThumb, new LayoutParams((int) ((getResources().getDisplayMetrics().widthPixels * 6.3f) / 100.0f), -1));
    }

    public void setStatus(boolean z) {
        this.status = z;
        if (z) {
            this.vBgOff.setAlpha(0.0f);
            this.vBgOn.setAlpha(1.0f);
            if (getWidth() > 0) {
                this.vThumb.setTranslationX(getWidth() - getHeight());
                return;
            }
            return;
        }
        this.vBgOff.setAlpha(1.0f);
        this.vBgOn.setAlpha(0.0f);
        this.vThumb.setTranslationX(0.0f);
    }

    public void setStatusWithAnim(boolean z) {
        this.status = z;
        if (z) {
            this.vBgOff.animate().alpha(0.0f).setDuration(300L).start();
            this.vBgOn.animate().alpha(1.0f).setDuration(300L).withEndAction(null).start();
            this.vThumb.animate().translationX(getWidth() - getHeight()).setDuration(300L).start();
            return;
        }
        this.vBgOff.animate().alpha(1.0f).setDuration(300L).start();
        this.vBgOn.animate().alpha(0.0f).setDuration(300L).withEndAction(null).start();
        this.vThumb.animate().translationX(0.0f).setDuration(300L).start();
    }

    @Override 
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.status) {
            this.vThumb.setTranslationX(getWidth() - getHeight());
        }
    }

    public void onClick(View view) {
        this.vThumb.clearAnimation();
        this.vBgOn.clearAnimation();
        this.vBgOff.clearAnimation();
        boolean z = !this.status;
        this.status = z;
        if (z) {
            this.vThumb.animate().translationX(getWidth() - getHeight()).setDuration(300L).start();
            this.vBgOff.animate().alpha(0.0f).setDuration(300L).start();
            this.vBgOn.animate().alpha(1.0f).setDuration(300L).setListener(new AnimatorListenerAdapter() { 
                @Override 
                public void onAnimationEnd(Animator animator) {
                    super.onAnimationEnd(animator);
                    if (ViewSwitch.this.statusResult != null) {
                        ViewSwitch.this.statusResult.onSwitchResult(ViewSwitch.this.status);
                    }
                }
            }).start();
            return;
        }
        this.vThumb.animate().translationX(0.0f).setDuration(300L).start();
        this.vBgOff.animate().alpha(1.0f).setDuration(300L).start();
        this.vBgOn.animate().alpha(0.0f).setDuration(300L).setListener(new AnimatorListenerAdapter() { 
            @Override 
            public void onAnimationEnd(Animator animator) {
                super.onAnimationEnd(animator);
                if (ViewSwitch.this.statusResult != null) {
                    ViewSwitch.this.statusResult.onSwitchResult(ViewSwitch.this.status);
                }
            }
        }).start();
    }
}
