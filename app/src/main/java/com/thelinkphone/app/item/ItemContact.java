package com.thelinkphone.app.item;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;


public class ItemContact {
    @SerializedName("arrPhone")
    private ArrayList<ItemPhone> arrPhone;
    @SerializedName("id")
    private String id;
    @SerializedName("name")
    private String name;
    @SerializedName("photo")
    private String photo;

    public ItemContact(String str, String str2, String str3, ArrayList<ItemPhone> arrayList) {
        this.id = str;
        this.name = str2;
        this.photo = str3;
        this.arrPhone = arrayList;
    }

    public ItemContact(String str, ArrayList<ItemPhone> arrayList) {
        this.id = str;
        this.arrPhone = arrayList;
    }

    public ItemContact(String str) {
        ArrayList<ItemPhone> arrayList = new ArrayList<>();
        this.arrPhone = arrayList;
        arrayList.add(new ItemPhone(str, 0));
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getPhoto() {
        return this.photo;
    }

    public ArrayList<ItemPhone> getArrPhone() {
        if (this.arrPhone == null) {
            this.arrPhone = new ArrayList<>();
        }
        return this.arrPhone;
    }

    public void setName(String str) {
        this.name = str;
    }

    public void setPhoto(String str) {
        this.photo = str;
    }
}
