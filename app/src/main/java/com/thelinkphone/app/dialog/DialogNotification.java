package com.thelinkphone.app.dialog;

import android.animation.LayoutTransition;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.thelinkphone.app.R;
import com.thelinkphone.app.custom.TextW;
import com.thelinkphone.app.utils.OtherUtils;

public class DialogNotification extends BaseDialog {

    private final int title;
    private final int content;
    private final int action;
    private final int iconRes;
    private final int accentColor;
    private final boolean theme;
    private final DialogResult dialogResult;

    // block-caller dialog — unchanged call site, still works
    public DialogNotification(Context context, boolean theme, DialogResult dialogResult) {
        this(context, R.string.block_contact, R.string.content_block, R.string.yes,
                theme, R.drawable.ic_call_block, dialogResult);
    }

    // your existing 4-arg call site — now content actually shows as body text
    public DialogNotification(Context context, int titleRes, int contentRes,
                              boolean theme, DialogResult dialogResult) {
        this(context, titleRes, contentRes, R.string.remove, theme, R.drawable.ic_trash, dialogResult);
    }

    // full control: title, body, button label, icon all explicit
    public DialogNotification(Context context, int titleRes, int contentRes, int actionRes,
                              boolean theme, int iconRes, DialogResult dialogResult) {
        super(context);
        this.title = titleRes;
        this.content = contentRes;
        this.action = actionRes;
        this.theme = theme;
        this.iconRes = iconRes;
        this.dialogResult = dialogResult;
        this.accentColor = Color.parseColor("#FF3B30");
    }

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        int widthScreen = OtherUtils.getWidthScreen(getContext());
        int pad = widthScreen / 25;

        LinearLayout outer = new LinearLayout(getContext());
        outer.setGravity(Gravity.CENTER);

        LinearLayout card = new LinearLayout(getContext());
        card.setLayoutTransition(new LayoutTransition());
        card.setOrientation(LinearLayout.VERTICAL);
        card.setGravity(Gravity.CENTER_HORIZONTAL);
        outer.addView(card, (widthScreen * 8) / 10, -2);
        setContentView(outer);

        // icon in a tinted circle
        FrameLayout iconWrap = new FrameLayout(getContext());
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor(theme ? "#FFE9E9" : "#4A1E1E"));
        iconWrap.setBackground(circle);

        ImageView icon = new ImageView(getContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(accentColor);
        FrameLayout.LayoutParams iconParams = new FrameLayout.LayoutParams(dp(24), dp(24));
        iconParams.gravity = Gravity.CENTER;
        iconWrap.addView(icon, iconParams);

        LinearLayout.LayoutParams iconWrapParams = new LinearLayout.LayoutParams(dp(52), dp(52));
        iconWrapParams.topMargin = pad;
        card.addView(iconWrap, iconWrapParams);

        // title
        TextW tvTitle = new TextW(getContext());
        tvTitle.setupText(600, 4.5f);
        tvTitle.setGravity(Gravity.CENTER);
        tvTitle.setText(title);
        tvTitle.setPadding(pad, pad / 2, pad, 0);
        card.addView(tvTitle, -2, -2);

        // body — this is the fix: content now actually renders as body text
        if (content != 0) {
            TextW tvContent = new TextW(getContext());
            tvContent.setTextColor(Color.parseColor("#8E8E93"));
            tvContent.setText(content);
            tvContent.setupText(400, 3.4f);
            tvContent.setGravity(Gravity.CENTER);
            tvContent.setPadding(pad, pad / 4, pad, pad);
            card.addView(tvContent, -2, -2);
        } else {
            View spacer = new View(getContext());
            card.addView(spacer, -1, pad);
        }

        // action row — two separate pills, not one bar split by a hard divider
        LinearLayout row = new LinearLayout(getContext());
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(pad, 0, pad, pad);
        card.addView(row, new LinearLayout.LayoutParams(-1, -2));

        TextW tvCancel = pillButton(getContext(), R.string.cancel,
                theme ? Color.parseColor("#1C1C1E") : Color.WHITE,
                theme ? Color.parseColor("#F0F0F0") : Color.parseColor("#3A3A3A"));
        tvCancel.setOnClickListener(v -> cancel());

        TextW tvAction = pillButton(getContext(), action, Color.WHITE, accentColor);
        tvAction.setOnClickListener(v -> {
            tvAction.setEnabled(false);
            cancel();
            if (dialogResult != null) dialogResult.onActionClick();
        });

        LinearLayout.LayoutParams leftParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        LinearLayout.LayoutParams rightParams = new LinearLayout.LayoutParams(0, dp(44), 1f);
        rightParams.setMarginStart(dp(10));
        row.addView(tvCancel, leftParams);
        row.addView(tvAction, rightParams);

        card.setBackground(OtherUtils.bgIcon(
                theme ? Color.WHITE : Color.parseColor("#2C2C2C"),
                (widthScreen * 4.0f) / 100.0f));
    }

    private TextW pillButton(Context context, int textRes, int textColor, int bgColor) {
        TextW tv = new TextW(context);
        tv.setupText(500, 3.8f);
        tv.setText(textRes);
        tv.setTextColor(textColor);
        tv.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(bgColor);
        bg.setCornerRadius(dp(100));
        tv.setBackground(bg);
        return tv;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, getContext().getResources().getDisplayMetrics());
    }
}