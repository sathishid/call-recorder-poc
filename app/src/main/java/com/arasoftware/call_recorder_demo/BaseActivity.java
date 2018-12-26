package com.arasoftware.call_recorder_demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import static com.arasoftware.call_recorder_demo.utils.AppContants.LOCATION_PERMISSION_REQUEST;
import static com.arasoftware.call_recorder_demo.utils.AppContants.PLAY_SERVICE_REQUEST;
import static com.arasoftware.call_recorder_demo.utils.AppContants.latitude;
import static com.arasoftware.call_recorder_demo.utils.AppContants.longitude;

public class BaseActivity extends AppCompatActivity {
    private static String TAG = "BaseActivity";

    private void init(Activity activityCompat) {
    }

    public void checkLocationPermission(Activity activityCompat) {
        init(activityCompat);
        if (ContextCompat.checkSelfPermission(activityCompat, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activityCompat,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST);
        } else {
            updateLocation();
        }
    }

    public void updateLocation() {
        try {
            FusedLocationProviderClient mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
            mFusedLocationClient.getLastLocation()
                    .addOnSuccessListener(new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // GPS location can be null if GPS is switched off
                            if (location != null) {
                                latitude = location.getLatitude();
                                longitude = location.getLongitude();
                            }
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Error trying to get last GPS location");
                            e.printStackTrace();
                        }
                    });
        } catch (SecurityException security) {
            Log.e(TAG, "" + security.getLocalizedMessage());
            security.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        int result = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this);
        if (result != ConnectionResult.SUCCESS) {
            GoogleApiAvailability.getInstance().getErrorDialog(this, result, PLAY_SERVICE_REQUEST);
        } else {
            checkLocationPermission(this);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST:

                return;
        }
    }
}