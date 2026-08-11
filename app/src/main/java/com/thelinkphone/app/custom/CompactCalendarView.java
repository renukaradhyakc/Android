package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.widget.LinearLayoutCompat;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.ScheduleAvailabilityUtils;


public class CompactCalendarView extends LinearLayout {

    public interface OnDateSelectedListener {
        void onDateSelected(Calendar date);
    }

    public static class Sizing {
        public final int rowHeightDp;
        public final int headerHeightDp;
        public final int weekdayLabelHeightDp;
        public final int dotSizeDp;
        public final int selectedCircleSizeDp;
        public final float dayTextSp;
        public final float monthTextSp;
        public final float weekdayTextSp;
        public final float arrowTextSp;
        public final int cellMarginDp;
        public final boolean showCardBackground;
        public final String cardBackgroundColorLight;
        public final String cardBackgroundColorDark;
        public final boolean fixedSelectionCircle; // false = old stretched-oval cell background (unchanged); true = real fixed-size circle
        public final boolean boldHeaderText; // false = original thin month/weekday header text (unchanged); true = bold
        public final boolean strokedDots; // false = original plain solid dot (unchanged); true = dot with contrast stroke

        public Sizing(int rowHeightDp, int headerHeightDp, int weekdayLabelHeightDp, int dotSizeDp, int selectedCircleSizeDp, float dayTextSp, float monthTextSp,
                      float weekdayTextSp, float arrowTextSp, int cellMarginDp,
                      boolean showCardBackground, String cardBackgroundColorLight, String cardBackgroundColorDark,
                      boolean fixedSelectionCircle, boolean boldHeaderText, boolean strokedDots) {
            this.rowHeightDp = rowHeightDp;
            this.headerHeightDp = headerHeightDp;
            this.weekdayLabelHeightDp = weekdayLabelHeightDp;
            this.dotSizeDp = dotSizeDp;
            this.selectedCircleSizeDp = selectedCircleSizeDp;
            this.dayTextSp = dayTextSp;
            this.monthTextSp = monthTextSp;
            this.weekdayTextSp = weekdayTextSp;
            this.arrowTextSp = arrowTextSp;
            this.cellMarginDp = cellMarginDp;
            this.showCardBackground = showCardBackground;
            this.cardBackgroundColorLight = cardBackgroundColorLight;
            this.cardBackgroundColorDark = cardBackgroundColorDark;
            this.fixedSelectionCircle = fixedSelectionCircle;
            this.boldHeaderText = boldHeaderText;
            this.strokedDots = strokedDots;
        }

        // Exactly the original hardcoded values — unchanged behavior for existing screens.
        // Exactly the original hardcoded values AND original stretched-oval selection
        // behavior — zero visual change for LayoutSchedulePreview, which relies on this.
        public static final Sizing DEFAULT = new Sizing(
                28, 24, 16, 4, 22, 11f, 12f, 9f, 13f, 1,
                false, null, null, false, false, false);

        // Bigger, more legible preset for full-width screens like the unified schedule view.
        // Toned-down, warmer preset for full-width screens like the unified schedule view.
        // Opts into the real fixed-circle fix since this screen has no existing behavior to protect.
        public static final Sizing FULL_WIDTH = new Sizing(
                36, 26, 18, 6, 23, 13f, 15f, 10.5f, 15f, 2,
                true, "#FFFFFF", "#1E1E1E", true, true, true);

        public static Sizing fullWidth(Context context) {
            float density = context.getResources().getDisplayMetrics().density;
            int widthScreenPx = OtherUtils.getWidthScreen(context);
            int widthDp = (int) (widthScreenPx / density);

            int cardWidthDp = (int) (widthDp * 0.97f);
            int cellWidthDp = cardWidthDp / 7;

            int rowHeightDp = Math.round(cellWidthDp * 0.766f);
            int headerHeightDp = Math.round(cellWidthDp * 0.553f);
            int weekdayLabelHeightDp = Math.round(cellWidthDp * 0.383f);
            int dotSizeDp = Math.round(cellWidthDp * 0.128f);
            int selectedCircleSizeDp = Math.round(cellWidthDp * 0.489f);
            float dayTextSp = cellWidthDp * 0.277f;
            float monthTextSp = dayTextSp + 2f;
            float weekdayTextSp = dayTextSp * 0.78f;
            float arrowTextSp = dayTextSp + 2f;
            int cellMarginDp = Math.max(1, Math.round(cellWidthDp * 0.0425f));

            return new Sizing(rowHeightDp, headerHeightDp, weekdayLabelHeightDp, dotSizeDp, selectedCircleSizeDp,
                    dayTextSp, monthTextSp, weekdayTextSp, arrowTextSp, cellMarginDp,
                    true, "#FFFFFF", "#1E1E1E", true, true, true);
        }
    }

    private final Context ctx;
    private final boolean theme;
    private final Sizing sizing;
    private final Calendar shownMonth = Calendar.getInstance();
    private Calendar selectedDate = Calendar.getInstance();
    private OnDateSelectedListener listener;

    private TextView tvMonthYear;
    private final TextView[] dayCells = new TextView[42];
    private final View[] dayDots = new View[42];
    private final View[] selectionBg = new View[42]; // only populated/used when sizing.fixedSelectionCircle is true
    private Set<Integer> scheduleDaysOfWeek = new HashSet<>();
    private boolean todayExpired = false; // true if today's recurring slots have all already passed
    private static final String DOT_COLOR = "#34C759";

    private Set<String> eventDates = new HashSet<>();
    private final View[] dayDotsEvent = new View[42];
    private String phoneDotColor = DOT_COLOR;       // defaults to old green, unchanged behavior
    private String eventDotColor = "#FF8000";
    private String selectedDateColor = "#007AFF";
    private static final SimpleDateFormat DATE_KEY_FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

    public CompactCalendarView(Context context, boolean theme) {
        this(context, theme, Sizing.DEFAULT);
    }

    public CompactCalendarView(Context context, boolean theme, Sizing sizing) {
        super(context);
        this.ctx = context;
        this.theme = theme;
        this.sizing = sizing;
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

        if (sizing.showCardBackground) {
            String bg = theme ? sizing.cardBackgroundColorLight : sizing.cardBackgroundColorDark;
            if (bg != null) {
                GradientDrawable cardBg = new GradientDrawable();
                cardBg.setColor(Color.parseColor(bg));
                cardBg.setCornerRadius(dp(16));
                setBackground(cardBg);
                setPadding(dp(10), dp(10), dp(10), dp(12));
                setElevation(dp(6));
                setClipToOutline(true);
            }
        }

        LinearLayout header = new LinearLayout(ctx);
        header.setOrientation(HORIZONTAL);
        header.setGravity(Gravity.CENTER_VERTICAL);
        LayoutParams headerParams = new LayoutParams(LayoutParams.MATCH_PARENT, dp(sizing.headerHeightDp));
        addView(header, headerParams);

        TextView tvPrev = smallArrow("‹", textColor);
        tvPrev.setOnClickListener(v -> {
            shownMonth.add(Calendar.MONTH, -1);
            advanceSelectedDateToShownMonth();
            render();
            if (listener != null) listener.onDateSelected(selectedDate);
        });
        header.addView(tvPrev, new LayoutParams(dp(sizing.headerHeightDp-4), LayoutParams.MATCH_PARENT));

        tvMonthYear = new TextView(ctx);
        tvMonthYear.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizing.monthTextSp);
        if (sizing.boldHeaderText) {
            tvMonthYear.setTypeface(tvMonthYear.getTypeface(), Typeface.BOLD);
        }
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
        header.addView(tvNext, new LayoutParams(dp(sizing.headerHeightDp-4), LayoutParams.MATCH_PARENT));

        LinearLayout labelsRow = new LinearLayout(ctx);
        labelsRow.setOrientation(HORIZONTAL);
        LayoutParams labelsParams = new LayoutParams(LayoutParams.MATCH_PARENT, dp(sizing.weekdayLabelHeightDp));
        labelsParams.topMargin = dp(2);
        addView(labelsRow, labelsParams);

        String[] dayLabels = {"S", "M", "T", "W", "T", "F", "S"};
        for (String d : dayLabels) {
            TextView tv = new TextView(ctx);
            tv.setText(d);
            tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizing.weekdayTextSp);
            if (sizing.boldHeaderText) {
                tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
            }
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
            LayoutParams rowParams = new LayoutParams(LayoutParams.MATCH_PARENT, dp(sizing.rowHeightDp));
            addView(rowLayout, rowParams);

            for (int col = 0; col < 7; col++) {
                final int idx = cellIndex;
                FrameLayout cellContainer = new FrameLayout(ctx);
                LayoutParams cellParams = new LayoutParams(0, LayoutParams.MATCH_PARENT);
                cellParams.weight = 1;
                cellParams.setMargins(dp(sizing.cellMarginDp), dp(sizing.cellMarginDp),
                        dp(sizing.cellMarginDp), dp(sizing.cellMarginDp));
                rowLayout.addView(cellContainer, cellParams);

                if (sizing.fixedSelectionCircle) {
                    LinearLayout cellStack = new LinearLayout(ctx);
                    cellStack.setOrientation(LinearLayout.VERTICAL);
                    cellContainer.addView(cellStack, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));

                    FrameLayout numberZone = new FrameLayout(ctx);
                    LinearLayout.LayoutParams numberZoneParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT, 0);
                    numberZoneParams.weight = 1;
                    cellStack.addView(numberZone, numberZoneParams);

                    View selBg = new View(ctx);
                    selBg.setVisibility(View.GONE);
                    FrameLayout.LayoutParams selParams = new FrameLayout.LayoutParams(
                            dp(sizing.selectedCircleSizeDp), dp(sizing.selectedCircleSizeDp));
                    selParams.gravity = Gravity.CENTER;
                    numberZone.addView(selBg, selParams);
                    selectionBg[idx] = selBg;

                    TextView cell = new TextView(ctx);
                    cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizing.dayTextSp);
                    cell.setGravity(Gravity.CENTER);
                    cell.setTextColor(textColor);
                    numberZone.addView(cell, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    dayCells[idx] = cell;


                    LinearLayout dotRow = new LinearLayout(ctx);
                    dotRow.setOrientation(LinearLayout.HORIZONTAL);
                    dotRow.setGravity(Gravity.CENTER);
                    LinearLayout.LayoutParams dotRowParams = new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    dotRowParams.gravity = Gravity.CENTER_HORIZONTAL;
                    dotRowParams.bottomMargin = dp(sizing.cellMarginDp);
                    cellStack.addView(dotRow, dotRowParams);

                    View dotEvent = new View(ctx);
                    dotEvent.setBackground(dotDrawable(eventDotColor));
                    dotEvent.setVisibility(View.GONE);
                    LinearLayout.LayoutParams dotEventParams =
                            new LinearLayout.LayoutParams(dp(sizing.dotSizeDp), dp(sizing.dotSizeDp));
                    dotEventParams.setMarginEnd(dp(sizing.cellMarginDp + 2));
                    dotRow.addView(dotEvent, dotEventParams);
                    dayDotsEvent[idx] = dotEvent;

                    View dot = new View(ctx);
                    dot.setBackground(dotDrawable(phoneDotColor));
                    dot.setVisibility(View.GONE);
                    LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(sizing.dotSizeDp), dp(sizing.dotSizeDp));
                    dotRow.addView(dot, dotParams);
                    dayDots[idx] = dot;
                } else {
                    TextView cell = new TextView(ctx);
                    cell.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizing.dayTextSp);
                    cell.setGravity(Gravity.CENTER);
                    cell.setTextColor(textColor);
                    cellContainer.addView(cell, new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                    dayCells[idx] = cell;

                    LinearLayout dotRow = new LinearLayout(ctx);
                    dotRow.setOrientation(LinearLayout.HORIZONTAL);
                    dotRow.setGravity(Gravity.CENTER);
                    FrameLayout.LayoutParams dotRowParams = new FrameLayout.LayoutParams(
                            FrameLayout.LayoutParams.WRAP_CONTENT, FrameLayout.LayoutParams.WRAP_CONTENT);
                    dotRowParams.gravity = Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL;
                    dotRowParams.bottomMargin = dp(2);
                    cellContainer.addView(dotRow, dotRowParams);

                    View dotEvent = new View(ctx);
                    dotEvent.setBackground(dotDrawable(eventDotColor));
                    dotEvent.setVisibility(View.GONE);
                    LinearLayout.LayoutParams dotEventParams = new LinearLayout.LayoutParams(dp(4), dp(4));
                    dotEventParams.setMarginEnd(dp(3));
                    dotRow.addView(dotEvent, dotEventParams);
                    dayDotsEvent[idx] = dotEvent;

                    View dot = new View(ctx);
                    dot.setBackground(dotDrawable(phoneDotColor));
                    dot.setVisibility(View.GONE);
                    LinearLayout.LayoutParams dotParams = new LinearLayout.LayoutParams(dp(4), dp(4));
                    dotRow.addView(dot, dotParams);
                    dayDots[idx] = dot;
                }

                cellIndex++;
                cellContainer.setOnClickListener(v -> onCellClicked(idx));
            }
        }
    }

    private TextView smallArrow(String symbol, int color) {
        TextView tv = new TextView(ctx);
        tv.setText(symbol);
        tv.setTextSize(TypedValue.COMPLEX_UNIT_SP, sizing.arrowTextSp);
        if (sizing.boldHeaderText) {
            tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        }
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
        int selectedColor = Color.parseColor(selectedDateColor);

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
                if (sizing.fixedSelectionCircle) {
                    cell.setBackground(null);
                    View selBg = selectionBg[i];
                    selBg.setBackground(circleDrawable(selectedColor));
                    selBg.setVisibility(View.VISIBLE);
                } else {
                    cell.setBackground(circleDrawable(selectedColor));
                }
                cell.setTextColor(Color.WHITE);
            } else {
                if (sizing.fixedSelectionCircle && selectionBg[i] != null) {
                    selectionBg[i].setVisibility(View.GONE);
                }
                cell.setBackground(null);
            }

            boolean isPastDate = ScheduleAvailabilityUtils.isPastDate(cursor);
            boolean isToday = ScheduleAvailabilityUtils.isSameDate(cursor, Calendar.getInstance());

            boolean hasSchedule = inCurrentMonth
                    && scheduleDaysOfWeek.contains(ScheduleAvailabilityUtils.backendDayOfWeek(cursor))
                    && !isPastDate
                    && !(isToday && todayExpired);

            dot.setBackground(dotDrawable(phoneDotColor));
            dot.setVisibility(hasSchedule ? View.VISIBLE : View.GONE);

            String dateKey = DATE_KEY_FMT.format(cursor.getTime());
            boolean hasEvent = inCurrentMonth && eventDates.contains(dateKey);
            dayDotsEvent[i].setBackground(dotDrawable(eventDotColor));
            dayDotsEvent[i].setVisibility(hasEvent ? View.VISIBLE : View.GONE);

            int eventDotMarginEnd = sizing.fixedSelectionCircle ? dp(sizing.cellMarginDp + 2) : dp(3);
            LinearLayout.LayoutParams eventDotParams = (LinearLayout.LayoutParams) dayDotsEvent[i].getLayoutParams();
            eventDotParams.setMarginEnd(hasSchedule && hasEvent ? eventDotMarginEnd : 0);
            dayDotsEvent[i].setLayoutParams(eventDotParams);

            cursor.add(Calendar.DAY_OF_MONTH, 1);
        }
    }

    private GradientDrawable circleDrawable(int color) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(color);
        return gd;
    }


    private GradientDrawable dotDrawable(String hexColor) {
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.OVAL);
        gd.setColor(Color.parseColor(hexColor));
        if (sizing.strokedDots) {
            int strokeColor = theme ? Color.WHITE : Color.parseColor("#2C2C2C");
            gd.setStroke(Math.max(1, dp(1) / 2), strokeColor);
        }
        return gd;
    }

    public void setSelectedDate(Calendar date) {
        if (date == null) return;
        selectedDate = (Calendar) date.clone();
        shownMonth.setTime(date.getTime());
        render();
    }

    public void setEventDates(Set<String> dates) {
        this.eventDates = dates != null ? dates : new HashSet<>();
        render();
    }

    public void setPhoneDotColor(String hexColor) {
        this.phoneDotColor = hexColor;
        render();
    }

    public void setEventDotColor(String hexColor) {
        this.eventDotColor = hexColor;
        render();
    }

    public void setSelectedDateColor(String hexColor) {
        this.selectedDateColor = hexColor;
        render();
    }
}