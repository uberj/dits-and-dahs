package com.example.uberj.test1.transcribe;

import com.example.uberj.test1.transcribe.storage.TranscribeTrainingEngineSettings;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingSession;

import java.util.List;

import androidx.lifecycle.MediatorLiveData;

public class SessionEngineWrapper {
    public List<TranscribeTrainingEngineSettings> settings;
    public List<TranscribeTrainingSession> session;


    public static MediatorLiveData<SessionEngineWrapper> bind(TranscribeTrainingSessionViewModel viewModel) {
        MediatorLiveData<SessionEngineWrapper> configMediator = new MediatorLiveData<>();
        SessionEngineWrapper wrapper = new SessionEngineWrapper();
        configMediator.addSource(viewModel.getLatestEngineSetting(), prevSettings -> {
            wrapper.settings = prevSettings;
            if (wrapper.isReady()) {
                configMediator.setValue(wrapper);
            }
        });
        configMediator.addSource(viewModel.getLatestTrainingSession(), prevSessions -> {
            wrapper.session = prevSessions;
            if (wrapper.isReady()) {
                configMediator.setValue(wrapper);
            }
        });
        return configMediator;
    }

    private boolean isReady() {
        return settings != null && session != null;
    }

    public TranscribeTrainingEngineSettings getSettingsOrNull() {
        if (settings.size() == 0) {
            return null;
        } else {
            return settings.get(0);
        }
    }

    public TranscribeTrainingSession getSessionOrNull() {
        if (session.size() == 0) {
            return null;
        } else  {
            return session.get(0);
        }
    }
}
