package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewItemInfo extends LinearLayout {
    private final ImageView im;
    private final TextW tv;

    public ViewItemInfo(Context context) {
        super(context);
        setOrientation(LinearLayout.VERTICAL);
        setGravity(1);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 25;
        int i2 = (int) ((widthScreen * 21.82f) / 360.0f);
        ImageView imageView = new ImageView(context);
        this.im = imageView;
        LayoutParams layoutParams = new LayoutParams(i2, i2);
        int i3 = i / 2;
        layoutParams.setMargins(0, i3, 0, i / 6);
        addView(imageView, layoutParams);
        TextW textW = new TextW(context);
        this.tv = textW;
        textW.setupText(400, 3.2f);
        textW.setSingleLine();
        textW.setPadding(i3, 0, i3, i3);
        addView(textW, -2, -2);
    }

    public void setInfo(int i, int i2, boolean z, boolean z2) {
        this.im.setImageResource(i);
        this.tv.setText(i2);
        if (z) {
            this.tv.setTextColor(Color.parseColor("#007AFF"));
        } else {
            this.tv.setAlpha(0.7f);
            this.im.setAlpha(0.7f);
            this.tv.setTextColor(Color.parseColor("#B8B8B8"));
            this.im.setColorFilter(Color.parseColor("#B8B8B8"));
        }
        if (z2) {
            setBackground(OtherUtils.bgIcon(-1, (OtherUtils.getWidthScreen(getContext()) * 3.0f) / 100.0f));
        } else {
            setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), (OtherUtils.getWidthScreen(getContext()) * 3.0f) / 100.0f));
        }
    }
}
