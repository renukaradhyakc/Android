package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

public class TrialStatusResponse {
    @SerializedName("success")
    public boolean success;

    @SerializedName("has_trial")
    public boolean has_trial;

    @SerializedName("consumed")
    public boolean consumed;

    @SerializedName("active")
    public boolean active;

    @SerializedName("days_remaining")
    public int days_remaining;

    @SerializedName("started_at")
    public String started_at;

    @SerializedName("expires_at")
    public String expires_at;

    public TrialStatusResponse() {}

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public boolean hasTrialRecord() {
        return has_trial;
    }

    public boolean isConsumed() {
        return consumed;
    }

    public boolean isActive() {
        return active;
    }

    public int getDaysRemaining() {
        return days_remaining;
    }

    public String getStartedAt() {
        return started_at;
    }

    public String getExpiresAt() {
        return expires_at;
    }
}
