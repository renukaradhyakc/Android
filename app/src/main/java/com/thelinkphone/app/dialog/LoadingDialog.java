package com.thelinkphone.app.dialog;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class LoadingDialog extends BaseDialog {
    private ImageView im;
    private final boolean theme;
    private TextW tv;

    public LoadingDialog(Context context) {
        super(context);
        this.theme = MyShare.getTheme(getContext());
    }

    @Override 
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.dialog_loading);
        TextW textW = (TextW) findViewById(R.id.tv_dialog);
        this.tv = textW;
        textW.setupText(450, 4.3f);
        this.im = (ImageView) findViewById(R.id.im);
        View findViewById = findViewById(R.id.ll_load);
        int widthScreen = OtherUtils.getWidthScreen(getContext()) / 20;
        if (this.theme) {
            findViewById.setBackground(OtherUtils.bgIcon(-1, widthScreen));
            this.tv.setTextColor(-16777216);
            this.im.clearColorFilter();
            return;
        }
        findViewById.setBackground(OtherUtils.bgIcon(Color.parseColor("#121212"), widthScreen));
        this.tv.setTextColor(-1);
        this.im.setColorFilter(-1);
    }

    public void showWithText(String str) {
        show();
        setText(str);
    }

    public void setText(String str) {
        TextW textW = this.tv;
        if (textW != null) {
            textW.setText(str);
        }
    }

    @Override 
    public void show() {
        super.show();
        if (this.im != null) {
            RotateAnimation rotateAnimation = new RotateAnimation(0.0f, 360.0f, 1, 0.5f, 1, 0.5f);
            rotateAnimation.setDuration(2000L);
            rotateAnimation.setRepeatCount(-1);
            this.im.startAnimation(rotateAnimation);
            this.tv.setText(R.string.loading);
        }
    }

    @Override 
    public void cancel() {
        super.cancel();
        ImageView imageView = this.im;
        if (imageView != null) {
            imageView.clearAnimation();
        }
    }
}
