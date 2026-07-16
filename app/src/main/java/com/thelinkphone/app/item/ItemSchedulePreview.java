package com.thelinkphone.app.item;

import android.util.Log;

import com.thelinkphone.app.model.PhoneSchedule;
import com.thelinkphone.app.model.UserSchedule;
import java.util.Calendar;
import java.util.List;

import java.util.ArrayList;

public class ItemSchedulePreview {

    public String scheduleName;
    public String phoneNumber;
    public int selectedDay;

    public ArrayList<ItemScheduleDay> days;

    public static ItemSchedulePreview from(PhoneSchedule schedule) {

        if (schedule == null) return null;

        Log.d("SCHEDULE_DEBUG", "schedule.userSchedules = " + (schedule.getUserSchedules() == null ? "null" : schedule.getUserSchedules().size()));

        Log.d("SCHEDULE_DEBUG", "schedule.schedule = " + (schedule.getSchedule() == null ? "null" : "present"));

        if (schedule.getSchedule() != null) {
            Log.d("SCHEDULE_DEBUG", "schedule.schedule.userSchedules = " + (schedule.getSchedule().getUserSchedules() == null ? "null" : schedule.getSchedule().getUserSchedules().size()));
        }

        ItemSchedulePreview item = new ItemSchedulePreview();
        List<UserSchedule> schedules;

        if (schedule.getSchedule() != null) {
            item.scheduleName = schedule.getSchedule().getScheduleName();
        }
        else {
            item.scheduleName = "Custom Schedule";
        }

        item.days = new ArrayList<>();

        for (int i = 1; i <= 7; i++) {

            ItemScheduleDay day = new ItemScheduleDay();
            day.day = i;
            day.active = false;
            day.selected = false;
            day.slots = new ArrayList<>();

            item.days.add(day);
        }

        if (schedule.getUserSchedules() != null && !schedule.getUserSchedules().isEmpty()) {
            schedules = schedule.getUserSchedules();
            Log.d("SCHEDULE_DEBUG", "Using phone_schedule.user_schedules");
        } else if (schedule.getSchedule() != null &&
                schedule.getSchedule().getUserSchedules() != null) {
            schedules = schedule.getSchedule().getUserSchedules();
            Log.d("SCHEDULE_DEBUG", "Using schedule.user_schedules");
        } else {
            schedules = new ArrayList<>();
            Log.d("SCHEDULE_DEBUG", "No schedules found");
        }

        Log.d("SCHEDULE_DEBUG", "resolved schedules size=" + schedules.size());
        Log.d("SCHEDULE_DEBUG", "Phone schedules count = " + (schedule.getUserSchedules() == null ? "null" : schedule.getUserSchedules().size()));

        Log.d("SCHEDULE_DEBUG", "Nested schedules count = " + (schedule.getSchedule() == null ? "schedule=null" : schedule.getSchedule().getUserSchedules() == null ? "userSchedules=null" : schedule.getSchedule().getUserSchedules().size()));

        Log.d("SCHEDULE_DEBUG", "Using schedules count = " + schedules.size());

        for (UserSchedule us : schedules) {
            Log.d("SCHEDULE_DEBUG", "day=" + us.getDayOfWeek() + " from=" + us.getFromTime() + " to=" + us.getToTime());
            int dayIndex = us.getDayOfWeek() - 1;
            if (dayIndex < 0 || dayIndex >= item.days.size())
                continue;

            ItemScheduleDay day = item.days.get(dayIndex);

            day.active = true;
            day.hasSchedule = true;

            ItemScheduleSlot slot = new ItemScheduleSlot();
            slot.from = us.getFromTime();
            slot.to = us.getToTime();

            day.slots.add(slot);
        }

        for (ItemScheduleDay d : item.days) {
            Log.d("SCHEDULE_DEBUG", "Day " + d.day + " hasSchedule=" + d.hasSchedule + " slots=" + d.slots.size());
        }

        item.selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        return item;
    }


}