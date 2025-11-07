package com.thelinkphone.app.screen.samsung;

import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.screen.PadResult;
import com.thelinkphone.app.utils.OtherUtils;



public class PadGalaxy extends LinearLayout implements View.OnClickListener {
    private boolean fist;
    private PadResult padGalaxyResult;
    private final int w;

    
    public static  void lambda$new$0(View view) {
    }

    public void setPadGalaxyResult(PadResult padResult) {
        this.padGalaxyResult = padResult;
    }

    public PadGalaxy(Context context) {
        super(context);
        this.fist = true;
        setOnClickListener(PadGalaxy$$ExternalSyntheticLambda0.INSTANCE);
        int widthScreen = OtherUtils.getWidthScreen(context);
        this.w = widthScreen;
        setOrientation(LinearLayout.VERTICAL);
        View view = new View(context);
        view.setBackgroundColor(-1);
        LayoutParams layoutParams = new LayoutParams(-1, widthScreen / 200);
        layoutParams.setMargins(widthScreen / 25, 0, widthScreen / 25, widthScreen / 100);
        addView(view, layoutParams);
        LinearLayout initLL = initLL();
        addViewNum(initLL, 1, "");
        addViewNum(initLL, 2, "ABC");
        addViewNum(initLL, 3, "DEF");
        LinearLayout initLL2 = initLL();
        addViewNum(initLL2, 4, "GHI");
        addViewNum(initLL2, 5, "JKL");
        addViewNum(initLL2, 6, "MNO");
        LinearLayout initLL3 = initLL();
        addViewNum(initLL3, 7, "PQRS");
        addViewNum(initLL3, 8, "TUV");
        addViewNum(initLL3, 9, "WXYZ");
        LinearLayout initLL4 = initLL();
        addViewNum(initLL4, -1, "*");
        addViewNum(initLL4, 0, "+");
        addViewNum(initLL4, -2, "#");
        View view2 = new View(context);
        view2.setBackgroundColor(-1);
        LayoutParams layoutParams2 = new LayoutParams(-1, widthScreen / 200);
        layoutParams2.setMargins(widthScreen / 25, 0, widthScreen / 25, 0);
        addView(view2, layoutParams2);
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

    private void addViewNum(LinearLayout linearLayout, int i, String str) {
        LinearLayout linearLayout2 = new LinearLayout(getContext());
        linearLayout2.setBackgroundResource(R.drawable.sel_tran);
        linearLayout2.setOnClickListener(this);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setGravity(1);
        linearLayout2.setId(i + 60);
        linearLayout.addView(linearLayout2, new LayoutParams(0, -2, 1.0f));
        TextW textW = new TextW(getContext());
        textW.setTextColor(-1);
        textW.setupText(600, 4.8f);
        linearLayout2.addView(textW, new LayoutParams(-2, -2));
        if (i == -1) {
            textW.setText("*");
        } else if (i == -2) {
            textW.setText("#");
        } else {
            textW.setText(i + "");
            TextW textW2 = new TextW(getContext());
            textW2.setText(str);
            textW2.setTextColor(-1);
            textW2.setupText(300, 2.7f);
            LayoutParams layoutParams = new LayoutParams(-2, -2);
            layoutParams.setMargins(0, 0, 0, this.w / 100);
            linearLayout2.addView(textW2, layoutParams);
        }
    }

    @Override 
    public void onClick(View view) {
        int id = view.getId();
        if (id == -2) {
            this.padGalaxyResult.onViewClick(false, "#");
        } else if (id == -1) {
            this.padGalaxyResult.onViewClick(false, "*");
        } else {
            PadResult padResult = this.padGalaxyResult;
            StringBuilder sb = new StringBuilder();
            sb.append("");
            sb.append(view.getId() - 60);
            padResult.onViewClick(false, sb.toString());
        }
    }
}
