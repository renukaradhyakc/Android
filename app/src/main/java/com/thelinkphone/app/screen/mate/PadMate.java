package com.thelinkphone.app.screen.mate;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.screen.PadResult;
import com.thelinkphone.app.utils.OtherUtils;



public class PadMate extends LinearLayout implements View.OnClickListener {
    private boolean fist;
    private PadResult itfPadResult;

    public void setItfPadResult(PadResult padResult) {
        this.itfPadResult = padResult;
    }

    public PadMate(Context context) {
        super(context);
        this.fist = true;
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
    }

    @Override 
    protected void onLayout(boolean z, int i, int i2, int i3, int i4) {
        super.onLayout(z, i, i2, i3, i4);
        if (this.fist) {
            this.fist = false;
            setTranslationY(getHeight());
        }
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
        int widthScreen = OtherUtils.getWidthScreen(getContext()) / 43;
        linearLayout2.setPadding(widthScreen, widthScreen, widthScreen, widthScreen);
        linearLayout.addView(linearLayout2, new LayoutParams(0, -2, 1.0f));
        TextW textW = new TextW(getContext());
        textW.setupText(600, 5.4f);
        textW.setTextColor(-1);
        if (i == -1) {
            textW.setText("*");
        } else if (i == -2) {
            textW.setText("#");
        } else if (i >= 0) {
            textW.setText(i + "");
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
