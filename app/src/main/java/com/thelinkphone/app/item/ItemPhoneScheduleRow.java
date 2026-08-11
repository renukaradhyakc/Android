package com.thelinkphone.app.item;

import com.thelinkphone.app.model.PartyPayload;
import com.thelinkphone.app.model.ScheduleSlot;
import com.thelinkphone.app.model.UnifiedScheduleEntry;
import com.thelinkphone.app.utils.ScheduleAvailabilityUtils;
import com.thelinkphone.app.utils.ScheduleValidationUtils;
import com.thelinkphone.app.utils.TimeFormatUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.List;

public class ItemPhoneScheduleRow {
    public final UnifiedScheduleEntry entry;
    private final String fromTimeRaw;
    private final String toTimeRaw;
    public final boolean active;
    private static final int MAX_NAME_CHARS = 13;

    private ItemPhoneScheduleRow(UnifiedScheduleEntry entry, String fromTimeRaw, String toTimeRaw, boolean active) {
        this.entry = entry;
        this.fromTimeRaw = fromTimeRaw;
        this.toTimeRaw = toTimeRaw;
        this.active = active;
    }

    public static final class Result {
        public final List<ItemPhoneScheduleRow> rows;
        public final ScheduleAvailabilityUtils.EmptyReason emptyReason;
        private Result(List<ItemPhoneScheduleRow> rows, ScheduleAvailabilityUtils.EmptyReason emptyReason) {
            this.rows = rows;
            this.emptyReason = emptyReason;
        }
    }

    public static Result from(List<UnifiedScheduleEntry> phoneEntries, Calendar targetDate) {
        if (ScheduleAvailabilityUtils.isPastDate(targetDate)) {
            return new Result(new ArrayList<>(), ScheduleAvailabilityUtils.EmptyReason.PAST_DATE);
        }

        boolean isToday = ScheduleAvailabilityUtils.isSameDate(targetDate, Calendar.getInstance());
        int targetDow = ScheduleAvailabilityUtils.backendDayOfWeek(targetDate);
        Calendar now = Calendar.getInstance();
        int nowMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);


        List<ItemPhoneScheduleRow> flat = new ArrayList<>();

        boolean hadAnyRangeForDay = false;

        for (UnifiedScheduleEntry e : phoneEntries) {
            if (e.getTimes() == null) continue;

            List<ScheduleSlot> daySlots = new ArrayList<>();

            for (ScheduleSlot slot : e.getTimes()) {
                if (slot.getDayOfWeek() != targetDow) continue;

                int fromMin = ScheduleValidationUtils.toMinutes(slot.getFromTime());
                int toMin = ScheduleValidationUtils.toMinutes(slot.getToTime());
                if (fromMin < 0 || toMin < 0) continue;

                daySlots.add(slot);
            }

            if (daySlots.isEmpty()) continue;

            daySlots.sort(Comparator.comparingInt(s -> ScheduleValidationUtils.toMinutes(s.getFromTime())));

            ScheduleSlot rangeStart = daySlots.get(0);
            ScheduleSlot rangeEnd = daySlots.get(0);
            int rangeEndMin = ScheduleValidationUtils.toMinutes(rangeEnd.getToTime());

            for (int i = 1; i < daySlots.size(); i++) {
                ScheduleSlot next = daySlots.get(i);
                int nextFromMin = ScheduleValidationUtils.toMinutes(next.getFromTime());
                int nextToMin = ScheduleValidationUtils.toMinutes(next.getToTime());

                if (nextFromMin <= rangeEndMin) {
                    if (nextToMin > rangeEndMin) {
                        rangeEnd = next;
                        rangeEndMin = nextToMin;
                    }
                } else {
                    hadAnyRangeForDay = true;
                    addRowIfStillRelevant(flat, e, rangeStart, rangeEnd, isToday, nowMinutes);
                    rangeStart = next;
                    rangeEnd = next;
                    rangeEndMin = nextToMin;
                }
            }
            hadAnyRangeForDay = true;
            addRowIfStillRelevant(flat, e, rangeStart, rangeEnd, isToday, nowMinutes);
        }
        sort(flat, isToday);

        if (flat.isEmpty()) {
            ScheduleAvailabilityUtils.EmptyReason reason = (isToday && hadAnyRangeForDay)
                    ? ScheduleAvailabilityUtils.EmptyReason.TODAY_EXPIRED
                    : ScheduleAvailabilityUtils.EmptyReason.NO_SCHEDULE;
            return new Result(flat, reason);
        }
        return new Result(flat, null);
    }

    private static void addRowIfStillRelevant(List<ItemPhoneScheduleRow> flat, UnifiedScheduleEntry e,
                                              ScheduleSlot rangeStart, ScheduleSlot rangeEnd,
                                              boolean isToday, int nowMinutes) {
        int fromMin = ScheduleValidationUtils.toMinutes(rangeStart.getFromTime());
        int toMin = ScheduleValidationUtils.toMinutes(rangeEnd.getToTime());

        if (isToday && toMin <= nowMinutes) return;

        boolean active = isToday && fromMin <= nowMinutes && nowMinutes < toMin;

        flat.add(new ItemPhoneScheduleRow(e, rangeStart.getFromTime(), rangeEnd.getToTime(), active));
    }

    private static void sort(List<ItemPhoneScheduleRow> flat, boolean isToday) {
        if (!isToday) {
            flat.sort(Comparator.comparingInt(r -> ScheduleValidationUtils.toMinutes(r.fromTimeRaw)));
            return;
        }
        flat.sort((a, b) -> {
            if (a.active != b.active) return a.active ? -1 : 1;
            return a.active
                    ? ScheduleValidationUtils.toMinutes(a.toTimeRaw) - ScheduleValidationUtils.toMinutes(b.toTimeRaw)
                    : ScheduleValidationUtils.toMinutes(a.fromTimeRaw) - ScheduleValidationUtils.toMinutes(b.fromTimeRaw);
        });
    }

    public String scheduleLabel() {
        return entry.usesPreExistingSchedule() && entry.getScheduleName() != null
                ? entry.getScheduleName() : "Custom";
    }

    public String timeLabel() {
        return TimeFormatUtils.toDisplayTime(fromTimeRaw)
                + " \u2013 " + TimeFormatUtils.toDisplayTime(toTimeRaw);
    }

    public String statusLabel() {
        return active ? "active" : "upcoming";
    }

    public boolean isCallalinkUser() {
        PartyPayload party = entry.getOtherParty();
        return party != null && party.isCallalinkUser();
    }

    public String rawNumber() {
        PartyPayload party = entry.getOtherParty();
        return party != null ? party.getPhoneNumber() : null;
    }

    public String nameLabel(android.content.Context context) {
        String name = ItemPartyDisplay.name(context, entry.getOtherParty());
        if (name.length() <= MAX_NAME_CHARS) return name;
        return name.substring(0, MAX_NAME_CHARS) + "...";
    }

    public String linkLabel() {
        return ItemPartyDisplay.link(entry.getOtherParty());
    }

    public String emailLabel() {
        PartyPayload party = entry.getOtherParty();
        return party != null ? party.getEmail() : null;
    }
}
