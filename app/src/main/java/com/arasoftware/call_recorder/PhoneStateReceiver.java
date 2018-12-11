package com.arasoftware.call_recorder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;

public class PhoneStateReceiver extends BroadcastReceiver {
    private static final String TAG="PhoneStateReceiver";
    private static final String AUDIO_RECORDER_FILE_EXT_3GP = ".3gp";
    private static final String AUDIO_RECORDER_FILE_EXT_MP4 = ".mp4";
    private static final String AUDIO_RECORDER_FOLDER = "AudioRecorder";
    private MediaRecorder recorder = null;
    private int currentFormat = 1;
    private String incomingNumber;
    private int output_formats[] = {MediaRecorder.OutputFormat.MPEG_4,
            MediaRecorder.OutputFormat.THREE_GPP};
    private String file_exts[] = {AUDIO_RECORDER_FILE_EXT_MP4,
            AUDIO_RECORDER_FILE_EXT_3GP};
    TelephonyManager telephonyManager;

    @Override
    public void onReceive(Context context, Intent intent) {
        String state = intent.getStringExtra(TelephonyManager.EXTRA_STATE);
        incomingNumber = intent.getStringExtra(TelephonyManager.EXTRA_INCOMING_NUMBER);
        if (state.equals(TelephonyManager.EXTRA_STATE_RINGING)) {
            Toast.makeText(context, "Ringing State Number is -" + incomingNumber, Toast.LENGTH_SHORT).show();
        }
        if ((state.equals(TelephonyManager.EXTRA_STATE_OFFHOOK))) {
            Log.i(TAG,"OFF Hook");
            startRecording(context);
        }
        if (state.equals(TelephonyManager.EXTRA_STATE_IDLE)) {
            Toast.makeText(context, "Idle State", Toast.LENGTH_SHORT).show();
            stopRecording();
        }
    }


    private void startRecording(Context context) {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.VOICE_COMMUNICATION);
        recorder.setOutputFormat(output_formats[currentFormat]);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);

        try {
            String fileName = incomingNumber;
            File cacheDir = new File(context.getFilesDir() + "/mine/");
            if (!cacheDir.exists())
                cacheDir.mkdirs();
            File file = new File(cacheDir, fileName + file_exts[currentFormat]);

            Log.i(TAG, file.getPath() + ">>>>>>>>>>");
            Toast.makeText(context, file.getPath(), Toast.LENGTH_SHORT).show();
            recorder.setOutputFile(file.getPath());
            recorder.prepare();
            recorder.start();
        } catch (IllegalStateException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Illegal State Exception");
            e.printStackTrace();
        } catch (IOException e) {
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            Log.e(TAG, e.getMessage());
            e.printStackTrace();
        }
    }

    private void stopRecording() {
        try {

            if (null != recorder) {
                recorder.stop();
                recorder.reset();
                recorder.release();
                recorder = null;
            }
        } catch (RuntimeException stopException) {
            Log.e(TAG, stopException.getMessage());
            stopException.printStackTrace();
        }
    }

}
