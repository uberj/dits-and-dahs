package com.example.uberj.test1.transcribe;

import android.app.Application;

import com.example.uberj.test1.storage.Repository;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionType;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingSession;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

class TranscribeTrainingMainScreenViewModel extends AndroidViewModel {
    private final Repository repository;
    public final MutableLiveData<ArrayList<String>> selectedStrings = new MutableLiveData<>(null);
    public final MutableLiveData<boolean[]> selectedStringsBooleanMap = new MutableLiveData<>(null);

    public TranscribeTrainingMainScreenViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
    }

    public LiveData<List<TranscribeTrainingSession>> getLatestSession(TranscribeSessionType sessionType) {
        return repository.transcribeTrainingSessionDAO.getLatestSession(sessionType.name());
    }

}
