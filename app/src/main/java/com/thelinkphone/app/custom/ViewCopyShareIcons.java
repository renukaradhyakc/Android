package com.thelinkphone.app.custom;

import android.content.Context;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.OtherUtils;

public class ViewCopyShareIcons extends LinearLayout {
    private final ImageView imCopy;
    private final ImageView imShare;

    public ViewCopyShareIcons(Context context) {
        super(context);
        setOrientation(LinearLayout.HORIZONTAL);
        int widthScreen = OtherUtils.getWidthScreen(context) / 25;
        int iconSize = widthScreen * 2;
        int iconPadding = widthScreen / 2;

        imCopy = new ImageView(context);
        imCopy.setImageResource(R.drawable.ic_copy);
        imCopy.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        addView(imCopy, iconSize, iconSize);

        imShare = new ImageView(context);
        imShare.setImageResource(R.drawable.ic_share);
        imShare.setPadding(iconPadding, iconPadding, iconPadding, iconPadding);
        LayoutParams shareParams = new LayoutParams(iconSize, iconSize);
        shareParams.setMargins(widthScreen / 4, 0, 0, 0);
        addView(imShare, shareParams);
    }

    public void setOnCopyClick(OnClickListener listener) {
        imCopy.setOnClickListener(listener);
    }

    public void setOnShareClick(OnClickListener listener) {
        imShare.setOnClickListener(listener);
    }

    public void setCopyIconColor(int color) {
        imCopy.setColorFilter(color);
    }
}