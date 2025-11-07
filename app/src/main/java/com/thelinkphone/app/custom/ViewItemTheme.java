package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import androidx.cardview.widget.CardView;
import com.bumptech.glide.Glide;
import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemTheme;
import com.thelinkphone.app.utils.OtherUtils;



public class ViewItemTheme extends RelativeLayout {
    private final ImageView imThumb;
    private final TextW tvDownload;

    public ViewItemTheme(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int i = widthScreen / 50;
        int i2 = (widthScreen - (i * 6)) / 2;
        CardView cardView = new CardView(context);
        float f = widthScreen;
        cardView.setRadius(f / 50.0f);
        cardView.setCardElevation(f / 80.0f);
        LayoutParams layoutParams = new LayoutParams(i2, (i2 * 580) / 312);
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
        this.tvDownload = textW;
        textW.setText(R.string.download);
        int i3 = (i * 2) / 3;
        textW.setPadding(0, i3, 0, i3);
        textW.setBackground(OtherUtils.bgIcon(-1, i));
        textW.setGravity(1);
        textW.setupText(600, 4.0f);
        LayoutParams layoutParams2 = new LayoutParams((widthScreen * 23) / 100, -2);
        layoutParams2.setMargins(i, i, i, i);
        relativeLayout.addView(textW, layoutParams2);
        TextW textW2 = new TextW(context);
        textW2.setupText(200, 3.0f);
        textW2.setGravity(1);
        textW2.setId(6545);
        textW2.setText(R.string.accept);
        textW2.setTextColor(-1);
        int i4 = i2 / 2;
        LayoutParams layoutParams3 = new LayoutParams(i4, -2);
        layoutParams3.addRule(12);
        int i5 = i2 / 20;
        layoutParams3.setMargins(0, i5, 0, i5);
        relativeLayout.addView(textW2, layoutParams3);
        TextW textW3 = new TextW(context);
        textW3.setupText(200, 3.0f);
        textW3.setGravity(1);
        textW3.setText(R.string.decline);
        textW3.setTextColor(-1);
        LayoutParams layoutParams4 = new LayoutParams(i4, -2);
        layoutParams4.addRule(6, textW2.getId());
        layoutParams4.addRule(21);
        relativeLayout.addView(textW3, layoutParams4);
        ImageView imageView2 = new ImageView(context);
        imageView2.setId(68665);
        imageView2.setImageResource(R.drawable.im_accept);
        imageView2.setScaleType(ImageView.ScaleType.FIT_CENTER);
        int i6 = i2 / 5;
        LayoutParams layoutParams5 = new LayoutParams(i4, i6);
        layoutParams5.addRule(2, textW2.getId());
        relativeLayout.addView(imageView2, layoutParams5);
        ImageView imageView3 = new ImageView(context);
        imageView3.setImageResource(R.drawable.im_decline);
        imageView3.setScaleType(ImageView.ScaleType.FIT_CENTER);
        LayoutParams layoutParams6 = new LayoutParams(i4, i6);
        layoutParams6.addRule(6, imageView2.getId());
        layoutParams6.addRule(21);
        relativeLayout.addView(imageView3, layoutParams6);
        int i7 = (i2 * 14) / 50;
        ImageView imageView4 = new ImageView(context);
        imageView4.setId(986);
        imageView4.setImageResource(R.drawable.im_avatar);
        LayoutParams layoutParams7 = new LayoutParams(i7, i7);
        layoutParams7.addRule(14);
        layoutParams7.setMargins(0, i4 - i7, 0, 0);
        relativeLayout.addView(imageView4, layoutParams7);
        TextW textW4 = new TextW(context);
        textW4.setId(68565551);
        textW4.setupText(600, 3.1f);
        textW4.setTextColor(-1);
        textW4.setText("Zee Avi");
        LayoutParams layoutParams8 = new LayoutParams(-2, -2);
        layoutParams8.addRule(3, imageView4.getId());
        layoutParams8.addRule(14);
        relativeLayout.addView(textW4, layoutParams8);
        TextW textW5 = new TextW(context);
        textW5.setupText(300, 3.1f);
        textW5.setTextColor(-1);
        textW5.setText("012-3456-789");
        LayoutParams layoutParams9 = new LayoutParams(-2, -2);
        layoutParams9.addRule(3, textW4.getId());
        layoutParams8.setMargins(0, widthScreen / 100, 0, 0);
        layoutParams9.addRule(14);
        relativeLayout.addView(textW5, layoutParams9);
    }

    public void setItemTheme(ItemTheme itemTheme, String str) {
        if (itemTheme.getThumb() == null || itemTheme.getThumb().isEmpty()) {
            Glide.with(getContext()).load(itemTheme.getLink()).placeholder((int) R.drawable.im_place_theme).into(this.imThumb);
            if (itemTheme.getLink().equals(str)) {
                this.tvDownload.setText(R.string.applied);
                this.tvDownload.setTextColor(Color.parseColor("#007AFF"));
                return;
            }
            this.tvDownload.setText(R.string.apply);
            this.tvDownload.setTextColor(Color.parseColor("#34C759"));
            return;
        }
        this.tvDownload.setText(R.string.download);
        this.tvDownload.setTextColor(Color.parseColor("#FF8F01"));
        Glide.with(getContext()).load(itemTheme.getThumb()).placeholder((int) R.drawable.im_place_theme).into(this.imThumb);
    }

    public void setDownloadClick(OnClickListener onClickListener) {
        this.tvDownload.setOnClickListener(onClickListener);
    }
}
