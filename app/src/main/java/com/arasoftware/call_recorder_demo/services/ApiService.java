package com.arasoftware.call_recorder_demo.services;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {
    @Multipart
    @POST("app.php?action=call")
    Call<ResponseBody> updateCallAudio(@Query("action") String action, @Part("user_id") RequestBody id,
                                       @Part MultipartBody.Part audio,
                                       @Part("call_type") RequestBody call_type);
}
