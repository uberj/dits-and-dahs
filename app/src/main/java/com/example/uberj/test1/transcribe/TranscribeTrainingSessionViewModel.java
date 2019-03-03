package com.example.uberj.test1.transcribe;

import android.app.Application;

import com.example.uberj.test1.CountDownTimer;
import com.example.uberj.test1.keyboards.Keys;
import com.example.uberj.test1.storage.Repository;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionType;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingEngineSettings;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingSession;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import timber.log.Timber;

public class TranscribeTrainingSessionViewModel extends AndroidViewModel {
    private final Application application;
    private final int durationMinutesRequested;
    private final int wpmRequested;
    private final TranscribeSessionType sessionType;
    private final Keys keys;
    private final Repository repository;
    private final int farnsworth;
    public final MutableLiveData<Long> durationRemainingMillis = new MutableLiveData<>(-1L);
    private final ArrayList<String> stringsReqested;
    private CountDownTimer countDownTimer;
    private TranscribeTrainingEngine engine;
    private boolean sessionHasBeenStarted = false;
    private static final String sessionStartLock = "lock";
    private long endTimeEpocMillis = -1;

    public TranscribeTrainingSessionViewModel(@NonNull Application application, int durationMinutesRequested, ArrayList<String> stringsRequested, int wpmRequested, int farnsworth, TranscribeSessionType sessionType, Keys keys) {
        super(application);
        this.repository = new Repository(application);
        this.application = application;
        this.durationMinutesRequested = durationMinutesRequested;
        this.wpmRequested = wpmRequested;
        this.stringsReqested = stringsRequested;
        this.farnsworth = farnsworth;
        this.sessionType = sessionType;
        this.keys = keys;
    }

    public long getDurationRequestedMillis() {
        return durationMinutesRequested * 60 * 1000;
    }

    public boolean isPaused() {
        return false;
    }

    public void pause() {
    }

    public void resume() {
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final int durationMinutesRequested;
        private final int wpmRequested;
        private final int fransworth;
        private final ArrayList<String> stringsRequested;
        private final TranscribeSessionType sessionType;
        private final Keys keys;


        public Factory(Application application, int durationMinutesRequested, int wpmRequested, ArrayList<String> stringsRequested, int fransworth, TranscribeSessionType sessionType, Keys keys) {
            this.application = application;
            this.durationMinutesRequested = durationMinutesRequested;
            this.wpmRequested = wpmRequested;
            this.fransworth = fransworth;
            this.stringsRequested = stringsRequested;
            this.sessionType = sessionType;
            this.keys = keys;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new TranscribeTrainingSessionViewModel(application, durationMinutesRequested, stringsRequested, wpmRequested, fransworth, sessionType, keys);
        }
    }

    public LiveData<List<TranscribeTrainingEngineSettings>> getLatestEngineSetting() {
        return repository.transcribeEngineSettingsDAO.getLatestEngineSetting(sessionType.name());
    }

    public void primeTheEngine(TranscribeTrainingEngineSettings previousSettings) {
        countDownTimer = setupCountDownTimer(1000 * (durationMinutesRequested * 60 + 1));
        engine = new TranscribeTrainingEngine(wpmRequested, farnsworth, stringsReqested);
        engine.prime();
    }

    private CountDownTimer setupCountDownTimer(long durationsMillis) {
        return new CountDownTimer(durationsMillis, 50) {
            public void onTick(long millisUntilFinished) {
                durationRemainingMillis.setValue(millisUntilFinished);
            }

            public void onFinish() {
                durationRemainingMillis.setValue(0l);
            }
        };
    }

    public void startTheEngine() {
        synchronized (sessionStartLock) {
            if (sessionHasBeenStarted) {
                Timber.d("Duped request to start the session");
                return;
            }
            engine.start();
            countDownTimer.start();
            sessionHasBeenStarted = true;
        }
    }

    public void recordSessionDetails() {
        Timber.d("Finishing Session");
        TranscribeTrainingSession trainingSession = new TranscribeTrainingSession();

        if (endTimeEpocMillis < 0) {
            trainingSession.endTimeEpocMillis = System.currentTimeMillis();
        } else {
            trainingSession.endTimeEpocMillis = endTimeEpocMillis;
        }
        long durationRequestedMillis = getDurationRequestedMillis();
        long durationWorkedMillis = durationRequestedMillis - durationRemainingMillis.getValue();

        trainingSession.endTimeEpocMillis = System.currentTimeMillis();
        trainingSession.durationRequestedMillis = durationRequestedMillis;
        trainingSession.durationWorkedMillis = durationWorkedMillis;
        trainingSession.completed = durationWorkedMillis == 0;
        // TODO
        // trainingSession.errorRate = need to do transcribe analysis to do this one
        trainingSession.sessionType = sessionType.name();
        if (Float.isNaN(trainingSession.errorRate)) {
            trainingSession.errorRate = -1;
        }

        repository.insertTranscribeTrainingSession(trainingSession);

        TranscribeTrainingEngineSettings settings = engine.getSettings();
        settings.durationRequestedMillis = durationRequestedMillis;
        settings.sessionType = sessionType.name();
        repository.insertTranscribeEngineSettings(settings);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        engine.destroy();
        recordSessionDetails();
    }
}
