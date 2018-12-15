package com.arasoftware.call_recorder_demo;

import android.Manifest;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.arch.lifecycle.ViewModelProviders;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.arasoftware.call_recorder_demo.fragments.AudioPlayerViewModel;
import com.arasoftware.call_recorder_demo.utils.AppContants;
import com.arasoftware.call_recorder_demo.utils.DeviceAdminDemo;
import com.arasoftware.call_recorder_demo.utils.TService;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final String TAG = "MainActivity";
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 101;
    private String[] permissions = new String[]{
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.PROCESS_OUTGOING_CALLS,
            Manifest.permission.RECORD_AUDIO,
    };
    TelephonyManager mTelephony;
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;

    ListView listView;
    ArrayAdapter adapter;
    private AudioPlayerViewModel mViewModel;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Intent intent;
        switch (item.getItemId()) {
            case R.id.menu_item_stop:
                intent = new Intent(this, TService.class);
                stopService(intent);
                return (true);
            case R.id.menu_item_start:
                intent = new Intent(this, TService.class);
                startService(intent);
                return (true);
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            // Initiate DevicePolicyManager.
            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            mAdminName = new ComponentName(this, DeviceAdminDemo.class);

            if (!mDPM.isAdminActive(mAdminName)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
                startActivityForResult(intent, REQUEST_CODE);
            } else {

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, TService.class);
        startService(intent);

        listView = findViewById(R.id.activity_main_files_lv);
        mViewModel = ViewModelProviders.of(this).get(AudioPlayerViewModel.class);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "CLICKED");
                mViewModel.setFile(adapter.getItem(position).toString());
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        String[] files = getFiles();
        if (files == null)
            return;
        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_expandable_list_item_1
                , files);
        listView.setAdapter(adapter);
    }

    private String[] getFiles() {
        File cacheDir = new File(AppContants.FILE_PATH);
        String[] fileNames = cacheDir.list();
        return fileNames;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (REQUEST_CODE == requestCode) {
            requestPermission();
        }
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

        Log.e(TAG, "Not granted, request for permission");

    }

    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        Log.e(TAG, "grantResults.length " + grantResults.length);

        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_READ_PHONE_STATE:
                if (permissionAccepted(permissions, grantResults)) {
                    Log.i(TAG, "All permission Accepted");
                    startService(new Intent(MainActivity.this, TService.class));
                }
                break;

        }
    }

    private boolean permissionAccepted(String permissions[], int[] grantResults) {
        boolean hasAccepted = true;

        for (int i = 0; i < grantResults.length; i++) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults.length > 0 && grantResults[i] == PackageManager.PERMISSION_GRANTED) {

                Log.e(TAG, "Accepted - " + permissions[i]);
                mTelephony = (TelephonyManager) this.getSystemService(
                        Context.TELEPHONY_SERVICE);


            } else {

                // permission denied
                Log.e(TAG, "Denied -- " + permissions[i]);

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
                                    Log.e(TAG, "Retry ,ask request for permission again");
                                }
                            })
                            .setNegativeButton("I'M SURE", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // do nothing
                                    Log.e(TAG, "Click of I m sure, permission request is denied");


                                }
                            })

                            .show();

                } else {
                    //user has denied with `Never Ask Again`
                    Log.e(TAG, "Never ask again, permission request is denied");
                    hasAccepted = false;
                }
            }
        }
        return hasAccepted;
    }

}