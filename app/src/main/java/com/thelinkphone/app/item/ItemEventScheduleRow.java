package com.thelinkphone.app.item;

import com.thelinkphone.app.model.UnifiedScheduleEntry;
import com.thelinkphone.app.utils.ScheduleAvailabilityUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class ItemEventScheduleRow {
    public final UnifiedScheduleEntry entry;
    public final ItemUnifiedSchedule.ScheduleStatus status;

    private ItemEventScheduleRow(UnifiedScheduleEntry entry, ItemUnifiedSchedule.ScheduleStatus status) {
        this.entry = entry;
        this.status = status;
    }

    public static final class Result {
        public final List<ItemEventScheduleRow> rows;
        public final ScheduleAvailabilityUtils.EmptyReason emptyReason;
        private Result(List<ItemEventScheduleRow> rows, ScheduleAvailabilityUtils.EmptyReason emptyReason) {
            this.rows = rows;
            this.emptyReason = emptyReason;
        }
    }

    public static Result from(List<UnifiedScheduleEntry> eventEntries, Calendar targetDate, String filterDateKey) {
        List<ItemEventScheduleRow> rows = new ArrayList<>();

        for (UnifiedScheduleEntry e : eventEntries) {
            if (filterDateKey != null && !filterDateKey.equals(e.getDate())) continue;
            rows.add(new ItemEventScheduleRow(e, ItemUnifiedSchedule.computeStatus(e)));
        }

        // Active first, then upcoming, then completed/cancelled trailing -- same ordering spirit as phone rows
        rows.sort(Comparator.comparingInt(r -> statusRank(r.status)));

        if (rows.isEmpty()) {
            ScheduleAvailabilityUtils.EmptyReason reason = ScheduleAvailabilityUtils.isPastDate(targetDate)
                    ? ScheduleAvailabilityUtils.EmptyReason.PAST_DATE
                    : ScheduleAvailabilityUtils.EmptyReason.NO_SCHEDULE;
            return new Result(rows, reason);
        }
        return new Result(rows, null);
    }

    private static int statusRank(ItemUnifiedSchedule.ScheduleStatus s) {
        switch (s) {
            case ACTIVE: return 0;
            case UPCOMING: return 1;
            case COMPLETED: return 2;
            case CANCELLED: return 3;
            default: return 4;
        }
    }

    public String statusLabel() {
        return status.name().toLowerCase(java.util.Locale.US);
    }
}