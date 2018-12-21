package com.arasoftware.call_recorder_demo.services;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.arasoftware.call_recorder_demo.utils.AppContants.BASE_URL;

public class ApiClient {
    Retrofit retrofit;
    ApiService callService;
    private static ApiClient apiClient;

    public static ApiClient getApiClient() {
        if (apiClient == null) {
            apiClient = new ApiClient();
        }
        return apiClient;
    }

    public ApiService getCallService() {
        if (retrofit == null) {
            Gson gson = new GsonBuilder()
                    .setLenient()
                    .create();

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create(gson))
                    .build();
            callService = retrofit.create(ApiService.class);
        }
        return callService;
    }
}
