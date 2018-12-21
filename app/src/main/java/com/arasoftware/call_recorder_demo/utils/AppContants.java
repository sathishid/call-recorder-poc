package com.arasoftware.call_recorder_demo.utils;

import android.content.Context;

import com.arasoftware.call_recorder_demo.models.User;

import java.io.File;

public class AppContants {
    public static final String FILE_PATH="/storage/emulated/0/com.ara/recordings";
    public static final String BASE_URL = "http://arasoftwares.in/callrec/api/";
    public static final String CALL_IN = "IN";
    public static final String CALL_OUT = "OUT";

    public static final int PLAY_SERVICE_REQUEST=118;
    public static final int LOCATION_PERMISSION_REQUEST=119;
    public static final int LOGIN_REQUEST=120;

    public static User CurrentUser;
    public volatile  static double latitude;
    public volatile  static double longitude;

    public static User getCurrentUser(){
        if(CurrentUser==null){
            CurrentUser=new User();
        }
        return CurrentUser;
    }
    public static final File getFilePath(Context context){
        return context.getApplicationContext().getFilesDir();
    }
}
