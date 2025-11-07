package com.thelinkphone.app.screen.samsung;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.screen.ActionScreenResult;
import com.thelinkphone.app.screen.PadResult;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewCallGalaxy extends RelativeLayout {
    private ActionScreenResult actionScreenResult;
    private boolean openPad;
    private final PadGalaxy padGalaxy;
    private final ImageView vHold;
    private final ImageView vMute;
    private final ImageView vRec;
    private final ImageView vSpeaker;
    private final int w;

    public ViewCallGalaxy(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        this.w = widthScreen;
        int i = (int) ((widthScreen * 16.7d) / 100.0d);
        int i2 = i / 2;
        ImageView imageView = new ImageView(context);
        imageView.setId(120);
        imageView.setImageResource(R.drawable.ic_end_call_galaxy);
        imageView.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallGalaxy.this.m227x30b95481(view);
            }
        });
        LayoutParams layoutParams = new LayoutParams(i, i);
        layoutParams.addRule(14);
        layoutParams.addRule(12);
        layoutParams.setMargins(0, 0, 0, MyShare.getSizeNavigation(context) + i2);
        addView(imageView, layoutParams);
        ImageView makeIm = makeIm(121, R.drawable.im_mode_mute_on, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallGalaxy.this.m228x9ae8dca0(view);
            }
        });
        this.vMute = makeIm;
        LayoutParams layoutParams2 = new LayoutParams(i, i);
        layoutParams2.addRule(14);
        layoutParams2.addRule(2, imageView.getId());
        layoutParams2.setMargins(i2, i2 / 2, i2, (i2 * 3) / 2);
        addView(makeIm, layoutParams2);
        ImageView makeIm2 = makeIm(126, R.drawable.im_mode_contact, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallGalaxy.this.m229x51864bf(view);
            }
        });
        LayoutParams layoutParams3 = new LayoutParams(i, i);
        layoutParams3.addRule(6, makeIm.getId());
        layoutParams3.addRule(16, makeIm.getId());
        addView(makeIm2, layoutParams3);
        ImageView makeIm3 = makeIm(123, R.drawable.im_mode_pad, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallGalaxy.this.m230x6f47ecde(view);
            }
        });
        LayoutParams layoutParams4 = new LayoutParams(i, i);
        layoutParams4.addRule(6, makeIm.getId());
        layoutParams4.addRule(17, makeIm.getId());
        addView(makeIm3, layoutParams4);
        ImageView makeIm4 = makeIm(124, R.drawable.im_mode_hold, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallGalaxy.this.m231xd97774fd(view);
            }
        });
        this.vHold = makeIm4;
        LayoutParams layoutParams5 = new LayoutParams(i, i);
        layoutParams5.addRule(14);
        layoutParams5.addRule(2, makeIm.getId());
        layoutParams5.setMargins(i2, 0, i2, 0);
        addView(makeIm4, layoutParams5);
        ImageView makeIm5 = makeIm(125, R.drawable.im_mode_rec, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallGalaxy.this.m232x43a6fd1c(view);
            }
        });
        this.vRec = makeIm5;
        LayoutParams layoutParams6 = new LayoutParams(i, i);
        layoutParams6.addRule(6, makeIm4.getId());
        layoutParams6.addRule(16, makeIm4.getId());
        addView(makeIm5, layoutParams6);
        ImageView makeIm6 = makeIm(122, R.drawable.im_mode_speaker, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallGalaxy.this.m233xadd6853b(view);
            }
        });
        this.vSpeaker = makeIm6;
        LayoutParams layoutParams7 = new LayoutParams(i, i);
        layoutParams7.addRule(6, makeIm4.getId());
        layoutParams7.addRule(17, makeIm4.getId());
        addView(makeIm6, layoutParams7);
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setGravity(1);
        linearLayout.setOrientation(LinearLayout.VERTICAL);
        LayoutParams layoutParams8 = new LayoutParams(-1, -2);
        layoutParams8.addRule(2, makeIm.getId());
        addView(linearLayout, layoutParams8);
        PadGalaxy padGalaxy = new PadGalaxy(context);
        this.padGalaxy = padGalaxy;
        padGalaxy.setId(40);
        padGalaxy.setPadGalaxyResult(new PadResult() { 
            @Override 
            public final void onViewClick(boolean z, String str) {
                ViewCallGalaxy.this.m234x18060d5a(z, str);
            }
        });
        linearLayout.addView(padGalaxy, -2, -2);
    }

    
    
    public  void m227x30b95481(View view) {
        this.actionScreenResult.onReject();
    }

    
    
    public  void m228x9ae8dca0(View view) {
        this.actionScreenResult.onMute();
    }

    
    
    public  void m229x51864bf(View view) {
        this.actionScreenResult.onOpenContact();
    }

    
    
    public  void m230x6f47ecde(View view) {
        onPadClick();
    }

    
    
    public  void m231xd97774fd(View view) {
        this.actionScreenResult.onHold();
    }

    
    
    public  void m232x43a6fd1c(View view) {
        this.actionScreenResult.onRecorder();
    }

    
    
    public  void m233xadd6853b(View view) {
        this.actionScreenResult.onSpeaker();
    }

    
    
    public  void m234x18060d5a(boolean z, String str) {
        if (z) {
            onPadClick();
            return;
        }
        ActionScreenResult actionScreenResult = this.actionScreenResult;
        if (actionScreenResult != null) {
            actionScreenResult.onPadClick(str);
        }
    }

    private ImageView makeIm(int i, int i2, OnClickListener onClickListener) {
        int i3 = (this.w * 4) / 100;
        ImageView imageView = new ImageView(getContext());
        imageView.setId(i);
        imageView.setPadding(i3, i3, i3, i3);
        imageView.setImageResource(i2);
        imageView.setOnClickListener(onClickListener);
        return imageView;
    }

    public void onPadClick() {
        boolean z = !this.openPad;
        this.openPad = z;
        if (z) {
            long j = 400;
            this.padGalaxy.animate().translationY(0.0f).setDuration(j).start();
            this.vHold.animate().alpha(0.0f).setDuration(j).start();
            this.vRec.animate().alpha(0.0f).setDuration(j).start();
            this.vSpeaker.animate().alpha(0.0f).setDuration(j).start();
            return;
        }
        float height = this.padGalaxy.getHeight();
        if (height == 0.0f) {
            height = this.w * 2;
        }
        long j2 = 400;
        this.padGalaxy.animate().translationY(height).setDuration(j2).start();
        this.vHold.animate().alpha(1.0f).setDuration(j2).start();
        this.vRec.animate().alpha(1.0f).setDuration(j2).start();
        this.vSpeaker.animate().alpha(1.0f).setDuration(j2).start();
    }

    public void setActionScreenResult(ActionScreenResult actionScreenResult) {
        this.actionScreenResult = actionScreenResult;
    }

    public void onStart() {
        setVisibility(View.GONE);
        setAlpha(0.0f);
    }

    public void onShow() {
        if (getVisibility() == View.GONE) {
            setVisibility(View.VISIBLE);
            animate().alpha(1.0f).setDuration(500L).start();
        }
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
