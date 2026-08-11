package com.thelinkphone.app.item;

import com.thelinkphone.app.utils.ScheduleAvailabilityUtils;

import java.util.Calendar;

public final class ItemScheduleEmptyState {

    public static ScheduleAvailabilityUtils.EmptyReason resolve(Calendar targetDate) {
        if (ScheduleAvailabilityUtils.isPastDate(targetDate)) {
            return ScheduleAvailabilityUtils.EmptyReason.PAST_DATE;
        }
        return ScheduleAvailabilityUtils.EmptyReason.NO_SCHEDULE;
    }
    private ItemScheduleEmptyState() {}
}