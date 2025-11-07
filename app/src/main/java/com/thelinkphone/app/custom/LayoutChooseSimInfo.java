package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.OtherUtils;



public class LayoutChooseSimInfo extends LinearLayout {
    private final ImageView imDown;
    private final TextW tvSim;

    public LayoutChooseSimInfo(Context context) {
        super(context);
        setOrientation(0);
        setGravity(16);
        TextW textW = new TextW(context);
        this.tvSim = textW;
        textW.setupText(400, 3.2f);
        ImageView imageView = new ImageView(getContext());
        this.imDown = imageView;
        imageView.setImageResource(R.drawable.im_next);
    }

    public void init(boolean z) {
        int widthScreen = OtherUtils.getWidthScreen(getContext());
        int i = widthScreen / 50;
        int i2 = (i * 3) / 4;
        setPadding(i, i2, i, i2);
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.im_sim);
        int i3 = widthScreen / 25;
        LayoutParams layoutParams = new LayoutParams(i3, i3);
        layoutParams.setMargins(0, 0, i, 0);
        addView(imageView, layoutParams);
        addView(this.tvSim, -2, -2);
        int i4 = (widthScreen * 3) / 100;
        LayoutParams layoutParams2 = new LayoutParams(i4, i4);
        layoutParams2.setMargins(i, 0, 0, 0);
        addView(this.imDown, layoutParams2);
        if (z) {
            imageView.clearColorFilter();
            this.imDown.clearColorFilter();
            this.tvSim.setTextColor(Color.parseColor("#8A8A8E"));
            return;
        }
        imageView.setColorFilter(Color.parseColor("#F5F5F5"));
        this.imDown.setColorFilter(Color.parseColor("#F5F5F5"));
        this.tvSim.setTextColor(Color.parseColor("#F5F5F5"));
    }

    public void hideViewNext() {
        this.imDown.setVisibility(View.GONE);
    }

    public void setSim(int i) {
        this.tvSim.setText("Sim " + (i + 1));
    }
}
