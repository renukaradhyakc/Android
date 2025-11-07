package com.thelinkphone.app;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;


public class BaseActivity extends AppCompatActivity {
    
    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().setNavigationBarColor(Color.parseColor("#EFEFEF"));
        getWindow().setStatusBarColor(-1);
        getWindow().getDecorView().setSystemUiVisibility(Build.VERSION.SDK_INT >= 26 ? 8208 : 8192);
    }
}
