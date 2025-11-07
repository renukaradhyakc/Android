package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

public class Event {

    @SerializedName("allowed")
    private boolean isWithinTime;

    @SerializedName("eventname")
    private String eventName;

    @SerializedName("slot_time")
    private String slotTime;

    @SerializedName("is_callalink_user")
    private boolean isCallalinkUser;

    @SerializedName("username")
    private String username;

    // --- Getters ---
    public boolean isWithinTime() {
        return isWithinTime;
    }

    public String getEventName() {
        return eventName;
    }

    public String getSlotTime() {
        return slotTime;
    }

    public boolean isCallalinkUser() {
        return isCallalinkUser;
    }

    public String getUsername() {
        return username;
    }

    // --- Setters ---
    public void isWithinTime(boolean allowed) {
        this.isWithinTime = allowed;
    }

    public void setEventName(String eventName) {
        this.eventName = eventName;
    }

    public void setSlotTime(String slotTime) {
        this.slotTime = slotTime;
    }

    public void setCallalinkUser(boolean callalinkUser) {
        isCallalinkUser = callalinkUser;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}