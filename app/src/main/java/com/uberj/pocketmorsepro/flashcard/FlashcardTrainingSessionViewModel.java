package com.uberj.pocketmorsepro.flashcard;

import android.app.Application;

import com.google.common.collect.Lists;
import com.uberj.pocketmorsepro.AudioManager;
import com.uberj.pocketmorsepro.CountDownTimer;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardSessionType;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardTrainingSession;
import com.uberj.pocketmorsepro.storage.Repository;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import timber.log.Timber;

class FlashcardTrainingSessionViewModel extends AndroidViewModel {
    public static final String TIME_LIMITED_SESSION_TYPE = "time_limited";
    private static final String sessionStartLock = "Lock";
    private final Repository repository;

    public final MutableLiveData<List<String>> transcribedMessage = new MutableLiveData<>(Lists.newArrayList());
    private final long durationUnitsRequested;
    private final long durationRequestedMillis;
    private final String durationUnit;
    private final int wpmRequested;
    private final FlashcardSessionType sessionType;
    private final int toneFrequency;
    private final List<String> requestedMessages;

    private CountDownTimer countDownTimer;
    private final MutableLiveData<Long> durationUnitsRemaining = new MutableLiveData<>(-1L);
    private boolean sessiontHasBeenStarted = false;
    private long endTimeEpocMillis = -1;
    private FlashcardTrainingEngine engine;
    private AudioManager audioManager;

    public FlashcardTrainingSessionViewModel(@NonNull Application application, List<String> requestedMessages, int durationUnitsRequested, String durationUnit, int wpmRequested, int toneFrequency, FlashcardSessionType sessionType) {
        super(application);
        this.durationUnitsRequested = durationUnitsRequested;
        this.durationUnit = durationUnit;
        this.durationRequestedMillis = 1000 * (durationUnitsRequested * 60);
        this.wpmRequested = wpmRequested;
        this.toneFrequency = toneFrequency;
        this.repository = new Repository(application);
        this.sessionType = sessionType;
        this.requestedMessages = requestedMessages;
        primeTheEngine();
        startTheEngine();
    }


    public void destroyEngine() {
        engine.destroy();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final int durationUnitsRequested;
        private final String durationUnit;
        private final int wpmRequested;
        private final FlashcardSessionType sessionType;
        private final int toneFrequency;
        private final ArrayList<String> requestedMessages;


        public Factory(Application application, ArrayList<String> requestedMessages, int durationUnitsRequested, String durationUnit, int wpmRequested, int toneFrequency, FlashcardSessionType sessionType) {
            this.application = application;
            this.requestedMessages = requestedMessages;
            this.durationUnitsRequested = durationUnitsRequested;
            this.durationUnit = durationUnit;
            this.wpmRequested = wpmRequested;
            this.toneFrequency = toneFrequency;
            this.sessionType = sessionType;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new FlashcardTrainingSessionViewModel(application, requestedMessages, durationUnitsRequested, durationUnit, wpmRequested, toneFrequency, sessionType);
        }
    }

    public long getDurationRequested() {
        if (durationUnit.equals(TIME_LIMITED_SESSION_TYPE)) {
            return durationRequestedMillis;
        } else {
            return -1L;
        }
    }


    public void primeTheEngine() {
        audioManager = new AudioManager(wpmRequested, toneFrequency, getApplication().getResources());
        if (durationUnit.equals(TIME_LIMITED_SESSION_TYPE)) {
            countDownTimer = setupCountDownTimer(1000 * (durationUnitsRequested * 60 + 1));
            engine = new FlashcardTrainingEngine(audioManager, requestedMessages, null);
        } else {
            durationUnitsRemaining.setValue(durationUnitsRequested);
            engine = new FlashcardTrainingEngine(audioManager, requestedMessages, durationUnitsRemaining);
        }
        engine.prime();
    }

    public void startTheEngine() {
        synchronized (sessionStartLock) {
            if (sessiontHasBeenStarted) {
                Timber.d("Duped request to start the session");
                return;
            }
            engine.start();
            if (countDownTimer != null) {
                countDownTimer.start();
            }
            sessiontHasBeenStarted = true;
        }
    }

    private CountDownTimer setupCountDownTimer(long durationsMillis) {
        return new CountDownTimer(durationsMillis, 50) {
            public void onTick(long millisUntilFinished) {
                durationUnitsRemaining.setValue(millisUntilFinished);
            }

            public void onFinish() {
                durationUnitsRemaining.setValue(0l);
            }
        };
    }


    @Override
    protected void onCleared() {
        super.onCleared();
        destroyEngine();
        recordSessionDetails();
    }

    public void recordSessionDetails() {
        Timber.d("Finishing Session");
        FlashcardTrainingSession trainingSession = new FlashcardTrainingSession();

        trainingSession.endTimeEpocMillis = System.currentTimeMillis();
        trainingSession.durationUnitsRequested = durationUnitsRequested;
        trainingSession.durationUnit = durationUnit;
        trainingSession.sessionType = sessionType.name();
        trainingSession.cards = requestedMessages;

        repository.insertFlashcardTrainingSessionAndEvents(trainingSession, engine.events);
    }

    public FlashcardTrainingEngine getEngine() {
        return engine;
    }

    public void pause() {
        if (engine == null) {
            return;
        }
        if (countDownTimer != null) {
            countDownTimer.pause();
        }
        engine.pause();
        endTimeEpocMillis = System.currentTimeMillis();

    }

    public void resume() {
        if (engine == null) {
            return;
        }
        engine.resume();
        endTimeEpocMillis = -1;
        if (countDownTimer != null && countDownTimer.isPaused()) {
            countDownTimer.resume();
        }
    }

    public boolean isPaused() {
        if (countDownTimer != null) {
            return countDownTimer.isPaused();
        }
        return false;
    }

    public LiveData<Long> getDurationUnitsRemaining() {
        return durationUnitsRemaining;
    }

    public void setDurationUnitsRemainingMillis(long durationUnitsRemainingMillis) {
        this.durationUnitsRemaining.setValue(durationUnitsRemainingMillis);
    }
}
