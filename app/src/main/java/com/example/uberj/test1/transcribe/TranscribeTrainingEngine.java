package com.example.uberj.test1.transcribe;

import com.example.uberj.test1.CWToneManager;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingEngineSettings;
import com.google.common.collect.ImmutableList;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import timber.log.Timber;

class TranscribeTrainingEngine {
    private static final EnumeratedDistribution<Integer> LENGTH_DISTRIBUTION = new EnumeratedDistribution<>(ImmutableList.of(
            Pair.create(2, 3D),
            Pair.create(3, 4D),
            Pair.create(4, 5D),
            Pair.create(5, 5D),
            Pair.create(6, 5D),
            Pair.create(7, 5D),
            Pair.create(8, 3D),
            Pair.create(9, 3D),
            Pair.create(10, 1D),
            Pair.create(11, 1D),
            Pair.create(12, 1D)
    ));

    private final Runnable audioLoop;
    private final CWToneManager cwToneManager;
    private final String pauseGate = "pauseGate";
    private final String farnsworthPause = "farnsworthPause";
    private final List<String> inPlayLetters;
    private final int letterWpmRequested;
    private final int transmitWpmRequested;
    private final Consumer<String> letterPlayedCallback;
    private final EnumeratedDistribution<String> nextLetterDistribution;

    private boolean audioThreadKeepAlive;
    private boolean isPaused;
    private Thread audioThread;
    private boolean engineIsStarted = false;
    private int lettersLeftInGroup = LENGTH_DISTRIBUTION.sample();
    private boolean awaitingShutdown = false;

    public TranscribeTrainingEngine(int letterWpmRequested, int transmitWpmRequested, List<org.apache.commons.lang3.tuple.Pair<String, Double>> inPlayLetters, Consumer<String> letterPlayedCallback) {
        this.nextLetterDistribution = new EnumeratedDistribution<>(letterWeights(inPlayLetters));
        this.inPlayLetters = justStrings(inPlayLetters);
        this.letterWpmRequested = letterWpmRequested;
        this.transmitWpmRequested = transmitWpmRequested;
        this.letterPlayedCallback = letterPlayedCallback;
        this.cwToneManager = new CWToneManager(letterWpmRequested, transmitWpmRequested);
        this.audioLoop = () -> {
            try {
                while (Thread.currentThread() == audioThread) {
                    while (isPaused)  {
                        synchronized (pauseGate) {
                            pauseGate.wait();
                        }
                    }

                    // play next letter
                    if (!awaitingShutdown && audioThreadKeepAlive) {
                        String currentLetter = nextLetter();
                        Timber.d("Playing letter: '%s'", currentLetter);
                        this.letterPlayedCallback.accept(currentLetter);
                        cwToneManager.playLetter(currentLetter);
                    }

                    // The session is ending soon
                    if (awaitingShutdown) {
                        return;
                    }

                    // start the callback timer to play again
                    synchronized (farnsworthPause) {
                        long millis = cwToneManager.wordSpaceToMillis();
                        System.out.println("Waiting: " + millis);
                        farnsworthPause.wait(millis);
                    }
                }
            } catch (InterruptedException e) {
                Timber.d(e, "Audio loop exiting");
                return;
            }
            Timber.d("Audio loop exiting outside of loop");
        };
    }

    private List<String> justStrings(List<org.apache.commons.lang3.tuple.Pair<String, Double>> inPlayLetters) {
        return inPlayLetters.stream().map(org.apache.commons.lang3.tuple.Pair::getKey).collect(Collectors.toList());
    }

    private List<Pair<String, Double>> letterWeights(List<org.apache.commons.lang3.tuple.Pair<String, Double>> inPlayLetters) {
        return inPlayLetters.stream().map(pair -> Pair.create(pair.getKey(), pair.getValue())).collect(Collectors.toList());
    }

    private String nextLetter() {
        if (lettersLeftInGroup <= 0) {
            lettersLeftInGroup = LENGTH_DISTRIBUTION.sample();
            Timber.d("Planning on playing %s letters", lettersLeftInGroup);
            return " ";
        }

        lettersLeftInGroup -= 1;

        return nextLetterDistribution.sample();
    }

    public void prime() {
        audioThread = new Thread(audioLoop);
    }

    public void start() {
        audioThreadKeepAlive = true;
        audioThread.start();
        engineIsStarted = true;
    }

    public void resume() {
        if (!isPaused || !engineIsStarted) {
            return;
        }
        isPaused = false;
        synchronized (pauseGate) {
            pauseGate.notify();
        }
    }

    public void pause() {
        if (isPaused || !engineIsStarted) {
            return;
        }
        isPaused = true;

        synchronized (farnsworthPause) {
            farnsworthPause.notify();
        }
    }

    public boolean isPaused() {
        return isPaused;
    }

    public void destroy() {
        if (!audioThreadKeepAlive) throw new AssertionError("Trying to destroy an already destroyed engine");
        audioThreadKeepAlive = false;
        if (audioThread != null && audioThread.isAlive() && !audioThread.isInterrupted()) {
            audioThread.interrupt();
            audioThread = null;
        }
        cwToneManager.destroy();
    }

    public TranscribeTrainingEngineSettings getSettings() {
        TranscribeTrainingEngineSettings settings = new TranscribeTrainingEngineSettings();
        settings.letterWpmRequested = letterWpmRequested;
        settings.transmitWpmRequested = transmitWpmRequested;
        settings.selectedStrings = inPlayLetters;
        return settings;
    }

    public void prepareForShutdown() {
        awaitingShutdown = true;
    }
}
