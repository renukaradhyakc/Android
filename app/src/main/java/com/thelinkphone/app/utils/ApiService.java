package com.thelinkphone.app.utils;

import com.google.gson.JsonObject;
import com.thelinkphone.app.model.Event;
import com.thelinkphone.app.model.EventResponse;
import com.thelinkphone.app.model.LoginResponse;
import com.thelinkphone.app.model.QRResponse;
import com.thelinkphone.app.model.QrRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface ApiService {
    @FormUrlEncoded
    @POST("authlogin")
    Call<LoginResponse> login(@Field("email") String email, @Field("password") String password);

    @POST("qrscan")
    Call<QRResponse> scanQr(@Body QrRequest qrRequest);

    @FormUrlEncoded
    @POST("login")
    Call<EventResponse> getEventData(@Field("email") String email, @Field("password") String password,@Field("client_date") String clientDate,
                                     @Field("client_time") String clientTime);

    @FormUrlEncoded
    @POST("check-event")
    Call<Event> checkEvent(@Field("email") String email, @Field("password") String password,
                           @Field("caller_number") String callerNumber);

    @GET("trial/status")
    Call<JsonObject> getTrialStatus(
            @Header("Authorization") String token
    );

}
