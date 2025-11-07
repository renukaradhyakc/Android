package com.thelinkphone.app.dialog;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.utils.OtherUtils;



public class DialogNotification extends BaseDialog {
    private final int action;
    private final int content;
    private final DialogResult dialogResult;
    private final boolean theme;
    private final int title;

    public DialogNotification(Context context, boolean z, DialogResult dialogResult) {
        super(context);
        this.dialogResult = dialogResult;
        this.theme = z;
        this.title = R.string.block_contact;
        this.content = R.string.content_block;
        this.action = R.string.yes;
    }

    public DialogNotification(Context context, int i, int i2, boolean z, DialogResult dialogResult) {
        super(context);
        this.dialogResult = dialogResult;
        this.theme = z;
        this.title = i;
        this.content = 0;
        this.action = i2;
    }

    @Override 
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        int widthScreen = OtherUtils.getWidthScreen(getContext());
        int i = widthScreen / 25;
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setGravity(17);
        LinearLayout linearLayout2 = new LinearLayout(getContext());
        linearLayout2.setLayoutTransition(new LayoutTransition());
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setGravity(1);
        linearLayout.addView(linearLayout2, (widthScreen * 8) / 10, -2);
        setContentView(linearLayout);
        if (this.content != 0) {
            TextW textW = new TextW(getContext());
            textW.setTextColor(Color.parseColor("#B8B8B8"));
            textW.setText(this.content);
            textW.setupText(400, 2.5f);
            textW.setPadding(i, i, i, 0);
            linearLayout2.addView(textW, -2, -2);
        }
        TextW textW2 = new TextW(getContext());
        textW2.setupText(450, 4.5f);
        textW2.setText(this.title);
        textW2.setPadding(0, i, 0, i);
        linearLayout2.addView(textW2, -2, -2);
        View view = new View(getContext());
        linearLayout2.addView(view, -1, 1);
        LinearLayout linearLayout3 = new LinearLayout(getContext());
        linearLayout3.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout2.addView(linearLayout3, -1, -2);
        int i2 = widthScreen / 40;
        TextW textW3 = new TextW(getContext());
        textW3.setupText(400, 4.3f);
        textW3.setText(R.string.cancel);
        textW3.setTextColor(Color.parseColor("#2194FF"));
        textW3.setPadding(0, i2, 0, i2);
        textW3.setGravity(1);
        textW3.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view2) {
                DialogNotification.this.m101xa62b70dd(view2);
            }
        });
        linearLayout3.addView(textW3, new LinearLayout.LayoutParams(0, -2, 1.0f));
        View view2 = new View(getContext());
        linearLayout3.addView(view2, 1, -1);
        TextW textW4 = new TextW(getContext());
        textW4.setupText(450, 4.3f);
        textW4.setText(this.action);
        textW4.setTextColor(Color.parseColor("#FF3F28"));
        textW4.setPadding(0, i2, 0, i2);
        textW4.setGravity(1);
        textW4.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view3) {
                DialogNotification.this.m102x3a69e07c(view3);
            }
        });
        linearLayout3.addView(textW4, new LinearLayout.LayoutParams(0, -2, 1.0f));
        if (this.theme) {
            linearLayout2.setBackground(OtherUtils.bgIcon(-1, (widthScreen * 4.0f) / 100.0f));
            view.setBackgroundColor(Color.parseColor("#dedede"));
            view2.setBackgroundColor(Color.parseColor("#dedede"));
            textW2.setTextColor(-16777216);
            return;
        }
        linearLayout2.setBackground(OtherUtils.bgIcon(Color.parseColor("#424141"), (widthScreen * 4.0f) / 100.0f));
        view.setBackgroundColor(Color.parseColor("#5c5c5c"));
        view2.setBackgroundColor(Color.parseColor("#5c5c5c"));
        textW2.setTextColor(-1);
    }



    public  void m101xa62b70dd(View view) {
        cancel();
    }



    public  void m102x3a69e07c(View view) {
        onBlock();
    }

    private void onBlock() {
        cancel();
        this.dialogResult.onActionClick();
    }
}
