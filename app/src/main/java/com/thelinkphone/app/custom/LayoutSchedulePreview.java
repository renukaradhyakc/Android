package com.thelinkphone.app.custom;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.thelinkphone.app.R;
import com.thelinkphone.app.item.ItemScheduleDay;
import com.thelinkphone.app.item.ItemSchedulePreview;
import com.thelinkphone.app.item.ItemScheduleSlot;
import com.thelinkphone.app.utils.MyShare;
import com.thelinkphone.app.utils.OtherUtils;
import com.thelinkphone.app.utils.ScheduleAvailabilityUtils;
import com.thelinkphone.app.utils.TimeFormatUtils;
import com.thelinkphone.app.utils.TimeZoneUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class LayoutSchedulePreview extends LinearLayout {

    public interface OnManageScheduleClickListener {
        void onManageScheduleClicked(String phoneNumber);
    }

    // How many complete slot cards should be visible before scrolling.
    // Change this single number to show more/fewer at a glance.
    private static final int VISIBLE_SLOT_COUNT = 3;

    private LinearLayout llSlots;
    private ScrollView slotScrollView;
    private TextView tvSelectedDate;
    private View bottomSpacer;
    private boolean theme;
    private TextView tvScheduleName;
    private CompactCalendarView calendarView;
    private ItemSchedulePreview preview;
    private int calendarHeight = 0;
    private int slotUnitHeight = 0; // one card + its trailing gap
    private TextView tvManageSchedule;
    private OnManageScheduleClickListener manageScheduleListener;
    private String currentPhoneNumber;
    private LinearLayout emptyStateView;
    private TextView tvEmptyTitle;
    private TextView tvEmptySubtitle;
    private View scheduleContentRoot;
    private LinearLayout noScheduleView;



    public LayoutSchedulePreview(Context context) {
        super(context);

        setOrientation(VERTICAL);

        LayoutInflater.from(context)
                .inflate(R.layout.layout_schedule_preview, this, true);
        scheduleContentRoot = getChildAt(0);
        applyTheme(context);
    }

    private void applyTheme(Context context) {
        theme = MyShare.getTheme(context);
        int widthScreen = OtherUtils.getWidthScreen(context);
        float radius = (widthScreen * 3.0f) / 100.0f;

        int cardColor = theme ? Color.WHITE : Color.parseColor("#424141");
        int textColor = theme ? Color.BLACK : Color.WHITE;

        setBackground(OtherUtils.bgIcon(cardColor, radius));

        TextView tvTitle = findViewById(R.id.tvTitle);
        tvTitle.setTextColor(textColor);

        tvScheduleName = findViewById(R.id.tvScheduleName);
        tvScheduleName.setTextColor(Color.parseColor("#007AFF"));

        tvManageSchedule = findViewById(R.id.tvManageSchedule);
        tvManageSchedule.setTextColor(Color.parseColor("#007AFF"));
        tvManageSchedule.setText("Manage Schedule  \u203A");
        tvManageSchedule.setOnClickListener(v -> {
            if (manageScheduleListener != null) {
                manageScheduleListener.onManageScheduleClicked(currentPhoneNumber);
            }
        });

        tvSelectedDate = findViewById(R.id.tvSelectedDate);
        tvSelectedDate.setTextColor(textColor);
        tvSelectedDate.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        tvSelectedDate.setGravity(Gravity.CENTER);

        llSlots = findViewById(R.id.llSlots);
        slotScrollView = findViewById(R.id.slotScrollView);

        emptyStateView = buildEmptyState(context);
        emptyStateView.setVisibility(GONE);

        LinearLayout slotContainer = findViewById(R.id.slotContainer);
        LayoutParams emptyStateParams = new LayoutParams(LayoutParams.MATCH_PARENT, 0);
        slotContainer.addView(emptyStateView, emptyStateParams);

        // Spacer that absorbs leftover space so the column's total height
        // still matches the calendar's height, without stretching the
        // ScrollView itself past N complete cards.
        bottomSpacer = new View(context);
        slotContainer.addView(bottomSpacer, new LayoutParams(LayoutParams.MATCH_PARENT, 0));

        FrameLayout calendarCard = findViewById(R.id.calendarCard);
        calendarView = new CompactCalendarView(context, theme);
        calendarCard.addView(calendarView, new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.WRAP_CONTENT));


        calendarView.setOnDateSelectedListener(date -> {
            if (preview == null || preview.days == null) return;
            int backendDay = ScheduleAvailabilityUtils.backendDayOfWeek(date);
            ItemScheduleDay found = findDay(preview.days, backendDay);
            showDay(found); // no stray second call — this was the bug that undid the correct render
        });

        SimpleDateFormat fmt = new SimpleDateFormat("MMM d, yyyy", Locale.getDefault());
        tvSelectedDate.setText(fmt.format(calendarView.getSelectedDate().getTime()));

        noScheduleView = buildNoScheduleState(context);
        noScheduleView.setVisibility(GONE);
        addView(noScheduleView, new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        // Capture calendar's real rendered height once layout finishes.
        calendarCard.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        int h = calendarCard.getHeight();
                        if (h > 0) {
                            calendarHeight = h;
                            calendarCard.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                            trySyncHeights();
                        }
                    }
                });
    }

    public void setSchedule(ItemSchedulePreview item, String phoneNumber) {

        preview = item;
        currentPhoneNumber = phoneNumber;

        setVisibility(VISIBLE);

        if (item == null) {
            Log.d("SCHEDULE_DEBUG", "Preview is NULL");
            scheduleContentRoot.setVisibility(GONE);
            noScheduleView.setVisibility(VISIBLE);
            return;
        }

        noScheduleView.setVisibility(GONE);
        scheduleContentRoot.setVisibility(VISIBLE);

        tvScheduleName.setText(item.scheduleName);

        if (item.days == null || item.days.isEmpty()) {
            calendarView.setScheduleDays(null);
            calendarView.setTodayExpired(false);
            showDay(null);
            return;
        }

        Set<Integer> scheduleDays = new HashSet<>();
        for (ItemScheduleDay day : item.days) {
            if (day.hasSchedule) {
                scheduleDays.add(day.day);
            }
        }

        calendarView.setScheduleDays(scheduleDays);
        Log.d("SCHEDULE_DEBUG", "Calendar highlighted days = " + scheduleDays);
        int realTodayBackendDay = ScheduleAvailabilityUtils.backendDayOfWeek(Calendar.getInstance());
        ItemScheduleDay realToday = findDay(item.days, realTodayBackendDay);
        boolean todayExpired = realToday == null || ScheduleAvailabilityUtils.allSlotsExpired(realToday.slots);
        calendarView.setTodayExpired(todayExpired);

        ItemScheduleDay selected = findDay(item.days, item.selectedDay);
        if (selected == null) selected = item.days.get(0);

        showDay(selected);
    }

    private void showDay(ItemScheduleDay day) {
        Log.d("SCHEDULE_DEBUG", "showDay called");
        Calendar selectedRealDate = calendarView.getSelectedDate();

        SimpleDateFormat labelFmt = new SimpleDateFormat("d MMM, EEEE", Locale.getDefault());
        tvSelectedDate.setText(labelFmt.format(selectedRealDate.getTime()));

        ScheduleAvailabilityUtils.Availability availability = ScheduleAvailabilityUtils.resolve(day, selectedRealDate);

        Log.d("SCHEDULE_DEBUG", "day=" + (day != null ? day.day : "null")
                + " visibleSlots=" + availability.visibleSlots.size()
                + " emptyReason=" + availability.emptyReason);

        if (availability.emptyReason != null) {
            applyEmptyStateReason(availability.emptyReason);

            llSlots.removeAllViews();
            slotScrollView.setVisibility(GONE);
            emptyStateView.setVisibility(VISIBLE);
            if (bottomSpacer.getLayoutParams() != null) {
                bottomSpacer.getLayoutParams().height = 0;
            }
            bottomSpacer.requestLayout();
            trySyncHeights();
            return;
        }

        List<String[]> displaySlots = new ArrayList<>();
        String timeZone = TimeZoneUtils.getUserTimezone(getContext());
        for (ItemScheduleSlot slot : availability.visibleSlots) {
            String range = TimeFormatUtils.toDisplayTime(slot.from) + " \u2013 " + TimeFormatUtils.toDisplayTime(slot.to);
            displaySlots.add(new String[]{range, timeZone});
        }

        emptyStateView.setVisibility(GONE);
        slotScrollView.setVisibility(VISIBLE);
        bottomSpacer.setVisibility(VISIBLE);

        renderSlots(getContext(), displaySlots);
        trySyncHeights();
    }

    private ItemScheduleDay findDay(List<ItemScheduleDay> days, int backendDay) {
        for (ItemScheduleDay d : days) {
            if (d.day == backendDay) return d;
        }
        return null;
    }

    private void renderSlots(Context context, List<String[]> slots) {
        llSlots.removeAllViews();

        int widthScreen = OtherUtils.getWidthScreen(context);
        int gap = widthScreen / 40;

        for (String[] slot : slots) {
            llSlots.addView(buildSlotCard(context, slot[0], slot[1]));

            View spacer = new View(context);
            llSlots.addView(spacer, new LayoutParams(LayoutParams.MATCH_PARENT, gap));
        }

        llSlots.post(() -> {
            if (llSlots.getChildCount() >= 2) {
                int cardHeight = llSlots.getChildAt(0).getHeight();
                slotUnitHeight = cardHeight + gap;
                trySyncHeights();
            }
        });
    }

    private void trySyncHeights() {
        if (calendarHeight <= 0) return;

        int labelHeight = tvSelectedDate.getHeight();
        LayoutParams scrollParams = (LayoutParams) slotScrollView.getLayoutParams();
        int available = calendarHeight - labelHeight - scrollParams.topMargin;
        if (available <= 0) return;

        if(slotUnitHeight > 0) {
            int desiredViewport = VISIBLE_SLOT_COUNT * slotUnitHeight;
            int viewportHeight = Math.min(desiredViewport, available);
            int leftover = Math.max(0, available - viewportHeight);

            if (scrollParams.height != viewportHeight) {
                scrollParams.height = viewportHeight;
                slotScrollView.setLayoutParams(scrollParams);
            }

            LayoutParams spacerParams = (LayoutParams) bottomSpacer.getLayoutParams();
            if (spacerParams.height != leftover) {
                spacerParams.height = leftover;
                bottomSpacer.setLayoutParams(spacerParams);
            }
        }

        LayoutParams emptyParams = (LayoutParams) emptyStateView.getLayoutParams();
        if (emptyParams.height != available) {
            emptyParams.height = available;
            emptyStateView.setLayoutParams(emptyParams);
        }
    }

    private View buildSlotCard(Context context, String timeRange, String timezone) {
        int widthScreen = OtherUtils.getWidthScreen(context);
        int pad = widthScreen / 40;
        float radius = (widthScreen * 2.5f) / 100.0f;

        int cardBg = theme ? Color.WHITE : Color.parseColor("#4A4A4A");
        int borderColor = theme ? Color.parseColor("#E5E5EA") : Color.parseColor("#5C5C5C");
        int titleColor = theme ? Color.parseColor("#1C1C1E") : Color.WHITE;
        int subtitleColor = theme ? Color.parseColor("#8E8E93") : Color.parseColor("#B8B8B8");

        LinearLayout card = new LinearLayout(context);
        card.setOrientation(HORIZONTAL);
        card.setGravity(Gravity.CENTER_VERTICAL);
        card.setPadding(pad, pad, pad, pad);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(cardBg);
        bg.setCornerRadius(radius);
        bg.setStroke(1, borderColor);
        card.setBackground(bg);
        card.setElevation(theme ? 2f : 0f);

        View dot = new View(context);
        GradientDrawable dotBg = new GradientDrawable();
        dotBg.setShape(GradientDrawable.OVAL);
        dotBg.setColor(Color.parseColor("#007AFF"));
        dot.setBackground(dotBg);
        int dotSize = (int) (widthScreen * 0.018f);
        LayoutParams dotParams = new LayoutParams(dotSize, dotSize);
        dotParams.setMarginEnd(pad);
        card.addView(dot, dotParams);

        LinearLayout textCol = new LinearLayout(context);
        textCol.setOrientation(VERTICAL);
        textCol.setGravity(Gravity.CENTER_HORIZONTAL);
        card.addView(textCol, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        TextView tvTime = new TextView(context);
        tvTime.setText(timeRange);
        tvTime.setTextColor(titleColor);
        tvTime.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10.5f);
        tvTime.setTypeface(tvTime.getTypeface(), Typeface.BOLD);
        tvTime.setSingleLine(true);
        tvTime.setEllipsize(TextUtils.TruncateAt.END);
        textCol.addView(tvTime, new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));

        TextView tvTimezone = new TextView(context);
        tvTimezone.setText(timezone);
        tvTimezone.setTextColor(subtitleColor);
        tvTimezone.setTextSize(TypedValue.COMPLEX_UNIT_SP, 9);
        tvTimezone.setSingleLine(true);
        tvTimezone.setEllipsize(TextUtils.TruncateAt.END);
        LayoutParams tzParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        tzParams.topMargin = pad / 6;
        textCol.addView(tvTimezone, tzParams);

        card.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));

        return card;
    }

    public void setOnManageScheduleClickListener(OnManageScheduleClickListener listener) {
        this.manageScheduleListener = listener;
    }

    private void applyEmptyStateReason(ScheduleAvailabilityUtils.EmptyReason reason) {
        switch (reason) {
            case PAST_DATE:
                tvEmptyTitle.setText("Nothing to show here");
                tvEmptySubtitle.setText("This date is in the past.");
                break;
            case TODAY_EXPIRED:
                tvEmptyTitle.setText("No more slots today");
                tvEmptySubtitle.setText("All of today's time slots have already ended.");
                break;
            case NO_SCHEDULE:
            default:
                tvEmptyTitle.setText("No schedule available");
                tvEmptySubtitle.setText("This day has no available time slots.");
                break;
        }
    }

    private LinearLayout buildEmptyState(Context context) {

        int textColor = theme ? Color.parseColor("#1C1C1E") : Color.WHITE;
        int secondary = theme ? Color.parseColor("#8E8E93") : Color.parseColor("#B8B8B8");

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(VERTICAL);
        root.setGravity(Gravity.CENTER);

        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.ic_no_schedule);
        icon.setColorFilter(secondary, android.graphics.PorterDuff.Mode.SRC_IN);
        LayoutParams iconParams = new LayoutParams(dp(28), dp(28));
        icon.setLayoutParams(iconParams);
        root.addView(icon);

        TextView title = new TextView(context);
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setTextColor(textColor);
        title.setGravity(Gravity.CENTER);
        tvEmptyTitle = title;

        LayoutParams titleParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(8);
        titleParams.gravity = Gravity.CENTER_HORIZONTAL;

        root.addView(title, titleParams);

        TextView subtitle = new TextView(context);
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        subtitle.setTextColor(secondary);
        subtitle.setGravity(Gravity.CENTER);
        tvEmptySubtitle = subtitle;

        LayoutParams subParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dp(4);
        subParams.gravity = Gravity.CENTER_HORIZONTAL;
        root.addView(subtitle, subParams);

        applyEmptyStateReason(ScheduleAvailabilityUtils.EmptyReason.NO_SCHEDULE);

        return root;
    }

    private LinearLayout buildNoScheduleState(Context context) {

        int textColor = theme ? Color.parseColor("#1C1C1E") : Color.WHITE;
        int secondary = theme ? Color.parseColor("#8E8E93") : Color.parseColor("#B8B8B8");

        LinearLayout root = new LinearLayout(context);
        root.setOrientation(VERTICAL);
        root.setGravity(Gravity.CENTER);
        root.setPadding(dp(8), dp(20), dp(8), dp(20));

        ImageView icon = new ImageView(context);
        icon.setImageResource(R.drawable.ic_no_schedule);
        icon.setColorFilter(secondary, android.graphics.PorterDuff.Mode.SRC_IN);
        root.addView(icon, new LayoutParams(dp(28), dp(28)));

        TextView title = new TextView(context);
        title.setText("No schedule set");
        title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        title.setTypeface(title.getTypeface(), Typeface.BOLD);
        title.setTextColor(textColor);
        title.setGravity(Gravity.CENTER);
        LayoutParams titleParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        titleParams.topMargin = dp(8);
        root.addView(title, titleParams);

        TextView subtitle = new TextView(context);
        subtitle.setText("Set up calling hours for this number.");
        subtitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11);
        subtitle.setTextColor(secondary);
        subtitle.setGravity(Gravity.CENTER);
        LayoutParams subParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        subParams.topMargin = dp(4);
        root.addView(subtitle, subParams);

        TextView cta = new TextView(context);
        cta.setText("Add Schedule  \u203A");
        cta.setTextColor(Color.parseColor("#007AFF"));
        cta.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        cta.setTypeface(cta.getTypeface(), Typeface.BOLD);
        cta.setGravity(Gravity.CENTER);
        cta.setOnClickListener(v -> {
            if (manageScheduleListener != null) {
                manageScheduleListener.onManageScheduleClicked(currentPhoneNumber);
            }
        });
        LayoutParams ctaParams = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        ctaParams.topMargin = dp(14);
        root.addView(cta, ctaParams);

        return root;
    }

    private int dp(int value) {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                value,
                getResources().getDisplayMetrics()
        );
    }
}