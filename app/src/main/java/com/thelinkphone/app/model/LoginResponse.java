package com.thelinkphone.app.model;

import com.google.gson.annotations.SerializedName;

public class LoginResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("user")
    private User user;
    @SerializedName("token")
    private String token;

    @SerializedName("domain_url")
    private String domain_url;

    @SerializedName("phone_number")
    private String phone_number;

    @SerializedName("timezone")
    private String timezone;

    public LoginResponse()
    {

    }

    public LoginResponse(String status, User user, String token, String domain_url, String phone_number, String timezone) {
        this.status = status;
        this.user = user;
        this.token = token;
        this.domain_url = domain_url;
        this.phone_number = phone_number;
        this.timezone = timezone;
    }

    public String getDomain_url() {
        return domain_url;
    }

    public void setDomain_url(String domain_url) {
        this.domain_url = domain_url;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getTimezone() { return timezone; }

    public void setTimezone(String timezone) { this.timezone = timezone; }

    // Getters and Setters
}


