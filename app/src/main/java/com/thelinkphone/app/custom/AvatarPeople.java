package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Typeface;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.BaseRequestOptions;
import com.bumptech.glide.request.RequestOptions;
import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.OtherUtils;



public class AvatarPeople extends RelativeLayout {
    private final ImageView im;
    private final TextView tv;

    public AvatarPeople(Context context) {
        super(context);
        ImageView imageView = new ImageView(getContext());
        this.im = imageView;
        TextView textView = new TextView(getContext());
        this.tv = textView;
        textView.setGravity(17);
        textView.setTextColor(-1);
        textView.setBackgroundResource(R.drawable.bg_no_contact);
        textView.setTextSize(0, (OtherUtils.getWidthScreen(context) * 7.0f) / 100.0f);
        textView.setTypeface(Typeface.createFromAsset(context.getAssets(), "fonts/SpotMono_1.otf"));
        addView(imageView, new LayoutParams(-1, -1));
        addView(textView, new LayoutParams(-1, -1));
    }

    public void setImage(String str, String str2) {
        if (str != null && !str.isEmpty()) {
            this.im.setVisibility(View.VISIBLE);
            this.tv.setVisibility(View.GONE);
            Glide.with(getContext()).load(str).apply((BaseRequestOptions<?>) new RequestOptions().circleCrop()).placeholder((int) R.drawable.ic_no_contact).error(R.drawable.ic_no_contact).into(this.im);
            return;
        }
        this.im.setVisibility(View.GONE);
        this.tv.setVisibility(View.VISIBLE);
        if (str2 == null || str2.isEmpty()) {
            this.tv.setText("?");
            return;
        }
        String substring = str2.substring(0, 1);
        if (str2.contains(" ") && str2.indexOf(" ") < str2.length() - 1) {
            substring = substring + str2.substring(str2.indexOf(" ") + 1, str2.indexOf(" ") + 2);
        }
        this.tv.setText(substring.toUpperCase());
    }

    public void setTextSize(float f) {
        this.tv.setTextSize(0, (OtherUtils.getWidthScreen(getContext()) * f) / 100.0f);
    }

    public void addStroke(int i, int i2) {
        setPadding(i, i, i, i);
        setBackground(OtherUtils.bgOval(i2));
    }
}
