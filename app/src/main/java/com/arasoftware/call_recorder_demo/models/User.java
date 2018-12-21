package com.arasoftware.call_recorder_demo.models;

import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

public class User {
    private int userId;
    private String userName;
    private double latitude;
    private double longitude;


    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
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

    public static User fromJson(String s) {

        try {
            JSONObject jsonObject = new JSONObject(s);
            User user = new User();
            user.setUserId(jsonObject.getInt("userid"));
            return user;

        } catch (JSONException jsonException) {
            Log.e("USER", jsonException.getLocalizedMessage() + "");
        }
        return null;
    }
}
