package com.uberj.pocketmorsepro.simplesocratic;

import com.uberj.pocketmorsepro.AudioManager;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticEngineEvent;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingEngineSettings;
import com.google.common.collect.Lists;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

import timber.log.Timber;

public class SocraticTrainingEngine {
    private static final int MISSED_LETTER_POINTS_REMOVED = 10;
    private static final int CORRECT_LETTER_POINTS_ADDED = 5;

    private static final String guessGate = "guessGate";
    private static final String pauseGate = "pauseGate";
    private static final String easyModePause = "easyModePause";
    private static final int LETTER_WEIGHT_MAX = 100;
    private static final int LETTER_WEIGHT_MIN = 0;
    private static final int INCLUSION_COMPETENCY_CUTOFF_WEIGHT = 50;

    private final AudioManager cwToneManager;
    private final Consumer<String> letterChosenCallback;
    private final int playLetterWPM;
    private static volatile boolean audioThreadKeepAlive = true;
    private final boolean easyMode;
    private volatile boolean isPaused = false;
    private volatile boolean engineIsStarted = false;
    private volatile boolean shortCircuitGuessWait;
    private List<String> playableKeys;
    private String currentLetter;
    private Thread audioThread;
    private Runnable audioLoop;
    private final Map<String, Integer> competencyWeights;
    private final List<String> letterOrder;
    public final List<SocraticEngineEvent> events = Lists.newArrayList();
    private volatile long sleepTime = -1;

    public SocraticTrainingEngine(AudioManager audioManager, List<String> letterOrder, int wpm, Consumer<String> letterChosenCallback, List<String> playableKeys, @Nonnull Map<String, Integer> competencyWeights, boolean easyMode) {
        this.cwToneManager = audioManager;
        this.easyMode = easyMode;
        this.letterOrder = letterOrder;
        this.letterChosenCallback = letterChosenCallback;
        this.playableKeys = playableKeys;
        this.competencyWeights = competencyWeights;
        this.playLetterWPM = wpm;
        this.audioLoop = () -> {
            try {
                while (Thread.currentThread() == audioThread) {
                    if (easyMode) {
                        while (sleepTime > 0) {
                            synchronized (easyModePause) {
                                if (sleepTime > 0) {
                                    long preSleepTime = sleepTime;
                                    sleepTime = 0;
                                    easyModePause.wait(preSleepTime);
                                }
                            }
                        }
                    }

                    while (isPaused)  {
                        synchronized (pauseGate) {
                            pauseGate.wait();
                        }
                    }

                    // play it letter
                    if (audioThreadKeepAlive) {
                        Timber.d("Playing letter: %s", currentLetter);
                        shortCircuitGuessWait = false;
                        cwToneManager.playMessage(currentLetter);
                        events.add(SocraticEngineEvent.letterDonePlaying(currentLetter));
                    }

                    synchronized (guessGate) {
                        // shortCircuitGuessWait will be true after a correct guess has been entered.
                        // if a correct guess has been been entered while a letter is being played we don't
                        // want to wait for the user to guess (because they already did)
                        if (shortCircuitGuessWait) {
                            long millis = cwToneManager.letterSpaceToMillis();
                            guessGate.wait(millis);
                        } else {
                            guessGate.wait(getGuessWaitTimeMillis());
                        }
                        shortCircuitGuessWait = false;
                    }
                }
            } catch (InterruptedException e) {
                Timber.d(e, "Audio loop exiting");
                return;
            }
            Timber.d("Audio loop exiting outside of loop");
        };
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
            shortCircuitGuessWait = true;
        } else {
            events.add(SocraticEngineEvent.incorrectGuess(guess));
        }

        if (easyMode) {
            synchronized (easyModePause) {
                sleepTime = isCorrectGuess ? 350L : 1500L;
                easyModePause.notifyAll();
            }
        }

        synchronized (guessGate) {
            guessGate.notify();
        }

        if (isCorrectGuess) {
            competencyWeights.computeIfPresent(guess,
                    (cLetter, existingCompetency) -> Math.min(LETTER_WEIGHT_MAX, existingCompetency + CORRECT_LETTER_POINTS_ADDED));
        } else {
            competencyWeights.computeIfPresent(guess,
                    (cLetter, existingCompetency) -> Math.max(LETTER_WEIGHT_MIN, existingCompetency - MISSED_LETTER_POINTS_REMOVED));
        }

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

            pmf.add(new Pair<>(letter, Math.max(1, LETTER_WEIGHT_MAX - (double) letterWeight)));
        }
        return pmf;
    }

    public void prime() {
        chooseDifferentLetter();
        audioThread = new Thread(audioLoop);
    }

    public void start() {
        audioThread.start();
        engineIsStarted = true;
        audioThreadKeepAlive = true;
    }

    public void destroy() {
        audioThreadKeepAlive = false;
        audioThread = null;
        cwToneManager.destroy();
        events.add(SocraticEngineEvent.destroyed());
    }

    public void resume() {
        if (!isPaused || !engineIsStarted) {
            return;
        }
        events.add(SocraticEngineEvent.resumed());
        isPaused = false;
        sleepTime = 0;
        synchronized (pauseGate) {
            pauseGate.notify();
        }
        synchronized (easyModePause) {
            easyModePause.notifyAll();
        }
    }

    public void pause() {
        if (isPaused || !engineIsStarted) {
            return;
        }
        events.add(SocraticEngineEvent.paused());
        isPaused = true;
        sleepTime = 0;

        synchronized (guessGate) {
            guessGate.notify();
        }
        synchronized (easyModePause) {
            easyModePause.notifyAll();
        }
    }

    public void setPlayableKeys(List<String> playableKeys) {
        this.playableKeys = playableKeys;
        if (!playableKeys.contains(currentLetter)) {
            chooseDifferentLetter();
        }
    }

    public int getCompetencyWeight(String letter) {
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
            if (weight < INCLUSION_COMPETENCY_CUTOFF_WEIGHT) {
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
        cwToneManager.playMessage(letter);
    }
}
