package com.arasoftware.call_recorder_demo.services;


import com.arasoftware.call_recorder_demo.models.Appointment;
import com.arasoftware.call_recorder_demo.models.CallLogInfo;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    @Multipart
    @POST("app.php?action=call")
    Call<ResponseBody> updateCallAudio(@Part("userId") RequestBody id,
                                       @Part MultipartBody.Part audio,
                                       @Part("status") RequestBody call_type);

    @FormUrlEncoded
    @POST("app.php?action=login")
    Call<ResponseBody> validateUser(@Field("username") String username,
                                    @Field("password") String password);

    @FormUrlEncoded
    @POST("app.php?action=appoinments")
    Call<List<Appointment>> findAppointments(@Field("user-id") int userId);


    @FormUrlEncoded
    @POST("app.php?action=attendance")
    Call<ResponseBody> saveAppointment(@Field("appoinment-id") int appointmentId,
                                       @Field("latitude") double latitude,
                                       @Field("longtitude") double longitude,
                                       @Field("user-id") int id);

    @POST("app.php?action=call_log")
    Call<ResponseBody> saveCallLogInfo(@Query("user_id") int id, @Body List<CallLogInfo> callLogInfos);

}
