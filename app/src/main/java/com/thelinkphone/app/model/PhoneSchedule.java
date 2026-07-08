package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class PhoneSchedule {

    @SerializedName("id")
    private int id;

    @SerializedName("phone_number_normalized")
    private String phoneNumberNormalized;

    @SerializedName("schedule_id")
    private Integer scheduleId;

    @SerializedName("schedule")
    private Schedule schedule;

    @SerializedName("user_schedules")
    private List<UserSchedule> userSchedules;

    public int getId() {
        return id;
    }

    public String getPhoneNumberNormalized() {
        return phoneNumberNormalized;
    }

    public Integer getScheduleId() {
        return scheduleId;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public List<UserSchedule> getUserSchedules() {
        return userSchedules;
    }
}
