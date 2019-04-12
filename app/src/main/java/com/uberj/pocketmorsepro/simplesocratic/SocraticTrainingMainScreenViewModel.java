package com.uberj.pocketmorsepro.simplesocratic;

import android.app.Application;

import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingEngineSettings;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingSessionWithEvents;
import com.uberj.pocketmorsepro.storage.Repository;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticSessionType;

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

    public LiveData<List<SocraticTrainingSessionWithEvents>> getLatestSession(SocraticSessionType sessionType) {
        return repository.socraticTrainingSessionDAO.getLatestSessionAndEvents(sessionType.name());
    }
}
