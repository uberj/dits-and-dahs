package com.uberj.ditsanddahs.simplesocratic;

import android.app.Application;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.uberj.ditsanddahs.AudioManager;
import com.uberj.ditsanddahs.CountDownTimer;
import com.uberj.ditsanddahs.GlobalSettings;
import com.uberj.ditsanddahs.KochLetterSequence;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.keyboards.Keys;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingEngineSettings;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingSession;
import com.uberj.ditsanddahs.storage.Repository;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticSessionType;
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
    private static final int CORRECT_GUESS = 100;
    private static final int INCORRECT_GUESS = 200;
    private final Repository repository;

    private final Boolean resetWeights;
    private final SocraticSessionType sessionType;
    private final Keys keys;
    public final MediaPlayer incorrectSound;
    public final MediaPlayer correctSound;
    private final HandlerThread guessSoundHandlerThread;
    private final Handler guessHandler;
    private final SocraticSettings socraticSettings;
    private final long durationRequestedMillis;
    public boolean enableAutoLetterIntroduce;

    private List<String> inPlayKeyNames;
    private CountDownTimer countDownTimer;
    private final MutableLiveData<Long> durationRemainingMillis = new MutableLiveData<>(-1L);
    private boolean sessiontHasBeenStarted = false;
    private long endTimeEpocMillis = -1;
    private SocraticTrainingEngine engine;
    private AudioManager audioManager;

    public SocraticTrainingSessionViewModel(@NonNull Application application, Boolean resetWeights, SocraticSessionType sessionType, Keys keys, SocraticSettings socraticSettings) {
        super(application);
        guessSoundHandlerThread = new HandlerThread("GuessSoundHandler", HandlerThread.MAX_PRIORITY);
        guessSoundHandlerThread.start();
        guessHandler = new Handler(guessSoundHandlerThread.getLooper(), this::guessCallBack);
        this.incorrectSound = MediaPlayer.create(application.getBaseContext(), R.raw.incorrect_mp3);
        this.correctSound = MediaPlayer.create(application.getBaseContext(), R.raw.correct_wav);
        this.resetWeights = resetWeights;
        this.socraticSettings = socraticSettings;
        this.durationRequestedMillis = 1000 * (socraticSettings.durationMinutesRequested * 60);
//        this.wpmRequested = socraticSettings.wpmRequested;
//        this.toneFrequency = socraticSettings.toneFrequency;
//        this.easyMode = socraticSettings.easyMode;
        this.repository = new Repository(application);
        this.sessionType = sessionType;
        this.keys = keys;
        this.enableAutoLetterIntroduce = socraticSettings.enableAutoLetterIntroduce;
    }

    private boolean guessCallBack(Message message) {
        if (socraticSettings.easyMode) {
            if (message.what == CORRECT_GUESS) {
                audioManager.playCorrectTone();
            } else if (message.what == INCORRECT_GUESS) {
                audioManager.playIncorrectTone();
            }
        }
        return true;
    }

    public void playCorrectSound() {
        guessHandler.sendEmptyMessage(CORRECT_GUESS);
    }

    public void playIncorrectSound() {
        guessHandler.sendEmptyMessage(INCORRECT_GUESS);
    }

    public void prepairShutDown() {
        engine.destroy();
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final Boolean resetWeights;
        private final SocraticSessionType sessionType;
        private final Keys keys;
        private final SocraticSettings socraticSettings;


        public Factory(Application application, Boolean resetWeights, SocraticSessionType sessionType, Keys keys, SocraticSettings socraticSettings) {
            this.application = application;
            this.resetWeights = resetWeights;
            this.sessionType = sessionType;
            this.keys = keys;
            this.socraticSettings = socraticSettings;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new SocraticTrainingSessionViewModel(application, resetWeights, sessionType, keys, socraticSettings);
        }
    }

    public LiveData<List<SocraticTrainingEngineSettings>> getLatestEngineSetting() {
        return repository.socraticEngineSettingsDAO.getLatestEngineSetting(sessionType.name());
    }

    public long getDurationRequestedMillis() {
        return durationRequestedMillis;
    }


    public void setUp(SocraticTrainingEngineSettings previousSettings, GlobalSettings globalSettings) {
        Map<String, Integer> competencyWeights = buildInitialCompetencyWeights(resetWeights ? null : previousSettings);
        inPlayKeyNames = getInitialInPlayKeyNames(previousSettings);
        countDownTimer = setupCountDownTimer(durationRequestedMillis + 1000);
        AudioManager.MorseConfig.Builder morseConfig = AudioManager.MorseConfig.builder();
        morseConfig.setToneFrequencyHz(socraticSettings.toneFrequency);
        morseConfig.setGlobalSettings(globalSettings);
        morseConfig.setLetterWpm(socraticSettings.wpmRequested);
        morseConfig.setEffectiveWpm(socraticSettings.wpmRequested);
        audioManager = new AudioManager(getApplication().getResources());
        engine = new SocraticTrainingEngine(audioManager, KochLetterSequence.sequence, this::letterChosenCallback, inPlayKeyNames, competencyWeights, morseConfig.build(), socraticSettings);
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
                engine.timeClick(millisUntilFinished);
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
        trainingSession.easyMode = socraticSettings.easyMode;

        repository.insertSocraticTrainingSessionAndEvents(trainingSession, engine.events);

        SocraticTrainingEngineSettings settings = engine.getSettings();
        settings.durationRequestedMillis = durationRequestedMillis;
        settings.sessionType = sessionType.name();
        repository.insertSocraticEngineSettings(settings);
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
    }

    public LiveData<Long> getDurationRemainingMillis() {
        return durationRemainingMillis;
    }

    public void setDurationRemainingMillis(long durationRemainingMillis) {
        this.durationRemainingMillis.setValue(durationRemainingMillis);
    }
}
