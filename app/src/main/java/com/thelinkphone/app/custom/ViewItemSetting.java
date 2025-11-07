package com.thelinkphone.app.custom;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewItemSetting extends LinearLayout {
    private SwitchListener switchListener;
    private final TextW tv;
    private final int w;

    
    public interface SwitchListener {
        void onSwitchChange(ViewItemSetting viewItemSetting, boolean z);
    }

    public ViewItemSetting(Context context) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
        setGravity(16);
        int widthScreen = OtherUtils.getWidthScreen(context);
        this.w = widthScreen;
        setBackgroundResource(R.drawable.sel_tran);
        TextW textW = new TextW(context);
        this.tv = textW;
        textW.setupText(400, 4.2f);
        int i = ((widthScreen / 25) * 3) / 2;
        textW.setPadding(i, 0, i, 0);
        addView(textW, new LayoutParams(0, -2, 1.0f));
    }

    public void setItem(int i, boolean z) {
        this.tv.setText(i);
        if (z) {
            this.tv.setTextColor(-16777216);
        } else {
            this.tv.setTextColor(-1);
        }
    }

    public void addNext() {
        int i = this.w / 25;
        int i2 = (int) (i * 3.2f);
        ImageView imageView = new ImageView(getContext());
        imageView.setPadding(i, i, i, i);
        imageView.setImageResource(R.drawable.im_next);
        addView(imageView, i2, i2);
    }

    public ImageView addMode(int i, OnClickListener onClickListener) {
        int i2 = this.w / 25;
        int i3 = (int) (i2 * 3.5f);
        ImageView imageView = new ImageView(getContext());
        imageView.setImageResource(i);
        imageView.setOnClickListener(onClickListener);
        imageView.setPadding(i2, i2, i2, i2);
        addView(imageView, i3, i3);
        return imageView;
    }

    public void addSwitch(boolean z, SwitchListener switchListener) {
        this.switchListener = switchListener;
        ViewSwitch viewSwitch = new ViewSwitch(getContext());
        viewSwitch.setStatus(z);
        viewSwitch.setStatusResult(new ViewSwitch.StatusResult() { 
            @Override 
            public final void onSwitchResult(boolean z2) {
                ViewItemSetting.this.m91x90cfe857(z2);
            }
        });
        int i = getResources().getDisplayMetrics().widthPixels;
        int i2 = (int) ((i * 6.3f) / 100.0f);
        LayoutParams layoutParams = new LayoutParams((int) ((i2 * 13.6f) / 8.3f), i2);
        layoutParams.setMargins(0, 0, (i / 25) / 2, 0);
        addView(viewSwitch, layoutParams);
    }

    
    
    public  void m91x90cfe857(boolean z) {
        this.switchListener.onSwitchChange(this, z);
    }
}
