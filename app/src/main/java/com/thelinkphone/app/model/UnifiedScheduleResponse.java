package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

public class UnifiedScheduleResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private UnifiedScheduleData data;

    public boolean isSuccess() {
        return success;
    }

    public UnifiedScheduleData getData() {
        return data;
    }
}