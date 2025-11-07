package com.thelinkphone.app.screen.mate;

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



public class ViewCallMate extends RelativeLayout {
    private ActionScreenResult actionScreenResult;
    private final LinearLayout llTop;
    private boolean openPad;
    private final PadMate padMate;
    private final ImageView vHold;
    private final ImageView vMute;
    private final ImageView vRec;
    private final ImageView vSpeaker;
    private final int w;

    public ViewCallMate(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        this.w = widthScreen;
        int i = (widthScreen * 19) / 100;
        LinearLayout linearLayout = new LinearLayout(context);
        linearLayout.setId(300);
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams layoutParams = new LayoutParams(-1, i);
        layoutParams.addRule(12);
        layoutParams.setMargins(0, widthScreen / 50, 0, MyShare.getSizeNavigation(context) + (widthScreen / 25));
        addView(linearLayout, layoutParams);
        linearLayout.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        linearLayout.addView(makeIm(301, R.drawable.im_mode_contact, new OnClickListener() {
            @Override 
            public final void onClick(View view) {
                ViewCallMate.this.m200x8bf0d6b5(view);
            }
        }), i, i);
        linearLayout.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        ImageView imageView = new ImageView(context);
        imageView.setImageResource(R.drawable.ic_end_call);
        imageView.setOnClickListener(new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallMate.this.m201x46667736(view);
            }
        });
        linearLayout.addView(imageView, i, i);
        linearLayout.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        linearLayout.addView(makeIm(302, R.drawable.im_mode_pad, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallMate.this.m202xdc17b7(view);
            }
        }), i, i);
        linearLayout.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        LinearLayout linearLayout2 = new LinearLayout(context);
        this.llTop = linearLayout2;
        linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
        LayoutParams layoutParams2 = new LayoutParams(-1, i);
        layoutParams2.addRule(2, linearLayout.getId());
        addView(linearLayout2, layoutParams2);
        linearLayout2.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        ImageView makeIm = makeIm(303, R.drawable.im_mode_rec, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallMate.this.m203xbb51b838(view);
            }
        });
        this.vRec = makeIm;
        linearLayout2.addView(makeIm, i, i);
        linearLayout2.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        ImageView makeIm2 = makeIm(304, R.drawable.im_mode_hold, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallMate.this.m204x75c758b9(view);
            }
        });
        this.vHold = makeIm2;
        linearLayout2.addView(makeIm2, i, i);
        linearLayout2.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        ImageView makeIm3 = makeIm(305, R.drawable.im_mode_speaker, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallMate.this.m205x303cf93a(view);
            }
        });
        this.vSpeaker = makeIm3;
        linearLayout2.addView(makeIm3, i, i);
        linearLayout2.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        ImageView makeIm4 = makeIm(306, R.drawable.im_mode_mute_on, new OnClickListener() { 
            @Override 
            public final void onClick(View view) {
                ViewCallMate.this.m206xeab299bb(view);
            }
        });
        this.vMute = makeIm4;
        linearLayout2.addView(makeIm4, i, i);
        linearLayout2.addView(new View(context), new LinearLayout.LayoutParams(0, -1, 1.0f));
        LinearLayout linearLayout3 = new LinearLayout(context);
        linearLayout3.setOrientation(LinearLayout.VERTICAL);
        linearLayout3.setGravity(1);
        LayoutParams layoutParams3 = new LayoutParams(-1, -2);
        layoutParams3.addRule(2, linearLayout.getId());
        addView(linearLayout3, layoutParams3);
        PadMate padMate = new PadMate(context);
        this.padMate = padMate;
        padMate.setItfPadResult(new PadResult() { 
            @Override 
            public final void onViewClick(boolean z, String str) {
                ViewCallMate.this.m207xa5283a3c(z, str);
            }
        });
        linearLayout3.addView(padMate, -1, -2);
    }

    
    
    public  void m200x8bf0d6b5(View view) {
        this.actionScreenResult.onOpenContact();
    }

    
    
    public  void m201x46667736(View view) {
        this.actionScreenResult.onReject();
    }

    
    
    public  void m202xdc17b7(View view) {
        onPadClick();
    }

    
    
    public  void m203xbb51b838(View view) {
        this.actionScreenResult.onRecorder();
    }

    
    
    public  void m204x75c758b9(View view) {
        this.actionScreenResult.onHold();
    }

    
    
    public  void m205x303cf93a(View view) {
        this.actionScreenResult.onSpeaker();
    }

    
    
    public  void m206xeab299bb(View view) {
        this.actionScreenResult.onMute();
    }

    
    
    public  void m207xa5283a3c(boolean z, String str) {
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
        int i3 = (this.w * 4) / 95;
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
            this.padMate.animate().translationY(0.0f).setDuration(j).start();
            this.llTop.animate().alpha(0.0f).setDuration(j).start();
            return;
        }
        float height = this.padMate.getHeight();
        if (height == 0.0f) {
            height = this.w * 2;
        }
        long j2 = 400;
        this.padMate.animate().translationY(height).setDuration(j2).start();
        this.llTop.animate().alpha(1.0f).setDuration(j2).start();
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
