package com.uberj.ditsanddahs.flashcard;

import android.app.Application;
import android.graphics.Color;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;

import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.AudioManager;
import com.uberj.ditsanddahs.CountDownTimer;
import com.uberj.ditsanddahs.GlobalSettings;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardSessionType;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardTrainingSession;
import com.uberj.ditsanddahs.storage.Repository;
import com.uberj.ditsanddahs.transcribe.DiffPatchMatch;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import org.apache.commons.lang3.tuple.Pair;

import timber.log.Timber;

class FlashcardTrainingSessionViewModel extends AndroidViewModel {
    public static final String TIME_LIMITED_SESSION_TYPE = "time_limited";
    private static final String sessionStartLock = "Lock";
    private final Repository repository;

    public final MutableLiveData<SpannableStringBuilder> titleText = new MutableLiveData<>();
    public final MutableLiveData<List<String>> transcribedMessage = new MutableLiveData<>(Lists.newArrayList());
    private final long durationUnitsRequested;
    private final long durationRequestedMillis;
    private final String durationUnit;
    private final int wpmRequested;
    private final FlashcardSessionType sessionType;
    private final int toneFrequency;
    private final List<String> requestedMessages;
    private final AtomicInteger wrongGuessCount = new AtomicInteger(0);
    private final GlobalSettings globalSettings;

    private CountDownTimer countDownTimer;
    public final MutableLiveData<Long> durationUnitsRemaining = new MutableLiveData<>(-1L);
    private boolean sessiontHasBeenStarted = false;
    private long endTimeEpocMillis = -1;
    private FlashcardTrainingEngine engine;
    private AudioManager audioManager;

    public FlashcardTrainingSessionViewModel(@NonNull Application application, List<String> requestedMessages, int durationUnitsRequested, String durationUnit, int wpmRequested, int toneFrequency, FlashcardSessionType sessionType, GlobalSettings globalSettings) {
        super(application);
        this.durationUnitsRequested = durationUnitsRequested;
        this.durationUnit = durationUnit;
        this.durationRequestedMillis = 1000 * (durationUnitsRequested * 60);
        this.wpmRequested = wpmRequested;
        this.toneFrequency = toneFrequency;
        this.repository = new Repository(application);
        this.sessionType = sessionType;
        this.requestedMessages = requestedMessages;
        this.globalSettings = globalSettings;
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
        private final GlobalSettings globalSettings;


        public Factory(Application application, ArrayList<String> requestedMessages, int durationUnitsRequested, String durationUnit, int wpmRequested, int toneFrequency, FlashcardSessionType sessionType, GlobalSettings globalSettings) {
            this.application = application;
            this.requestedMessages = requestedMessages;
            this.durationUnitsRequested = durationUnitsRequested;
            this.durationUnit = durationUnit;
            this.wpmRequested = wpmRequested;
            this.toneFrequency = toneFrequency;
            this.sessionType = sessionType;
            this.globalSettings = globalSettings;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T) new FlashcardTrainingSessionViewModel(application, requestedMessages, durationUnitsRequested, durationUnit, wpmRequested, toneFrequency, sessionType, globalSettings);
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
        morseConfig.setGlobalSettings(globalSettings);
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

    public void setDurationUnitsRemainingMillis(long durationUnitsRemainingMillis) {
        this.durationUnitsRemaining.setValue(durationUnitsRemainingMillis);
    }

    public boolean guess(String guess) {
        Pair<Boolean, List<DiffPatchMatch.Diff>> guessDetails = engine.submitGuess(guess);
        if (guessDetails.getKey()) {
            titleText.postValue(new SpannableStringBuilder(""));
            wrongGuessCount.set(0);
            return true;
        }
        // TODO, show a diff.
//        int count = wrongGuessCount.incrementAndGet();
//        if (count >= 3) {
//            SpannableStringBuilder value = buildErrorDiffText(guessDetails.getValue());
//            titleText.postValue(value);
//        }

        return false;
    }

    private SpannableStringBuilder buildErrorDiffText(List<DiffPatchMatch.Diff> diffs) {
        SpannableStringBuilder ssb = new SpannableStringBuilder("");
        ForegroundColorSpan missColor = new ForegroundColorSpan(Color.RED);
        ForegroundColorSpan hitColor = new ForegroundColorSpan(Color.GREEN);
        List<Pair<Integer, Integer>> missSpans = Lists.newArrayList();
        List<Pair<Integer, Integer>> hitSpans = Lists.newArrayList();
        for (DiffPatchMatch.Diff diff : diffs) {
            if (diff.operation.equals(DiffPatchMatch.Operation.DELETE)) {
                for (int i = 0; i < diff.text.length(); i++) {
                    int start = ssb.length();
                    ssb.append("_");
                    int end = ssb.length();
                    missSpans.add(Pair.of(start, end));
                }
                ssb.append(" ");
            } else if (diff.operation.equals(DiffPatchMatch.Operation.INSERT)) {
            } else if (diff.operation.equals(DiffPatchMatch.Operation.EQUAL)) {
                for (int i = 0; i < diff.text.length(); i++) {
                    char c = diff.text.charAt(i);
                    int start = ssb.length();
                    ssb.append(c);
                    int end = ssb.length();
                    hitSpans.add(Pair.of(start, end));
                    ssb.append(" ");
                }
            }
        }

        for (Pair<Integer, Integer> hitSpan : hitSpans) {
            ssb.setSpan(hitColor, hitSpan.getLeft(), hitSpan.getRight(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        for (Pair<Integer, Integer> missSpan : missSpans) {
            ssb.setSpan(missColor, missSpan.getLeft(), missSpan.getRight(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        return ssb;
    }
}
