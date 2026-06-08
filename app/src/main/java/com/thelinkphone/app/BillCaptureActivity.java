package com.thelinkphone.app;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.thelinkphone.app.fragment.BillCaptureFragment;

public class BillCaptureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getSupportFragmentManager()
                .beginTransaction()
                .replace(android.R.id.content, new BillCaptureFragment())
                .commit();
    }
}