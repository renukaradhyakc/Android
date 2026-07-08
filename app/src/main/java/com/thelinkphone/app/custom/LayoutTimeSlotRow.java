package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemTimeSlot;
import com.thelinkphone.app.utils.TimeSlotUtils;

import java.util.List;

public class LayoutTimeSlotRow extends LinearLayout {

    public interface RowActionListener {
        void onDeleteClicked(ItemTimeSlot slot);
        void onTimeChanged();
    }
    private final Context ctx;
    private final ItemTimeSlot slot;
    private final boolean theme;

    public LayoutTimeSlotRow(Context context, ItemTimeSlot slot, boolean theme, boolean interactive, RowActionListener listener) {
        super(context);
        this.ctx = context;
        this.slot = slot;
        this.theme = theme;
        build(interactive, listener);
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics());
    }

    private int timeBoxWidth() {
        int widthScreen = com.thelinkphone.app.utils.OtherUtils.getWidthScreen(ctx);
        return (widthScreen * 29) / 100; // ~26% of screen width per box
    }

    private void build(boolean interactive, RowActionListener listener) {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);
        setPadding(0, dp(6), 0, dp(6));

        int textColor = theme ? Color.parseColor("#1C1C1E") : Color.WHITE;
        int borderColor = theme ? Color.parseColor("#D1D1D6") : Color.parseColor("#5C5C5C");

        TextView tvFrom = timeDropdown(slot.fromTime, textColor, borderColor);
        TextView tvTo = timeDropdown(slot.toTime, textColor, borderColor);

        if (interactive) {
            tvFrom.setOnClickListener(v -> showTimePicker(tvFrom, true, listener));
            tvTo.setOnClickListener(v -> showTimePicker(tvTo, false, listener));
        }

        int boxWidth = timeBoxWidth();
        LayoutParams timeParams = new LayoutParams(boxWidth, LayoutParams.WRAP_CONTENT);

        addView(tvFrom, timeParams);

        TextView dash = new TextView(ctx);
        dash.setText("-");
        dash.setTextColor(textColor);
        dash.setPadding(dp(6), 0, dp(6), 0);
        addView(dash, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        addView(tvTo, timeParams);

        // Only action on a slot row: delete. Add/copy live once per day, above.
        if(interactive) {
            ImageView delete = new ImageView(ctx);
            delete.setImageResource(R.drawable.ic_delete);
            delete.setPadding(dp(8), dp(8), dp(8), dp(8));
            delete.setOnClickListener(v -> listener.onDeleteClicked(slot));
            addView(delete, new LayoutParams(dp(36), dp(36)));
        }
    }

    private TextView timeDropdown(String text, int textColor, int borderColor) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextColor(textColor);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(10), dp(8), dp(10), dp(8));
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setStroke(1, borderColor);
        bg.setCornerRadius(dp(6));
        tv.setBackground(bg);
        return tv;
    }

    private void showTimePicker(TextView target, boolean isFromTime, RowActionListener listener) {
        List<String> options = TimeSlotUtils.getTimeOptions();
        PopupMenu popup = new PopupMenu(ctx, target);
        for (String time : options) popup.getMenu().add(time);
        popup.setOnMenuItemClickListener(item -> {
            String selected = item.getTitle().toString();
            target.setText(selected);
            if (isFromTime) slot.fromTime = selected; else slot.toTime = selected;
            listener.onTimeChanged();
            return true;
        });
        popup.show();
    }
}