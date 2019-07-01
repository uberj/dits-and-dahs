package com.uberj.ditsanddahs.transcribe;

import android.app.Application;

import com.annimon.stream.function.Supplier;
import com.google.common.base.Preconditions;
import com.uberj.ditsanddahs.AudioManager;
import com.uberj.ditsanddahs.CountDownTimer;
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
    private final ArrayList<String> stringsRequested;
    private final boolean targetIssueLetters;
    private final int audioToneFrequency;
    private final int secondAudioToneFrequency;
    private final int startDelaySeconds;
    private final int endDelaySeconds;
    private CountDownTimer countDownTimer = null;
    private TranscribeTrainingEngine engine;
    private boolean sessionHasBeenStarted = false;
    private static final String sessionStartLock = "lock";
    private long endTimeEpocMillis = -1;
    private List<String> playedMessage = Lists.newArrayList();
    private final int fadeInOutPercentage;

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

        private Params(int durationMinutesRequested, ArrayList<String> stringsRequested, int letterWpmRequested, int effectiveWpmRequested, boolean targetIssueLetters, TranscribeSessionType sessionType, int audioToneFrequency, int startDelaySeconds, int endDelaySeconds, int fadeInOutPercentage, int secondAudioToneFrequency) {
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
            private Integer secondAudioToneFrequency;

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

            public void setSecondAudioToneFrequency(int secondAudioToneFrequency) {
                this.secondAudioToneFrequency = secondAudioToneFrequency;
            }

            public Params build() {
                if (sessionType.equals(TranscribeSessionType.RANDOM_LETTER_ONLY)) {
                    Preconditions.checkNotNull(stringsRequested);
                } else {
                    Preconditions.checkArgument(stringsRequested == null);
                }

                Preconditions.checkNotNull(letterWpmRequested);
                Preconditions.checkNotNull(effectiveWpmRequested);
                Preconditions.checkNotNull(targetIssueLetters);
                Preconditions.checkNotNull(sessionType);
                Preconditions.checkNotNull(audioToneFrequency);
                Preconditions.checkNotNull(startDelaySeconds);
                Preconditions.checkNotNull(endDelaySeconds);
                Preconditions.checkNotNull(fadeInOutPercentage);
                Preconditions.checkNotNull(secondAudioToneFrequency);
                return new Params(durationMinutesRequested, stringsRequested, letterWpmRequested, effectiveWpmRequested, targetIssueLetters, sessionType, audioToneFrequency, startDelaySeconds, endDelaySeconds, fadeInOutPercentage, secondAudioToneFrequency);
            }
        }
    }

    public TranscribeTrainingSessionViewModel(@NonNull Application application, Params params) {
        super(application);
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
        engine.pause();
    }

    public void resume() {
        if (countDownTimer != null) {
            countDownTimer.resume();
        }
        engine.resume();
    }

    public boolean isARequstedString(String buttonLetter) {
        return stringsRequested.contains(buttonLetter);
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
        Supplier<String> letterSupplier;
        if (sessionType.equals(TranscribeSessionType.RANDOM_LETTER_ONLY)) {
            countDownTimer = setupCountDownTimer(1000 * (durationMinutesRequested * 60 + 1));
            letterSupplier = new RandomLetterSupplier(buildWeightedStrings(prevSession));
        } else if (sessionType.equals(TranscribeSessionType.RANDOM_QSO)) {
            List<String> messages = RandomQSO.generate();
            letterSupplier = new QSOWordSupplier(messages);
        } else {
            throw new RuntimeException("unknown session type: " + sessionType);
        }

        AudioManager.MorseConfig.Builder morseConfig = AudioManager.MorseConfig.builder();
        morseConfig.setToneFrequencyHz(audioToneFrequency);
        morseConfig.setFadeInOutPercentage(fadeInOutPercentage);
        morseConfig.setLetterWpm(letterWpmRequested);
        morseConfig.setEffectiveWpm(effectiveWpmRequested);
        AudioManager audioManager = new AudioManager(getApplication().getResources());
        engine = new TranscribeTrainingEngine(audioManager, startDelaySeconds, this::letterPlayedCallback, letterSupplier, this::messagePlayingComplete, morseConfig.build());
        engine.prime();
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

                // unlimited delay is negative. Don't do anything
                if (endDelaySeconds < 0) {
                    return;
                }

                // there's some off by one error somewhere, a second before the delay ends, get ready to shut down
                if (millisUntilFinished <= endDelaySeconds * 1000) {
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
        synchronized (sessionStartLock) {
            if (sessionHasBeenStarted) {
                Timber.d("Duped request to start the session");
                return;
            }
            if (countDownTimer != null) {
                countDownTimer.start();
            }
            engine.start();
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
        engine.destroy();
        recordSessionDetails();
    }
}
