package com.thelinkphone.app;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;
import androidx.core.view.GravityCompat;

import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.utils.OtherUtils;




public class ActivityPreview extends BaseActivity {


    @Override 
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(new ViewPreviewPro(this));
    }


    public void goPro() {

                ActivityPreview.this.setResult(-1);
                Toast.makeText(ActivityPreview.this, "Done", Toast.LENGTH_SHORT).show();
                ActivityPreview.this.finish();

    }



    public class ViewPreviewPro extends RelativeLayout {
        public ViewPreviewPro(Context context) {
            super(context);
            setBackgroundColor(-1);
            int widthScreen = OtherUtils.getWidthScreen(context);
            int i = (widthScreen * 15) / 100;
            int i2 = (widthScreen / 25) * 3;
            int i3 = i2 / 4;
            ImageView imageView = new ImageView(context);
            imageView.setId(132);
            imageView.setImageResource(R.drawable.ic_back);
            imageView.setPadding(i3, i3, i3, i3);
            imageView.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewPreviewPro.this.m47x3172a7fb(view);
                }
            });
            addView(imageView, i, i);
            int i4 = widthScreen / 4;
            ImageView imageView2 = new ImageView(context);
            imageView2.setId(133);
            imageView2.setImageResource(R.drawable.im_pro);
            LayoutParams layoutParams = new LayoutParams(i4, i4);
            layoutParams.addRule(14);
            layoutParams.addRule(3, imageView.getId());
            addView(imageView2, layoutParams);
            TextW textW = new TextW(context);
            textW.setId(134);
            textW.setText(R.string.start_like_a_pro);
            textW.setTextColor(Color.parseColor("#505050"));
            textW.setGravity(1);
            textW.setupText(600, 6.2f);
            LayoutParams layoutParams2 = new LayoutParams(-1, -2);
            layoutParams2.addRule(3, imageView2.getId());
            int i5 = widthScreen / 100;
            layoutParams2.setMargins(0, i5, 0, 0);
            addView(textW, layoutParams2);
            TextW textW2 = new TextW(context);
            textW2.setId(135);
            textW2.setText(R.string.unlock_all_feature);
            textW2.setTextColor(Color.parseColor("#979797"));
            textW2.setGravity(1);
            textW2.setupText(400, 4.1f);
            LayoutParams layoutParams3 = new LayoutParams(-1, -2);
            layoutParams3.addRule(3, textW.getId());
            layoutParams3.setMargins(0, widthScreen / 150, 0, 0);
            addView(textW2, layoutParams3);
            TextW textW3 = new TextW(context);
            textW3.setId(136);
            textW3.setOnClickListener(new OnClickListener() { 
                @Override 
                public final void onClick(View view) {
                    ViewPreviewPro.this.m48xd3423bc(view);
                }
            });
            textW3.setText("premium");
            textW3.setPadding(0, i3, 0, i3);
            textW3.setGravity(1);
            textW3.setBackgroundResource(R.drawable.sel_tv_action);
            textW3.setTextColor(-1);
            textW3.setupText(600, 4.5f);
            LayoutParams layoutParams4 = new LayoutParams(-1, -2);
            layoutParams4.addRule(12);
            layoutParams4.addRule(14);
            layoutParams4.setMargins(i2, widthScreen / 10, i2, widthScreen / 15);
            addView(textW3, layoutParams4);
            LinearLayout linearLayout = new LinearLayout(context);
            linearLayout.setOrientation(LinearLayout.VERTICAL);
            linearLayout.setGravity(16);
            LayoutParams layoutParams5 = new LayoutParams(-1, -1);
            layoutParams5.addRule(3, textW2.getId());
            layoutParams5.addRule(2, textW3.getId());
            layoutParams5.setMargins(0, 0, 0, widthScreen / 30);
            addView(linearLayout, layoutParams5);
            LinearLayout linearLayout2 = new LinearLayout(context);
            linearLayout2.setOrientation(LinearLayout.HORIZONTAL);
            linearLayout2.setGravity(GravityCompat.END);
            linearLayout2.setPadding(0, 0, widthScreen / 50, i5);
            linearLayout.addView(linearLayout2, -1, -2);
            TextW textW4 = new TextW(context);
            textW4.setText(R.string.basic);
            textW4.setupText(400, 3.5f);
            textW4.setTextColor(Color.parseColor("#909090"));
            textW4.setGravity(1);
            linearLayout2.addView(textW4, (widthScreen * 13) / 100, -2);
            TextW textW5 = new TextW(context);
            textW5.setText(R.string.premium);
            textW5.setupText(400, 3.5f);
            textW5.setTextColor(Color.parseColor("#909090"));
            textW5.setGravity(1);
            linearLayout2.addView(textW5, (widthScreen * 20) / 100, -2);
            ViewItem viewItem = new ViewItem(context);
            viewItem.setData(R.drawable.im_preview_pro, R.string.call_pro, true);
            linearLayout.addView(viewItem, -1, -2);
            ViewItem viewItem2 = new ViewItem(context);
            viewItem2.setData(R.drawable.im_preview_custom, R.string.custom_lock, true);
            linearLayout.addView(viewItem2, -1, -2);
            ViewItem viewItem3 = new ViewItem(context);
            viewItem3.setData(R.drawable.im_preview_all_style, R.string.all_theme, false);
            linearLayout.addView(viewItem3, -1, -2);
            ViewItem viewItem4 = new ViewItem(context);
            viewItem4.setData(R.drawable.im_preview_remove_ads, R.string.remove_ads, false);
            linearLayout.addView(viewItem4, -1, -2);
            ViewItem viewItem5 = new ViewItem(context);
            viewItem5.setData(R.drawable.im_preview_full_feature, R.string.full_feature, false);
            linearLayout.addView(viewItem5, -1, -2);
        }



        public  void m47x3172a7fb(View view) {
            ActivityPreview.this.onBackPressed();
        }



        public  void m48xd3423bc(View view) {
            ActivityPreview.this.goPro();
        }
    }


    class ViewItem extends LinearLayout {
        private final ImageView imBasic;
        private final ImageView imIcon;
        private final TextW tv;

        public ViewItem(Context context) {
            super(context);
            int widthScreen = OtherUtils.getWidthScreen(context);
            setOrientation(LinearLayout.HORIZONTAL);
            setGravity(16);
            int i = (widthScreen * 9) / 100;
            int i2 = widthScreen / 25;
            ImageView imageView = new ImageView(context);
            this.imIcon = imageView;
            LayoutParams layoutParams = new LayoutParams(i, i);
            int i3 = i2 / 2;
            layoutParams.setMargins(i2, i3, i2, i3);
            addView(imageView, layoutParams);
            TextW textW = new TextW(context);
            this.tv = textW;
            textW.setupText(500, 3.6f);
            textW.setTextColor(Color.parseColor("#505050"));
            textW.setSingleLine();
            textW.setEllipsize(TextUtils.TruncateAt.END);
            addView(textW, new LayoutParams(0, -2, 1.0f));
            ImageView imageView2 = new ImageView(context);
            this.imBasic = imageView2;
            int i4 = (widthScreen * 6) / 100;
            addView(imageView2, (widthScreen * 13) / 100, i4);
            ImageView imageView3 = new ImageView(context);
            imageView3.setImageResource(R.drawable.im_done_pro);
            LayoutParams layoutParams2 = new LayoutParams((widthScreen * 20) / 100, i4);
            layoutParams2.setMargins(0, 0, i3, 0);
            addView(imageView3, layoutParams2);
        }

        public void setData(int i, int i2, boolean z) {
            this.imIcon.setImageResource(i);
            this.tv.setText(i2);
            if (z) {
                this.imBasic.setImageResource(R.drawable.im_done_pro);
            } else {
                this.imBasic.setImageResource(R.drawable.im_default_pro);
            }
        }
    }
}
