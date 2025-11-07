package com.thelinkphone.app.screen.pixel;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.screen.ActionScreenResult;
import com.thelinkphone.app.screen.PadResult;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewCallPixel extends RelativeLayout {
    private ActionScreenResult actionScreenResult;
    private final ImageView imEndCall;
    private boolean openPad;
    private final PadPixel padPixel;
    private final int size;
    private final ImageView vHold;
    private final ImageView vMute;
    private final ImageView vRec;
    private final ImageView vSpeaker;
    private final int w;

    public ViewCallPixel(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        this.w = widthScreen;
        int i = (widthScreen * 18) / 100;
        this.size = i;
        int i2 = widthScreen / 100;
        int i3 = (widthScreen * 13) / 100;
        int i4 = (widthScreen - (i3 * 3)) / 4;
        PadPixel padPixel = new PadPixel(context);
        this.padPixel = padPixel;
        padPixel.setId(40);
        padPixel.setItfPadResult(new PadResult() { 
            @Override 
            public final void onViewClick(boolean z, String str) {
                ViewCallPixel.this.m218x2e919c1(z, str);
            }
        });
        ImageView imageView = new ImageView(context);
        this.imEndCall = imageView;
        imageView.setId(99);
        ImageView makeIm = makeIm(50, R.drawable.im_mode_mute_on, new OnClickListener() {
            @Override 
            public final void onClick(View view) {
                ViewCallPixel.this.m219xf6789e02(view);
            }
        });
        this.vMute = makeIm;
        LayoutParams layoutParams = new LayoutParams(i3, i3);
        layoutParams.addRule(14);
        layoutParams.addRule(2, imageView.getId());
        layoutParams.setMargins(i4, widthScreen / 50, i4, widthScreen / 25);
        addView(makeIm, layoutParams);
        ImageView makeIm2 = makeIm(51, R.drawable.im_mode_speaker, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallPixel.this.m220xea082243(view);
            }
        });
        this.vSpeaker = makeIm2;
        LayoutParams layoutParams2 = new LayoutParams(i3, i3);
        layoutParams2.addRule(16, makeIm.getId());
        layoutParams2.addRule(6, makeIm.getId());
        addView(makeIm2, layoutParams2);
        ImageView makeIm3 = makeIm(52, R.drawable.im_mode_pad, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallPixel.this.m221xdd97a684(view);
            }
        });
        LayoutParams layoutParams3 = new LayoutParams(i3, i3);
        layoutParams3.addRule(17, makeIm.getId());
        layoutParams3.addRule(6, makeIm.getId());
        addView(makeIm3, layoutParams3);
        ImageView makeIm4 = makeIm(53, R.drawable.im_mode_hold, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallPixel.this.m222xd1272ac5(view);
            }
        });
        this.vHold = makeIm4;
        LayoutParams layoutParams4 = new LayoutParams(i3, i3);
        layoutParams4.addRule(14);
        layoutParams4.addRule(2, makeIm.getId());
        addView(makeIm4, layoutParams4);
        ImageView makeIm5 = makeIm(54, R.drawable.im_mode_rec, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallPixel.this.m223xc4b6af06(view);
            }
        });
        this.vRec = makeIm5;
        LayoutParams layoutParams5 = new LayoutParams(i3, i3);
        layoutParams5.addRule(17, makeIm.getId());
        layoutParams5.addRule(2, makeIm.getId());
        addView(makeIm5, layoutParams5);
        ImageView makeIm6 = makeIm(55, R.drawable.im_mode_contact, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallPixel.this.m224xb8463347(view);
            }
        });
        LayoutParams layoutParams6 = new LayoutParams(i3, i3);
        layoutParams6.addRule(16, makeIm.getId());
        layoutParams6.addRule(2, makeIm.getId());
        addView(makeIm6, layoutParams6);
        addView(padPixel, -1, -2);
        imageView.setPadding(i2, i2, i2, i2);
        imageView.setImageResource(R.drawable.im_decline);
        imageView.setPivotX(i / 2.0f);
        imageView.setPivotY(i / 2.0f);
        imageView.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallPixel.this.m225xabd5b788(view);
            }
        });
        LayoutParams layoutParams7 = new LayoutParams(i, i);
        layoutParams7.addRule(14);
        layoutParams7.addRule(8, padPixel.getId());
        layoutParams7.setMargins(0, 0, 0, MyShare.getSizeNavigation(context) + (widthScreen / 100) + i);
        addView(imageView, layoutParams7);
    }

    
    
    public  void m218x2e919c1(boolean z, String str) {
        if (z) {
            onPadClick();
            return;
        }
        ActionScreenResult actionScreenResult = this.actionScreenResult;
        if (actionScreenResult != null) {
            actionScreenResult.onPadClick(str);
        }
    }

    
    
    public  void m219xf6789e02(View view) {
        this.actionScreenResult.onMute();
    }

    
    
    public  void m220xea082243(View view) {
        this.actionScreenResult.onSpeaker();
    }

    
    
    public  void m221xdd97a684(View view) {
        onPadClick();
    }

    
    
    public  void m222xd1272ac5(View view) {
        this.actionScreenResult.onHold();
    }

    
    
    public  void m223xc4b6af06(View view) {
        this.actionScreenResult.onRecorder();
    }

    
    
    public  void m224xb8463347(View view) {
        this.actionScreenResult.onOpenContact();
    }

    
    
    public  void m225xabd5b788(View view) {
        this.actionScreenResult.onReject();
    }

    private ImageView makeIm(int i, int i2, OnClickListener onClickListener) {
        int i3 = (this.w * 2) / 100;
        ImageView imageView = new ImageView(getContext());
        imageView.setId(i);
        imageView.setPadding(i3, i3, i3, i3);
        imageView.setImageResource(i2);
        imageView.setOnClickListener(onClickListener);
        return imageView;
    }

    public void setActionScreenResult(ActionScreenResult actionScreenResult) {
        this.actionScreenResult = actionScreenResult;
    }

    public void onStart() {
        setVisibility(View.GONE);
        setAlpha(0.0f);
        this.imEndCall.setTranslationY(-this.size);
    }

    public void onShow() {
        if (getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
            animate().alpha(1.0f).setDuration(500L).start();
            this.imEndCall.animate().translationY(0.0f).setDuration(500L).start();
        }
    }

    public void onPadClick() {
        boolean z = !this.openPad;
        this.openPad = z;
        if (z) {
            this.padPixel.animate().translationY(0.0f).setDuration(400).start();
            return;
        }
        float height = this.padPixel.getHeight();
        if (height == 0.0f) {
            height = this.w * 2;
        }
        this.padPixel.animate().translationY(height).setDuration(400).start();
    }

    public void updateUI(boolean z, boolean z2, boolean z3, boolean z4) {
        if (z) {
            this.vMute.setColorFilter(Color.parseColor("#2B5EE1"));
        } else {
            this.vMute.clearColorFilter();
        }
        if (z2) {
            this.vSpeaker.setColorFilter(Color.parseColor("#2B5EE1"));
        } else {
            this.vSpeaker.clearColorFilter();
        }
        if (z3) {
            this.vHold.setColorFilter(Color.parseColor("#2B5EE1"));
        } else {
            this.vHold.clearColorFilter();
        }
        if (z4) {
            this.vRec.setColorFilter(Color.parseColor("#2B5EE1"));
        } else {
            this.vRec.clearColorFilter();
        }
    }
}
