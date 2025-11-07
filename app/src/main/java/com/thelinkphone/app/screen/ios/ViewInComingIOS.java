package com.thelinkphone.app.screen.ios;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import androidx.cardview.widget.CardView;

import com.thelinkphone.app.R;
import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;

import com.romainpiel.shimmer.Shimmer;
import com.romainpiel.shimmer.ShimmerTextView;

public class ViewInComingIOS extends CardView {
    private ActionAcceptResult actionScreenResult;
    private final Handler handler;
    private boolean isRemove;
    private final Runnable runnable;
    private final Shimmer shimmer;
    private final int size;
    private final int sizeNavi;
    private int start;
    private int touch;
    private final ShimmerTextView tvSlide;
    private boolean up;
    private final View vBlur;
    private final int w;
    private final int width;

    static int access$212(ViewInComingIOS viewInComingIOS, int i) {
        int i2 = viewInComingIOS.touch + i;
        viewInComingIOS.touch = i2;
        return i2;
    }

    static int access$220(ViewInComingIOS viewInComingIOS, int i) {
        int i2 = viewInComingIOS.touch - i;
        viewInComingIOS.touch = i2;
        return i2;
    }

    public void setActionScreenResult(ActionAcceptResult actionAcceptResult) {
        this.actionScreenResult = actionAcceptResult;
    }

    public ViewInComingIOS(Context context) {
        super(context);
        this.touch = 0;
        this.runnable = new Runnable() {
            @Override
            public void run() {
                if (ViewInComingIOS.this.up) {
                    int i = (ViewInComingIOS.this.width - ViewInComingIOS.this.touch) / 2;
                    ViewInComingIOS.access$212(ViewInComingIOS.this, i >= 20 ? i : 20);
                    if (ViewInComingIOS.this.touch < ViewInComingIOS.this.width - ViewInComingIOS.this.size) {
                        ViewInComingIOS.this.update();
                        ViewInComingIOS.this.handler.postDelayed(this, 15L);
                        return;
                    }
                    ViewInComingIOS viewInComingIOS = ViewInComingIOS.this;
                    viewInComingIOS.touch = viewInComingIOS.width - ViewInComingIOS.this.size;
                    ViewInComingIOS.this.update();
                    if (ViewInComingIOS.this.actionScreenResult != null) {
                        ViewInComingIOS.this.actionScreenResult.onAccept();
                        ViewInComingIOS.this.touch = 0;
                        return;
                    }
                    return;
                }
                int i2 = ViewInComingIOS.this.touch / 2;
                ViewInComingIOS.access$220(ViewInComingIOS.this, i2 >= 20 ? i2 : 20);
                if (ViewInComingIOS.this.touch <= 0) {
                    ViewInComingIOS.this.touch = 0;
                    ViewInComingIOS.this.shimmer.start(ViewInComingIOS.this.tvSlide);
                    ViewInComingIOS.this.tvSlide.animate().alpha(0.95f).setDuration(200L).start();
                    ViewInComingIOS.this.update();
                    return;
                }
                ViewInComingIOS.this.update();
                ViewInComingIOS.this.handler.postDelayed(this, 15L);
            }
        };
        int widthScreen = OtherUtils.getWidthScreen(context);
        this.w = widthScreen;
        this.width = widthScreen - (widthScreen / 4);
        this.sizeNavi = MyShare.getSizeNavigation(context);
        int i = (int) ((widthScreen * 21.2f) / 100.0f);
        this.size = i;
        setCardBackgroundColor(0);
        setElevation(0.0f);
        setRadius(i / 2.0f);
        RelativeLayout relativeLayout = new RelativeLayout(context);
        addView(relativeLayout, -1, -1);
        View blurView = LayoutInflater.from(context).inflate(R.layout.layout_blur, (ViewGroup) null);
        this.vBlur = blurView;
        relativeLayout.addView(blurView, -1, -1);
        ShimmerTextView shimmerTextView = new ShimmerTextView(context);
        this.tvSlide = shimmerTextView;
        shimmerTextView.setText(R.string.slide_to_answer);
        if (Build.VERSION.SDK_INT >= 28) {
            shimmerTextView.setTypeface(Typeface.create(Typeface.SANS_SERIF, 400, false));
        } else {
            shimmerTextView.setTypeface(Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL));
        }
        shimmerTextView.setTextSize(0, (widthScreen * 4.8f) / 100.0f);
        shimmerTextView.setTextColor(Color.parseColor("#333333"));
        shimmerTextView.setGravity(1);
        shimmerTextView.setAlpha(0.95f);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -2);
        layoutParams.addRule(15);
        layoutParams.setMargins(i, 0, widthScreen / 50, 0);
        relativeLayout.addView(shimmerTextView, layoutParams);
        Shimmer duration = new Shimmer().setDirection(0).setRepeatCount(-1).setDuration(3000L);
        this.shimmer = duration;
        duration.start(shimmerTextView);
        int i2 = widthScreen / 100;
        ImageView imageView = new ImageView(context);
        imageView.setPadding(i2, i2, i2, i2);
        imageView.setImageResource(R.drawable.im_incoming_ios);
        relativeLayout.addView(imageView, i, i);
        this.handler = new Handler();
    }

    public void setViewRoot(ViewGroup viewGroup) {

    }

    @Override
    public boolean onTouchEvent(MotionEvent motionEvent) {
        int action = motionEvent.getAction();
        if (action == 0) {
            this.start = ((int) motionEvent.getRawX()) - (this.w / 8);
            this.touch = 0;
            update();
            this.handler.removeCallbacks(this.runnable);
            this.tvSlide.animate().alpha(0.0f).setDuration(200L).start();
            this.shimmer.cancel();
        } else if (action != 1) {
            if (action == 2 && this.start < this.size) {
                int rawX = (int) ((motionEvent.getRawX() - (this.w / 8)) - this.start);
                this.touch = rawX;
                if (rawX < 0) {
                    this.touch = 0;
                } else {
                    int i = this.width;
                    int i2 = this.size;
                    if (rawX > i - i2) {
                        this.touch = i - i2;
                    }
                }
                update();
            }
        } else if (this.start < this.size) {
            this.up = this.touch >= (this.width * 3) / 5;
            this.handler.post(this.runnable);
        }
        return true;
    }


    public void update() {
        RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) getLayoutParams();
        int i = this.w;
        layoutParams.setMargins((i / 8) + this.touch, 0, i / 8, this.sizeNavi + (i / 7));
        setLayoutParams(layoutParams);
    }

    public void onRemove() {
        if (getVisibility() == View.GONE && getParent() != null) {
            this.isRemove = true;
            ((ViewGroup) getParent()).removeView(this);
        } else if (this.isRemove) {
        } else {
            this.isRemove = true;
            animate().alpha(0.0f).setDuration(500L).withEndAction(new Runnable() {
                @Override
                public final void run() {
                    ViewInComingIOS.this.m195xefef0ba4();
                }
            }).start();
        }
    }


    public void m195xefef0ba4() {
        if (getParent() != null) {
            ((ViewGroup) getParent()).removeView(this);
        }
    }

    public void setContentTextSlide(int i) {
        this.tvSlide.setText(i);
    }
}
