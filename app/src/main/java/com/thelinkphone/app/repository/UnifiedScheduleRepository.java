package com.thelinkphone.app.repository;

import com.thelinkphone.app.model.UnifiedScheduleResponse;
import com.thelinkphone.app.utils.ApiService;

import retrofit2.Callback;

public class UnifiedScheduleRepository {

    private final ApiService api;
    private final String token;

    public UnifiedScheduleRepository(ApiService api, String token) {
        this.api = api;
        this.token = token;
    }

    public void getUnifiedSchedules(Callback<UnifiedScheduleResponse> callback) {
        api.getUnifiedSchedules(token).enqueue(callback);
    }
}