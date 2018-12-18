package com.arasoftware.call_recorder_demo.utils;

import com.arasoftware.call_recorder_demo.models.User;

public class AppContants {
    public static final String FILE_PATH="/storage/emulated/0/com.ara/recordings";
    public static final String BASE_URL = "http://arasoftwares.in/callrec/api/";
    public static final String CALL_IN = "IN";
    public static final String CALL_OUT = "OUT";

    public static final int PLAY_SERVICE_REQUEST=118;
    public static final int LOCATION_PERMISSION_REQUEST=119;

    public static User CurrentUser;
    public static User getCurrentUser(){
        if(CurrentUser==null){
            CurrentUser=new User();
        }
        return CurrentUser;
    }
}
