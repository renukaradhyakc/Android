package com.thelinkphone.app;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;
import com.thelinkphone.app.utils.MyShare;



public class ActivityApplyTheme extends AppCompatActivity {
    private final Runnable rEnd = new Runnable() { 
        @Override 
        public final void run() {
            ActivityApplyTheme.this.m42xc71b07b0();
        }
    };

    @Override 
    public void onBackPressed() {
    }


    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(1280);
        window.addFlags(512);
        window.setNavigationBarColor(0);
        window.setStatusBarColor(0);
        setContentView(R.layout.activity_apply_theme);
        boolean theme = MyShare.getTheme(this);
        View findViewById = findViewById(R.id.v_while);
        if (theme) {
            findViewById.setAlpha(0.0f);
            findViewById.animate().setDuration(2500L).alpha(1.0f).withEndAction(this.rEnd).start();
            return;
        }
        findViewById.setAlpha(1.0f);
        findViewById.animate().setDuration(2500L).alpha(0.0f).withEndAction(this.rEnd).start();
    }



    public  void m42xc71b07b0() {
        new Handler().postDelayed(new Runnable() { 
            @Override 
            public final void run() {
                ActivityApplyTheme.this.startAc();
            }
        }, 1500L);
    }


    public void startAc() {
        startActivity(new Intent(this, ActivityHome.class));
        finish();
    }
}
