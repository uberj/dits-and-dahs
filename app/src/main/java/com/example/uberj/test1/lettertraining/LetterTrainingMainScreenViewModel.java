package com.example.uberj.test1.lettertraining;

import android.app.Application;

import com.example.uberj.test1.storage.LetterTrainingEngineSettings;
import com.example.uberj.test1.storage.LetterTrainingSession;
import com.example.uberj.test1.storage.Repository;
import com.example.uberj.test1.storage.SessionType;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class LetterTrainingMainScreenViewModel extends AndroidViewModel {
    private final Repository repository;

    public LetterTrainingMainScreenViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
    }

    public LiveData<List<LetterTrainingEngineSettings>> getLatestEngineSettings(SessionType sessionType) {
        return repository.engineSettingsDAO.getLatestEngineSetting(sessionType.name());
    }

    public LiveData<List<LetterTrainingSession>> getLatestSession(SessionType sessionType) {
        return repository.letterTrainingSessionDAO.getLatestSession(sessionType.name());
    }
}
