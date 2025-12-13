package com.thelinkphone.app.screen.other;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.widget.Button;import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.screen.ActionScreenResult;
import com.thelinkphone.app.screen.PadResult;
import com.thelinkphone.app.screen.samsung.PadGalaxy;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewCallOther extends RelativeLayout {
    private ActionScreenResult actionScreenResult;
    private final LinearLayout llMode;
    private boolean openPad;
    private final PadGalaxy padGalaxy;
    private final ImageView vHold;
    private final ImageView vMute;
    private final ImageView vRec;
    private final ImageView vSpeaker;
    private final int w;

    public void setActionScreenResult(ActionScreenResult actionScreenResult) {
        this.actionScreenResult = actionScreenResult;
    }

    public ViewCallOther(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        this.w = widthScreen;
        int i = (widthScreen * 19) / 100;
        ImageView imageView = new ImageView(context);
        imageView.setId(5550);
        imageView.setImageResource(R.drawable.ic_end_call);
        imageView.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallOther.this.m209xb800b955(view);
            }
        });
        LayoutParams layoutParams = new LayoutParams(i, i);
        layoutParams.addRule(14);
        layoutParams.addRule(12);
        int i2 = i / 2;
        layoutParams.setMargins(i2, i / 4, i2, (MyShare.getSizeNavigation(context) + (widthScreen / 4)) - i2);
        addView(imageView, layoutParams);
        ImageView makeIm = makeIm(5551, R.drawable.im_mode_contact, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallOther.this.m210xab903d96(view);
            }
        });
        LayoutParams layoutParams2 = new LayoutParams(i, i);
        layoutParams2.addRule(6, imageView.getId());
        layoutParams2.addRule(16, imageView.getId());
        addView(makeIm, layoutParams2);
        ImageView makeIm2 = makeIm(5552, R.drawable.im_mode_pad, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallOther.this.m211x9f1fc1d7(view);
            }
        });
        LayoutParams layoutParams3 = new LayoutParams(i, i);
        layoutParams3.addRule(6, imageView.getId());
        layoutParams3.addRule(17, imageView.getId());
        addView(makeIm2, layoutParams3);
        
        LinearLayout linearLayout = new LinearLayout(context);
        this.llMode = linearLayout;
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setId(5554);
        linearLayout.setGravity(1);
        LayoutParams layoutParams4 = new LayoutParams(-1, i);
        layoutParams4.addRule(2, imageView.getId());
        addView(linearLayout, layoutParams4);
        linearLayout.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        
        ImageView makeIm3 = makeIm(121, R.drawable.im_mode_mute_on, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallOther.this.m212x92af4618(view);
            }
        });
        this.vMute = makeIm3;
        linearLayout.addView(makeIm3, i, -1);
        linearLayout.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        ImageView makeIm4 = makeIm(122, R.drawable.im_mode_speaker, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallOther.this.m213x863eca59(view);
            }
        });
        this.vSpeaker = makeIm4;
        linearLayout.addView(makeIm4, i, -1);
        linearLayout.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        ImageView makeIm5 = makeIm(124, R.drawable.im_mode_hold, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallOther.this.m214x79ce4e9a(view);
            }
        });
        this.vHold = makeIm5;
        linearLayout.addView(makeIm5, i, -1);
        linearLayout.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        ImageView makeIm6 = makeIm(125, R.drawable.im_mode_rec, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallOther.this.m215x6d5dd2db(view);
            }
        });
        this.vRec = makeIm6;
        linearLayout.addView(makeIm6, i, -1);
        linearLayout.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        LinearLayout linearLayout2 = new LinearLayout(context);
        linearLayout2.setGravity(1);
        linearLayout2.setOrientation(LinearLayout.VERTICAL);
        LayoutParams layoutParams5 = new LayoutParams(-1, -2);
        layoutParams5.addRule(2, imageView.getId());
        addView(linearLayout2, layoutParams5);
        PadGalaxy padGalaxy = new PadGalaxy(context);
        this.padGalaxy = padGalaxy;
        padGalaxy.setId(40);
        padGalaxy.setPadGalaxyResult(new PadResult() { 
            @Override 
            public final void onViewClick(boolean z, String str) {
                ViewCallOther.this.m216x60ed571c(z, str);
            }
        });
        linearLayout2.addView(padGalaxy, -2, -2);
    }

    
    
    public  void m209xb800b955(View view) {
        this.actionScreenResult.onReject();
    }

    
    
    public  void m210xab903d96(View view) {
        this.actionScreenResult.onOpenContact();
    }

    
    
    public  void m211x9f1fc1d7(View view) {
        onPadClick();
    }

    
    
    public  void m212x92af4618(View view) {
        this.actionScreenResult.onMute();
    }

    
    
    public  void m213x863eca59(View view) {
        this.actionScreenResult.onSpeaker();
    }

    
    
    public  void m214x79ce4e9a(View view) {
        this.actionScreenResult.onHold();
    }

    
    
    public  void m215x6d5dd2db(View view) {
        this.actionScreenResult.onRecorder();
    }

    
    
    public  void m216x60ed571c(boolean z, String str) {
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

    public void onPadClick() {
        boolean z = !this.openPad;
        this.openPad = z;
        if (z) {
            long j = 400;
            this.padGalaxy.animate().translationY(0.0f).setDuration(j).start();
            this.llMode.animate().alpha(0.0f).setDuration(j).start();
            return;
        }
        float height = this.padGalaxy.getHeight();
        if (height == 0.0f) {
            height = this.w * 2;
        }
        long j2 = 400;
        this.padGalaxy.animate().translationY(height).setDuration(j2).start();
        this.llMode.animate().alpha(1.0f).setDuration(j2).start();
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
