package com.uberj.ditsanddahs.transcribe;

import android.app.Application;

import com.annimon.stream.function.Supplier;
import com.google.common.base.Preconditions;
import com.uberj.ditsanddahs.AudioManager;
import com.uberj.ditsanddahs.CountDownTimer;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.qsolib.RandomQSO;
import com.uberj.ditsanddahs.storage.Repository;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeSessionType;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeTrainingSession;
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
    private final Repository repository;
    public final MutableLiveData<Boolean> sessionIsFinished = new MutableLiveData<>(false);
    public final MutableLiveData<Long> durationRemainingMillis = new MutableLiveData<>(-1L);
    public final MutableLiveData<List<String>> transcribedMessage = new MutableLiveData<>(Lists.newArrayList());
    public final MutableLiveData<String> titleText = new MutableLiveData<>();
    public final MutableLiveData<Boolean> sessionHasBeenStarted= new MutableLiveData<>(false);
    public final MutableLiveData<Boolean> endTimerInProgress = new MutableLiveData<>(false);
    private final ArrayList<String> stringsRequested;
    private final boolean targetIssueLetters;
    private final int audioToneFrequency;
    private final int secondAudioToneFrequency;
    private final int startDelaySeconds;
    private final int endDelaySeconds;
    private final int secondsBetweenStationTransmissions;
    private final String startTimerTitleString;
    private final String endTimerTitleString;
    private CountDownTimer countDownTimer = null;
    private TranscribeTrainingEngine engine;
    private long endTimeEpocMillis = -1;
    private List<String> playedMessage = Lists.newArrayList();
    private final int fadeInOutPercentage;
    private CountDownTimer startTimer = null;
    private CountDownTimer endTimer = null;

    public static class Params {
        private final int durationMinutesRequested;
        private final ArrayList<String> stringsRequested;
        private final int letterWpmRequested;
        private final int effectiveWpmRequested;
        private final boolean targetIssueLetters;
        private final TranscribeSessionType sessionType;
        private final int audioToneFrequency;
        private final int startDelaySeconds;
        private final int endDelaySeconds;
        private final int fadeInOutPercentage;
        private final int secondAudioToneFrequency;
        private final int secondsBetweenStationTransmissions;

        private Params(int durationMinutesRequested, ArrayList<String> stringsRequested, int letterWpmRequested, int effectiveWpmRequested, boolean targetIssueLetters, TranscribeSessionType sessionType, int audioToneFrequency, int startDelaySeconds, int endDelaySeconds, int fadeInOutPercentage, int secondAudioToneFrequency, Integer secondsBetweenStationTransmissions) {
            this.durationMinutesRequested = durationMinutesRequested;
            this.stringsRequested = stringsRequested;
            this.letterWpmRequested = letterWpmRequested;
            this.effectiveWpmRequested = effectiveWpmRequested;
            this.targetIssueLetters = targetIssueLetters;
            this.sessionType = sessionType;
            this.audioToneFrequency = audioToneFrequency;
            this.startDelaySeconds = startDelaySeconds;
            this.endDelaySeconds = endDelaySeconds;
            this.fadeInOutPercentage = fadeInOutPercentage;
            this.secondAudioToneFrequency = secondAudioToneFrequency;
            this.secondsBetweenStationTransmissions = secondsBetweenStationTransmissions;
        }

        public static Builder builder() {
            return new Builder();
        }

        public static class Builder {
            private Integer durationMinutesRequested;
            private ArrayList<String> stringsRequested;
            private Integer letterWpmRequested;
            private Integer effectiveWpmRequested;
            private Boolean targetIssueLetters;
            private TranscribeSessionType sessionType;
            private Integer audioToneFrequency;
            private Integer startDelaySeconds;
            private Integer endDelaySeconds;
            private Integer fadeInOutPercentage;
            private Integer secondAudioToneFrequency = -1;
            private Integer secondsBetweenStationTransmissions = -1;

            public Builder setDurationMinutesRequested(int durationMinutesRequested) {
                this.durationMinutesRequested = durationMinutesRequested;
                return this;
            }

            public Builder setStringsRequested(ArrayList<String> stringsRequested) {
                this.stringsRequested = stringsRequested;
                return this;
            }

            public Builder setLetterWpmRequested(int letterWpmRequested) {
                this.letterWpmRequested = letterWpmRequested;
                return this;
            }

            public Builder setEffectiveWpmRequested(int effectiveWpmRequested) {
                this.effectiveWpmRequested = effectiveWpmRequested;
                return this;
            }

            public Builder setTargetIssueLetters(boolean targetIssueLetters) {
                this.targetIssueLetters = targetIssueLetters;
                return this;
            }

            public Builder setSessionType(TranscribeSessionType sessionType) {
                this.sessionType = sessionType;
                return this;
            }

            public Builder setAudioToneFrequency(int audioToneFrequency) {
                this.audioToneFrequency = audioToneFrequency;
                return this;
            }

            public Builder setStartDelaySeconds(int startDelaySeconds) {
                this.startDelaySeconds = startDelaySeconds;
                return this;
            }

            public Builder setEndDelaySeconds(int endDelaySeconds) {
                this.endDelaySeconds = endDelaySeconds;
                return this;
            }

            public Builder setFadeInOutPercentage(int fadeInOutPercentage) {
                this.fadeInOutPercentage = fadeInOutPercentage;
                return this;
            }

            public Builder setSecondAudioToneFrequency(int secondAudioToneFrequency) {
                this.secondAudioToneFrequency = secondAudioToneFrequency;
                return this;
            }

            public Builder setSecondsBetweenStationTransmissions(int secondsBetweenStationTransmissions) {
                this.secondsBetweenStationTransmissions = secondsBetweenStationTransmissions;
                return this;
            }

            public Params build() {
                Preconditions.checkNotNull(sessionType);
                if (sessionType.equals(TranscribeSessionType.RANDOM_LETTER_ONLY)) {
                    Preconditions.checkNotNull(stringsRequested);
                } else {
                    Preconditions.checkArgument(stringsRequested == null);
                    Preconditions.checkNotNull(secondAudioToneFrequency);
                    Preconditions.checkNotNull(secondsBetweenStationTransmissions);
                }

                Preconditions.checkNotNull(letterWpmRequested);
                Preconditions.checkNotNull(effectiveWpmRequested);
                Preconditions.checkNotNull(targetIssueLetters);
                Preconditions.checkNotNull(audioToneFrequency);
                Preconditions.checkNotNull(startDelaySeconds);
                Preconditions.checkNotNull(endDelaySeconds);
                Preconditions.checkNotNull(fadeInOutPercentage);
                return new Params(durationMinutesRequested, stringsRequested, letterWpmRequested, effectiveWpmRequested, targetIssueLetters, sessionType, audioToneFrequency, startDelaySeconds, endDelaySeconds, fadeInOutPercentage, secondAudioToneFrequency, secondsBetweenStationTransmissions);
            }
        }
    }

    public TranscribeTrainingSessionViewModel(@NonNull Application application, Params params) {
        super(application);
        this.startTimerTitleString = application.getResources().getString(R.string.start_timer_title);
        this.endTimerTitleString = application.getResources().getString(R.string.end_timer_title);
        this.repository = new Repository(application);
        this.durationMinutesRequested = params.durationMinutesRequested;
        this.letterWpmRequested = params.letterWpmRequested;
        this.effectiveWpmRequested = params.effectiveWpmRequested;
        this.stringsRequested = params.stringsRequested;
        this.targetIssueLetters = params.targetIssueLetters;
        this.sessionType = params.sessionType;
        this.audioToneFrequency = params.audioToneFrequency;
        this.startDelaySeconds = params.startDelaySeconds;
        this.endDelaySeconds = params.endDelaySeconds;
        this.fadeInOutPercentage = params.fadeInOutPercentage;
        this.secondAudioToneFrequency = params.secondAudioToneFrequency;
        this.secondsBetweenStationTransmissions = params.secondsBetweenStationTransmissions;
    }

    public void finishSessionWithTimer(int finalEndDelaySeconds) {
        // Some weird stuff going on in this CountDownTimer -- instead of fixing it, I'll just hack it until it works, thus I subtract 1
        endTimer = new CountDownTimer((finalEndDelaySeconds * 1000) - 1, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                titleText.postValue(String.format(endTimerTitleString, (millisUntilFinished / 1000) + 1));
            }

            @Override
            public void onFinish() {
                sessionIsFinished.postValue(true);
            }
        };
        endTimerInProgress.postValue(true);
        endTimer.start();
    }

    public void primeEngineWithCountdown(TranscribeTrainingSession session) {
        startTimer = new CountDownTimer(startDelaySeconds * 1000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                titleText.postValue(String.format(startTimerTitleString, (millisUntilFinished / 1000) + 1));
            }

            @Override
            public void onFinish() {
                titleText.postValue("");
                primeTheEngine(session);
                startTheEngine();
            }
        };
        startTimer.start();
    }

    public long getDurationRequestedMillis() {
        return durationMinutesRequested * 60 * 1000;
    }

    public boolean isPaused() {
        return engine.isPaused();
    }

    public void pause() {
        if (countDownTimer != null) {
            countDownTimer.pause();
        }
        if (engine != null) {
            engine.pause();
        }
    }

    public void resume() {
        if (countDownTimer != null) {
            countDownTimer.resume();
        }
        if (engine != null) {
            engine.resume();
        }
    }

    public static class Factory implements ViewModelProvider.Factory {
        private final Application application;
        private final Params params;


        public Factory(Application application, Params params) {
            this.application = application;
            this.params = params;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new TranscribeTrainingSessionViewModel(application, params);
        }
    }

    public LiveData<List<TranscribeTrainingSession>> getLatestTrainingSession() {
        return repository.transcribeTrainingSessionDAO.getLatestSession(sessionType.name());
    }

    private void letterPlayedCallback(String letter) {
        playedMessage.add(letter);
    }

    public void primeTheEngine(TranscribeTrainingSession prevSession) {
        AudioManager.MorseConfig.Builder morseConfigBuilder = AudioManager.MorseConfig.builder();
        morseConfigBuilder.setToneFrequencyHz(audioToneFrequency);
        morseConfigBuilder.setFadeInOutPercentage(fadeInOutPercentage);
        morseConfigBuilder.setLetterWpm(letterWpmRequested);
        morseConfigBuilder.setEffectiveWpm(effectiveWpmRequested);

        Supplier<Pair<String, AudioManager.MorseConfig>> letterSupplier;
        if (sessionType.equals(TranscribeSessionType.RANDOM_LETTER_ONLY)) {
            //countDownTimer = setupCountDownTimer(1000 * (durationMinutesRequested * 60 + 1));
            countDownTimer = setupCountDownTimer(1000 * 10);
            letterSupplier = new RandomLetterSupplier(buildWeightedStrings(prevSession), morseConfigBuilder.build());
        } else if (sessionType.equals(TranscribeSessionType.RANDOM_QSO)) {
            List<String> messages = RandomQSO.generate();
            AudioManager.MorseConfig morseConfig0 = morseConfigBuilder.build();

            // Make the second station morse config have the configured tone freq
            morseConfigBuilder.setToneFrequencyHz(secondAudioToneFrequency);
            AudioManager.MorseConfig morseConfig1 = morseConfigBuilder.build();

            letterSupplier = new QSOWordSupplier(messages, morseConfig0, morseConfig1);
        } else {
            throw new RuntimeException("unknown session type: " + sessionType);
        }

        AudioManager audioManager = new AudioManager(getApplication().getResources());
        synchronized (this) {
            engine = new TranscribeTrainingEngine(
                    audioManager,
                    this::letterPlayedCallback,
                    letterSupplier,
                    this::messagePlayingComplete,
                    secondsBetweenStationTransmissions
            );

            engine.prime();
        }
    }

    public boolean hasEngineBeenPrimed() {
        synchronized (this) {
            return engine != null;
        }
    }

    private Void messagePlayingComplete() {
        sessionIsFinished.postValue(true);
        return null;
    }

    private List<Pair<String, Double>> buildWeightedStrings(TranscribeTrainingSession prevSession) {
        List<Pair<String, Double>> weightedRequestedStrings = Lists.newArrayList();
        Map<String, Double> errorMap = null;
        if (prevSession != null && targetIssueLetters) {
            errorMap = TranscribeUtil.calculateErrorMap(prevSession);
        }

        for (String s : stringsRequested) {
            Double error;
            if (errorMap != null) {
                if (errorMap.containsKey(s)) {
                    error = errorMap.get(s);
                } else {
                    error = 0D;
                }
            } else {
                error = 0D;
            }
            weightedRequestedStrings.add(Pair.of(s, 1D + error));
        }

        return weightedRequestedStrings;
    }

    private CountDownTimer setupCountDownTimer(long durationsMillis) {
        return new CountDownTimer(durationsMillis, 50) {
            public void onTick(long millisUntilFinished) {
                durationRemainingMillis.setValue(millisUntilFinished);
                // If we get here, its done
                if (millisUntilFinished <= 0) {
                    engine.prepareForShutdown();
                }

                // unlimited delay is set. Don't do anything
                if (endDelaySeconds < 0) {
                    return;
                }

                // Two seconds before the end, kill the transmitting. End Seconds Delay
                if (!engine.isPreparedToShutDown() && millisUntilFinished <= 2000) {
                    engine.prepareForShutdown();
                }
            }

            public void onFinish() {
                engine.prepareForShutdown();
                durationRemainingMillis.setValue(0l);
            }
        };
    }

    public void startTheEngine() {
        if (sessionIsFinished.getValue()) {
            return;
        }
        if (sessionHasBeenStarted.getValue()) {
            Timber.d("Duped request to start the session");
            return;
        }
        if (countDownTimer != null) {
            countDownTimer.start();
        }
        engine.start();
        sessionHasBeenStarted.postValue(true);
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
        trainingSession.completed = durationWorkedMillis == 0;
        trainingSession.targetIssueLetters = targetIssueLetters;
        trainingSession.effectiveWpm = (long) effectiveWpmRequested;
        trainingSession.letterWpm = (long) letterWpmRequested;
        trainingSession.audioToneFrequency = audioToneFrequency;
        trainingSession.startDelaySeconds = startDelaySeconds;
        trainingSession.endDelaySeconds = endDelaySeconds;

        trainingSession.sessionType = sessionType.name();
        trainingSession.stringsRequested = stringsRequested == null ? Lists.newArrayList() : stringsRequested;

        trainingSession.playedMessage = playedMessage;
        trainingSession.enteredKeys = transcribedMessage.getValue();

        repository.insertTranscribeTrainingSession(trainingSession);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        startTimer.cancel();
        if (engine != null) {
            engine.destroy();
            recordSessionDetails();
        }
    }
}
