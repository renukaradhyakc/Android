package com.thelinkphone.app;

import android.annotation.SuppressLint;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import androidx.appcompat.app.AppCompatActivity;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.screen.ActionScreenResult;
import com.thelinkphone.app.screen.BackgroundManager;
import com.thelinkphone.app.screen.ios.ViewCall;
import com.thelinkphone.app.screen.ios.ViewInComingIOS;
import com.thelinkphone.app.utils.MyConst;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class ActivityFakeRingCall extends AppCompatActivity {
    private String name;
    private String photo;
    private MediaPlayer player;
    private Vibrator v;
    private ViewRingFake viewRingFake;

    
    @SuppressLint("InvalidWakeLockTag")
    @Override 
    public void onCreate(Bundle bundle) {
        int i;
        super.onCreate(bundle);
        KeyguardManager keyguardManager = (KeyguardManager) getApplicationContext().getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
            if (keyguardManager != null) {
                keyguardManager.requestDismissKeyguard(this, null);
            }
            i = 512;
        } else {
            i = 6816384;
        }
        getWindow().getDecorView().setSystemUiVisibility(1280);
        getWindow().addFlags(i);
        getWindow().setNavigationBarColor(0);
        getWindow().setStatusBarColor(0);
        PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (powerManager != null) {
            powerManager.newWakeLock(26, "tag").acquire(5000L);
        }
        Intent intent = getIntent();
        this.name = intent.getStringExtra(MyConst.DATA_NAME);
        this.photo = intent.getStringExtra(MyConst.DATA_PHOTO);
        ViewRingFake viewRingFake = new ViewRingFake(this);
        this.viewRingFake = viewRingFake;
        setContentView(viewRingFake);
        makeRingCall();
    }

    private void makeRingCall() {
        new Handler().postDelayed(new Runnable() { 
            @Override 
            public final void run() {
                ActivityFakeRingCall.this.m43x21c76e4e();
            }
        }, 300L);
    }

    
    
    public  void m43x21c76e4e() {
        MediaPlayer create = MediaPlayer.create(this, RingtoneManager.getDefaultUri(1));
        this.player = create;
        if (create != null) {
            create.setLooping(true);
            this.player.start();
        }
        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        this.v = vibrator;
        long[] jArr = {0, 100, 200, 0, 200, 100, 0};
        if (vibrator != null) {
            if (Build.VERSION.SDK_INT >= 26) {
                this.v.vibrate(VibrationEffect.createWaveform(jArr, new int[]{-1, -1, -1, -1, -1, -1, -1}, 2));
            } else {
                this.v.vibrate(jArr, 2);
            }
        }
    }

    void stopSoundAndVibrate() {
        MediaPlayer mediaPlayer = this.player;
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            this.player.stop();
        }
        Vibrator vibrator = this.v;
        if (vibrator != null) {
            vibrator.cancel();
        }
    }

    
    @Override 
    public void onResume() {
        super.onResume();
        this.viewRingFake.getBackgroundManager().onResume();
    }

    
    @Override 
    public void onPause() {
        super.onPause();
        this.viewRingFake.getBackgroundManager().onPause();
        stopSoundAndVibrate();
    }

    @Override 
    public void onBackPressed() {
        super.onBackPressed();
        stopSoundAndVibrate();
    }

    
    @Override 
    public void onDestroy() {
        super.onDestroy();
        String str = this.photo;
        if (str != null && !str.isEmpty()) {
            OtherUtils.deleteFile(this.photo);
        }
        this.viewRingFake.getBackgroundManager().onDestroy();
    }

    
    class ViewRingFake extends RelativeLayout {
        private final BackgroundManager backgroundManager;
        public int callDuration;
        private final Handler handler;
        private final Runnable runnable;
        private final TextW tvStatus;
        private final ViewCall viewCall;
        private final ViewInComingIOS viewInComingIOS;

        public ViewRingFake(Context context) {
            super(context);
            this.runnable = new Runnable() { 
                @Override 
                public void run() {
                    ViewRingFake.this.callDuration++;
                    ViewRingFake.this.tvStatus.setText(OtherUtils.longToDur(ViewRingFake.this.callDuration));
                    ViewRingFake.this.handler.postDelayed(this, 1000L);
                }
            };
            setBackgroundColor(-16777216);
            this.handler = new Handler();
            this.backgroundManager = new BackgroundManager(this);
            int widthScreen = OtherUtils.getWidthScreen(context);
            int i = widthScreen / 25;
            int i2 = (widthScreen * 20) / 100;
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.icon200);
            imageView.setId(100);
            TextW textW = new TextW(context);
            textW.setupText(600, 6.3f);
            textW.setTextColor(-1);
            textW.setText(ActivityFakeRingCall.this.name);
            TextW textW2 = new TextW(context);
            this.tvStatus = textW2;
            textW2.setupText(400, 3.8f);
            textW2.setTextColor(-1);
            textW2.setText(R.string.is_calling);
            LayoutParams layoutParams = new LayoutParams(i2, i2);
            layoutParams.addRule(21);
            int i3 = (widthScreen * 13) / 100;
            layoutParams.setMargins(0, MyShare.getSizeNotification(context) + i3, i, 0);
            addView(imageView, layoutParams);
            if (ActivityFakeRingCall.this.photo != null && !ActivityFakeRingCall.this.photo.isEmpty()) {
                Glide.with(getContext()).load(ActivityFakeRingCall.this.photo).apply((BaseRequestOptions<?>) new RequestOptions().circleCrop()).into(imageView);
            } else {
                imageView.setVisibility(View.GONE);
            }
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setId(456456);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(16);
            linearLayout.setPadding(i, 0, i, 0);
            LayoutParams layoutParams2 = new LayoutParams(-1, i2);
            layoutParams2.setMargins(0, MyShare.getSizeNotification(context) + i3, 0, 0);
            layoutParams2.addRule(16, imageView.getId());
            addView(linearLayout, layoutParams2);
            linearLayout.addView(textW, -1, -2);
            LinearLayout.LayoutParams layoutParams3 = new LinearLayout.LayoutParams(-1, -2);
            layoutParams3.setMargins(0, widthScreen / 200, 0, 0);
            linearLayout.addView(textW2, layoutParams3);
            ViewInComingIOS viewInComingIOS = new ViewInComingIOS(context);
            this.viewInComingIOS = viewInComingIOS;
            viewInComingIOS.setViewRoot(this);
            viewInComingIOS.setContentTextSlide(R.string.slide_to_answer);
            viewInComingIOS.setActionScreenResult(new ActionAcceptResult() { 
                @Override 
                public void onReject() {
                }

                @Override 
                public void onAccept() {
                    ViewRingFake.this.viewInComingIOS.onRemove();
                    ViewRingFake.this.viewCall.onShow();
                    ViewRingFake.this.handler.removeCallbacks(ViewRingFake.this.runnable);
                    ViewRingFake.this.handler.post(ViewRingFake.this.runnable);
                    ActivityFakeRingCall.this.stopSoundAndVibrate();
                }
            });
            LayoutParams layoutParams4 = new LayoutParams(-1, (int) ((widthScreen * 21.2f) / 100.0f));
            layoutParams4.addRule(12);
            int i4 = widthScreen / 8;
            layoutParams4.setMargins(i4, 0, i4, MyShare.getSizeNavigation(context) + (widthScreen / 7));
            addView(viewInComingIOS, layoutParams4);
            ViewCall viewCall = new ViewCall(context);
            this.viewCall = viewCall;
            viewCall.onStart();
            LayoutParams layoutParams5 = new LayoutParams(-1, -1);
            layoutParams5.addRule(3, linearLayout.getId());
            addView(viewCall, layoutParams5);
            viewCall.setActionScreenResult(new ActionScreenResult() { 
                @Override 
                public void onAccept() {
                }

                @Override 
                public void onHold() {
                }

                @Override 
                public void onMute() {
                }

                @Override 
                public void onOpenContact() {
                }

                @Override 
                public void onPadClick(String str) {
                }

                @Override 
                public void onRecorder() {
                }

                @Override 
                public void onSpeaker() {
                }

                @Override 
                public void onReject() {
                    ActivityFakeRingCall.this.stopSoundAndVibrate();
                    ActivityFakeRingCall.this.finish();
                }

                @Override
                public void onAddMessage() {
                    // code when "+" button is clicked
                }
            });
        }

        public BackgroundManager getBackgroundManager() {
            return this.backgroundManager;
        }
    }
}
