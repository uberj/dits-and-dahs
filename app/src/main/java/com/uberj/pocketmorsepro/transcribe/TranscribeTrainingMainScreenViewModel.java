package com.uberj.pocketmorsepro.transcribe;

import android.app.Application;

import com.uberj.pocketmorsepro.storage.Repository;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeSessionType;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeTrainingSession;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

public class TranscribeTrainingMainScreenViewModel extends AndroidViewModel {
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
