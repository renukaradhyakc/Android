package com.thelinkphone.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.view.View;
import android.widget.Toast;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.thelinkphone.app.item.ItemSimInfo;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.SimUtils;


import java.util.ArrayList;


public class DialerActivity extends AppCompatActivity {
    private Uri callNumber;
    private PhoneAccountHandle handle;
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback() { 
        @Override 
        public final void onActivityResult(Object obj) {
            DialerActivity.this.m52lambda$new$0$comcallos14callscreencolorphoneDialerActivity((ActivityResult) obj);
        }
    });


    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        View view = new View(this);
        setContentView(view);
        boolean theme = MyShare.getTheme(this);
        getWindow().setStatusBarColor(0);
        if (theme) {
            getWindow().setNavigationBarColor(Color.parseColor("#EFEFEF"));
            getWindow().getDecorView().setSystemUiVisibility(Build.VERSION.SDK_INT >= 26 ? 9232 : 9216);
            view.setBackgroundColor(-1);
        } else {
            getWindow().setNavigationBarColor(Color.parseColor("#2C2C2C"));
            getWindow().getDecorView().setSystemUiVisibility(1024);
            view.setBackgroundColor(Color.parseColor("#2C2C2C"));
        }
        getDataCall();
    }

    private void getDataCall() {
        Intent intent = getIntent();
        if (intent != null) {
            this.handle = (PhoneAccountHandle) intent.getParcelableExtra("android.telecom.extra.PHONE_ACCOUNT_HANDLE");
        }
        if (intent != null && intent.getAction() != null && intent.getAction().equals("android.intent.action.CALL") && intent.getData() != null) {
            this.callNumber = intent.getData();
            if (OtherUtils.checkPer(this)) {
                OtherUtils.requestDefault(this, this.launcher);
                return;
            } else {
                initOutgoingCall();
                return;
            }
        }
        Toast.makeText(this, (int) R.string.unknown_error_occurred, Toast.LENGTH_SHORT).show();
        finish();
    }



    public  void m52lambda$new$0$comcallos14callscreencolorphoneDialerActivity(ActivityResult activityResult) {
        if (activityResult.getResultCode() == -1) {
            if (OtherUtils.checkPer(this)) {
                try {
                    gotoSettingApp();
                    Toast.makeText(this, (int) R.string.default_phone_app_prompt, Toast.LENGTH_SHORT).show();
                } catch (Exception unused) {
                }
                finish();
                return;
            }
            initOutgoingCall();
        }
    }

    private void initOutgoingCall() {
        PhoneAccountHandle phoneAccountHandle;
        if (ActivityCompat.checkSelfPermission(this, "android.permission.CALL_PHONE") != 0) {
            Toast.makeText(this, (int) R.string.request_per, Toast.LENGTH_SHORT).show();
            gotoSettingApp();
            finish();
            return;
        }
        if (this.handle == null) {
            ArrayList<ItemSimInfo> availableSIMCardLabels = SimUtils.getAvailableSIMCardLabels(this);
            if (availableSIMCardLabels.size() > 0) {
                int posSim = MyShare.getPosSim(this);
                if (posSim < availableSIMCardLabels.size()) {
                    phoneAccountHandle = availableSIMCardLabels.get(posSim).handle;
                } else {
                    phoneAccountHandle = availableSIMCardLabels.get(0).handle;
                }
                if (phoneAccountHandle != null) {
                    this.handle = phoneAccountHandle;
                }
            }
        }
        TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
        if (this.handle == null) {
            this.handle = telecomManager.getDefaultOutgoingPhoneAccount("tel");
        }
        if (this.handle == null) {
            Toast.makeText(this, (int) R.string.error, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Bundle bundle = new Bundle();
        bundle.putParcelable("android.telecom.extra.PHONE_ACCOUNT_HANDLE", this.handle);
        bundle.putBoolean("android.telecom.extra.START_CALL_WITH_VIDEO_STATE", false);
        bundle.putBoolean("android.telecom.extra.START_CALL_WITH_SPEAKERPHONE", false);
        telecomManager.placeCall(this.callNumber, bundle);
        finish();
    }

    private void gotoSettingApp() {
        Intent intent = new Intent("android.settings.APPLICATION_DETAILS_SETTINGS", Uri.fromParts("package", getPackageName(), null));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }
}
