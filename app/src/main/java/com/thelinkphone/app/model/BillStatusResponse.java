package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

public class BillStatusResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private Data data;

    public boolean isSuccess() {
        return success;
    }

    public Data getData() {
        return data;
    }

    public static class Data {

        @SerializedName("bill_id")
        private int billId;

        @SerializedName("status")
        private String status;

        @SerializedName("points")
        private int points;

        public int getBillId() {
            return billId;
        }

        public String getStatus() {
            return status;
        }
        public int getPoints() { return points; }
    }
}