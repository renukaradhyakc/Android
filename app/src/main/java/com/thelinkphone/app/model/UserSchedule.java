package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

public class UserSchedule {

    @SerializedName("id")
    private int id;

    @SerializedName("day_of_week")
    private int dayOfWeek;

    @SerializedName("from_time")
    private String fromTime;

    @SerializedName("to_time")
    private String toTime;

    @SerializedName("phone_schedule_id")
    private Integer phoneScheduleId;

    public int getId() {
        return id;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public String getFromTime() {
        return fromTime;
    }

    public String getToTime() {
        return toTime;
    }

    public Integer getPhoneScheduleId() {
        return phoneScheduleId;
    }
}