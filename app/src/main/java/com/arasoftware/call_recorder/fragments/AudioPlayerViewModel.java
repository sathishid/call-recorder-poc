package com.arasoftware.call_recorder.fragments;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

public class AudioPlayerViewModel extends ViewModel {
    private MutableLiveData<String> liveDataFile;


    public AudioPlayerViewModel() {
        this.liveDataFile = new MutableLiveData<String>();
    }

    public LiveData<String> getLiveDataFile() {
        return liveDataFile;
    }


    public void setFile(String file) {
        liveDataFile.setValue(file);
    }
}
