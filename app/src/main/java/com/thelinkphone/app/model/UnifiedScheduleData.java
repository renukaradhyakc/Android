package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class UnifiedScheduleData {

    @SerializedName("given_by_me")
    private List<UnifiedScheduleEntry> givenByMe;

    @SerializedName("given_to_me")
    private List<UnifiedScheduleEntry> givenToMe;

    public List<UnifiedScheduleEntry> getGivenByMe() {
        return givenByMe;
    }

    public List<UnifiedScheduleEntry> getGivenToMe() {
        return givenToMe;
    }
}