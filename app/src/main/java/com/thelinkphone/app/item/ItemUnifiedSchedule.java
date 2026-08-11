package com.thelinkphone.app.item;

import com.thelinkphone.app.model.ScheduleSlot;
import com.thelinkphone.app.model.UnifiedScheduleEntry;
import com.thelinkphone.app.utils.TimeFormatUtils;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ItemUnifiedSchedule {
    public enum ScheduleStatus { ACTIVE, UPCOMING, COMPLETED, CANCELLED }
    public final List<UnifiedScheduleEntry> phoneEntries = new ArrayList<>();
    public final List<UnifiedScheduleEntry> eventEntries = new ArrayList<>();

    // for CompactCalendarView blue dot -- weekday-recurring, same idea as setScheduleDays()
    public final Set<Integer> phoneDaysOfWeek = new HashSet<>();

    // for CompactCalendarView orange dot -- exact dates, format yyyy-MM-dd (matches entry.getDate())
    public final Set<String> eventDates = new HashSet<>();

    public static ItemUnifiedSchedule from(List<UnifiedScheduleEntry> entries) {
        ItemUnifiedSchedule item = new ItemUnifiedSchedule();
        if (entries == null) return item;

        for (UnifiedScheduleEntry entry : entries) {
            if (entry.isPhone()) {
                item.phoneEntries.add(entry);
                List<ScheduleSlot> times = entry.getTimes();
                if (times != null) {
                    for (ScheduleSlot slot : times) {
                        // ASSUMPTION: ScheduleSlot has getDayOfWeek() returning the same
                        // backend day-of-week int ScheduleAvailabilityUtils.backendDayOfWeek() produces.
                        // Correct the method name below if it's different.
                        item.phoneDaysOfWeek.add(slot.getDayOfWeek());
                    }
                }
            } else if (entry.isEvent()) {
                item.eventEntries.add(entry);
                if (entry.getDate() != null) {
                    item.eventDates.add(entry.getDate());
                }
            }
        }
        return item;
    }

    private static final SimpleDateFormat DATE_FMT = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
    private static final SimpleDateFormat TIME_FMT = new SimpleDateFormat("hh:mm a", Locale.US);

    public static ScheduleStatus computeStatus(UnifiedScheduleEntry entry) {
        if (entry.getStatus() != null && "cancelled".equalsIgnoreCase(entry.getStatus().trim())) {
            return ScheduleStatus.CANCELLED;
        }

        String date = entry.getDate();
        String slotTime = entry.getSlotTime(); // e.g. "12:50 AM - 12:55 AM"
        if (date == null || slotTime == null || !slotTime.contains("-")) {
            return ScheduleStatus.UPCOMING; // can't determine -- default to a safe, visible state
        }

        String[] parts = slotTime.split("-");
        if (parts.length != 2) return ScheduleStatus.UPCOMING;

        Calendar start = TimeFormatUtils.combineDateAndTime(date, parts[0].trim());
        Calendar end = TimeFormatUtils.combineDateAndTime(date, parts[1].trim());
        if (start == null || end == null) return ScheduleStatus.UPCOMING;

        Calendar now = Calendar.getInstance();
        if (now.after(end)) return ScheduleStatus.COMPLETED;
        if (now.before(start)) return ScheduleStatus.UPCOMING;
        return ScheduleStatus.ACTIVE;
    }
}