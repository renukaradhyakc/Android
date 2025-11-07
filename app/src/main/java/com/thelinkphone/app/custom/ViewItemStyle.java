package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewItemStyle extends RelativeLayout {
    private final ImageView imThumb;
    private final TextW tvApply;

    public ViewItemStyle(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 50;
        int i2 = (widthScreen - (i * 6)) / 2;
        CardView cardView = new CardView(context);
        float f = widthScreen;
        cardView.setRadius(f / 50.0f);
        cardView.setCardElevation(f / 80.0f);
        LayoutParams layoutParams = new LayoutParams(i2, (i2 * 480) / 312);
        layoutParams.setMargins(0, i, 0, i);
        layoutParams.addRule(14);
        addView(cardView, layoutParams);
        RelativeLayout relativeLayout = new RelativeLayout(context);
        cardView.addView(relativeLayout, -1, -1);
        ImageView imageView = new ImageView(context);
        this.imThumb = imageView;
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        relativeLayout.addView(imageView, -1, -1);
        TextW textW = new TextW(context);
        this.tvApply = textW;
        textW.setText(R.string.apply);
        int i3 = (i * 2) / 3;
        textW.setPadding(0, i3, 0, i3);
        textW.setBackground(OtherUtils.bgIcon(-1, i));
        textW.setGravity(1);
        textW.setTextColor(-1);
        textW.setupText(600, 4.0f);
        LayoutParams layoutParams2 = new LayoutParams((widthScreen * 25) / 100, -2);
        layoutParams2.setMargins(i, i, i, i);
        relativeLayout.addView(textW, layoutParams2);
    }

    public void setItemTheme(int i, boolean z, boolean z2) {
        Glide.with(getContext()).load(Integer.valueOf(i)).placeholder((int) R.drawable.im_place_theme).into(this.imThumb);
        if (z) {
            this.tvApply.setText(R.string.applied);
            this.tvApply.setTextColor(Color.parseColor("#007AFF"));
        } else if (z2) {
            this.tvApply.setText(R.string.apply);
            this.tvApply.setTextColor(Color.parseColor("#34C759"));
        } else {
            this.tvApply.setText("_premium");
            this.tvApply.setTextColor(Color.parseColor("#FF8F01"));
        }
    }

    public void onApplyClick(OnClickListener onClickListener) {
        this.tvApply.setOnClickListener(onClickListener);
    }

    public void onTop() {
        setPadding(0, OtherUtils.getWidthScreen(getContext()) / 50, 0, 0);
    }
}
