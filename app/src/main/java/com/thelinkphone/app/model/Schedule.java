package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class Schedule {

    @SerializedName("id")
    private int id;

    @SerializedName("schedule_name")
    private String scheduleName;

    @SerializedName("is_default")
    private boolean isDefault;

    @SerializedName("is_custom")
    private boolean isCustom;

    @SerializedName("slots")
    private List<ScheduleSlot> slots;

    public int getId() {
        return id;
    }
    public String getScheduleName() {
        return scheduleName;
    }

    public boolean isDefault() {
        return isDefault;
    }

    public boolean isCustom() {
        return isCustom;
    }

    public List<ScheduleSlot> getSlots() {
        return slots;
    }
}