package com.thelinkphone.app.item;

import com.google.gson.annotations.SerializedName;


public class ItemPhone {
    @SerializedName("number")
    private String number;
    @SerializedName("type")
    private int type;

    public ItemPhone(String str, int i) {
        this.number = str;
        this.type = i;
        if (str == null) {
            this.number = "";
        }
    }

    public String getNumber() {
        return this.number;
    }

    public void setNumber(String str) {
        this.number = str;
    }

    public int getType() {
        return this.type;
    }

    public void setType(int i) {
        this.type = i;
    }
}
