package com.arasoftware.call_recorder_demo.models;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.annotations.SerializedName;

public class User {
    @SerializedName("userid")
    private int userId;
    @SerializedName("username")
    private String userName;
    private String role;

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

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

    public boolean isInspector(){
        if(role==null)
            return false;
        return role.compareTo("inspector")==0;
    }

    public static User fromJson(String s) {

        try {
            Gson gson = new Gson();
            User user= gson.fromJson(s, User.class);
            if(user.role==null)
                return null;
            return user;
        } catch (JsonSyntaxException jsonException) {
            Log.e("USER", jsonException.getLocalizedMessage() + "");
        }
        return null;
    }
}
