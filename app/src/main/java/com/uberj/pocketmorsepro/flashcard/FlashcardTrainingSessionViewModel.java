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
    private static final String sessionStartLock = "Lock";
    private final Repository repository;

    public final MutableLiveData<List<String>> transcribedMessage = new MutableLiveData<>(Lists.newArrayList());
    private final int durationMinutesRequested;
    private final long durationRequestedMillis;
    private final int wpmRequested;
    private final FlashcardSessionType sessionType;
    private final int toneFrequency;
    private final List<String> requestedMessages;

    private CountDownTimer countDownTimer;
    private final MutableLiveData<Long> durationRemainingMillis = new MutableLiveData<>(-1L);
    private boolean sessiontHasBeenStarted = false;
    private long endTimeEpocMillis = -1;
    private FlashcardTrainingEngine engine;
    private AudioManager audioManager;

    public FlashcardTrainingSessionViewModel(@NonNull Application application, List<String> requestedMessages, int durationMinutesRequested, int wpmRequested, int toneFrequency, FlashcardSessionType sessionType) {
        super(application);
        this.durationMinutesRequested = durationMinutesRequested;
        this.durationRequestedMillis = 1000 * (durationMinutesRequested * 60);
        this.wpmRequested = wpmRequested;
        this.toneFrequency = toneFrequency;
        this.repository = new Repository(application);
        this.sessionType = sessionType;
        this.requestedMessages = requestedMessages;
        primeTheEngine();
        startTheEngine();
    }


    public void prepairShutDown() {
        engine.destroy();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final int durationMinutesRequested;
        private final int wpmRequested;
        private final FlashcardSessionType sessionType;
        private final int toneFrequency;
        private final ArrayList<String> requestedMessages;


        public Factory(Application application, ArrayList<String> requestedMessages, int durationMinutesRequested, int wpmRequested, int toneFrequency, FlashcardSessionType sessionType) {
            this.application = application;
            this.requestedMessages = requestedMessages;
            this.durationMinutesRequested = durationMinutesRequested;
            this.wpmRequested = wpmRequested;
            this.toneFrequency = toneFrequency;
            this.sessionType = sessionType;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new FlashcardTrainingSessionViewModel(application, requestedMessages, durationMinutesRequested, wpmRequested, toneFrequency, sessionType);
        }
    }

    public long getDurationRequestedMillis() {
        return durationRequestedMillis;
    }


    public void primeTheEngine() {
        countDownTimer = setupCountDownTimer(1000 * (durationMinutesRequested * 60 + 1));
        audioManager = new AudioManager(wpmRequested, toneFrequency, getApplication().getResources());
        engine = new FlashcardTrainingEngine(audioManager, requestedMessages);
        engine.prime();
    }

    public void startTheEngine() {
        synchronized (sessionStartLock) {
            if (sessiontHasBeenStarted) {
                Timber.d("Duped request to start the session");
                return;
            }
            engine.start();
            countDownTimer.start();
            sessiontHasBeenStarted = true;
        }
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


    @Override
    protected void onCleared() {
        super.onCleared();
        recordSessionDetails();
    }

    public void recordSessionDetails() {
        Timber.d("Finishing Session");
        FlashcardTrainingSession trainingSession = new FlashcardTrainingSession();

        if (endTimeEpocMillis < 0) {
            trainingSession.endTimeEpocMillis = System.currentTimeMillis();
        } else {
            trainingSession.endTimeEpocMillis = endTimeEpocMillis;
        }
        long durationWorkedMillis = durationRequestedMillis - durationRemainingMillis.getValue();

        trainingSession.endTimeEpocMillis = System.currentTimeMillis();
        trainingSession.durationRequestedMillis = durationRequestedMillis;
        trainingSession.durationWorkedMillis = durationWorkedMillis;
        trainingSession.completed = durationWorkedMillis == 0;
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
        return countDownTimer.isPaused();
    }

    public LiveData<Long> getDurationRemainingMillis() {
        return durationRemainingMillis;
    }

    public void setDurationRemainingMillis(long durationRemainingMillis) {
        this.durationRemainingMillis.setValue(durationRemainingMillis);
    }
}
