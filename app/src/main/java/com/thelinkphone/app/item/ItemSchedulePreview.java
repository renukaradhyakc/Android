package com.thelinkphone.app.item;

import com.thelinkphone.app.model.PhoneSchedule;
import com.thelinkphone.app.model.UserSchedule;
import java.util.Calendar;
import com.thelinkphone.app.model.UserSchedule;

import java.util.ArrayList;

public class ItemSchedulePreview {

    public String scheduleName;
    public String phoneNumber;
    public int selectedDay;

    public ArrayList<ItemScheduleDay> days;

    public static ItemSchedulePreview from(PhoneSchedule schedule) {

        if (schedule == null) return null;

        ItemSchedulePreview item = new ItemSchedulePreview();

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

        if (schedule.getUserSchedules() != null) {

            for (UserSchedule us : schedule.getUserSchedules()) {

                int dayIndex = us.getDayOfWeek() - 1;

                if (dayIndex < 0 || dayIndex >= item.days.size())
                    continue;

                ItemScheduleDay day = item.days.get(dayIndex);

                day.active = true;

                ItemScheduleSlot slot = new ItemScheduleSlot();
                slot.from = us.getFromTime();
                slot.to = us.getToTime();

                day.slots.add(slot);
            }
        }

        item.selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_WEEK);

        return item;
    }


}