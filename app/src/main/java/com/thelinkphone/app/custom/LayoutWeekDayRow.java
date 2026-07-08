package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemTimeSlot;
import com.thelinkphone.app.item.ItemWeekDaySchedule;

import java.util.ArrayList;

public class LayoutWeekDayRow extends LinearLayout {

    public interface WeekDayRowListener {
        void onCopyRequested(ItemWeekDaySchedule sourceDay);
        void onScheduleChanged();
    }

    private final Context ctx;
    private final ItemWeekDaySchedule day;
    private final boolean theme;
    private final WeekDayRowListener listener;
    private LinearLayout slotsContainer;
    private TextView tvUnavailable;
    private ToggleSwitch dayToggle;
    private LinearLayout actionRow;
    private LinearLayout headerSlotContainer;
    private final boolean interactive;
    private static final int LABEL_WIDTH_DP = 44;

    public LayoutWeekDayRow(Context context, ItemWeekDaySchedule day, boolean theme, WeekDayRowListener listener) {
        this(context, day, theme, true, listener);
    }

    public LayoutWeekDayRow(Context context, ItemWeekDaySchedule day, boolean theme,
                            boolean interactive, WeekDayRowListener listener) {
        super(context);
        this.ctx = context;
        this.day = day;
        this.theme = theme;
        this.interactive = interactive;
        this.listener = listener;
        setOrientation(VERTICAL);
        build();
        render();
    }

    private int dp(int v) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, v, ctx.getResources().getDisplayMetrics());
    }

    private void build() {
        setPadding(0, dp(14), 0, dp(14));

        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        addView(header, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        dayToggle = new ToggleSwitch(ctx, true);
        dayToggle.setInteractive(interactive);
        LayoutParams toggleParams = new LayoutParams(dayToggle.getPreferredWidth(), dayToggle.getPreferredHeight());
        toggleParams.setMarginEnd(dp(12));
        header.addView(dayToggle, toggleParams);
        dayToggle.setOnToggleListener(on -> {
            if (!interactive) return;
            day.available = on;
            if (on && day.slots.isEmpty()) {
                day.slots.add(new ItemTimeSlot("09:00 AM", "05:00 PM"));
            }
            render();
            listener.onScheduleChanged();
        });

        TextView tvLabel = new TextView(ctx);
        tvLabel.setText(day.label); // already abbreviated, e.g. "MON"
        tvLabel.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tvLabel.setTypeface(tvLabel.getTypeface(), android.graphics.Typeface.BOLD);
        tvLabel.setTextColor(theme ? Color.BLACK : Color.WHITE);
        tvLabel.setSingleLine(true);
        LayoutParams labelParams = new LayoutParams(dp(LABEL_WIDTH_DP), LayoutParams.WRAP_CONTENT);
        header.addView(tvLabel, labelParams);

        if (interactive) {
            View spacer = new View(ctx);
            header.addView(spacer, new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f));

            actionRow = new LinearLayout(ctx);
            actionRow.setOrientation(HORIZONTAL);

            addIcon(actionRow, R.drawable.ic_add, () -> {
                day.slots.add(new ItemTimeSlot("09:00 AM", "05:00 PM"));
                render();
                listener.onScheduleChanged();
            });
            addIcon(actionRow, R.drawable.ic_copy, () -> listener.onCopyRequested(day));
            header.addView(actionRow, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
        } else {
            headerSlotContainer = new LinearLayout(ctx);
            headerSlotContainer.setOrientation(HORIZONTAL);
            LayoutParams headerSlotParams = new LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f);
            header.addView(headerSlotContainer, headerSlotParams);
        }

        slotsContainer = new LinearLayout(ctx);
        slotsContainer.setOrientation(VERTICAL);
        LayoutParams slotsParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        slotsParams.leftMargin = dp(LABEL_WIDTH_DP + 34); // aligns under label, past toggle
        slotsParams.topMargin = dp(6);
        addView(slotsContainer, slotsParams);

        tvUnavailable = new TextView(ctx);
        tvUnavailable.setText("Unavailable");
        tvUnavailable.setTextColor(Color.parseColor("#8E8E93"));
        tvUnavailable.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        LayoutParams unavailParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        unavailParams.leftMargin = dp(LABEL_WIDTH_DP + 34);
        addView(tvUnavailable, unavailParams);

        View divider = new View(ctx);
        divider.setBackgroundColor(theme ? Color.parseColor("#E5E5EA") : Color.parseColor("#3A3A3C"));
        LayoutParams dividerParams = new LayoutParams(LayoutParams.MATCH_PARENT, dp(1));
        dividerParams.topMargin = dp(14);
        addView(divider, dividerParams);
    }

    private void addIcon(LinearLayout parent, int drawableRes, Runnable onClick) {
        ImageView icon = new ImageView(ctx);
        icon.setImageResource(drawableRes);
        icon.setPadding(dp(8), dp(8), dp(8), dp(8));
        icon.setOnClickListener(v -> onClick.run());
        parent.addView(icon, new LayoutParams(dp(32), dp(32)));
    }

    private void render() {
        dayToggle.setOn(day.available, false, false);

        boolean hasSlots = day.available && !day.slots.isEmpty();

        if (interactive) {
            actionRow.setVisibility(day.available ? VISIBLE : GONE);
            tvUnavailable.setVisibility(hasSlots ? GONE : VISIBLE);
            slotsContainer.setVisibility(hasSlots ? VISIBLE : GONE);
            slotsContainer.removeAllViews();
            if (!hasSlots) return;

            for (ItemTimeSlot slot : day.slots) {
                slotsContainer.addView(buildSlotRow(slot));
            }
            return;
        }

        headerSlotContainer.removeAllViews();
        slotsContainer.removeAllViews();

        tvUnavailable.setVisibility(hasSlots ? GONE : VISIBLE);

        if (!hasSlots) {
            slotsContainer.setVisibility(GONE);
            return;
        }

        headerSlotContainer.addView(buildSlotRow(day.slots.get(0)));

        if (day.slots.size() > 1) {
            slotsContainer.setVisibility(VISIBLE);
            for (int i = 1; i < day.slots.size(); i++) {
                slotsContainer.addView(buildSlotRow(day.slots.get(i)));
            }
        } else {
            slotsContainer.setVisibility(GONE);
        }
    }

    private LayoutTimeSlotRow buildSlotRow(ItemTimeSlot slot) {
        return new LayoutTimeSlotRow(ctx, slot, theme, interactive,
            new LayoutTimeSlotRow.RowActionListener() {
                @Override
                public void onDeleteClicked(ItemTimeSlot s) {
                    if (!interactive) return;
                    day.slots.remove(s);
                    if (day.slots.isEmpty()) day.available = false;
                    render();
                    listener.onScheduleChanged();
                }

                @Override
                public void onTimeChanged() {
                    if (!interactive) return;
                    listener.onScheduleChanged();
                }
            });
    }

    public void applySchedule(boolean available, ArrayList<ItemTimeSlot> slots) {
        day.available = available;
        day.slots.clear();
        if (slots != null) {
            day.slots.addAll(slots);
        }
        render();
    }

    public void applyCopiedSlots(ArrayList<ItemTimeSlot> sourceSlots) {
        day.available = true;
        day.slots.clear();
        if(sourceSlots != null) {
            for (ItemTimeSlot s : sourceSlots) {
                day.slots.add(new ItemTimeSlot(s.fromTime, s.toTime));
            }
        }
        render();
    }
}