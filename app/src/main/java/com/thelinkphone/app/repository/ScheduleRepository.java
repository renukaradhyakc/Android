package com.thelinkphone.app.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.thelinkphone.app.model.ScheduleResponse;
import com.thelinkphone.app.utils.ApiClient;
import com.thelinkphone.app.utils.ApiService;
import com.thelinkphone.app.model.ScheduleListResponse;

import retrofit2.Callback;

public class ScheduleRepository {

    private final ApiService apiService;
    private final String bearerToken;

    public ScheduleRepository(Context context) {
        SharedPreferences prefs = context.getSharedPreferences("app_prefs", Context.MODE_PRIVATE);

        String token = prefs.getString("auth_token", null);

        apiService = ApiClient.getClient().create(ApiService.class);
        bearerToken = token != null ? "Bearer " + token : null;
    }

    public void getSchedules(Callback<ScheduleListResponse> callback) {
        if (bearerToken == null) {
            callback.onFailure(null, new Throwable("Authentication token not found"));
            return;
        }

        apiService.getSchedules(bearerToken).enqueue(callback);
    }

    public void getScheduleDetails(int scheduleId, Callback<ScheduleResponse> callback
    ) {
        if (bearerToken == null) {
            callback.onFailure(null, new Throwable("Authentication token not found"));
            return;
        }

        apiService.getScheduleDetails(bearerToken, scheduleId).enqueue(callback);
    }
}