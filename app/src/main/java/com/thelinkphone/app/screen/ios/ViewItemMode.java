package com.thelinkphone.app.screen.ios;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewItemMode extends LinearLayout {
    private final ImageView im;
    private final TextW tv;

    public ViewItemMode(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
        setGravity(1);
        int widthScreen = OtherUtils.getWidthScreen(context);
        float f = widthScreen;
        int i = (int) ((6.8f * f) / 200.0f);
        int i2 = (int) ((f * 19.9f) / 100.0f);
        setPadding(i, i, i, i);
        int i3 = (widthScreen * 4) / 100;
        ImageView imageView = new ImageView(context);
        this.im = imageView;
        imageView.setPadding(i3, i3, i3, i3);
        imageView.setBackground(OtherUtils.selNum("#50ffffff", "#9affffff"));
        addView(imageView, i2, i2);
        TextW textW = new TextW(context);
        this.tv = textW;
        textW.setupText(400, 3.9f);
        textW.setTextColor(-1);
        textW.setGravity(1);
        textW.setPadding(0, i3 / 4, 0, 0);
        addView(textW, i2, -2);
    }

    public void setMode(int i, int i2) {
        this.im.setImageResource(i);
        this.tv.setText(i2);
    }

    public void setEnable(boolean z) {
        if (z) {
            this.im.setBackground(OtherUtils.selNum("#f9ffffff", "#f0ffffff"));
            this.im.setColorFilter(-16777216);
            return;
        }
        this.im.setBackground(OtherUtils.selNum("#50ffffff", "#9affffff"));
        this.im.clearColorFilter();
    }
}
