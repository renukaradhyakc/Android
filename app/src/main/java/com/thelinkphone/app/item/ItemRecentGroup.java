package com.thelinkphone.app.item;

import com.google.gson.annotations.SerializedName;
import com.thelinkphone.app.model.ContactLookupResult;
import com.thelinkphone.app.utils.MyConst;

import java.util.ArrayList;


public class ItemRecentGroup {
    @SerializedName("arrRecent")
    public ArrayList<ItemRecent> arrRecent;
    @SerializedName("country")
    public String country;
    @SerializedName("name")
    public String name;
    @SerializedName("nameType")
    public String nameType;
    @SerializedName("photo")
    public String photo;
    @SerializedName("time")
    public long time;

    public String normalizedNumber;
    public boolean isCallalinkUser;
    public Integer callalinkId;
    public String callalinkFirstName;
    public String callalinkLastName;
    public String callalinkDomainUrl;
    public boolean badgeAnimationPlayed = false;

    public ItemRecentGroup() {
    }

    public ItemRecentGroup(ItemRecent itemRecent, String str, String str2) {
        this.name = str;
        this.photo = str2;
        ArrayList<ItemRecent> arrayList = new ArrayList<>();
        this.arrRecent = arrayList;
        arrayList.add(itemRecent);
        this.time = itemRecent.time;
        this.country = itemRecent.country;
        this.nameType = itemRecent.typeNumber;
    }

    public void addRecent(ItemRecent itemRecent) {
        this.time = Math.max(this.time, itemRecent.time);
        String str = this.country;
        if ((str == null || str.isEmpty()) && itemRecent.country != null && !itemRecent.country.isEmpty()) {
            this.country = itemRecent.country;
        }
        String str2 = this.nameType;
        if (str2 == null || str2.isEmpty()) {
            this.nameType = itemRecent.typeNumber;
        }
        this.arrRecent.add(itemRecent);
    }

    public void applyContactLookup(ContactLookupResult result) {
        this.isCallalinkUser = result != null && result.isUser();
        if (result != null && result.isUser()) {
            this.callalinkId = result.getId();
            this.callalinkFirstName = result.getFirstName();
            this.callalinkLastName = result.getLastName();
            this.callalinkDomainUrl = result.getDomainUrl();
        }
    }

    public String getCallalinkLink() {
        if (!isCallalinkUser || callalinkDomainUrl == null || callalinkDomainUrl.isEmpty()) {
            return null;
        }
        return MyConst.CALL_URL + callalinkDomainUrl;
    }

    public String getDisplayNameForQr() {
        if (name != null && !name.trim().isEmpty()) {
            return name;
        }
        if (callalinkFirstName != null && !callalinkFirstName.trim().isEmpty()) {
            return callalinkFirstName;
        }
        return null;
    }
}
