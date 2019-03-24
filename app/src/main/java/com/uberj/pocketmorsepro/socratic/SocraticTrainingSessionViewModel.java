package com.uberj.pocketmorsepro.socratic;

import android.app.Application;

import com.uberj.pocketmorsepro.CWToneManager;
import com.uberj.pocketmorsepro.CountDownTimer;
import com.uberj.pocketmorsepro.KochLetterSequence;
import com.uberj.pocketmorsepro.keyboards.Keys;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingEngineSettings;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSession;
import com.uberj.pocketmorsepro.storage.Repository;
import com.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import timber.log.Timber;

class SocraticTrainingSessionViewModel extends AndroidViewModel {
    private static final String sessionStartLock = "Lock";
    private final Repository repository;

    private final Boolean resetWeights;
    // TODO remove minutes requested in favor of millis requested
    private final int durationMinutesRequested;
    private final long durationRequestedMillis;
    private final int wpmRequested;
    private final SocraticSessionType sessionType;
    private final Keys keys;

    private List<String> inPlayKeyNames;
    private CountDownTimer countDownTimer;
    private final MutableLiveData<Long> durationRemainingMillis = new MutableLiveData<>(-1L);
    private boolean sessiontHasBeenStarted = false;
    private int totalUniqueLettersChosen;
    private int totalCorrectGuesses;
    private int totalAccurateSymbolsGuessed;
    private int totalIncorrectGuesses;
    private long endTimeEpocMillis = -1;
    private SocraticTrainingEngine engine;
    private long currentLetterFirstPlayedAt;
    private String currentMessage;

    public SocraticTrainingSessionViewModel(@NonNull Application application, Boolean resetWeights, int durationMinutesRequested, int wpmRequested, SocraticSessionType sessionType, Keys keys) {
        super(application);
        this.resetWeights = resetWeights;
        this.durationMinutesRequested = durationMinutesRequested;
        this.durationRequestedMillis = 1000 * (durationMinutesRequested * 60);
        this.wpmRequested = wpmRequested;
        this.repository = new Repository(application);
        this.sessionType = sessionType;
        this.keys = keys;
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final Boolean resetWeights;
        private final int durationMinutesRequested;
        private final int wpmRequested;
        private final SocraticSessionType sessionType;
        private final Keys keys;


        public Factory(Application application, Boolean resetWeights, int durationMinutesRequested, int wpmRequested, SocraticSessionType sessionType, Keys keys) {
            this.application = application;
            this.resetWeights = resetWeights;
            this.durationMinutesRequested = durationMinutesRequested;
            this.wpmRequested = wpmRequested;
            this.sessionType = sessionType;
            this.keys = keys;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new SocraticTrainingSessionViewModel(application, resetWeights, durationMinutesRequested, wpmRequested, sessionType, keys);
        }
    }

    public LiveData<List<SocraticTrainingEngineSettings>> getLatestEngineSetting() {
        return repository.socraticEngineSettingsDAO.getLatestEngineSetting(sessionType.name());
    }

    public long getDurationRequestedMillis() {
        return durationRequestedMillis;
    }


    public void primeTheEngine(SocraticTrainingEngineSettings previousSettings) {
        Map<String, Integer> competencyWeights = buildInitialCompetencyWeights(resetWeights ? null : previousSettings);
        inPlayKeyNames = getInitialInPlayKeyNames(previousSettings);
        countDownTimer = setupCountDownTimer(1000 * (durationMinutesRequested * 60 + 1));
        engine = new SocraticTrainingEngine(KochLetterSequence.sequence, wpmRequested, this::letterChosenCallback, this::letterDonePlayingCallback, inPlayKeyNames, competencyWeights);
        engine.prime();
    }

    private void letterDonePlayingCallback(String playedLetter) {
        if (currentLetterFirstPlayedAt < 0) {
            currentLetterFirstPlayedAt = System.currentTimeMillis();
        }
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
        engine.destroy();
        recordSessionDetails();
    }

    private List<String> buildFirstTwoKeyList() {
        return Lists.newArrayList(
                KochLetterSequence.sequence.get(0),
                KochLetterSequence.sequence.get(1)
        );
    }

    private List<String> getInitialInPlayKeyNames(SocraticTrainingEngineSettings engineSettings) {
        if (engineSettings != null && engineSettings.activeLetters != null && engineSettings.activeLetters.size() != 0) {
            return engineSettings.activeLetters;
        }
        return buildFirstTwoKeyList();
    }

    private Map<String,Integer> buildBlankWeights(List<String> playableKeys) {
        Map<String, Integer> competencyWeights = Maps.newHashMap();
        for (String playableKey : playableKeys) {
            competencyWeights.put(playableKey, 0);
        }
        return competencyWeights;
    }

    private Map<String, Integer> buildInitialCompetencyWeights(SocraticTrainingEngineSettings weights) {
        if (weights == null) {
            return buildBlankWeights(keys.allPlayableKeysNames());
        }

        Map<String, Integer> competencyWeights;
        List<String> playableKeys = keys.allPlayableKeysNames();
        if (weights.weights.size() == 0) {
            competencyWeights = buildBlankWeights(playableKeys);
        } else {
            competencyWeights = weights.weights;
            for (String playableKey : playableKeys) {
                if (!competencyWeights.containsKey(playableKey)) {
                    competencyWeights.put(playableKey, 0);
                }
            }
        }

        return competencyWeights;
    }

    private float calcWpmAverage(long durationWorkedMillis) {
        int spacesBetweenLetters = (totalCorrectGuesses - 1) * 3;
        // accurateWords = (accurateSymbols / 50)
        float accurateSymbols = (float) (totalAccurateSymbolsGuessed + spacesBetweenLetters);
        float accurateWords = accurateSymbols / 50f;
        // wpmAverage = accurateWords / minutes
        float minutesWorked = (float) (durationWorkedMillis / 1000) / 60;
        return accurateWords / minutesWorked;
    }

    public List<String> getInPlayKeyNames() {
        return this.inPlayKeyNames;
    }

    public void recordSessionDetails() {
        Timber.d("Finishing Session");
        SocraticTrainingSession trainingSession = new SocraticTrainingSession();

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

        repository.insertSocraticTrainingSessionAndEvents(trainingSession, engine.events);

        SocraticTrainingEngineSettings settings = engine.getSettings();
        settings.durationRequestedMillis = durationRequestedMillis;
        settings.sessionType = sessionType.name();
        repository.insertSocraticEngineSettings(settings);
    }

    public void updateCompetencyWeights(String letter, boolean wasCorrectGuess) {
        if (wasCorrectGuess) {
            totalCorrectGuesses++;
            totalAccurateSymbolsGuessed += CWToneManager.numSymbolsForStringNoFarnsworth(letter);
        } else {
            totalIncorrectGuesses++;
        }
    }

    public SocraticTrainingEngine getEngine() {
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

    private void letterChosenCallback(String letterChosen) {
        totalUniqueLettersChosen++;
    }

    public LiveData<Long> getDurationRemainingMillis() {
        return durationRemainingMillis;
    }

    public void setDurationRemainingMillis(long durationRemainingMillis) {
        this.durationRemainingMillis.setValue(durationRemainingMillis);
    }
}
