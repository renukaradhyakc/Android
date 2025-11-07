package com.thelinkphone.app.item;

import com.google.gson.annotations.SerializedName;


public class ItemTheme {
    @SerializedName("link")
    private String link;
    @SerializedName("name")
    private String name;
    @SerializedName("thumb")
    private String thumb;

    public ItemTheme(String str, String str2, String str3) {
        this.name = str;
        this.link = str2;
        this.thumb = str3;
    }

    public String getName() {
        return this.name;
    }

    public String getLink() {
        return this.link;
    }

    public String getThumb() {
        return this.thumb;
    }

    public void setThumb(String str) {
        this.thumb = str;
    }

    public void setLink(String str) {
        this.link = str;
    }

    public void setName(String str) {
        this.name = str;
    }
}
