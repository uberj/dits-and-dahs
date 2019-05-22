package com.uberj.ditsanddahs.simplesocratic;

import android.app.Application;

import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingEngineSettings;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingSessionWithEvents;
import com.uberj.ditsanddahs.storage.Repository;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticSessionType;

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
