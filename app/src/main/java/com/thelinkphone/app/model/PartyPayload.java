package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

public class PartyPayload {
    @SerializedName("name")
    private String name;

    @SerializedName("first_name")
    private String firstName;

    @SerializedName("last_name")
    private String lastName;

    @SerializedName("phone_number")
    private String phoneNumber;

    @SerializedName("email")
    private String email;

    @SerializedName("domain_url")
    private String domainUrl;

    @SerializedName("is_callalink_user")
    private boolean isCallalinkUser;

    public String getName() { return name; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
    public String getDomainUrl() { return domainUrl; }
    public boolean isCallalinkUser() { return isCallalinkUser; }
}