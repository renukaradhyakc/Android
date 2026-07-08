package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

public class ToggleSwitch extends FrameLayout {

    public interface OnToggleListener {
        void onToggled(boolean isOn);
    }

    private boolean isOn = false;
    private View thumb;
    private GradientDrawable trackBg;
    private OnToggleListener listener;
    private final int trackWidth, trackHeight, thumbSize, margin;
    private boolean interactive = true;

    public ToggleSwitch(Context context) {
        this(context, false);
    }

    /** compact=true → smaller size, used for the per-day toggle. */
    public ToggleSwitch(Context context, boolean compact) {
        super(context);
        trackWidth = dp(context, compact ? 34 : 42);
        trackHeight = dp(context, compact ? 20 : 24);
        thumbSize = dp(context, compact ? 14 : 18);
        margin = (trackHeight - thumbSize) / 2;
        build(context);
    }

    private int dp(Context c, int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, c.getResources().getDisplayMetrics());
    }

    private void build(Context context) {
        trackBg = new GradientDrawable();
        trackBg.setShape(GradientDrawable.RECTANGLE);
        trackBg.setCornerRadius(trackHeight / 2f);
        trackBg.setColor(Color.parseColor("#D1D5DB"));
        setBackground(trackBg);

        thumb = new View(context);
        GradientDrawable thumbBg = new GradientDrawable();
        thumbBg.setShape(GradientDrawable.OVAL);
        thumbBg.setColor(Color.WHITE);
        thumb.setBackground(thumbBg);
        setLayoutParams(new ViewGroup.LayoutParams(trackWidth, trackHeight));

        LayoutParams thumbParams = new LayoutParams(thumbSize, thumbSize);
        thumbParams.gravity = Gravity.START | Gravity.CENTER_VERTICAL;
        thumbParams.setMarginStart(margin);
        addView(thumb, thumbParams);

        setOnClickListener(v -> { if (!interactive) return; setOn(!isOn, true); } );
    }

    public void setOn(boolean on, boolean animate) {
        setOn(on, animate, true);
    }

    public void setOn(boolean on, boolean animate, boolean notify) {
        if (this.isOn == on) return;
        this.isOn = on;

        trackBg.setColor(on ? Color.parseColor("#6366F1") : Color.parseColor("#D1D5DB"));
        float targetTranslation = on ? (trackWidth - thumbSize - margin - margin) : 0;

        if (animate) {
            thumb.animate().translationX(targetTranslation).setDuration(150).start();
        } else {
            thumb.setTranslationX(targetTranslation);
        }

        if (notify && listener != null) listener.onToggled(isOn);
    }

    public boolean isOn() {
        return isOn;
    }

    public void setOnToggleListener(OnToggleListener listener) {
        this.listener = listener;
    }

    public void setInteractive(boolean interactive) {
        this.interactive = interactive;
    }

    public int getPreferredWidth() {
        return trackWidth;
    }

    public int getPreferredHeight() {
        return trackHeight;
    }
}