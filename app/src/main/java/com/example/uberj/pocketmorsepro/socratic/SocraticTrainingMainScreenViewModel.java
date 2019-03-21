package com.example.uberj.pocketmorsepro.socratic;

import android.app.Application;

import com.example.uberj.pocketmorsepro.socratic.storage.SocraticTrainingEngineSettings;
import com.example.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSession;
import com.example.uberj.pocketmorsepro.storage.Repository;
import com.example.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

public class SocraticTrainingMainScreenViewModel extends AndroidViewModel {
    private final Repository repository;

    public SocraticTrainingMainScreenViewModel(@NonNull Application application) {
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
