package com.thelinkphone.app.utils;

import com.thelinkphone.app.item.ItemScheduleDay;
import com.thelinkphone.app.item.ItemScheduleSlot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class ScheduleAvailabilityUtils {
    public enum EmptyReason {
        NO_SCHEDULE,   // this weekday just has no recurring schedule
        PAST_DATE,     // the whole day is already behind us
        TODAY_EXPIRED  // today had a schedule, but every slot has already ended
    }

    public static final class Availability {
        public final List<ItemScheduleSlot> visibleSlots;
        public final EmptyReason emptyReason; // null when visibleSlots is non-empty

        private Availability(List<ItemScheduleSlot> visibleSlots, EmptyReason emptyReason) {
            this.visibleSlots = visibleSlots;
            this.emptyReason = emptyReason;
        }
    }

    public static Availability resolve(ItemScheduleDay day, Calendar targetDate) {
        boolean isPast = isPastDate(targetDate);
        boolean isToday = isSameDate(targetDate, Calendar.getInstance());

        List<ItemScheduleSlot> raw =
                (day != null && day.slots != null) ? day.slots : new ArrayList<>();
        boolean hadRawSlots = !raw.isEmpty();

        if (isPast) {
            return new Availability(new ArrayList<>(), EmptyReason.PAST_DATE);
        }

        List<ItemScheduleSlot> cleaned = sortAndDedupe(raw);

        if (isToday) {
            List<ItemScheduleSlot> future = new ArrayList<>();
            for (ItemScheduleSlot s : cleaned) {
                if (!TimeFormatUtils.isBeforeNow(s.to)) {
                    future.add(s);
                }
            }
            cleaned = future;
        }

        if (cleaned.isEmpty()) {
            EmptyReason reason = (isToday && hadRawSlots)
                    ? EmptyReason.TODAY_EXPIRED
                    : EmptyReason.NO_SCHEDULE;
            return new Availability(cleaned, reason);
        }

        return new Availability(cleaned, null);
    }
    public static boolean allSlotsExpired(List<ItemScheduleSlot> slots) {
        if (slots == null || slots.isEmpty()) return true;
        for (ItemScheduleSlot s : slots) {
            if (!TimeFormatUtils.isBeforeNow(s.to)) return false;
        }
        return true;
    }

    private static List<ItemScheduleSlot> sortAndDedupe(List<ItemScheduleSlot> input) {
        List<ItemScheduleSlot> sorted = new ArrayList<>(input);
        sorted.sort((a, b) -> {
            Calendar ca = TimeFormatUtils.parseTimeOnly(a.from);
            Calendar cb = TimeFormatUtils.parseTimeOnly(b.from);
            if (ca == null || cb == null) return 0;
            return ca.compareTo(cb);
        });

        List<ItemScheduleSlot> result = new ArrayList<>();
        Set<String> seen = new HashSet<>();
        for (ItemScheduleSlot s : sorted) {
            String key = s.from + "|" + s.to;
            if (seen.add(key)) {
                result.add(s);
            }
        }
        return result;
    }

    public static int backendDayOfWeek(Calendar cal) {
        switch (cal.get(Calendar.DAY_OF_WEEK)) {
            case Calendar.MONDAY:    return 1;
            case Calendar.TUESDAY:   return 2;
            case Calendar.WEDNESDAY: return 3;
            case Calendar.THURSDAY:  return 4;
            case Calendar.FRIDAY:    return 5;
            case Calendar.SATURDAY:  return 6;
            case Calendar.SUNDAY:    return 7;
            default: return 1;
        }
    }

    public static boolean isSameDate(Calendar a, Calendar b) {
        return a.get(Calendar.YEAR) == b.get(Calendar.YEAR)
                && a.get(Calendar.DAY_OF_YEAR) == b.get(Calendar.DAY_OF_YEAR);
    }

    public static String dayLabel(int backendDay) {
        return switch (backendDay) {
            case 1 -> "Monday";
            case 2 -> "Tuesday";
            case 3 -> "Wednesday";
            case 4 -> "Thursday";
            case 5 -> "Friday";
            case 6 -> "Saturday";
            case 7 -> "Sunday";
            default -> "";
        };
    }

    public static boolean isPastDate(Calendar date) {
        Calendar today = Calendar.getInstance();
        if (date.get(Calendar.YEAR) != today.get(Calendar.YEAR)) {
            return date.get(Calendar.YEAR) < today.get(Calendar.YEAR);
        }
        return date.get(Calendar.DAY_OF_YEAR) < today.get(Calendar.DAY_OF_YEAR);
    }

    private ScheduleAvailabilityUtils() {
    }
}