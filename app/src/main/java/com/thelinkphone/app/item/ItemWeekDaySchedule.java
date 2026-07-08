package com.thelinkphone.app.item;

import java.util.ArrayList;

public class ItemWeekDaySchedule {
    public int dayOfWeek;      // 1 = Mon ... 7 = Sun, matches UserSchedule.WEEKDAY
    public String label;       // "MONDAY"
    public boolean available;  // false => show "Unavailable", disable checkbox-off state
    public ArrayList<ItemTimeSlot> slots;

    public ItemWeekDaySchedule(int dayOfWeek, String label) {
        this.dayOfWeek = dayOfWeek;
        this.label = label;
        this.available = false;
        this.slots = new ArrayList<>();
    }
}