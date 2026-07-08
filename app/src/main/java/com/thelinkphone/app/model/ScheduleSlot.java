package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

public class ScheduleSlot {

    @SerializedName("day_of_week")
    private int dayOfWeek;

    @SerializedName("from_time")
    private String fromTime;

    @SerializedName("to_time")
    private String toTime;

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public String getFromTime() {
        return fromTime;
    }

    public String getToTime() {
        return toTime;
    }
}