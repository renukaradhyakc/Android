package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.thelinkphone.app.utils.OtherUtils;


public class LayoutItemTab extends LinearLayout {
    private final ImageView im;
    private final TextW tv;

    public LayoutItemTab(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
        setGravity(17);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 100;
        int i2 = i * 2;
        setPadding(0, i2, 0, i2);
        ImageView imageView = new ImageView(context);
        this.im = imageView;
        addView(imageView, -1, (widthScreen * 9) / 100);
        TextW textW = new TextW(context);
        this.tv = textW;
        textW.setupText(600, 2.8f);
        textW.setPadding(i, 0, i, i);
        textW.setTextColor(Color.parseColor("#8A8A8E"));
        LayoutParams layoutParams = new LayoutParams(-2, -2);
        layoutParams.setMargins(0, (-widthScreen) / 100, 0, 0);
        addView(textW, layoutParams);
    }

    public void setData(int i, int i2) {
        this.im.setImageResource(i);
        this.tv.setText(i2);
    }

    public void setChoose(boolean z) {
        if (z) {
            this.im.setColorFilter(Color.parseColor("#007AFF"));
            this.tv.setTextColor(Color.parseColor("#007AFF"));
            return;
        }
        this.im.clearColorFilter();
        this.tv.setTextColor(Color.parseColor("#8A8A8E"));
    }
}
