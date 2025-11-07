package com.thelinkphone.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.custom.ViewProgress;
import com.thelinkphone.app.utils.OtherUtils;




public class ActivitySplash extends BaseActivity {

    
    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        if (OtherUtils.checkPer(this) || !OtherUtils.checkPermission(this)) {
            startActivity(new Intent(this, ActivityRequestPermission.class));
            finish();
            return;
        }else{
            onEnd();
        }
        setContentView(new ViewSplash(this));
    }

    
    public void onEnd() {
        startActivity(new Intent(this, ActivityHome.class));
        finish();
    }

    
    @Override 
    public void onResume() {
        super.onResume();

    }

    
    @Override 
    public void onPause() {
        super.onPause();

    }

    
    class ViewSplash extends LinearLayout {
        public ViewSplash(Context context) {
            super(context);
            setBackgroundColor(-1);
            setOrientation(LinearLayout.VERTICAL);
            setGravity(17);
            int widthScreen = OtherUtils.getWidthScreen(context);
            ImageView imageView = new ImageView(context);
            imageView.setId(32416);
            imageView.setImageResource(R.drawable.icon200);
            int i = (widthScreen * 3) / 10;
            addView(imageView, i, i);
            TextW textW = new TextW(context);
            textW.setId(5457);
            textW.setupText(700, 7.5f);
            textW.setText(ActivitySplash.this.getString(R.string.call_screen));
            LayoutParams layoutParams = new LayoutParams(-2, -2);
            int i2 = widthScreen / 20;
            layoutParams.setMargins(i2, 0, i2, 0);
            addView(textW, layoutParams);
            TextW textW2 = new TextW(context);
            textW2.setId(6532);
            textW2.setupText(400, 3.6f);
            textW2.setTextColor(Color.parseColor("#B8B8B8"));
            textW2.setText(ActivitySplash.this.getString(R.string.splash_content));
            LayoutParams layoutParams2 = new LayoutParams(-2, -2);
            layoutParams2.setMargins(i2, 0, i2, i2);
            addView(textW2, layoutParams2);
            View viewProgress = new ViewProgress(context);
            LayoutParams layoutParams3 = new LayoutParams(-1, widthScreen / 50);
            layoutParams3.setMargins(i2, 0, i2, i2);
            addView(viewProgress, layoutParams3);
            TextW textW3 = new TextW(context);
            textW3.setupText(400, 3.1f);
            textW3.setText("This action may contain ads");
            textW3.setTextColor(Color.parseColor("#B8B8B8"));
            LayoutParams layoutParams4 = new LayoutParams(-2, -2);
            layoutParams4.setMargins(i2, widthScreen / 5, i2, 0);
            addView(textW3, layoutParams4);
        }
    }
}
