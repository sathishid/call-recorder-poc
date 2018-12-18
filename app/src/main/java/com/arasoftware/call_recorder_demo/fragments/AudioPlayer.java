package com.arasoftware.call_recorder_demo.fragments;

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

import com.arasoftware.call_recorder_demo.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

public class AudioPlayer extends Fragment implements MediaPlayer.OnPreparedListener {
    private static final String TAG = "AudioPlayer";
    private AudioPlayerViewModel mViewModel;
    private MediaPlayer mediaPlayer;
    ImageButton button_play;
    SeekBar seekBar_progress;
    public static final int TAG_PLAY = 1;
    public static final int TAG_STOP = 0;
    File cacheDir;


    public static AudioPlayer newInstance() {
        return new AudioPlayer();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.audio_player_fragment, container, false);
        button_play = view.findViewById(R.id.fragment_audio_player_play_btn);
        seekBar_progress = view.findViewById(R.id.fragment_audio_player_progress_sb);
        button_play.setTag(TAG_PLAY);
        cacheDir = this.getContext().getApplicationContext().getFilesDir();
        button_play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopPlayer();
                button_play.setEnabled(false);
            }
        });
        return view;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mViewModel = ViewModelProviders.of(getActivity()).get(AudioPlayerViewModel.class);
        mViewModel.getLiveDataFile()
                .observe(this, file -> {
                    Log.i(TAG, file);
                    startPlayer(file);
                });
    }

    private boolean InitAudioControls(String fileName) {
        try {

            if (mediaPlayer != null && mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
                mediaPlayer.release();
            }
            File file = new File(cacheDir, fileName);
            Log.i(TAG, file.getPath() + ": File Exists:" + file.exists());

            mediaPlayer = new MediaPlayer();
            mediaPlayer.setOnErrorListener(onErrorListener);
            mediaPlayer.setOnCompletionListener(onCompletionListener);
            mediaPlayer.setOnPreparedListener(this);

            seekBar_progress.setOnSeekBarChangeListener(onSeekBarChangeListener);
            seekBar_progress.setMax(mediaPlayer.getDuration());

            String audioFile = file.getPath();
            Log.d(TAG, audioFile);
            FileInputStream fileInputStream = new FileInputStream(audioFile);
            mediaPlayer.setDataSource(fileInputStream.getFD());

            mediaPlayer.prepareAsync();
            return true;
        } catch (IllegalStateException exception) {
            Log.e(TAG, "Illegal State-" + exception.getMessage());
            exception.printStackTrace();
            return false;
        } catch (IOException exception) {
            Log.e(TAG, "File Not Found-" + exception.getMessage());
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
        InitAudioControls(fileName);
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
            button_play.setEnabled(false);
        }
    };

    MediaPlayer.OnErrorListener onErrorListener = new MediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            Log.e(TAG, "" + what + extra);
            return false;
        }
    };

    @Override
    public void onPrepared(MediaPlayer mp) {
        Log.i(TAG, "On Prepared..");
        mp.start();
        showSnackBar(button_play, mp.getDuration() + "");
        seekbarUpdateHandler.postDelayed(updateSeekbar, 0);
        button_play.setEnabled(true);
    }
}
