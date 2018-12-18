package com.arasoftware.call_recorder_demo.utils;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaRecorder;
import android.os.IBinder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import com.arasoftware.call_recorder_demo.services.ApiService;

import java.io.File;
import java.io.IOException;
import java.util.Date;

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

public class TService extends Service {
    private static final String TAG = "TService";
    private MediaRecorder recorder;
    private File audiofile;
    private boolean recordstarted = false;

    public static final String ACTION_IN = "android.intent.action.PHONE_STATE";
    public static final String ACTION_OUT = "android.intent.action.NEW_OUTGOING_CALL";
    private TelephonyManager telephonyManager;
    private boolean isRegistered = false;
    CallReceiver callReceiver;
    Retrofit retrofit;
    ApiService callService;

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "destroying");
        getApplicationContext().unregisterReceiver(callReceiver);
        Log.d(TAG, "Un Registered Receiver");
        super.onDestroy();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "On Start Starting");
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .build();
            callService = retrofit.create(ApiService.class);
        }
        if (!isRegistered) {
            final IntentFilter filter = new IntentFilter();
            filter.addAction(ACTION_OUT);
            filter.addAction(ACTION_IN);
            callReceiver = new CallReceiver();
            getApplicationContext().registerReceiver(callReceiver, filter);
            isRegistered = true;
            Log.d(TAG, "Registered Broadcast");
        } else {
            Log.d(TAG, "Already Registered Broadcast");
        }
        return super.onStartCommand(intent, flags, startId);
    }


    private void startRecording(String number) {
        Toast.makeText(this, "Started Recording", Toast.LENGTH_LONG).show();
        Log.i(TAG, "Started Recording");
        File sampleDir = getApplicationContext().getFilesDir();

        if (!sampleDir.exists()) {
            sampleDir.mkdirs();
        }
        try {
            audiofile = File.createTempFile(number, ".amr", sampleDir);
        } catch (IOException e) {
            Log.e(TAG, "Unable to Create File (Audio) - " + e.getLocalizedMessage());
            e.printStackTrace();
            audiofile = null;
        }
        if (audiofile == null) {
            Toast.makeText(this, "Unable to Create File (Audio)", Toast.LENGTH_LONG).show();
            Log.e(TAG, "Unable to Create File (Audio)");
            return;
        }

        Toast.makeText(this, audiofile.getAbsolutePath(), Toast.LENGTH_LONG).show();
        Log.d(TAG, "File Path" + audiofile.getAbsolutePath());
        recorder = new MediaRecorder();

        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
        recorder.setOutputFile(audiofile.getAbsolutePath());
        Log.i(TAG, audiofile.getAbsolutePath());
        try {
            recorder.prepare();
            recorder.start();
            Log.i(TAG, "RECORD Started..");
            Toast.makeText(this, "RECORDING STARTED", Toast.LENGTH_LONG).show();
            recordstarted = true;
        } catch (IllegalStateException e) {
            Toast.makeText(this, "illegal" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Message :" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(this, "iLLegal" + e.getMessage(), Toast.LENGTH_LONG).show();
            Log.e(TAG, "Message :" + e.getMessage());
            e.printStackTrace();
        }


    }

    private void stopRecording(String fileName, String type) {
        if (recordstarted) {
            recorder.stop();
            recorder.release();
            recorder = null;
            recordstarted = false;
            uploadAudio(audiofile.getAbsolutePath(), type);
            Toast.makeText(this, "RECORDING STOPPED", Toast.LENGTH_LONG).show();
        }
    }

    public void uploadAudio(String fileName, String strType) {
        if (fileName == null) fileName = "Recorded";

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

        callService.updateCallAudio("call", id, body, type)
                .enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                        try {
                            Log.i(TAG, "" + response.body().string());
                        } catch (IOException exception) {
                            Log.e(TAG, "" + exception.getLocalizedMessage());
                        }
                    }

                    @Override
                    public void onFailure(Call<ResponseBody> call, Throwable t) {
                        Log.e(TAG, t.getLocalizedMessage() + "");
                    }
                });


    }


    public abstract class PhoneCallReceiver extends BroadcastReceiver {

        public static final String TAG = "PhoneCallReceiver";
        //The receiver will be recreated whenever android feels like it.  We need a static variable to remember data between instantiations
        private int lastState = TelephonyManager.CALL_STATE_IDLE;
        private Date callStartTime;
        private boolean isIncoming;
        private String savedNumber;  //because the passed incoming is only valid in ringing


        @Override
        public void onReceive(Context context, Intent intent) {
            Log.i(TAG, "OnReceive Started.." + intent.getAction());

            savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            //We listen to two intents.  The new outgoing call only tells us of an outgoing call.  We use it to get the number.
            if (intent.getAction().equals("android.intent.action.NEW_OUTGOING_CALL")) {
                savedNumber = intent.getExtras().getString("android.intent.extra.PHONE_NUMBER");
            } else {
                String stateStr = intent.getExtras().getString(TelephonyManager.EXTRA_STATE);
                String number = intent.getExtras().getString(TelephonyManager.EXTRA_INCOMING_NUMBER);

                int state = 0;
                if (stateStr.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
                    state = TelephonyManager.CALL_STATE_IDLE;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_OFFHOOK)) {
                    state = TelephonyManager.CALL_STATE_OFFHOOK;
                } else if (stateStr.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
                    state = TelephonyManager.CALL_STATE_RINGING;
                }
                if (savedNumber == null && number == null) {
                    number = "Record";
                } else if (number == null) {
                    number = savedNumber;
                }

                onCallStateChanged(context, state, number);
            }
        }

        //Derived classes should override these to respond to specific events of interest
        protected abstract void onIncomingCallReceived(Context ctx, String number, Date start);

        protected abstract void onIncomingCallAnswered(Context ctx, String number, Date start);

        protected abstract void onIncomingCallEnded(Context ctx, String number, Date start, Date end);

        protected abstract void onOutgoingCallStarted(Context ctx, String number, Date start);

        protected abstract void onOutgoingCallEnded(Context ctx, String number, Date start, Date end);

        protected abstract void onMissedCall(Context ctx, String number, Date start);

        //Deals with actual events

        //Incoming call-  goes from IDLE to RINGING when it rings, to OFFHOOK when it's answered, to IDLE when its hung up
        //Outgoing call-  goes from IDLE to OFFHOOK when it dials out, to IDLE when hung up
        public void onCallStateChanged(Context context, int state, String number) {
            if (lastState == state) {
                //No change, debounce extras
                return;
            }
            Toast.makeText(getApplicationContext(), state + " - " + (lastState != TelephonyManager.CALL_STATE_RINGING), Toast.LENGTH_LONG).show();
            switch (state) {
                case TelephonyManager.CALL_STATE_RINGING:
                    isIncoming = true;
                    callStartTime = new Date();
                    savedNumber = number;
                    Log.i(TAG, "Incoming Call-" + savedNumber);
                    Toast.makeText(getApplicationContext(), "INCOMING", Toast.LENGTH_LONG).show();
                    onIncomingCallReceived(context, number, callStartTime);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK:
                    //Transition of ringing->offhook are pickups of incoming calls.  Nothing done on them
                    if (lastState != TelephonyManager.CALL_STATE_RINGING) {
                        isIncoming = false;
                        callStartTime = new Date();
                        Log.i(TAG, "About to Start Recording");
                        Toast.makeText(getApplicationContext(), "Call Out Attend", Toast.LENGTH_LONG).show();
                        startRecording(number + "_Out");
                        onOutgoingCallStarted(context, savedNumber, callStartTime);
                    } else {
                        isIncoming = true;
                        callStartTime = new Date();
                        Toast.makeText(getApplicationContext(), "Call In Attend", Toast.LENGTH_LONG).show();
                        startRecording(number + "_In");
                        onIncomingCallAnswered(context, savedNumber, callStartTime);
                    }

                    break;
                case TelephonyManager.CALL_STATE_IDLE:
                    //Went to idle-  this is the end of a call.  What type depends on previous state(s)
                    if (lastState == TelephonyManager.CALL_STATE_RINGING) {
                        //Ring but no pickup-  a miss
                        onMissedCall(context, savedNumber, callStartTime);
                    } else if (isIncoming) {
                        Toast.makeText(getApplicationContext(), "Call In Stop ", Toast.LENGTH_LONG).show();
                        stopRecording(number, CALL_IN);
                        onIncomingCallEnded(context, savedNumber, callStartTime, new Date());
                    } else {
                        Toast.makeText(getApplicationContext(), "Call Out Stop", Toast.LENGTH_LONG).show();
                        stopRecording(number, CALL_OUT);
                        onOutgoingCallEnded(context, savedNumber, callStartTime, new Date());
                    }
                    break;
            }
            lastState = state;
        }


    }

    public class CallReceiver extends PhoneCallReceiver {

        @Override
        protected void onIncomingCallReceived(Context ctx, String number, Date start) {
            Log.d(TAG, "onIncomingCallReceived - " + number + " " + start.toString());
        }

        @Override
        protected void onIncomingCallAnswered(Context ctx, String number, Date start) {
            Log.d(TAG, "onIncomingCallAnswered " + number + " " + start.toString());
        }

        @Override
        protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
            Log.d(TAG, "onIncomingCallEnded -" + number + " " + start.toString() + "\t" + end.toString());
        }

        @Override
        protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
            Log.d(TAG, "onOutgoingCallStarted -" + number + " " + start.toString());
        }

        @Override
        protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
            Log.d(TAG, "onOutgoingCallEnded -" + number + " " + start.toString() + "\t" + end.toString());
        }

        @Override
        protected void onMissedCall(Context ctx, String number, Date start) {
            Log.d(TAG, "onMissedCall - " + number + " " + start.toString());
        }

    }
}