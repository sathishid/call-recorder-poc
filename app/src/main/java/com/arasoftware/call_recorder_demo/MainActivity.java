package com.arasoftware.call_recorder_demo;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;

import com.arasoftware.call_recorder_demo.adapters.AppointmentAdapter;
import com.arasoftware.call_recorder_demo.listeners.ListViewClickListener;
import com.arasoftware.call_recorder_demo.models.Appointment;
import com.arasoftware.call_recorder_demo.services.ApiClient;
import com.arasoftware.call_recorder_demo.services.ApiService;
import com.arasoftware.call_recorder_demo.services.CallRecordingService;
import com.arasoftware.call_recorder_demo.services.DeviceAdminManager;

import java.io.File;
import java.util.Calendar;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static com.arasoftware.call_recorder_demo.services.ApiClient.getApiClient;
import static com.arasoftware.call_recorder_demo.utils.AppContants.CurrentUser;
import static com.arasoftware.call_recorder_demo.utils.AppContants.LOGIN_REQUEST;
import static com.arasoftware.call_recorder_demo.utils.AppContants.latitude;
import static com.arasoftware.call_recorder_demo.utils.AppContants.longitude;

public class MainActivity extends BaseActivity implements ListViewClickListener<Appointment> {
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
    ApiClient apiClient;
    TelephonyManager mTelephony;
    private static final int REQUEST_CODE = 0;
    private DevicePolicyManager mDPM;
    private ComponentName mAdminName;
    private View mProgressView;
    RecyclerView recyclerView;
    ArrayAdapter adapter;
    private AppointmentAdapter mAdapter;
    private List<Appointment> appointmentList;

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
                intent = new Intent(this, CallRecordingService.class);
                stopService(intent);
                return (true);
            case R.id.menu_item_start:
                intent = new Intent(this, CallRecordingService.class);
                startService(intent);
                return (true);
            case R.id.menu_item_current_location:
                String latLng = latitude + "," + longitude;
                Snackbar.make(recyclerView, latLng, Snackbar.LENGTH_LONG).show();
                return true;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.activity_main_appointments_rv);
        mProgressView = findViewById(R.id.activity_main_login_progress);
        try {
            // Initiate DevicePolicyManager.
            mDPM = (DevicePolicyManager) getSystemService(Context.DEVICE_POLICY_SERVICE);
            mAdminName = new ComponentName(this, DeviceAdminManager.class);

            if (!mDPM.isAdminActive(mAdminName)) {
                Intent intent = new Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mAdminName);
                intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION, "Click on Activate button to secure your application.");
                startActivityForResult(intent, REQUEST_CODE);
            } else {
                apiClient = getApiClient();
                loginRequest();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loginRequest() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, LOGIN_REQUEST);
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, CallRecordingService.class);
        startService(intent);


    }

    @Override
    protected void onResume() {
        super.onResume();
        String[] files = getFiles();
        if (files == null)
            return;

    }

    private String[] getFiles() {
        File cacheDir = getApplicationContext().getFilesDir();
        String[] fileNames = cacheDir.list();
        return fileNames;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_CODE:
                requestPermission();
                loginRequest();
                break;
            case LOGIN_REQUEST:
                fetchAppointments();
                break;
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
                    startService(new Intent(MainActivity.this, CallRecordingService.class));
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

    private void prepareRecylerView() {

        mAdapter = new AppointmentAdapter(appointmentList, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        DividerItemDecoration dividerItemDecoration = new DividerItemDecoration(recyclerView.getContext(),
                DividerItemDecoration.VERTICAL);
        recyclerView.addItemDecoration(dividerItemDecoration);
        recyclerView.setAdapter(mAdapter);

    }

    private void fetchAppointments() {
        ApiService service = getApiClient().getCallService();

        showProgress(true);
        service.findAppointments(CurrentUser.getUserId())
                .enqueue(new Callback<List<Appointment>>() {
                    @Override
                    public void onResponse(Call<List<Appointment>> call, Response<List<Appointment>> response) {
                        showProgress(false);
                        appointmentList = response.body();
                        prepareRecylerView();

                        Log.e(TAG, "Appointments Collected.." + appointmentList.size());
                    }

                    @Override
                    public void onFailure(Call<List<Appointment>> call, Throwable t) {
                        showProgress(false);
                        Log.e(TAG, "" + t.getLocalizedMessage());
                    }
                });
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
            recyclerView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            recyclerView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public void onItemClick(Appointment selectedObject, int position) {
        showDialog(selectedObject, 1000, position);
    }

    private void saveAppointment(int position) {
        Appointment appointment = appointmentList.get(position);
        getApiClient().getCallService()
                .saveAppointment(appointment.getId(), appointment.getLatitude(),
                        appointment.getLongitude())
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        appointmentList.remove(position);
                        mAdapter.notifyItemRemoved(position);
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, "" + t.getLocalizedMessage());
                    }
                });
    }

    private void showDialog(String message, boolean canAttend, int position) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message)
                .setPositiveButton((canAttend) ? R.string.confirm : R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                if (!canAttend) {
                                    dialog.dismiss();
                                }
                                saveAppointment(position);
                            }
                        })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        dialog.cancel();
                    }
                });
        // Create the AlertDialog object and return it
        builder.create().show();
    }

    private void showDialog(Appointment appointment, float radius, int position) {
        boolean canAttend = false;
        String message = null;
        float[] results = new float[1];
        Location.distanceBetween(latitude, longitude,
                appointment.getLatitude(), appointment.getLongitude(), results);
        if (results[0] > radius)
            message = "You can attend only when you reached  " + appointment.getPlace();
        else {
            Calendar calendar = Calendar.getInstance();

            int hours = calendar.get(Calendar.HOUR_OF_DAY);
            int minutes = calendar.get(Calendar.MINUTE);
            String hm[] = appointment.getTime().split(":");
            int appHour = Integer.parseInt(hm[0]);
            int appMin = Integer.parseInt(hm[0]);
            int diffHrs = hours - appHour;
            int diffMin = Math.abs(minutes - appMin);

            if (diffHrs == 0 && diffMin <= 15) {
                canAttend = true;
                message = getString(R.string.can_attend);

            } else {
                message = "You can attend appointment at " + appointment.getTime();
            }
        }
        showDialog(message, canAttend, position);
    }
}