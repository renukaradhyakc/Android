package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.thelinkphone.app.utils.ScheduleAvailabilityUtils;


public class CompactCalendarView extends LinearLayout {

    public interface OnDateSelectedListener {
        void onDateSelected(Calendar date);
    }

    private final Context ctx;
    private final boolean theme;
    private final Calendar shownMonth = Calendar.getInstance();
    private Calendar selectedDate = Calendar.getInstance();
    private OnDateSelectedListener listener;

    private TextView tvMonthYear;
    private final TextView[] dayCells = new TextView[42];
    private final View[] dayDots = new View[42];
    private Set<Integer> scheduleDaysOfWeek = new HashSet<>();
    private boolean todayExpired = false; // true if today's recurring slots have all already passed
    private static final String DOT_COLOR = "#34C759";

    public CompactCalendarView(Context context, boolean theme) {
        super(context);
        this.ctx = context;
        this.theme = theme;
        setOrientation(VERTICAL);
        build();
        render();
    }

    public void setOnDateSelectedListener(OnDateSelectedListener l) {
        this.listener = l;
    }

    public Calendar getSelectedDate() {
        return selectedDate;
    }

    public void setScheduleDays(Set<Integer> daysOfWeek) {
        this.scheduleDaysOfWeek = daysOfWeek != null ? daysOfWeek : new HashSet<>();
        render();
    }

    public void setTodayExpired(boolean expired) {
        this.todayExpired = expired;
        render();
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, value, ctx.getResources().getDisplayMetrics());
    }

    private void build() {
        int textColor = theme ? Color.BLACK : Color.WHITE;
        int mutedColor = theme ? Color.parseColor("#8A8A8E") : Color.parseColor("#B8B8B8");

        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams headerParams = new LayoutParams(LayoutParams.MATCH_PARENT, dp(24));
        addView(header, headerParams);

        TextView tvPrev = smallArrow("‹", textColor);
        tvPrev.setOnClickListener(v -> {
            shownMonth.add(Calendar.MONTH, -1);
            advanceSelectedDateToShownMonth();
            render();
            if (listener != null) listener.onDateSelected(selectedDate);
        });
        header.addView(tvPrev, new LayoutParams(dp(20), LayoutParams.MATCH_PARENT));

        tvMonthYear = new TextView(ctx);
        tvMonthYear.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12);
        tvMonthYear.setTextColor(textColor);
        tvMonthYear.setGravity(Gravity.CENTER);
        tvMonthYear.setSingleLine(true);
        LayoutParams monthParams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
        monthParams.weight = 1;
        header.addView(tvMonthYear, monthParams);

        TextView tvNext = smallArrow("›", textColor);
        tvNext.setOnClickListener(v -> {
            shownMonth.add(Calendar.MONTH, 1);
            advanceSelectedDateToShownMonth();
            render();
            if (listener != null) listener.onDateSelected(selectedDate);
        });
        header.addView(tvNext, new LayoutParams(dp(20), LayoutParams.MATCH_PARENT));

        LinearLayout labelsRow = new LinearLayout(ctx);
        labelsRow.setOrientation(HORIZONTAL);
        LayoutParams labelsParams = new LayoutParams(LayoutParams.MATCH_PARENT, dp(16));
        labelsParams.topMargin = dp(2);
        addView(labelsRow, labelsParams);

        String[] dayLabels = {"S", "M", "T", "W", "T", "F", "S"};
        for (String d : dayLabels) {
            TextView tv = new TextView(ctx);
            tv.setText(d);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
            tv.setTextColor(mutedColor);
            tv.setGravity(Gravity.CENTER);
            LayoutParams p = new LayoutParams(0, LayoutParams.MATCH_PARENT);
            p.weight = 1;
            labelsRow.addView(tv, p);
        }

        int cellIndex = 0;
        for (int row = 0; row < 6; row++) {
            LinearLayout rowLayout = new LinearLayout(ctx);
            rowLayout.setOrientation(HORIZONTAL);
            LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, dp(28));
            addView(rowLayout, rowParams);

            for (int col = 0; col < 7; col++) {
                final int idx = cellIndex;
                FrameLayout cellContainer = new FrameLayout(ctx);
                LayoutParams cellParams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
                cellParams.weight = 1;
                cellParams.setMargins(dp(1), dp(1), dp(1), dp(1));
                rowLayout.addView(cellContainer, cellParams);

                TextView cell = new TextView(ctx);
                cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
                cell.setGravity(Gravity.CENTER);
                cell.setTextColor(textColor);
                cellContainer.addView(cell, new FrameLayout.LayoutParams(
                        FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                dayCells[idx] = cell;

                View dot = new View(ctx);
                GradientDrawable dotBg = new GradientDrawable();
                dotBg.setShape(GradientDrawable.OVAL);
                dotBg.setColor(Color.parseColor(DOT_COLOR));
                dot.setBackground(dotBg);
                dot.setVisibility(View.GONE);
                FrameLayout.LayoutParams dotParams = new FrameLayout.LayoutParams(dp(4), dp(4));
                dotParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                dotParams.bottomMargin = dp(2);
                cellContainer.addView(dot, dotParams);
                dayDots[idx] = dot;

                cellIndex++;
                cellContainer.setOnClickListener(v -> onCellClicked(idx));
            }
        }
    }

    private TextView smallArrow(String symbol, int color) {
        TextView tv = new TextView(ctx);
        tv.setText(symbol);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        tv.setTextColor(color);
        tv.setGravity(Gravity.CENTER);
        tv.setPadding(dp(4), 0, dp(4), 0);
        return tv;
    }

    private void onCellClicked(int idx) {
        Object tag = dayCells[idx].getTag();
        if (!(tag instanceof Calendar)) return;

        Calendar clicked = (Calendar) tag;
        selectedDate = clicked;

        boolean isDifferentMonth = clicked.get(Calendar.YEAR) != shownMonth.get(Calendar.YEAR)
                || clicked.get(Calendar.MONTH) != shownMonth.get(Calendar.MONTH);
        if (isDifferentMonth) {
            shownMonth.setTime(clicked.getTime());
        }
        render();
        if (listener != null) listener.onDateSelected(selectedDate);
    }

    private void advanceSelectedDateToShownMonth() {
        int day = selectedDate.get(Calendar.DAY_OF_MONTH);
        Calendar candidate = (Calendar) shownMonth.clone();
        int maxDay = candidate.getActualMaximum(Calendar.DAY_OF_MONTH);
        candidate.set(Calendar.DAY_OF_MONTH, Math.min(day, maxDay));
        selectedDate = candidate;
    }

    private void render() {
        SimpleDateFormat fmt = new SimpleDateFormat("MMM yyyy", Locale.getDefault());
        tvMonthYear.setText(fmt.format(shownMonth.getTime()));

        Calendar cursor = (Calendar) shownMonth.clone();
        cursor.set(Calendar.DAY_OF_MONTH, 1);
        int firstDayOfWeek = cursor.get(Calendar.DAY_OF_WEEK);
        cursor.add(Calendar.DAY_OF_MONTH, -(firstDayOfWeek - 1));

        int mutedColor = theme ? Color.parseColor("#C7C7CC") : Color.parseColor("#6E6E6E");
        int textColor = theme ? Color.BLACK : Color.WHITE;
        int accent = Color.parseColor("#007AFF");

        for (int i = 0; i < 42; i++) {
            TextView cell = dayCells[i];
            View dot = dayDots[i];
            boolean inCurrentMonth = cursor.get(Calendar.YEAR) == shownMonth.get(Calendar.YEAR)
                    && cursor.get(Calendar.MONTH) == shownMonth.get(Calendar.MONTH);

            cell.setText(String.valueOf(cursor.get(Calendar.DAY_OF_MONTH)));
            cell.setTag(cursor.clone());
            cell.setTextColor(inCurrentMonth ? textColor : mutedColor);

            boolean isSelected = inCurrentMonth
                    && cursor.get(Calendar.YEAR) == selectedDate.get(Calendar.YEAR)
                    && cursor.get(Calendar.DAY_OF_YEAR) == selectedDate.get(Calendar.DAY_OF_YEAR);

            if (isSelected) {
                cell.setBackground(circleDrawable(accent));
                cell.setTextColor(Color.WHITE);
            } else {
                cell.setBackground(null);
            }

            boolean isPastDate = ScheduleAvailabilityUtils.isPastDate(cursor);
            boolean isToday = ScheduleAvailabilityUtils.isSameDate(cursor, Calendar.getInstance());

            boolean hasSchedule = inCurrentMonth
                    && scheduleDaysOfWeek.contains(ScheduleAvailabilityUtils.backendDayOfWeek(cursor))
                    && !isPastDate
                    && !(isToday && todayExpired);

            dot.setVisibility(hasSchedule ? View.VISIBLE : View.GONE);

            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private GradientDrawable circleDrawable(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(color);
        return gd;
    }

    public void setSelectedDate(Calendar date) {
        if (date == null) return;
        selectedDate = (Calendar) date.clone();
        shownMonth.setTime(date.getTime());
        render();
    }
}