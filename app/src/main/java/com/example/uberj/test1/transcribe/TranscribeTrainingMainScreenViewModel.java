package com.example.uberj.test1.transcribe;

import android.app.Application;

import com.example.uberj.test1.storage.Repository;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionType;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingEngineSettings;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingSession;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

class TranscribeTrainingMainScreenViewModel extends AndroidViewModel {
    private final Repository repository;

    public TranscribeTrainingMainScreenViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
    }

    public LiveData<List<TranscribeTrainingEngineSettings>> getLatestEngineSettings(TranscribeSessionType sessionType) {
        return repository.transcribeEngineSettingsDAO.getLatestEngineSetting(sessionType.name());
    }

    public LiveData<List<TranscribeTrainingSession>> getLatestSession(TranscribeSessionType sessionType) {
        return repository.transcribeTrainingSessionDAO.getLatestSession(sessionType.name());
    }

}
