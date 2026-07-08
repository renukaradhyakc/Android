package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class TimeZoneResponse {

    @SerializedName("timezones")
    private List<String> timezones;

    public List<String> getTimezones() {
        return timezones;
    }

    public void setTimezones(List<String> timezones) {
        this.timezones = timezones;
    }
}