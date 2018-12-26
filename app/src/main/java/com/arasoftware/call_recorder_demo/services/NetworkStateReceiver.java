package com.arasoftware.call_recorder_demo.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.arasoftware.call_recorder_demo.utils.AppContants;

public class NetworkStateReceiver extends BroadcastReceiver {
    private static final String TAG = "NetworkStateReceiver";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        Log.i(TAG, "Network State");
        if(AppContants.CurrentUser==null) return;
        if (isOnline(context)) {
            Intent uploadIntent = new Intent(context, UploadService.class);
            context.startService(uploadIntent);
        }
    }

    public boolean isOnline(Context context) {

        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        //should check null because in airplane mode it will be null
        return (netInfo != null && netInfo.isConnected());
    }
}

