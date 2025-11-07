package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.OtherUtils;



public class LayoutChooseSim extends LinearLayout {
    private final TextW tvSim;

    public LayoutChooseSim(Context context) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(16);
        TextW textW = new TextW(context);
        this.tvSim = textW;
        textW.setupText(400, 3.2f);
    }

    public void init(boolean z) {
        int widthScreen = OtherUtils.getWidthScreen(getContext());
        int i = widthScreen / 50;
        int i2 = i * 3;
        int i3 = i2 / 4;
        setPadding(i, i3, i, i3);
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.im_sim);
        int i4 = widthScreen / 25;
        LayoutParams layoutParams = new LayoutParams(i4, i4);
        layoutParams.setMargins(0, 0, i, 0);
        addView(imageView, layoutParams);
        LayoutParams layoutParams2 = new LayoutParams(-2, -2);
        layoutParams2.setMargins(0, 0, i2 / 2, 0);
        addView(this.tvSim, layoutParams2);
        ImageView imageView2 = new ImageView(getContext());
        imageView2.setImageResource(R.drawable.ic_down);
        LayoutParams layoutParams3 = new LayoutParams(i, i);
        layoutParams3.setMargins(i, 0, 0, 0);
        addView(imageView2, layoutParams3);
        if (z) {
            setBackground(OtherUtils.selLayout("#F5F5F5", "#d5d5d5"));
            imageView.clearColorFilter();
            imageView2.clearColorFilter();
            this.tvSim.setTextColor(Color.parseColor("#8A8A8E"));
            return;
        }
        setBackground(OtherUtils.selLayout("#3E3E3E", "#4e4e4e"));
        imageView.setColorFilter(Color.parseColor("#F5F5F5"));
        imageView2.setColorFilter(Color.parseColor("#F5F5F5"));
        this.tvSim.setTextColor(Color.parseColor("#F5F5F5"));
    }

    public void setSim(int i) {
        this.tvSim.setText("Sim " + (i + 1));
    }
}
