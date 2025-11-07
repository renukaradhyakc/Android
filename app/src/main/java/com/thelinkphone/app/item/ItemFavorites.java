package com.thelinkphone.app.item;

import com.google.gson.annotations.SerializedName;


public class ItemFavorites {
    @SerializedName("id")
    public String id;
    @SerializedName("name")
    public String name;
    @SerializedName("number")
    public String number;
    @SerializedName("photo")
    public String photo;
    @SerializedName("type")
    public int type;

    public ItemFavorites() {
    }

    public ItemFavorites(ItemFavorites itemFavorites) {
        this.id = itemFavorites.id;
        this.type = itemFavorites.type;
        this.number = itemFavorites.number;
    }
}
