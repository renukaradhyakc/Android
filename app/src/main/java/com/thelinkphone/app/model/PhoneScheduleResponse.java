package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

public class PhoneScheduleResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private PhoneSchedule data;

    public boolean isSuccess() {
        return success;
    }

    public PhoneSchedule getData() {
        return data;
    }
}