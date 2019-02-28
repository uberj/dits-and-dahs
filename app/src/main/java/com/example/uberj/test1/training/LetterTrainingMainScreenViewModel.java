package com.example.uberj.test1.training;

import android.app.Application;

import com.example.uberj.test1.socratic.storage.SocraticTrainingEngineSettings;
import com.example.uberj.test1.socratic.storage.SocraticTrainingSession;
import com.example.uberj.test1.storage.Repository;
import com.example.uberj.test1.socratic.storage.SocraticSessionType;

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

    public LiveData<List<SocraticTrainingEngineSettings>> getLatestEngineSettings(SocraticSessionType sessionType) {
        return repository.socraticEngineSettingsDAO.getLatestEngineSetting(sessionType.name());
    }

    public LiveData<List<SocraticTrainingSession>> getLatestSession(SocraticSessionType sessionType) {
        return repository.socraticTrainingSessionDAO.getLatestSession(sessionType.name());
    }
}
