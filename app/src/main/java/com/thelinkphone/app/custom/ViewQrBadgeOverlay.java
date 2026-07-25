package com.thelinkphone.app.custom;

import android.content.Context;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.OtherUtils;

public class ViewQrBadgeOverlay extends RelativeLayout {
    private final ImageView imBadge;

    public ViewQrBadgeOverlay(Context context, View content, int contentSize) {
        super(context);

        LayoutParams contentParams = new LayoutParams(contentSize, contentSize);
        addView(content, contentParams);

        int badgeSize = (int) (contentSize * 0.34f);
        int widthScreen = OtherUtils.getWidthScreen(context);
        int badgePad = widthScreen / 150;

        imBadge = new ImageView(context);
        imBadge.setId(View.generateViewId());
        imBadge.setImageResource(R.drawable.ic_qr_badge);
        imBadge.setBackground(OtherUtils.bgOval(-1));
        imBadge.setPadding(badgePad, badgePad, badgePad, badgePad);
        LayoutParams badgeParams = new LayoutParams(badgeSize, badgeSize);
        badgeParams.addRule(RelativeLayout.ALIGN_PARENT_END);
        badgeParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
        addView(imBadge, badgeParams);

        imBadge.setVisibility(View.GONE);
    }

    public void setOnBadgeClick(OnClickListener listener) {
        imBadge.setOnClickListener(listener);
    }

    public void setBadgeVisible(boolean visible) {
        imBadge.setVisibility(visible ? View.VISIBLE : View.GONE);
    }
}