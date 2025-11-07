package com.thelinkphone.app.dialog;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.SvgRatingBar;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.utils.ActionUtils;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class DialogRate extends BaseDialog {
    private DialogResult dialogResult;
    private float star;
    private TextW tvRate;

    public DialogRate(Context context) {
        super(context);
    }

    public DialogRate(Context context, DialogResult dialogResult) {
        super(context);
        this.dialogResult = dialogResult;
    }

    @Override 
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        int widthScreen = OtherUtils.getWidthScreen(getContext());
        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setGravity(17);
        LinearLayout linearLayout2 = new LinearLayout(getContext());
        linearLayout2.setLayoutTransition(new LayoutTransition());
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        linearLayout2.setGravity(1);
        linearLayout.addView(linearLayout2, (widthScreen * 7) / 10, -2);
        setContentView(linearLayout);
        int i = widthScreen / 25;
        linearLayout2.setBackgroundResource(R.drawable.bg_dialog_rate);
        int i2 = widthScreen / 3;
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(R.drawable.im_icon_rate);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(i2, i2);
        layoutParams.gravity = 1;
        int i3 = i / 4;
        layoutParams.setMargins(0, i / 2, 0, i3);
        linearLayout2.addView(imageView, layoutParams);
        TextW textW = new TextW(getContext());
        textW.setupText(600, 6.0f);
        textW.setText(R.string.rate_app);
        textW.setTextColor(Color.parseColor("#333333"));
        LinearLayout.LayoutParams layoutParams2 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams2.gravity = 1;
        linearLayout2.addView(textW, layoutParams2);
        TextW textW2 = new TextW(getContext());
        textW2.setupText(400, 4.0f);
        textW2.setText(R.string.rate_content);
        textW2.setTextColor(Color.parseColor("#7B7B7B"));
        LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-2, -2);
        layoutParams3.gravity = 1;
        layoutParams3.setMargins(0, i3, 0, i);
        linearLayout2.addView(textW2, layoutParams3);
        View inflate = LayoutInflater.from(getContext()).inflate(R.layout.layout_star, (ViewGroup) null);
        linearLayout2.addView(inflate, -1, -2);
        ((SvgRatingBar) inflate.findViewById(R.id.sb)).setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener() { 
            @Override 
            public final void onRatingChanged(RatingBar ratingBar, float f, boolean z) {
                DialogRate.this.m103x523e46d2(ratingBar, f, z);
            }
        });
        this.star = 4.5f;
        LinearLayout linearLayout3 = new LinearLayout(getContext());
        linearLayout3.setOrientation(LinearLayout.HORIZONTAL);
        LinearLayout.LayoutParams layoutParams4 = new LinearLayout.LayoutParams(-1, widthScreen / 10);
        layoutParams4.setMargins(i, i, i, i);
        linearLayout2.addView(linearLayout3, layoutParams4);
        TextW textW3 = new TextW(getContext());
        textW3.setTextColor(-1);
        textW3.setText(R.string.cancel);
        textW3.setupText(500, 4.0f);
        textW3.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DialogRate.this.m104x6c59c571(view);
            }
        });
        textW3.setGravity(17);
        textW3.setBackgroundResource(R.drawable.sel_tv_cancel);
        linearLayout3.addView(textW3, new LinearLayout.LayoutParams(0, -1, 1.0f));
        linearLayout3.addView(new View(getContext()), i, i);
        TextW textW4 = new TextW(getContext());
        this.tvRate = textW4;
        textW4.setTextColor(-1);
        this.tvRate.setText(R.string.rate_now);
        this.tvRate.setupText(600, 4.0f);
        this.tvRate.setOnClickListener(new View.OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                DialogRate.this.m105x86754410(view);
            }
        });
        this.tvRate.setGravity(17);
        this.tvRate.setBackgroundResource(R.drawable.sel_tv_rate);
        linearLayout3.addView(this.tvRate, new LinearLayout.LayoutParams(0, -1, 1.0f));
    }

    
    
    public  void m103x523e46d2(RatingBar ratingBar, float f, boolean z) {
        this.star = f;
        change();
    }

    
    
    public  void m104x6c59c571(View view) {
        cancel();
        DialogResult dialogResult = this.dialogResult;
        if (dialogResult != null) {
            dialogResult.onActionClick();
        }
    }

    
    
    public  void m105x86754410(View view) {
        rateApp();
    }

    private void change() {
        TextW textW = this.tvRate;
        if (textW == null) {
            return;
        }
        if (this.star < 4.0f) {
            textW.setText(R.string.feedback);
        } else {
            textW.setText(R.string.rate_now);
        }
    }

    private void rateApp() {
        cancel();
        if (this.star < 4.0f) {
            feedback();
        } else {
            rate();
        }
        DialogResult dialogResult = this.dialogResult;
        if (dialogResult != null) {
            dialogResult.onActionClick();
        }
    }

    private void feedback() {
        MyShare.rated(getContext());
    }

    private void rate() {
        MyShare.rated(getContext());
        ActionUtils.ratePkg(getContext(), getContext().getPackageName());
    }
}
