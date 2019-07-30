package com.uberj.ditsanddahs.simplesocratic;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import androidx.core.util.Consumer;

import com.annimon.stream.Optional;
import com.uberj.ditsanddahs.AudioManager;
import com.uberj.ditsanddahs.WeightUtil;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticEngineEvent;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingEngineSettings;
import com.google.common.collect.Lists;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;

public class SocraticTrainingEngine {
    private static final String pauseGate = "pauseGate";
    private static final int LETTER_WEIGHT_MAX = 100;

    private final AudioManager cwToneManager;
    private final Consumer<String> letterChosenCallback;
    private final int playLetterWPM;
    private final boolean easyMode;
    private final Handler eventHandler;
    private final int missedLetterPointsRemoved;
    private final int correctLetterPointsAdded;
    private final int includeNewLetterCutoffScore;
    private volatile boolean isPaused = false;
    private volatile boolean engineIsStarted = false;
    private List<String> playableKeys;
    private String currentLetter;
    private final Map<String, Integer> competencyWeights;
    private final List<String> letterOrder;
    public final List<SocraticEngineEvent> events = Lists.newArrayList();

    private static final int PLAY_CURRENT_MESSAGE = 100;
    private static final int CORRECT_GUESS = 101;
    private static final int INCORRECT_GUESS = 102;
    private static final int TIME_CLICK = 103;
    private static final int CHOOSE_NEW_MESSAGE = 104;
    private volatile long mostRecentEventAt;
    private final AudioManager.MorseConfig morseConfig;

    public SocraticTrainingEngine(
            AudioManager audioManager,
            List<String> letterOrder,
            Consumer<String> letterChosenCallback,
            List<String> playableKeys, @Nonnull Map<String, Integer> competencyWeights,
            AudioManager.MorseConfig morseConfig,
            SocraticSettings socraticSettings) {
        this.cwToneManager = audioManager;
        this.easyMode = socraticSettings.easyMode;
        this.letterOrder = letterOrder;
        this.letterChosenCallback = letterChosenCallback;
        this.playableKeys = playableKeys;
        this.competencyWeights = competencyWeights;
        this.playLetterWPM = socraticSettings.wpmRequested;
        this.morseConfig = morseConfig;
        this.missedLetterPointsRemoved = socraticSettings.missedLetterPointsRemoved;
        this.correctLetterPointsAdded = socraticSettings.correctLetterPointsAdded;
        this.includeNewLetterCutoffScore = socraticSettings.includeNewLetterCutoffScore;
        this.currentLetter = letterOrder.get(0);
        HandlerThread eventHandlerThread = new HandlerThread("GuessHandler", HandlerThread.MAX_PRIORITY);
        eventHandlerThread.start();
        eventHandler = new Handler(eventHandlerThread.getLooper(), this::eventCallback);
    }

    private boolean eventCallback(Message message) {
        if (message.what == CHOOSE_NEW_MESSAGE) {
            chooseDifferentLetter();
            eventHandler.sendEmptyMessage(PLAY_CURRENT_MESSAGE);
        } else if (message.what == PLAY_CURRENT_MESSAGE) {
            mostRecentEventAt = System.currentTimeMillis();
            cwToneManager.playMessage(currentLetter, morseConfig);
            events.add(SocraticEngineEvent.letterDonePlaying(currentLetter));
        } else if (message.what == CORRECT_GUESS) {
            mostRecentEventAt = System.currentTimeMillis();
            eventHandler.removeMessages(PLAY_CURRENT_MESSAGE);
            eventHandler.sendEmptyMessageDelayed(PLAY_CURRENT_MESSAGE, easyMode ? 350L : 50L);
        } else if (message.what == INCORRECT_GUESS) {
            mostRecentEventAt = System.currentTimeMillis();
            eventHandler.removeMessages(PLAY_CURRENT_MESSAGE);
            eventHandler.sendEmptyMessageDelayed(PLAY_CURRENT_MESSAGE, 1500L);
        } else if (message.what == TIME_CLICK) {
            if (mostRecentEventAt + getGuessWaitTimeMillis() <= System.currentTimeMillis()) {
                mostRecentEventAt = System.currentTimeMillis();
                cwToneManager.playMessage(currentLetter, morseConfig);
                events.add(SocraticEngineEvent.letterDonePlaying(currentLetter));
            }
        } else {
            throw new RuntimeException("Unknown message type: " + message.what);
        }
        return true;
    }

    public void timeClick(long millisUntilFinished) {
        eventHandler.sendEmptyMessage(TIME_CLICK);
    }

    private long getGuessWaitTimeMillis() {
        return 3000;
    }

    public Optional<Boolean> guess(String guess) {
        // Don't allow guesses when we are paused
        if (isPaused) {
            return Optional.empty();
        }

        boolean isCorrectGuess = false;
        if (guess.equals(currentLetter)) {
            events.add(SocraticEngineEvent.correctGuess(currentLetter));
            chooseDifferentLetter();
            isCorrectGuess = true;
            WeightUtil.increment(competencyWeights, guess, correctLetterPointsAdded);
        } else {
            events.add(SocraticEngineEvent.incorrectGuess(guess));
            WeightUtil.decrement(competencyWeights, currentLetter, missedLetterPointsRemoved);
        }

        Message msg = new Message();
        msg.what = isCorrectGuess ? CORRECT_GUESS : INCORRECT_GUESS;

        eventHandler.sendMessageAtFrontOfQueue(msg);

        return Optional.of(isCorrectGuess);
    }

    private void chooseDifferentLetter() {
        List<Pair<String, Double>> pmfCompetencyWeights = buildPmfCompetencyWeights(playableKeys);
        currentLetter = new EnumeratedDistribution<>(pmfCompetencyWeights).sample();
        letterChosenCallback.accept(currentLetter);
        events.add(SocraticEngineEvent.letterChosen(currentLetter));
    }

    private List<Pair<String,Double>> buildPmfCompetencyWeights(List<String> playableKeys) {
        ArrayList<Pair<String, Double>> pmf = Lists.newArrayList();
        for (Map.Entry<String, Integer> entry : competencyWeights.entrySet()) {
            String letter = entry.getKey();
            int letterWeight = entry.getValue();
            if (!playableKeys.contains(letter)) {
                continue;
            }

            double repeatDeterrent;
            if (letter.equals(currentLetter)) {
                repeatDeterrent = 60;
            } else {
                repeatDeterrent = 0;
            }

            pmf.add(new Pair<>(letter, Math.max(1, LETTER_WEIGHT_MAX - (double) letterWeight - repeatDeterrent)));
        }
        return pmf;
    }

    public void start() {
        eventHandler.sendEmptyMessageDelayed(CHOOSE_NEW_MESSAGE, 50L);
        engineIsStarted = true;
    }

    public void destroy() {
        cwToneManager.destroy();
        events.add(SocraticEngineEvent.destroyed());
    }

    public void resume() {
        if (!isPaused || !engineIsStarted) {
            return;
        }
        events.add(SocraticEngineEvent.resumed());
        isPaused = false;
    }

    public void pause() {
        synchronized (pauseGate) {
            if (isPaused || !engineIsStarted) {
                return;
            }
            events.add(SocraticEngineEvent.paused());
            isPaused = true;
        }
    }

    public void setPlayableKeys(List<String> playableKeys) {
        this.playableKeys = playableKeys;
        if (!playableKeys.contains(currentLetter)) {
            chooseDifferentLetter();
        }
    }

    public synchronized int getCompetencyWeight(String letter) {
        if (!competencyWeights.containsKey(letter)) {
            competencyWeights.put(letter, 0);
            return 0;
        }
        return competencyWeights.get(letter);
    }

    public boolean isValidGuess(String letter) {
        return playableKeys.contains(letter);
    }

    public boolean shouldIntroduceNewLetter() {
        // The player should be competent at every letter on the board before a new one is introduced
        for (String playableKey : playableKeys) {
            Integer weight = competencyWeights.get(playableKey);
            if (weight == null) {
                throw new RuntimeException("Something is very wrong. No competency weight for furthest letter " + weight);
            }
            if (weight < includeNewLetterCutoffScore) {
                return false;
            }
        }
        return true;
    }

    public Optional<List<String>> introduceLetter() {
        int furthestLetterIdx = -1;
        for (String playableKey : playableKeys) {
            furthestLetterIdx = Math.max(furthestLetterIdx, letterOrder.indexOf(playableKey));
        }
        if (furthestLetterIdx > letterOrder.size()) {
            return Optional.empty();
        }
        String nextLetter = letterOrder.get(furthestLetterIdx + 1);
        this.playableKeys.add(nextLetter);
        return Optional.of(this.playableKeys);
    }

    public SocraticTrainingEngineSettings getSettings() {
        SocraticTrainingEngineSettings engineSettings = new SocraticTrainingEngineSettings();
        engineSettings.weights = competencyWeights;
        engineSettings.activeLetters = playableKeys;
        engineSettings.playLetterWPM = playLetterWPM;
        return engineSettings;
    }

    public void playLetter(String letter) {
        cwToneManager.playMessage(letter, morseConfig);
    }

    public String getCurrentLetter() {
        return currentLetter;
    }
}
