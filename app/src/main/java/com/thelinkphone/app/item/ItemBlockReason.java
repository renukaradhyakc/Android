package com.thelinkphone.app.item;

import com.google.gson.annotations.SerializedName;

public class ItemBlockReason {
    @SerializedName("number")
    private String number;
    @SerializedName("timestamp")
    private long timestamp;
    @SerializedName("reason")
    private int reason;

    public ItemBlockReason(String number, long timestamp, int reason) {
        this.number = number;
        this.timestamp = timestamp;
        this.reason = reason;
        if (number == null) {
            this.number = "";
        }
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public int getReason() {
        return this.reason;
    }

    public void setReason(int reason) {
        this.reason = reason;
    }
}