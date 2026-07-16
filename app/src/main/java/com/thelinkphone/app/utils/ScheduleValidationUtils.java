package com.thelinkphone.app.utils;

import com.thelinkphone.app.item.ItemTimeSlot;
import com.thelinkphone.app.item.ItemWeekDaySchedule;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Single source of truth for time-slot validity across the custom schedule editor.
 * Used both at INPUT time (filtering picker options so invalid states are
 * unreachable) and at SAVE time (final guard before hitting the API, since
 * UI state can theoretically desync from these constraints).
 */
public final class ScheduleValidationUtils {

    public static final class ValidationResult {
        public final boolean valid;
        public final String errorMessage; // null when valid

        private ValidationResult(boolean valid, String errorMessage) {
            this.valid = valid;
            this.errorMessage = errorMessage;
        }

        static ValidationResult ok() {
            return new ValidationResult(true, null);
        }

        static ValidationResult fail(String msg) {
            return new ValidationResult(false, msg);
        }
    }

    /** Converts a time string to minutes-since-midnight. Returns -1 if unparseable. */
    public static int toMinutes(String time) {
        Calendar cal = TimeFormatUtils.parseTimeOnly(time);
        if (cal == null) return -1;
        return cal.get(Calendar.HOUR_OF_DAY) * 60 + cal.get(Calendar.MINUTE);
    }

    private static String minutesToLabel(int minutes) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, minutes / 60);
        cal.set(Calendar.MINUTE, minutes % 60);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(cal.getTime());
    }

    private static List<ItemTimeSlot> sortedSlots(ItemWeekDaySchedule day) {
        List<ItemTimeSlot> sorted = new ArrayList<>(day.slots);
        sorted.sort(Comparator.comparingInt(s -> toMinutes(s.fromTime)));
        return sorted;
    }

    /** The slot immediately before {@code editingSlot} in sorted-by-start-time order, or null. */
    private static ItemTimeSlot findPrevious(ItemWeekDaySchedule day, ItemTimeSlot editingSlot) {
        List<ItemTimeSlot> sorted = sortedSlots(day);
        int idx = sorted.indexOf(editingSlot);
        if (idx <= 0) return null;
        return sorted.get(idx - 1);
    }

    /** The slot immediately after {@code editingSlot} in sorted-by-start-time order, or null. */
    private static ItemTimeSlot findNext(ItemWeekDaySchedule day, ItemTimeSlot editingSlot) {
        List<ItemTimeSlot> sorted = sortedSlots(day);
        int idx = sorted.indexOf(editingSlot);
        if (idx < 0 || idx >= sorted.size() - 1) return null;
        return sorted.get(idx + 1);
    }

    // ---------------------------------------------------------------
    // Input-time filtering: restrict picker options to valid choices
    // ---------------------------------------------------------------

    /**
     * Valid "from" options for {@code editingSlot}: must be >= previous slot's end
     * (back-to-back allowed) and strictly < this slot's own end time.
     */
    public static List<String> getValidFromOptions(List<String> allOptions, ItemWeekDaySchedule day,
                                                   ItemTimeSlot editingSlot) {
        ItemTimeSlot prev = findPrevious(day, editingSlot);
        int lowerBound = prev != null ? toMinutes(prev.toTime) : 0;
        int upperBound = toMinutes(editingSlot.toTime); // exclusive

        List<String> result = new ArrayList<>();
        for (String option : allOptions) {
            int m = toMinutes(option);
            if (m < 0) continue;
            if (lowerBound >= 0 && m < lowerBound) continue;
            if (upperBound >= 0 && m >= upperBound) continue;
            result.add(option);
        }
        return result;
    }

    /**
     * Valid "to" options for {@code editingSlot}: must be strictly > this slot's own
     * start time and <= next slot's start (back-to-back allowed).
     */
    public static List<String> getValidToOptions(List<String> allOptions, ItemWeekDaySchedule day,
                                                 ItemTimeSlot editingSlot) {
        ItemTimeSlot next = findNext(day, editingSlot);
        int lowerBound = toMinutes(editingSlot.fromTime); // exclusive
        int upperBound = next != null ? toMinutes(next.fromTime) : -1; // exclusive-or-equal

        List<String> result = new ArrayList<>();
        for (String option : allOptions) {
            int m = toMinutes(option);
            if (m < 0) continue;
            if (lowerBound >= 0 && m <= lowerBound) continue;
            if (upperBound >= 0 && m > upperBound) continue;
            result.add(option);
        }
        return result;
    }

    /**
     * Suggests a sensible default slot when the user adds a new one (or toggles a day
     * on): starts right after the last existing slot ends, defaults to a 1-hour
     * duration, clipped to the end of the day. Returns null if there's no room left.
     */
    public static ItemTimeSlot suggestNextSlot(ItemWeekDaySchedule day) {
        if (day.slots == null || day.slots.isEmpty()) {
            return new ItemTimeSlot("09:00 AM", "05:00 PM");
        }
        List<ItemTimeSlot> sorted = sortedSlots(day);
        ItemTimeSlot last = sorted.get(sorted.size() - 1);
        int startMinutes = toMinutes(last.toTime);
        if (startMinutes < 0) return null;

        int lastOptionMinutes = 23 * 60 + 45; // 11:45 PM, the last 15-min slot of the day
        int endMinutes = Math.min(startMinutes + 60, lastOptionMinutes);
        if (endMinutes <= startMinutes) return null; // no room left today

        return new ItemTimeSlot(minutesToLabel(startMinutes), minutesToLabel(endMinutes));
    }

    // ---------------------------------------------------------------
    // Save-time validation: defense in depth, never trust UI state alone
    // ---------------------------------------------------------------

    /** Validates a single slot: from must be strictly before to. */
    public static boolean isSlotDurationValid(ItemTimeSlot slot) {
        int from = toMinutes(slot.fromTime);
        int to = toMinutes(slot.toTime);
        return from >= 0 && to >= 0 && from < to;
    }

    /** Validates every slot within one day: parseable, positive duration, no overlaps/duplicates. */
    public static ValidationResult validateDay(ItemWeekDaySchedule day) {
        if (day == null || !day.available) return ValidationResult.ok();

        if (day.slots == null || day.slots.isEmpty()) {
            return ValidationResult.fail(day.label + ": mark as unavailable or add at least one time slot.");
        }

        List<ItemTimeSlot> sorted = sortedSlots(day);

        for (ItemTimeSlot slot : sorted) {
            if (!isSlotDurationValid(slot)) {
                return ValidationResult.fail(day.label + ": end time must be after start time ("
                        + slot.fromTime + " \u2013 " + slot.toTime + ").");
            }
        }

        for (int i = 1; i < sorted.size(); i++) {
            ItemTimeSlot prev = sorted.get(i - 1);
            ItemTimeSlot curr = sorted.get(i);
            int prevTo = toMinutes(prev.toTime);
            int currFrom = toMinutes(curr.fromTime);
            if (currFrom < prevTo) {
                boolean isDuplicate = prev.fromTime.equals(curr.fromTime) && prev.toTime.equals(curr.toTime);
                if (isDuplicate) {
                    return ValidationResult.fail(day.label + ": duplicate time slot ("
                            + curr.fromTime + " \u2013 " + curr.toTime + ").");
                }
                return ValidationResult.fail(day.label + ": overlapping time slots ("
                        + prev.fromTime + "\u2013" + prev.toTime + " and "
                        + curr.fromTime + "\u2013" + curr.toTime + ").");
            }
        }

        return ValidationResult.ok();
    }

    /** Validates the whole week. Returns the first failure found, or ok() if everything passes. */
    public static ValidationResult validateWeek(List<ItemWeekDaySchedule> week) {
        if (week == null) return ValidationResult.ok();

        boolean anyAvailable = false;
        for (ItemWeekDaySchedule day : week) {
            if (day.available) anyAvailable = true;
            ValidationResult result = validateDay(day);
            if (!result.valid) return result;
        }
        if (!anyAvailable) {
            return ValidationResult.fail("Set at least one available day before saving.");
        }
        return ValidationResult.ok();
    }

    private ScheduleValidationUtils() {
    }
}