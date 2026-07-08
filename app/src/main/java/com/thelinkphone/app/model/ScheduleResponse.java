package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

public class ScheduleResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private Schedule data;

    public boolean isSuccess() {
        return success;
    }

    public Schedule getData() {
        return data;
    }
}
