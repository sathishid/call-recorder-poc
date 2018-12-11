package com.arasoftware.call_recorder.fragments;

import android.arch.lifecycle.ViewModelProviders;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;

import com.arasoftware.call_recorder.R;

import java.io.File;

public class audio_player extends Fragment implements MediaPlayer.OnPreparedListener {
    private static final String TAG = "audio_player";
    private AudioPlayerViewModel mViewModel;
    private MediaPlayer mediaPlayer;
    ImageButton button_play;
    SeekBar seekBar_progress;
    public static final int TAG_PLAY = 1;
    public static final int TAG_STOP = 0;
    File cacheDir;

    public static audio_player newInstance() {
        return new audio_player();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_audio_player, container, false);

        button_play = view.findViewById(R.id.fragment_audio_player_play_btn);
        seekBar_progress = view.findViewById(R.id.fragment_audio_player_progress_sb);
        button_play.setTag(TAG_PLAY);
        cacheDir = new File(getActivity().getFilesDir() + "/mine/");
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(AudioPlayerViewModel.class);
        mViewModel.getLiveDataFile()
                .observe(this, file -> {
                    startPlayer(file);
                });
    }

    private boolean InitAudioControls(String fileName) {
        try {

            if (mediaPlayer != null && mediaPlayer.isPlaying())
                mediaPlayer.stop();
            File file = new File(cacheDir, fileName);
            Log.i(TAG, file.getPath());
            mediaPlayer = new MediaPlayer();
            String audioFile = file.getPath();
            Log.d(TAG, audioFile);
            mediaPlayer.setDataSource(audioFile);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            seekBar_progress.setOnSeekBarChangeListener(onSeekBarChangeListener);
            seekBar_progress.setMax(mediaPlayer.getDuration());
            mediaPlayer.setOnPreparedListener(this);
            mediaPlayer.prepareAsync();
            return true;
        } catch (Exception exception) {
            Log.e(TAG, "Message-" + exception.getMessage());
            exception.printStackTrace();
            return false;
        }
    }


    private void stopPlayer() {
        if (mediaPlayer == null)
            return;
        button_play.setEnabled(false);
        button_play.setTag(TAG_PLAY);
        mediaPlayer.release();
        mediaPlayer = null;
        seekBar_progress.setMax(0);
        seekbarUpdateHandler.removeCallbacks(updateSeekbar);
    }

    public void showSnackBar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG);
        snackbar.show();
    }

    private void startPlayer(String fileName) {
        if (button_play.getTag().toString().compareTo(TAG_STOP + "") == 0) {
            if (mediaPlayer != null)
                mediaPlayer.stop();
        }
        if (!InitAudioControls(fileName)) {
            showSnackBar(button_play, "Unable to Start Player");
            Log.e(TAG, "Unable to start Player");
            return;
        }


    }

    private Handler seekbarUpdateHandler = new Handler();
    private Runnable updateSeekbar = new Runnable() {
        @Override
        public void run() {

            int progress = mediaPlayer.getCurrentPosition();
            seekBar_progress.setProgress(progress);

            seekbarUpdateHandler.postDelayed(this, 50);
        }
    };
    SeekBar.OnSeekBarChangeListener onSeekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (mediaPlayer != null && fromUser) {
                mediaPlayer.seekTo(progress * 1000);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    };

    MediaPlayer.OnCompletionListener onCompletionListener = new MediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(MediaPlayer mp) {
            stopPlayer();
        }
    };

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        seekbarUpdateHandler.postDelayed(updateSeekbar, 0);
        button_play.setEnabled(true);
        button_play.setTag(TAG_STOP);
    }
}
