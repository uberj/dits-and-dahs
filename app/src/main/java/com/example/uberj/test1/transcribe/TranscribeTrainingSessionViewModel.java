package com.example.uberj.test1.transcribe;

import android.app.Application;

import com.example.uberj.test1.CountDownTimer;
import com.example.uberj.test1.keyboards.Keys;
import com.example.uberj.test1.storage.Repository;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionType;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingEngineSettings;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingSession;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import timber.log.Timber;

public class TranscribeTrainingSessionViewModel extends AndroidViewModel {
    private final int durationMinutesRequested;
    private final int letterWpmRequested;
    private final int effectiveWpmRequested;
    private final TranscribeSessionType sessionType;
    private final Keys keys;
    private final Repository repository;
    private final int farnsworth;
    public final MutableLiveData<Long> durationRemainingMillis = new MutableLiveData<>(-1L);
    public final MutableLiveData<List<String>> transcribedMessage = new MutableLiveData<>(Lists.newArrayList());
    private final ArrayList<String> stringsRequested;
    private final boolean targetIssueLetters;
    private CountDownTimer countDownTimer;
    private TranscribeTrainingEngine engine;
    private boolean sessionHasBeenStarted = false;
    private static final String sessionStartLock = "lock";
    private long endTimeEpocMillis = -1;
    private long sessionEndingTimeBufferCuttOffMillis = 5 * 1000;
    private List<String> playedMessage = Lists.newArrayList();

    public TranscribeTrainingSessionViewModel(@NonNull Application application, int durationMinutesRequested, ArrayList<String> stringsRequested, int letterWpmRequested, int effectiveWpmRequested, int farnsworth, boolean targetIssueLetters, TranscribeSessionType sessionType, Keys keys) {
        super(application);
        this.repository = new Repository(application);
        this.durationMinutesRequested = durationMinutesRequested;
        this.letterWpmRequested = letterWpmRequested;
        this.effectiveWpmRequested = effectiveWpmRequested;
        this.stringsRequested = stringsRequested;
        this.farnsworth = farnsworth;
        this.targetIssueLetters = targetIssueLetters;
        this.sessionType = sessionType;
        this.keys = keys;
    }

    public long getDurationRequestedMillis() {
        return durationMinutesRequested * 60 * 1000;
    }

    public boolean isPaused() {
        return engine.isPaused();
    }

    public void pause() {
        countDownTimer.pause();
        engine.pause();
    }

    public void resume() {
        countDownTimer.resume();
        engine.resume();
    }

    public boolean isARequstedString(String buttonLetter) {
        return stringsRequested.contains(buttonLetter);
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final int durationMinutesRequested;
        private final int letterWpmRequested;
        private final int effectivetWpmRequested;
        private final int fransworth;
        private final ArrayList<String> stringsRequested;
        private final TranscribeSessionType sessionType;
        private final Keys keys;
        private final boolean targetIssueLetters;


        public Factory(Application application, int durationMinutesRequested, int letterWpmRequested, int effectivetWpmRequested, ArrayList<String> stringsRequested, int fransworth, boolean targetIssueLetters, TranscribeSessionType sessionType, Keys keys) {
            this.application = application;
            this.durationMinutesRequested = durationMinutesRequested;
            this.letterWpmRequested = letterWpmRequested;
            this.effectivetWpmRequested = effectivetWpmRequested;
            this.fransworth = fransworth;
            this.targetIssueLetters = targetIssueLetters;
            this.stringsRequested = stringsRequested;
            this.sessionType = sessionType;
            this.keys = keys;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new TranscribeTrainingSessionViewModel(application, durationMinutesRequested, stringsRequested, letterWpmRequested, effectivetWpmRequested, fransworth, targetIssueLetters, sessionType, keys);
        }
    }

    public LiveData<List<TranscribeTrainingEngineSettings>> getLatestEngineSetting() {
        return repository.transcribeEngineSettingsDAO.getLatestEngineSetting(sessionType.name());
    }

    public LiveData<List<TranscribeTrainingSession>> getLatestTrainingSession() {
        return repository.transcribeTrainingSessionDAO.getLatestSession(sessionType.name());
    }

    private void letterPlayedCallback(String letter) {
        playedMessage.add(letter);
    }

    public void primeTheEngine(TranscribeTrainingSession prevSession) {
        countDownTimer = setupCountDownTimer(1000 * (durationMinutesRequested * 60 + 1));
        Map<String, Double> errorMap = null;
        if (prevSession != null && targetIssueLetters) {
            errorMap = TranscribeUtil.calculateErrorMap(prevSession);
        }
        List<Pair<String, Double>> weightedRequestedStrings = Lists.newArrayList();
        for (String s : stringsRequested) {
            Double error;
            if (errorMap != null) {
                error = errorMap.getOrDefault(s, 0D);
            } else {
                error = 0D;
            }
            weightedRequestedStrings.add(Pair.of(s, 1D + error));
        }
        engine = new TranscribeTrainingEngine(letterWpmRequested, effectiveWpmRequested, weightedRequestedStrings, this::letterPlayedCallback);
        engine.prime();
    }

    private CountDownTimer setupCountDownTimer(long durationsMillis) {
        return new CountDownTimer(durationsMillis, 50) {
            public void onTick(long millisUntilFinished) {
                durationRemainingMillis.setValue(millisUntilFinished);
                if (millisUntilFinished <= sessionEndingTimeBufferCuttOffMillis) {
                    engine.prepareForShutdown();
                }
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
        trainingSession.targetIssueLetters = targetIssueLetters;
        trainingSession.effectiveWpm = (long) effectiveWpmRequested;
        trainingSession.letterWpm = (long) letterWpmRequested;

        trainingSession.sessionType = sessionType.name();
        if (Double.isNaN(trainingSession.overallAccuracyRate)) {
            trainingSession.overallAccuracyRate = -1;
        }

        trainingSession.stringsRequested = stringsRequested;

        trainingSession.playedMessage = playedMessage;
        trainingSession.enteredKeys = transcribedMessage.getValue();

        repository.insertTranscribeTrainingSession(trainingSession);

        TranscribeTrainingEngineSettings settings = engine.getSettings();
        settings.durationRequestedMillis = durationRequestedMillis;
        settings.sessionType = sessionType.name();
        settings.targetIssueLetters = targetIssueLetters;
        repository.insertTranscribeEngineSettings(settings);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        engine.destroy();
        recordSessionDetails();
    }
}
