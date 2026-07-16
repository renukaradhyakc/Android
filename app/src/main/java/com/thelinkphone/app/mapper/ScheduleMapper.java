package com.thelinkphone.app.mapper;

import com.thelinkphone.app.item.ItemScheduleDay;
import com.thelinkphone.app.item.ItemSchedulePreview;
import com.thelinkphone.app.item.ItemScheduleSlot;
import com.thelinkphone.app.model.PhoneSchedule;
import com.thelinkphone.app.model.PhoneScheduleResponse;
import com.thelinkphone.app.model.UserSchedule;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ScheduleMapper {
    public static ItemSchedulePreview toItem(PhoneScheduleResponse response) {

        List<UserSchedule> schedules;

        if (response == null || !response.isSuccess() || response.getData() == null) {
            return null;
        }

        PhoneSchedule phoneSchedule = response.getData();
        ItemSchedulePreview preview = new ItemSchedulePreview();

        preview.phoneNumber = phoneSchedule.getPhoneNumberNormalized();

        if (phoneSchedule.getSchedule() != null) {
            preview.scheduleName = phoneSchedule.getSchedule().getScheduleName();
        } else {
            preview.scheduleName = "Custom Schedule";
        }

        // Create Monday -> Sunday
        preview.days = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {
            ItemScheduleDay day = new ItemScheduleDay();
            day.day = i;
            day.active = true;
            day.selected = false;
            day.hasSchedule = false;
            day.slots = new ArrayList<>();

            preview.days.add(day);
        }

        // Today selection
        int calendarDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        // Calendar: Sunday=1 ... Saturday=7
        // UI: Monday=0 ... Sunday=6
        int today = switch (calendarDay) {
            case Calendar.MONDAY    -> 1;
            case Calendar.TUESDAY   -> 2;
            case Calendar.WEDNESDAY -> 3;
            case Calendar.THURSDAY  -> 4;
            case Calendar.FRIDAY    -> 5;
            case Calendar.SATURDAY  -> 6;
            case Calendar.SUNDAY    -> 7;
            default -> 1;
        };

        preview.selectedDay = today;
        for (ItemScheduleDay day : preview.days) {
            day.selected = (day.day == today);
        }

        // Slots
        if (phoneSchedule.getUserSchedules() != null && !phoneSchedule.getUserSchedules().isEmpty()) {
            schedules = phoneSchedule.getUserSchedules();
        } else if (phoneSchedule.getSchedule() != null &&
                phoneSchedule.getSchedule().getUserSchedules() != null) {
            schedules = phoneSchedule.getSchedule().getUserSchedules();
        } else {
            schedules = new ArrayList<>();
        }
        for (UserSchedule us : schedules) {
            int backendDay = us.getDayOfWeek();
            if (backendDay < 1 || backendDay > 7) {
                continue;
            }
            ItemScheduleSlot slot = new ItemScheduleSlot();
            slot.from = us.getFromTime();
            slot.to = us.getToTime();
            ItemScheduleDay day = preview.days.get(backendDay - 1);

            day.slots.add(slot);
            day.hasSchedule = true;
        }
        return preview;
    }

    private ScheduleMapper() {
    }
}