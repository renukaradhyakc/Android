package com.thelinkphone.app.utils;

import com.google.gson.JsonObject;
import com.thelinkphone.app.model.BillUploadResponse;
import com.thelinkphone.app.model.Event;
import com.thelinkphone.app.model.EventResponse;
import com.thelinkphone.app.model.LoginResponse;
import com.thelinkphone.app.model.QRResponse;
import com.thelinkphone.app.model.QrRequest;
import com.thelinkphone.app.model.TrialStatusResponse;
import com.thelinkphone.app.model.BillStatusResponse;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

import okhttp3.MultipartBody;
import retrofit2.http.Multipart;
import retrofit2.http.Part;

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
    Call<TrialStatusResponse> getTrialStatus(
            @Query("email") String email
    );

    @FormUrlEncoded
    @POST("trial/start")
    Call<Object> startTrial(
            @Field("email") String email
    );

    @Multipart
    @POST("bills")
    Call<BillUploadResponse> uploadBill(
            @Header("Authorization") String token,
            @Part MultipartBody.Part bill
    );

    @GET("bills/{id}/status")
    Call<BillStatusResponse> getBillStatus(
            @Header("Authorization") String token,
            @Path("id") long billId
    );

}
