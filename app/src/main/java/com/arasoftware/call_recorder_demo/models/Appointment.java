package com.arasoftware.call_recorder_demo.models;

import com.google.gson.Gson;
import com.google.gson.annotations.SerializedName;

public class Appointment {
    @SerializedName("appoinment_id")
    private int id;
    private double latitude;
    @SerializedName("longtitude")
    private double longitude;
    @SerializedName("appoinment_user_id")
    private int userId;
    @SerializedName("appoinment_appt_data")
    private String date;
    @SerializedName("appt_time")
    private String time;

    @SerializedName("area_place")
    private String place;

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getPlace() {
        return place;
    }

    public void setPlace(String place) {
        this.place = place;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public static Appointment[] fronJsonArray(String jsonArray) {
        Gson gson = new Gson();
        return gson.fromJson(jsonArray, Appointment[].class);
    }
}
