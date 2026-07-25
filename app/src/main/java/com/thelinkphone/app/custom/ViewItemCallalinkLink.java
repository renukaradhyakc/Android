package com.thelinkphone.app.custom;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.thelinkphone.app.R;
import com.thelinkphone.app.utils.ActionUtils;
import com.thelinkphone.app.utils.OtherUtils;

public class ViewItemCallalinkLink extends RelativeLayout {
    private final TextW tvTitle;
    private final TextW tvLink;
    private final ViewCopyShareIcons icons;
    private String currentLink;
    private String currentName;

    public ViewItemCallalinkLink(Context context) {
        super(context);
        int widthScreen = OtherUtils.getWidthScreen(context) / 25;
        setPadding(widthScreen, widthScreen, widthScreen, widthScreen);

        LinearLayout titleRow = new LinearLayout(context);
        titleRow.setId(View.generateViewId());
        titleRow.setOrientation(LinearLayout.HORIZONTAL);
        addView(titleRow, -1, -2);

        TextW textW = new TextW(context);
        this.tvTitle = textW;
        textW.setText("CallALink - Link");
        textW.setupText(350, 3.2f);
        titleRow.addView(textW, -2, -2);

        LinearLayout valueRow = new LinearLayout(context);
        valueRow.setId(View.generateViewId());
        valueRow.setOrientation(LinearLayout.HORIZONTAL);
        valueRow.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams valueRowParams = new LayoutParams(-1, -2);
        valueRowParams.addRule(RelativeLayout.BELOW, titleRow.getId());
        addView(valueRow, valueRowParams);

        TextW textW2 = new TextW(context);
        this.tvLink = textW2;
        textW2.setupText(400, 3.9f);
        textW2.setTextColor(Color.parseColor("#007AFF"));
        textW2.setSingleLine();
        textW2.setEllipsize(TextUtils.TruncateAt.END);
        textW2.setOnClickListener(v -> openAsDeepLink());
        LinearLayout.LayoutParams tvLinkParams = new LinearLayout.LayoutParams(0, -2, 1f);
        valueRow.addView(textW2, tvLinkParams);

        this.icons = new ViewCopyShareIcons(context);
        icons.setCopyIconColor(Color.parseColor("#007AFF"));
        icons.setOnCopyClick(v -> {
            if (currentLink != null) {
                OtherUtils.copyToClip(getContext(), currentLink, "callalink_link", R.string.link_copied);
            }
        });
        icons.setOnShareClick(v -> {
            if (currentLink != null) {
                String clickableLinkUrl = currentLink;
                String directAppLink = "callalink://call?user=" + currentLink;
                String shareText;
                if (!TextUtils.isEmpty(currentName)) {
                    shareText = "Hi! You can call " + currentName + " directly using CallALink app.\n\n" +
                            "🔗 Click this link to call " + currentName + ": " + clickableLinkUrl + "\n\n" +
                            "If the link doesn't work, copy and paste this in your browser: " + directAppLink + "\n\n" +
                            "Make sure you have CallALink app installed first!\n\n" +
                            "📱 CallALink - Easy calling with QR codes and links!";
                } else {
                    shareText = "Hi! You can call directly using CallALink app.\n\n" +
                            "🔗 Click this link to call: " + clickableLinkUrl + "\n\n" +
                            "If the link doesn't work, copy and paste this in your browser: " + directAppLink + "\n\n" +
                            "Make sure you have CallALink app installed first!\n\n" +
                            "📱 CallALink - Easy calling with QR codes and links!";
                }
                ActionUtils.shareText(getContext(), shareText);
            }
        });
        valueRow.addView(icons, -2, -2);
    }

    public void setLink(String link, String displayName) {
        this.currentLink = (link == null || link.isEmpty()) ? null : link;
        this.currentName = (displayName == null || displayName.trim().isEmpty()) ? null : displayName;
        if (currentLink == null) {
            setVisibility(View.GONE);
            return;
        }
        setVisibility(View.VISIBLE);
        tvLink.setText(currentLink);
        tvLink.setOnLongClickListener(v -> {
            OtherUtils.copyToClip(getContext(), currentLink, "callalink_link", R.string.link_copied);
            return true;
        });
    }

    private void openAsDeepLink() {
        if (currentLink == null) return;
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW,
                    Uri.parse(currentLink));
            getContext().startActivity(intent);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(getContext(), "No app found to open this link", Toast.LENGTH_SHORT).show();
        }
    }
}