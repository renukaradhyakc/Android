package com.thelinkphone.app.dialog;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public abstract class BaseDialogIOS extends Dialog implements View.OnClickListener {
    LinearLayout llAdd;
    private LinearLayout llDialog;
    boolean theme;
    private View vDialog;

    abstract void action(int i);

    public BaseDialogIOS(Context context) {
        super(context, R.style.Theme_Dialog);
        requestWindowFeature(1);
        this.theme = MyShare.getTheme(getContext());
        if (getWindow() != null) {
            getWindow().setBackgroundDrawableResource(17170445);
            if (this.theme) {
                getWindow().setStatusBarColor(Color.parseColor("#A5A5A5"));
                getWindow().setNavigationBarColor(Color.parseColor("#A5A5A5"));
                return;
            }
            getWindow().setStatusBarColor(Color.parseColor("#242424"));
            getWindow().setNavigationBarColor(Color.parseColor("#242424"));
        }
    }


    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.dialog_ios);
        this.llAdd = (LinearLayout) findViewById(R.id.ll_add);
        this.llDialog = (LinearLayout) findViewById(R.id.ll_dialog);
        View findViewById = findViewById(R.id.v_dialog);
        this.vDialog = findViewById;
        findViewById.setOnClickListener(this);
        OtherUtils.anim((View) this.llDialog, (int) R.anim.show_dialog, false);
        OtherUtils.anim(this.vDialog, (int) R.anim.fade_in, false);
        TextW textW = (TextW) findViewById(R.id.tv_cancel_dialog);
        textW.setOnClickListener(this);
        textW.setupText(600, 4.5f);
        textW.setTextColor(Color.parseColor("#2194FF"));
        int widthScreen = OtherUtils.getWidthScreen(getContext()) / 25;
        if (this.theme) {
            float f = widthScreen;
            textW.setBackground(OtherUtils.bgIcon(-1, f));
            this.llAdd.setBackground(OtherUtils.bgIcon(-1, f));
            return;
        }
        float f2 = widthScreen;
        textW.setBackground(OtherUtils.bgIcon(Color.parseColor("#121212"), f2));
        this.llAdd.setBackground(OtherUtils.bgIcon(Color.parseColor("#121212"), f2));
    }

    @Override 
    public void onClick(View view) {
        hideDialog(view.getId());
    }

    public void hideDialog(int i) {
        OtherUtils.anim((View) this.llDialog, (int) R.anim.hide_dialog, true);
        Animation loadAnimation = AnimationUtils.loadAnimation(getContext(), R.anim.fade_out);
        loadAnimation.setAnimationListener(new AnonymousClass1(i));
        this.vDialog.startAnimation(loadAnimation);
    }




    public class AnonymousClass1 implements Animation.AnimationListener {
        final  int val$id;

        @Override 
        public void onAnimationRepeat(Animation animation) {
        }

        @Override 
        public void onAnimationStart(Animation animation) {
        }

        AnonymousClass1(int i) {
            this.val$id = i;
        }

        @Override 
        public void onAnimationEnd(Animation animation) {
            BaseDialogIOS.this.action(this.val$id);
            BaseDialogIOS.this.vDialog.setVisibility(View.GONE);
            new Handler().post(new Runnable() { 
                @Override 
                public final void run() {
                    AnonymousClass1.this.m97x45342d52();
                }
            });
        }



        public  void m97x45342d52() {
            BaseDialogIOS.this.cancel();
        }
    }


    public void addText(int i, int i2, int i3) {
        this.llAdd.addView(createText(i, i2, i3), new LinearLayout.LayoutParams(-1, -2));
    }


    public void addDivider() {
        this.llAdd.addView(viewDivider(), new LinearLayout.LayoutParams(-1, 1));
    }

    private TextW createText(int i, int i2, int i3) {
        TextW textW = new TextW(getContext());
        textW.setGravity(1);
        textW.setId(i);
        textW.setText(i2);
        textW.setTextColor(i3);
        textW.setupText(450, 4.5f);
        int dimension = (int) getContext().getResources().getDimension(R.dimen.padding_dialog);
        textW.setPadding(0, dimension, 0, dimension);
        textW.setOnClickListener(this);
        return textW;
    }

    private View viewDivider() {
        View view = new View(getContext());
        view.setBackgroundColor(Color.parseColor("#E2E2E2"));
        return view;
    }
}
