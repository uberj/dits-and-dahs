package com.example.uberj.test1.transcribe;

import android.app.Application;

import com.example.uberj.test1.CountDownTimer;
import com.example.uberj.test1.keyboards.Keys;
import com.example.uberj.test1.storage.Repository;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionType;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingEngineSettings;

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
    private final int farnsworthSpaces;
    public final MutableLiveData<Long> durationRemainingMillis = new MutableLiveData<>(-1L);
    private CountDownTimer countDownTimer;
    private TranscribeTrainingEngine engine;
    private boolean sessionHasBeenStarted = false;
    private static final String sessionStartLock = "lock";

    public TranscribeTrainingSessionViewModel(@NonNull Application application, int durationMinutesRequested, int wpmRequested, int farnsworthSpaces, TranscribeSessionType sessionType, Keys keys) {
        super(application);
        this.repository = new Repository(application);
        this.application = application;
        this.durationMinutesRequested = durationMinutesRequested;
        this.wpmRequested = wpmRequested;
        this.farnsworthSpaces = farnsworthSpaces;
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
        private final int fransworthSpaces;
        private final TranscribeSessionType sessionType;
        private final Keys keys;


        public Factory(Application application, int durationMinutesRequested, int wpmRequested, int fransworthSpaces, TranscribeSessionType sessionType, Keys keys) {
            this.application = application;
            this.durationMinutesRequested = durationMinutesRequested;
            this.wpmRequested = wpmRequested;
            this.fransworthSpaces = fransworthSpaces;
            this.sessionType = sessionType;
            this.keys = keys;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new TranscribeTrainingSessionViewModel(application, durationMinutesRequested, wpmRequested, fransworthSpaces, sessionType, keys);
        }
    }

    public LiveData<List<TranscribeTrainingEngineSettings>> getLatestEngineSetting() {
        return repository.transcribeEngineSettingsDAO.getLatestEngineSetting(sessionType.name());
    }

    public void primeTheEngine(TranscribeTrainingEngineSettings previousSettings) {
        List<String> inPlayKeyNames = null;
        countDownTimer = setupCountDownTimer(1000 * (durationMinutesRequested * 60 + 1));
        engine = new TranscribeTrainingEngine(wpmRequested, farnsworthSpaces, inPlayKeyNames);
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
}
