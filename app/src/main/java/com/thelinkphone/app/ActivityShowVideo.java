package com.thelinkphone.app;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.screen.ActionAcceptResult;
import com.thelinkphone.app.screen.BackgroundManager;
import com.thelinkphone.app.screen.ios.ViewInComingIOS;
import com.thelinkphone.app.utils.MyConst;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;



public class ActivityShowVideo extends AppCompatActivity {
    private String path;
    private ViewShow viewShow;


    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().getDecorView().setSystemUiVisibility(1280);
        getWindow().addFlags(512);
        getWindow().setNavigationBarColor(0);
        getWindow().setStatusBarColor(0);
        String stringExtra = getIntent().getStringExtra(MyConst.DATA_NAME);
        this.path = stringExtra;
        if (stringExtra == null || stringExtra.isEmpty()) {
            finish();
            return;
        }
        ViewShow viewShow = new ViewShow(this);
        this.viewShow = viewShow;
        setContentView(viewShow);
    }


    @Override 
    public void onResume() {
        super.onResume();
        this.viewShow.getBackgroundManager().onResume();
    }


    @Override 
    public void onPause() {
        super.onPause();
        this.viewShow.getBackgroundManager().onPause();
    }


    @Override 
    public void onDestroy() {
        super.onDestroy();
        this.viewShow.getBackgroundManager().onDestroy();
    }


    class ViewShow extends RelativeLayout {
        private final BackgroundManager backgroundManager;

        public ViewShow(final Context context) {
            super(context);
            setBackgroundColor(-16777216);
            this.backgroundManager = new BackgroundManager(this, ActivityShowVideo.this.path);
            int widthScreen = OtherUtils.getWidthScreen(context);
            int i = widthScreen / 25;
            int i2 = (widthScreen * 20) / 100;
            ImageView imageView = new ImageView(context);
            imageView.setImageResource(R.drawable.icon200);
            imageView.setId(100);
            TextW textW = new TextW(context);
            textW.setupText(600, 6.3f);
            textW.setTextColor(-1);
            textW.setText(R.string.app_name);
            TextW textW2 = new TextW(context);
            textW2.setupText(400, 3.8f);
            textW2.setTextColor(-1);
            textW2.setText(R.string.is_calling);
            LayoutParams layoutParams = new LayoutParams(i2, i2);
            layoutParams.addRule(21);
            int i3 = (widthScreen * 13) / 100;
            layoutParams.setMargins(0, MyShare.getSizeNotification(context) + i3, i, 0);
            addView(imageView, layoutParams);
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
            viewInComingIOS.setViewRoot(this);
            viewInComingIOS.setContentTextSlide(R.string.slide_to_apply);
            viewInComingIOS.setActionScreenResult(new ActionAcceptResult() { 
                @Override 
                public void onReject() {
                }

                @Override 
                public void onAccept() {
                    MyShare.putPhoto(ViewShow.this.getContext(), ActivityShowVideo.this.path);
                    Toast.makeText(context, (int) R.string.done, Toast.LENGTH_SHORT).show();
                    ActivityShowVideo.this.finish();
                }
            });
            LayoutParams layoutParams4 = new LayoutParams(-1, (int) ((widthScreen * 21.2f) / 100.0f));
            layoutParams4.addRule(12);
            int i4 = widthScreen / 8;
            layoutParams4.setMargins(i4, 0, i4, MyShare.getSizeNavigation(context) + (widthScreen / 7));
            addView(viewInComingIOS, layoutParams4);
        }

        public BackgroundManager getBackgroundManager() {
            return this.backgroundManager;
        }
    }
}
