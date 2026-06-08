package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

public class BillUploadResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("data")
    private Data data;

    @SerializedName("error")
    private String error;

    public boolean isSuccess() { return success; }
    public Data getData() { return data; }
    public String getError() { return error; }

    public static class Data {
        @SerializedName("bill_id")
        private int billId;

        @SerializedName("message")
        private String message;

        public int getBillId() { return billId; }
        public String getMessage() { return message; }
    }
}