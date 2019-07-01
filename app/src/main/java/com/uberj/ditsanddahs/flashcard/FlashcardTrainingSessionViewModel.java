package com.uberj.ditsanddahs.flashcard;

import android.app.Application;

import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.AudioManager;
import com.uberj.ditsanddahs.CountDownTimer;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardSessionType;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardTrainingSession;
import com.uberj.ditsanddahs.storage.Repository;

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
    private final int fadeInOutPercentage;

    private CountDownTimer countDownTimer;
    private final MutableLiveData<Long> durationUnitsRemaining = new MutableLiveData<>(-1L);
    private boolean sessiontHasBeenStarted = false;
    private long endTimeEpocMillis = -1;
    private FlashcardTrainingEngine engine;
    private AudioManager audioManager;

    public FlashcardTrainingSessionViewModel(@NonNull Application application, List<String> requestedMessages, int durationUnitsRequested, String durationUnit, int wpmRequested, int toneFrequency, FlashcardSessionType sessionType, int fadeInOutPercentage) {
        super(application);
        this.durationUnitsRequested = durationUnitsRequested;
        this.durationUnit = durationUnit;
        this.durationRequestedMillis = 1000 * (durationUnitsRequested * 60);
        this.wpmRequested = wpmRequested;
        this.toneFrequency = toneFrequency;
        this.repository = new Repository(application);
        this.sessionType = sessionType;
        this.requestedMessages = requestedMessages;
        this.fadeInOutPercentage = fadeInOutPercentage;
        primeTheEngine();
        startTheEngine();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final int durationUnitsRequested;
        private final String durationUnit;
        private final int wpmRequested;
        private final FlashcardSessionType sessionType;
        private final int toneFrequency;
        private final ArrayList<String> requestedMessages;
        private int fadeInOutPercentage;


        public Factory(Application application, ArrayList<String> requestedMessages, int durationUnitsRequested, String durationUnit, int wpmRequested, int toneFrequency, FlashcardSessionType sessionType, int fadeInOutPercentage) {
            this.application = application;
            this.requestedMessages = requestedMessages;
            this.durationUnitsRequested = durationUnitsRequested;
            this.durationUnit = durationUnit;
            this.wpmRequested = wpmRequested;
            this.toneFrequency = toneFrequency;
            this.sessionType = sessionType;
            this.fadeInOutPercentage = fadeInOutPercentage;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new FlashcardTrainingSessionViewModel(application, requestedMessages, durationUnitsRequested, durationUnit, wpmRequested, toneFrequency, sessionType, fadeInOutPercentage);
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
        AudioManager.MorseConfig.Builder morseConfig = AudioManager.MorseConfig.builder();
        morseConfig.setLetterWpm(wpmRequested);
        morseConfig.setEffectiveWpm(wpmRequested);
        morseConfig.setFadeInOutPercentage(fadeInOutPercentage);
        morseConfig.setToneFrequencyHz(toneFrequency);
        audioManager = new AudioManager(getApplication().getResources());
        if (durationUnit.equals(TIME_LIMITED_SESSION_TYPE)) {
            countDownTimer = setupCountDownTimer(1000 * (durationUnitsRequested * 60 + 1));
            engine = new FlashcardTrainingEngine(audioManager, sessionType, requestedMessages, null, morseConfig.build());
        } else {
            durationUnitsRemaining.setValue(durationUnitsRequested);
            engine = new FlashcardTrainingEngine(audioManager, sessionType, requestedMessages, durationUnitsRemaining, morseConfig.build());
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
        engine.destroy();
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
