package com.arasoftware.call_recorder_demo.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

import com.arasoftware.call_recorder_demo.models.CallLogInfo;
import com.arasoftware.call_recorder_demo.services.ApiClient;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.arasoftware.call_recorder_demo.utils.AppContants.CurrentUser;

public class UtilMethods {
    private static void sendMissedCalls(Context context,Date lastFetchDate) {

        final String TAG = "SEND_MISSED_CALLS";
        try {


            ContentResolver cr = context.getApplicationContext().getContentResolver();
            String strOrder = android.provider.CallLog.Calls.DATE + " DESC";
            Uri callUri = Uri.parse("content://call_log/calls");
            Cursor managedCursor = cr.query(callUri, null, null, null, strOrder);

            int nameColumn = managedCursor.getColumnIndex(CallLog.Calls.CACHED_NAME);
            int number = managedCursor.getColumnIndex(CallLog.Calls.NUMBER);
            int type = managedCursor.getColumnIndex(CallLog.Calls.TYPE);
            int date = managedCursor.getColumnIndex(CallLog.Calls.DATE);
            int duration = managedCursor.getColumnIndex(CallLog.Calls.DURATION);
            List<CallLogInfo> callLogs = new ArrayList<>();
            while (managedCursor.moveToNext()) {
                String name = managedCursor.getString(nameColumn);
                String phNumber = managedCursor.getString(number);
                String callType = managedCursor.getString(type);
                String callDate = managedCursor.getString(date);
                Date callDayTime = new Date(Long.valueOf(callDate));
                String callDuration = managedCursor.getString(duration);
                String dir = null;
                int dircode = Integer.parseInt(callType);


                switch (dircode) {
                    case CallLog.Calls.OUTGOING_TYPE:
                        dir = "OUTGOING";
                        break;

                    case CallLog.Calls.INCOMING_TYPE:
                        dir = "INCOMING";
                        break;

                    case CallLog.Calls.MISSED_TYPE:
                        dir = "MISSED";
                        if(callDayTime.compareTo(lastFetchDate)>0) {
                            CallLogInfo callLogInfo = new CallLogInfo(name, phNumber, callDayTime, dir);
                            callLogs.add(callLogInfo);
                        }
                        break;
                }

            }
            managedCursor.close();

            ApiClient.getApiClient().getCallService()
                    .saveCallLogInfo(CurrentUser.getUserId(), callLogs)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(context, "SUCCESS", Toast.LENGTH_LONG).show();
                            } else {
                                Log.e(TAG, "error");
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, "error" + t.getLocalizedMessage());
                        }
                    });

        } catch (Exception exception) {
            Log.e(TAG, "" + exception.getMessage());
        }

    }

    public static void sendMissedCallsAsync(Context context,Date date) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                sendMissedCalls(context,date);
            }
        };
        Thread t = new Thread(runnable);
        t.start();
    }
}
