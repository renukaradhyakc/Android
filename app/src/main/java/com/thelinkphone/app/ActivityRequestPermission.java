package com.thelinkphone.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.core.app.ActivityCompat;

import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class ActivityRequestPermission extends BaseActivity {
    private boolean onDone;
    private ViewPer viewPer;
    private final String[] per = {"android.permission.READ_CONTACTS", "android.permission.CALL_PHONE", "android.permission.READ_CALL_LOG", "android.permission.WRITE_CALL_LOG", "android.permission.READ_PHONE_STATE", "android.permission.READ_PHONE_NUMBERS"};
    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback() { 
        @Override 
        public final void onActivityResult(Object obj) {
            ActivityRequestPermission.this.m49xbd3b9769((ActivityResult) obj);
        }
    });

    @Override
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        ViewPer viewPer = new ViewPer(this);
        this.viewPer = viewPer;
        setContentView(viewPer);
    }


    @Override 
    public void onResume() {
        super.onResume();
        if (!OtherUtils.checkPer(this)) {
            if (OtherUtils.checkPermission(this)) {
                onStartMain();
                return;
            } else {
                this.viewPer.modePer();
                return;
            }
        }
        this.viewPer.modeDefault();
    }



    public  void m49xbd3b9769(ActivityResult activityResult) {
        if (activityResult.getResultCode() == -1) {
            onStartMain();
        }
    }

    private void onStartMain() {
        if (this.onDone) {
            return;
        }
        this.onDone = true;
        startActivity(new Intent(this, ActivityHome.class));
        finish();
    }



    public class ViewPer extends RelativeLayout {
        private boolean modeDefault;
        private final TextW tvAction;
        private final TextW tvContent;
        private final TextW tvTitle;

        public ViewPer(Context context) {
            super(context);
            setBackgroundColor(-1);
            int widthScreen = OtherUtils.getWidthScreen(context);
            ImageView imageView = new ImageView(context);
            imageView.setId(32416);
            imageView.setImageResource(R.drawable.icon200);
            imageView.setBackground(OtherUtils.bgIcon(-1, widthScreen / 9.0f));
            int i = widthScreen / 2;
            LayoutParams layoutParams = new LayoutParams(i, i);
            layoutParams.addRule(14);
            int i2 = widthScreen / 3;
            layoutParams.setMargins(0, i2, 0, 0);
            addView(imageView, layoutParams);
            int i3 = widthScreen / 20;
            TextW textW = new TextW(context);
            this.tvAction = textW;
            textW.setId(6541);
            textW.setupText(600, 4.2f);
            textW.setTextColor(-1);
            int i4 = i3 / 2;
            textW.setPadding(i3, i4, i3, i4);
            textW.setBackgroundResource(R.drawable.sel_tv_action);
            textW.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewPer.this.m50x99bbc5dd(view);
                }
            });
            LayoutParams layoutParams2 = new LayoutParams(-2, -2);
            layoutParams2.addRule(14);
            layoutParams2.addRule(12);
            layoutParams2.setMargins(0, 0, 0, i2);
            addView(textW, layoutParams2);
            TextW textW2 = new TextW(context);
            this.tvContent = textW2;
            textW2.setId(65);
            textW2.setupText(400, 4.5f);
            textW2.setPadding(i3, 0, i3, 0);
            textW2.setGravity(1);
            LayoutParams layoutParams3 = new LayoutParams(-1, -2);
            layoutParams3.addRule(2, textW.getId());
            layoutParams3.setMargins(0, 0, 0, i3 * 3);
            addView(textW2, layoutParams3);
            TextW textW3 = new TextW(context);
            this.tvTitle = textW3;
            textW3.setupText(600, 6.2f);
            textW3.setPadding(i3, 0, i3, 0);
            textW3.setGravity(1);
            LayoutParams layoutParams4 = new LayoutParams(-1, -2);
            layoutParams4.addRule(2, textW2.getId());
            layoutParams4.setMargins(0, 0, 0, i4);
            addView(textW3, layoutParams4);
        }



        public  void m50x99bbc5dd(View view) {
            getSize();
            if (this.modeDefault) {
                ActivityRequestPermission activityRequestPermission = ActivityRequestPermission.this;
                OtherUtils.requestDefault(activityRequestPermission, activityRequestPermission.launcher);
                return;
            }
            ActivityRequestPermission activityRequestPermission2 = ActivityRequestPermission.this;
            ActivityCompat.requestPermissions(activityRequestPermission2, activityRequestPermission2.per, 1);
        }

        private void getSize() {
            if (MyShare.getSizeNotification(getContext()) == 0) {
                Point point = new Point();
                ((WindowManager) ActivityRequestPermission.this.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRealSize(point);
                int[] iArr = new int[2];
                getLocationOnScreen(iArr);
                int i = iArr[1];
                int height = (point.y - i) - getHeight();
                MyShare.putSizeNotification(getContext(), i);
                MyShare.putSizeNavigation(getContext(), height);
            }
        }

        public void modeDefault() {
            this.modeDefault = true;
            this.tvAction.setText(R.string.set_default_phone);
            this.tvContent.setText(R.string.default_content);
            this.tvTitle.setText(R.string.phone_default);
        }

        public void modePer() {
            this.modeDefault = false;
            this.tvAction.setText(R.string.grant);
            this.tvContent.setText(R.string.per_content);
            this.tvTitle.setText(R.string.permission);
        }
    }


    class ViewOther extends RelativeLayout {
        public ViewOther(Context context) {
            super(context);
        }
    }
}
