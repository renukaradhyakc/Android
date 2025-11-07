package com.thelinkphone.app;

import android.os.Build;
import android.os.Bundle;
import android.view.Window;
import androidx.appcompat.app.AppCompatActivity;


public class BaseActivityUi extends AppCompatActivity {

    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Window window = getWindow();
        window.getDecorView().setSystemUiVisibility(Build.VERSION.SDK_INT >= 26 ? 8208 : 8192);
        window.setNavigationBarColor(-1);
        window.setStatusBarColor(-1);
    }
}
