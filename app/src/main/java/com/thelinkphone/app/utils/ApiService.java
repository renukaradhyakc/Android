package com.thelinkphone.app.utils;

import com.thelinkphone.app.model.BillUploadResponse;
import com.thelinkphone.app.model.Event;
import com.thelinkphone.app.model.EventResponse;
import com.thelinkphone.app.model.GenericResponse;
import com.thelinkphone.app.model.LoginResponse;
import com.thelinkphone.app.model.PhoneScheduleResponse;
import com.thelinkphone.app.model.QRResponse;
import com.thelinkphone.app.model.QrRequest;
import com.thelinkphone.app.model.ScheduleListResponse;
import com.thelinkphone.app.model.ScheduleResponse;
import com.thelinkphone.app.model.TimeZoneResponse;
import com.thelinkphone.app.model.TrialStatusResponse;
import com.thelinkphone.app.model.BillStatusResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
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
    Call<QRResponse> scanQr(@Header("Authorization") String token,@Body QrRequest qrRequest);

    @FormUrlEncoded
    @POST("login")
    Call<EventResponse> getEventData(@Field("email") String email, @Field("password") String password,@Field("client_date") String clientDate,
                                     @Field("client_time") String clientTime);

    @FormUrlEncoded
    @POST("call-permission")
    Call<Event> checkEvent(@Header("Authorization") String token,
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

    @GET("phone-schedules/{phoneNumber}")
    Call<PhoneScheduleResponse> getPhoneSchedule(
            @Header("Authorization") String token,
            @Path("phoneNumber") String phoneNumber
    );

    @FormUrlEncoded
    @POST("phone-schedules/existing")
    Call<PhoneScheduleResponse> assignExisting(
            @Header("Authorization") String token,
            @Field("phone_number") String phoneNumber,
            @Field("schedule_id") int scheduleId
    );

    @FormUrlEncoded
    @POST("phone-schedules/custom")
    Call<PhoneScheduleResponse> assignCustom(
            @Header("Authorization") String token,
            @Field("phone_number") String phoneNumber,
            @FieldMap Map<String, String> slotFields
    );

    @FormUrlEncoded
    @PUT("phone-schedules")
    Call<PhoneScheduleResponse> updateSchedule(
            @Header("Authorization") String token,
            @Field("phone_number") String phoneNumber,
            @Field("schedule_id") Integer scheduleId,
            @Field("slots") String slotsJson
    );

    @DELETE("phone-schedules/{phone}")
    Call<GenericResponse> deleteSchedule(
            @Header("Authorization") String token,
            @Path("phone") String phoneNumber
    );

    @GET("timezones")
    Call<TimeZoneResponse> getTimezones(
            @Header("Authorization") String token
    );

    @GET("schedules")
    Call<ScheduleListResponse> getSchedules(
            @Header("Authorization") String token
    );

    @GET("schedules/{id}")
    Call<ScheduleResponse> getScheduleDetails(
            @Header("Authorization") String token,
            @Path("id") int scheduleId
    );
}
