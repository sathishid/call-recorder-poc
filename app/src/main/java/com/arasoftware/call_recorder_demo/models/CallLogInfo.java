package com.arasoftware.call_recorder_demo.models;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class CallLogInfo {
    private String name;
    @SerializedName("mobile")
    private String number;
    @SerializedName("datetime")
    private Date date;
    @SerializedName("call_type")
    private String type;

    public CallLogInfo(String name, String number, Date date, String type) {
        this.name = name;
        this.number = number;
        this.date = date;
        this.type = type;
    }

}
