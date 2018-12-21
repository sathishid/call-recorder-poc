package com.arasoftware.call_recorder_demo.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.IBinder;
import android.util.Log;

import com.arasoftware.call_recorder_demo.utils.AppContants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.arasoftware.call_recorder_demo.utils.AppContants.BASE_URL;
import static com.arasoftware.call_recorder_demo.utils.AppContants.CALL_IN;
import static com.arasoftware.call_recorder_demo.utils.AppContants.CALL_OUT;
import static com.arasoftware.call_recorder_demo.utils.AppContants.FILE_PATH;

public class UploadService extends Service {
    private static final String TAG = "UploadService";
    private boolean uploading = false;
    Retrofit retrofit;
    ApiService callService;
    public volatile boolean stopNow = false;

    public UploadService() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i(TAG, "Upload Service Up & Running");
        if (isNetworkAvailable()) {
            if (retrofit == null) {
                retrofit = new Retrofit.Builder()
                        .baseUrl(BASE_URL)
                        .build();
                callService = retrofit.create(ApiService.class);
            }
            stopNow = false;
            beginUploadAsync();
            return START_STICKY;
        } else {
            Log.i(TAG, "Internet not connected.");
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopNow = true;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void beginUploadAsync() {
        if (uploading) return;

        String[] fileNames;
        //File cacheDir = getApplicationContext().getFilesDir();
        File cacheDir = AppContants.getFilePath(this);
        if (!cacheDir.exists()) {
            Log.e(TAG, cacheDir.getAbsolutePath() + " Not Found");
            return;
        }
        fileNames = cacheDir.list();

        if (fileNames.length == 0) {
            Log.i(TAG, "Either all files or uploaded or files not written in this directory");
            return;
        }
        uploading = true;
        new UploadFiles().execute();
    }


    class UploadFiles extends AsyncTask<Void, String, String> {
        @Override
        protected String doInBackground(Void... voids) {
            String[] fileNames;
            File cacheDir = AppContants.getFilePath(UploadService.this);
            fileNames = cacheDir.list();
            String type = CALL_IN;
            for (String fileName : fileNames) {
                if (stopNow) break;
                if(fileName.contains("_")) {
                    String mobileTypeSplit[] = fileName.split("_");
                    type = (mobileTypeSplit[1].contains(CALL_IN)) ? CALL_IN : CALL_OUT;
                }
                File file=new File(cacheDir,fileName);
                uploadAudio(file.getAbsolutePath(), type);
            }

            return "Success";
        }

        @Override
        protected void onPostExecute(String s) {
            uploading = false;
            super.onPostExecute(s);
        }

        @Override
        protected void onProgressUpdate(String... values) {
            for (String value : values)
                Log.i(TAG, value);
        }

        public void uploadAudio(String fileName, String strType) {
            Log.i(TAG, "Uploading File " + fileName);
            if (fileName == null) return;

            String strId = "1";
            // create multipart
            //pass it like this
            File file = new File(fileName);
            RequestBody requestFile =
                    RequestBody.create(MediaType.parse("audio/AMR"), file);
            Log.i(TAG, file.length() + "");

            MultipartBody.Part body =
                    MultipartBody.Part.createFormData("audioFile", file.getName(), requestFile);

            RequestBody id =
                    RequestBody.create(MediaType.parse("text/plain"), strId);
            RequestBody type =
                    RequestBody.create(MediaType.parse("text/plain"), strType);

            callService.updateCallAudio( id, body, type)
                    .enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            try {
                               file.delete();
                                Log.i(TAG, fileName + " " + response.body().string());
                            } catch (IOException exception) {
                                Log.e(TAG, "" + exception.getLocalizedMessage());
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Log.e(TAG, "ERROR: " + fileName + " " + t.getLocalizedMessage() + "");
                        }
                    });


        }
    }

}
