package com.thelinkphone.app.screen.pixel;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.screen.PadResult;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class PadPixel extends LinearLayout implements View.OnClickListener {
    private boolean fist;
    private PadResult itfPadResult;

    public void setItfPadResult(PadResult padResult) {
        this.itfPadResult = padResult;
    }

    @Override 
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.fist) {
            this.fist = false;
            setTranslationY(getHeight());
        }
    }

    public PadPixel(Context context) {
        super(context);
        this.fist = true;
        setOnClickListener(this);
        setBackgroundColor(-1);
        int widthScreen = (OtherUtils.getWidthScreen(getContext()) * 18) / 100;
        setPadding(0, widthScreen / 2, 0, MyShare.getSizeNavigation(context) + widthScreen);
        setOrientation(LinearLayout.VERTICAL);
        LinearLayout initLL = initLL();
        addViewNum(initLL, 1);
        addViewNum(initLL, 2);
        addViewNum(initLL, 3);
        LinearLayout initLL2 = initLL();
        addViewNum(initLL2, 4);
        addViewNum(initLL2, 5);
        addViewNum(initLL2, 6);
        LinearLayout initLL3 = initLL();
        addViewNum(initLL3, 7);
        addViewNum(initLL3, 8);
        addViewNum(initLL3, 9);
        LinearLayout initLL4 = initLL();
        addViewNum(initLL4, -1);
        addViewNum(initLL4, 0);
        addViewNum(initLL4, -2);
        LinearLayout initLL5 = initLL();
        initLL5.addView(new View(getContext()), new LayoutParams(0, widthScreen, 2.0f));
        addViewNum(initLL5, -3);
    }

    private LinearLayout initLL() {
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(17);
        linearLayout.setWeightSum(4.0f);
        addView(linearLayout, new LayoutParams(-1, -2));
        return linearLayout;
    }

    private void addViewNum(LinearLayout linearLayout, int i) {
        LinearLayout linearLayout2 = new LinearLayout(getContext());
        linearLayout2.setOnClickListener(this);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setGravity(1);
        linearLayout2.setId(i + 60);
        int widthScreen = OtherUtils.getWidthScreen(getContext());
        int i2 = widthScreen / 50;
        linearLayout2.setPadding(i2, i2, i2, i2);
        linearLayout.addView(linearLayout2, new LayoutParams(0, -2, 1.0f));
        TextW textW = new TextW(getContext());
        textW.setupText(600, 5.0f);
        textW.setTextColor(-16777216);
        if (i == -1) {
            textW.setText("*");
        } else if (i == -2) {
            textW.setText("#");
        } else if (i >= 0) {
            textW.setText(i + "");
        } else {
            textW.setupText(400, 4.4f);
            textW.setText(getResources().getString(R.string.hide));
            textW.setGravity(17);
            linearLayout2.addView(textW, new LayoutParams(-1, (widthScreen * 18) / 100));
            return;
        }
        linearLayout2.setBackgroundResource(R.drawable.sel_tran);
        linearLayout2.addView(textW, new LayoutParams(-2, -2));
    }

    @Override 
    public void onClick(View view) {
        if (view == this) {
            return;
        }
        switch (view.getId()) {
            case 57:
                this.itfPadResult.onViewClick(true, "");
                return;
            case 58:
                this.itfPadResult.onViewClick(false, "#");
                return;
            case 59:
                this.itfPadResult.onViewClick(false, "*");
                return;
            default:
                PadResult padResult = this.itfPadResult;
                StringBuilder sb = new StringBuilder();
                sb.append("");
                sb.append(view.getId() - 60);
                padResult.onViewClick(false, sb.toString());
                return;
        }
    }
}
