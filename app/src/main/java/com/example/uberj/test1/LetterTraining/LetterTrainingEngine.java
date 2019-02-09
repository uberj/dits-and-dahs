package com.example.uberj.test1.LetterTraining;

import android.util.Log;

import com.example.uberj.test1.CWToneManager;
import com.example.uberj.test1.storage.LetterTrainingEngineSettings;
import com.google.common.collect.Lists;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;

import javax.annotation.Nonnull;

public class LetterTrainingEngine {
    private static final int MISSED_LETTER_POINTS_REMOVED = 10;
    private static final int CORRECT_LETTER_POINTS_ADDED = 5;

    private static final String guessGate = "guessGate";
    private static final String pauseGate = "pauseGate";
    private static final String audioGate = "audioGate";
    private static final String TAG = "LetterTrainingEngine";
    private static final int LETTER_WEIGHT_MAX = 100;
    private static final int LETTER_WEIGHT_MIN = 0;
    private static final int INCLUSION_COMPETENCY_CUTOFF_WEIGHT = 50;

    private final CWToneManager cwToneManager;
    private final Consumer<String> letterChosenCallback;
    private final int playLetterWPM;
    private volatile boolean threadKeepAlive = true;
    private volatile boolean isPaused = false;
    private volatile boolean isInitialized = false;
    private List<String> playableKeys;
    private String currentLetter;
    private Thread audioThread;
    private Runnable audioLoop;
    private final Map<String, Integer> competencyWeights;
    private final List<String> letterOrder;

    public LetterTrainingEngine(List<String> letterOrder, int wpm, Consumer<String> letterChosenCallback, List<String> playableKeys, @Nonnull Map<String, Integer> competencyWeights) {
        this.letterOrder = letterOrder;
        this.letterChosenCallback = letterChosenCallback;
        this.cwToneManager = new CWToneManager(wpm);
        this.playableKeys = playableKeys;
        this.competencyWeights = competencyWeights;
        this.playLetterWPM = wpm;
        this.audioLoop = () -> {
            while (true) {
                /*/
                This loop will wait for ex

                Pause
                -----
                - When the engine is paused
                    * No timeout
                    * resume() should be the only one to trigger this notify

                Playing
                -------
                - When the tone manager is playing audio
                    * Timeout happens after audio tone is done
                    * Nobody should end this?

                WaitGuess
                ---------
                - When the engine is waiting for the player to guess
                    * Timeout happens after getGuessWaitTimeMillis()
                    * pause() and guess() should end this

                /*/
                while (isPaused)  {
                    try {
                        synchronized (pauseGate) {
                            pauseGate.wait();
                        }
                    } catch (InterruptedException e) {
                        return;
                    }
                }

                if (!threadKeepAlive) {
                    return;
                }

                CWToneManager.PCMDetails pcmDetails = cwToneManager.calcPCMDetails(currentLetter);
                long waitTimeMillis = (long) (1000L * ((1F / pcmDetails.symbolsPerSecond) * pcmDetails.totalNumberSymbols));

                // play it letter
                cwToneManager.playLetter(currentLetter);

                try {  // Wait until the letter is done playing
                    synchronized (audioGate) {
                        audioGate.wait(waitTimeMillis);
                    }
                } catch (InterruptedException e) {
                    return;
                }

                // start the callback timer to play again

                synchronized (guessGate) {
                    try {
                        guessGate.wait(getGuessWaitTimeMillis());
                    } catch (InterruptedException e) {
                        return;
                    }
                }
            }
        };
    }

    private long getGuessWaitTimeMillis() {
        return 3000;
    }

    public Optional<Boolean> guess(String guess) {
        // If its right
        if (isPaused) {
            return Optional.empty();
        }

        boolean isCorrectGuess = false;
        if (guess.equals(currentLetter)) {
            // Make sure we always choose a different letter. this makes the handler of the
            // letterPlayedCallback able to calculate number of letters played a lot easier
            chooseDifferentLetter();
            isCorrectGuess = true;
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

    public void initEngine() {
        chooseDifferentLetter();
        audioThread = new Thread(audioLoop);
        audioThread.start();
        isInitialized = true;
    }

    public void destroy() {
        threadKeepAlive = false;
        if (audioThread != null) {
            audioThread.interrupt();
        }
    }

    public void resume() {
        if (!isPaused || !isInitialized) {
            return;
        }
        isPaused = false;
        synchronized (pauseGate) {
            pauseGate.notify();
        }
    }

    public void pause() {
        if (isPaused || !isInitialized) {
            return;
        }
        isPaused = true;
        synchronized (audioGate) {
            audioGate.notify();
        }

        synchronized (guessGate) {
            guessGate.notify();
        }
    }

    public void setPlayableKeys(List<String> playableKeys) {
        this.playableKeys = playableKeys;
        if (!playableKeys.contains(currentLetter)) {
            chooseDifferentLetter();
        }
    }

    public List<String> getNQualifiedLetters(int n) {
        ArrayList<String> qualifiedLetters = Lists.newArrayList();
        for (String letter : letterOrder) {
            Integer weight = competencyWeights.get(letter);
            if (weight == null) {
                continue;
            }
            if (weight < INCLUSION_COMPETENCY_CUTOFF_WEIGHT) {
                continue;
            }

            qualifiedLetters.add(letter);
            if(--n == 0){
                break;
            }
        }
        return qualifiedLetters;
    }

    public int getCompetencyWeight(String letter) {
        return competencyWeights.get(letter);
    }

    public boolean isValidGuess(String letter) {
        return playableKeys.contains(letter);
    }

    public boolean shouldIntroduceNewLetter() {
        int furthestLetterIdx = -1;
        for (String playableKey : playableKeys) {
            furthestLetterIdx = Math.max(furthestLetterIdx, letterOrder.indexOf(playableKey));
        }

        if (furthestLetterIdx == -1) {
            throw new RuntimeException("Something is very wrong. No playable keys found in letter order list");
        }

        String furthestLetter = letterOrder.get(furthestLetterIdx);
        assert competencyWeights != null;
        Integer weight = competencyWeights.get(furthestLetter);
        if (weight == null) {
            throw new RuntimeException("Something is very wrong. No competency weight for furthest letter " + furthestLetter);
        }

        return weight > INCLUSION_COMPETENCY_CUTOFF_WEIGHT;
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

    public LetterTrainingEngineSettings getSettings() {
        LetterTrainingEngineSettings engineSettings = new LetterTrainingEngineSettings();
        engineSettings.weights = competencyWeights;
        engineSettings.activeLetters = playableKeys;
        engineSettings.playLetterWPM = playLetterWPM;
        return engineSettings;
    }
}
