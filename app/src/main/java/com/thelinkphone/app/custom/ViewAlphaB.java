package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.LinearLayout;
import com.thelinkphone.app.utils.OtherUtils;


public class ViewAlphaB extends LinearLayout {
    private final TextW tvAlphaB;

    public ViewAlphaB(Context context) {
        super(context);
        setOrientation(1);
        int widthScreen = OtherUtils.getWidthScreen(context) / 25;
        TextW textW = new TextW(context);
        this.tvAlphaB = textW;
        textW.setupText(500, 3.7f);
        textW.setTextColor(Color.parseColor("#8A8A8E"));
        textW.setPadding(widthScreen, (widthScreen * 5) / 4, widthScreen, widthScreen / 2);
        addView(textW, -1, -2);
        View view = new View(context);
        view.setBackgroundColor(Color.parseColor("#8A8A8E"));
        LayoutParams layoutParams = new LayoutParams(-1, 1);
        layoutParams.setMargins(widthScreen, 0, widthScreen, 0);
        addView(view, layoutParams);
    }

    public void setAlphaB(String str) {
        this.tvAlphaB.setText(str);
    }
}
