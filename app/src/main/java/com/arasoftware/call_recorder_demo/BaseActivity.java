package com.arasoftware.call_recorder_demo;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.arasoftware.call_recorder_demo.models.User;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.places.GeoDataClient;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBufferResponse;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import static com.arasoftware.call_recorder_demo.utils.AppContants.LOCATION_PERMISSION_REQUEST;
import static com.arasoftware.call_recorder_demo.utils.AppContants.PLAY_SERVICE_REQUEST;
import static com.arasoftware.call_recorder_demo.utils.AppContants.getCurrentUser;

public class BaseActivity extends AppCompatActivity {
    private static String TAG = "BaseActivity";
    public GeoDataClient mGeoDataClient;
    public PlaceDetectionClient mPlaceDetectionClient;


    private void init(Activity activityCompat) {
        if (mGeoDataClient == null)
            mGeoDataClient = Places.getGeoDataClient(activityCompat, null);

        if (mPlaceDetectionClient == null)
            mPlaceDetectionClient = Places.getPlaceDetectionClient(activityCompat, null);
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

    private void updateLocation() {
        try {
            Task<PlaceLikelihoodBufferResponse> placeResult = mPlaceDetectionClient.getCurrentPlace(null);
            placeResult.addOnSuccessListener(new OnSuccessListener<PlaceLikelihoodBufferResponse>() {
                @Override
                public void onSuccess(PlaceLikelihoodBufferResponse placeLikelihoods) {
                    if (placeLikelihoods.getCount() > 0) {
                        PlaceLikelihood likelihood = placeLikelihoods.get(0);
                        LatLng latLng = likelihood.getPlace().getLatLng();
                        User currentUser = getCurrentUser();
                        currentUser.setLatitude(latLng.latitude);
                        currentUser.setLongitude(latLng.longitude);
                        Log.i(TAG, "Latitude:" + latLng.latitude + " Longitude:" + latLng.longitude);
                        placeLikelihoods.release();
                    }
                }
            });
            placeResult.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e(TAG, "Message : " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (SecurityException security) {
            Log.e(TAG, security.getLocalizedMessage());
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