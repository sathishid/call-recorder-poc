package com.arasoftware.call_recorder;

import android.Manifest;
import android.app.Activity;
import android.arch.lifecycle.ViewModelProviders;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.arasoftware.call_recorder.fragments.AudioPlayerViewModel;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity>>>>>>>>>>";
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;
    private String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CALL_PHONE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.MODIFY_AUDIO_SETTINGS};
    TelephonyManager mTelephony;
    ListView listView;
    ArrayAdapter adapter;
    private MediaPlayer mediaPlayer;
    File cacheDir;
    private AudioPlayerViewModel mViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        cacheDir = new File(this.getFilesDir() + "/mine/");
        requestPermission();
        listView = findViewById(R.id.activity_main_lv_recordings);
        mViewModel = ViewModelProviders.of(this).get(AudioPlayerViewModel.class);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i("CLICKED", "CLICKED");
                mViewModel.setFile(adapter.getItem(position).toString());
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        String[] files = getFiles();
        if (files == null)
            return;
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1
                , files);
        listView.setAdapter(adapter);
    }

    private String[] getFiles() {
        File cacheDir = new File(this.getFilesDir() + "/mine/");
        String[] fileNames = cacheDir.list();
        return fileNames;
    }


    private void requestPermission() {
        if ((int) Build.VERSION.SDK_INT < 23) {
            mTelephony = (TelephonyManager) this.getSystemService(
                    Context.TELEPHONY_SERVICE);
            return;
        }
        boolean isNotGranted = false;
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                isNotGranted = true;
            }
        }
        if (!isNotGranted) {
            mTelephony = (TelephonyManager) this.getSystemService(
                    Context.TELEPHONY_SERVICE);
            Log.e("Granted", "");
            return;
        }
        ActivityCompat.requestPermissions(this,
                permissions,
                MY_PERMISSIONS_REQUEST_READ_PHONE_STATE);

        Log.e("Not granted", ", request for permission");

    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.e("grantResults.length", " " + grantResults.length);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE: {
                for (int i = 0; i < grantResults.length; i++) {
                    // If request is cancelled, the result arrays are empty.
                    if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                        Log.e("Accepted", "");
                        mTelephony = (TelephonyManager) this.getSystemService(
                                Context.TELEPHONY_SERVICE);

                    } else {

                        // permission denied
                        Log.e("Denied", "");

                        boolean should = ActivityCompat.shouldShowRequestPermissionRationale((Activity) this,
                                permissions[i]);
                        if (should) {
                            //user denied without Never ask again, just show rationale explanation
                            new android.app.AlertDialog.Builder(this)
                                    .setTitle("Permission Denied")
                                    .setMessage("Without this permission the app will be unable to receive Push Notifications.Are you sure you want to deny this permission?")
                                    .setPositiveButton("RE-TRY", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // continue with delete
                                            Log.e("Retry", ",ask request for permission again");
                                        }
                                    })
                                    .setNegativeButton("I'M SURE", new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            // do nothing
                                            Log.e("Click of I m sure", ", permission request is denied");


                                        }
                                    })

                                    .show();

                        } else {
                            //user has denied with `Never Ask Again`
                            Log.e("Never ask again", ", permission request is denied");

                        }
                    }
                    break;
                }
            }


        }
    }

}
