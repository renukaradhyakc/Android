package com.thelinkphone.app.repository;

import com.thelinkphone.app.model.GenericResponse;
import com.thelinkphone.app.model.PhoneScheduleResponse;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.utils.MyShare;

import java.util.Map;

import retrofit2.Callback;

public class PhoneScheduleRepository {

    private final ApiService api;
    private final String token;

    public PhoneScheduleRepository(ApiService api, String token) {
        this.api = api;
        this.token = token;
    }

    public void getSchedule(String phone, Callback<PhoneScheduleResponse> callback) {
        api.getPhoneSchedule(token, phone).enqueue(callback);
    }

    public void assignExisting(String phone, int scheduleId, Callback<PhoneScheduleResponse> callback) {
        api.assignExisting(token, phone, scheduleId).enqueue(callback);
    }

    public void assignCustom(String phone, Map<String, String> slotFields, Callback<PhoneScheduleResponse> callback) {
        api.assignCustom(token, phone, slotFields).enqueue(callback);
    }

    public void deleteSchedule(String phone, Callback<GenericResponse> callback) {
        api.deleteSchedule(token, phone).enqueue(callback);
    }
}
