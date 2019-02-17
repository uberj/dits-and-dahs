package com.example.uberj.test1.LetterTraining;

import android.app.Application;

import com.example.uberj.test1.storage.LetterTrainingEngineSettings;
import com.example.uberj.test1.storage.LetterTrainingSession;
import com.example.uberj.test1.storage.Repository;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

class LetterTrainingMainScreenViewModel extends AndroidViewModel {
    private final Repository repository;

    public LetterTrainingMainScreenViewModel(@NonNull Application application) {
        super(application);
        repository = new Repository(application);
    }

    public LiveData<List<LetterTrainingEngineSettings>> getLatestEngineSettings() {
        return repository.engineSettingsDAO.getLatestEngineSetting();
    }

    public LiveData<List<LetterTrainingSession>> getLatestSession() {
        return repository.letterTrainingSessionDAO.getLatestSession();
    }
}
