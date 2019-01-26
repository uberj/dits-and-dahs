package com.example.uberj.test1.LetterTraining;

import com.example.uberj.test1.CWToneManager;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.Semaphore;

public class LetterTrainingEngine {
    private static final Semaphore mutex = new Semaphore(1);

    private final Random r = new Random();
    private final CWToneManager cwToneManager;
    private List<String> playableKeys;
    private String currentLetter;
    private Thread audioThread;
    private volatile boolean threadKeepAlive = true;
    private volatile boolean isPaused = false;
    private Runnable audioLoop;

    public LetterTrainingEngine(List<String> playableKeys) {
        this.cwToneManager = new CWToneManager();
        this.playableKeys = playableKeys;
        this.audioLoop = () -> {
            while (true) {
                // This isn't working, multiple things are playing
                // I think I shouldn't call interrupt and instead flip a volitile to indicate the cancel request
                if (mutex.tryAcquire(1)) {
                    // Got the lock
                    try {
                        // Process record
                        if (!threadKeepAlive) {
                            return;
                        }

                        try {
                            // play it letter
                            cwToneManager.playLetter(currentLetter);
                            // start the callback timer to play again
                            Thread.sleep(getSleepPeriodMilis());
                        } catch (InterruptedException e) {
                            return;
                        }
                    } finally {
                        // Make sure to unlock so that we don't cause a deadlock
                        mutex.release(1);
                    }
                } else {
                    // Someone else had the lock
                    return;
                }


            }
        };
    }

    public Optional<Boolean> guess(String guess) {
        // If its right
        if (isPaused) {
            return Optional.empty();
        }

        boolean isCorrectGuess = false;
        if (guess.equals(currentLetter)) {
            currentLetter = playableKeys.get(r.nextInt(playableKeys.size()));
            isCorrectGuess = true;
        }

        audioThread.interrupt();
        audioThread = new Thread(audioLoop);
        audioThread.start();
        return Optional.of(isCorrectGuess);
    }

    public void initEngine() {
        currentLetter = playableKeys.get(r.nextInt(playableKeys.size()));
        audioThread = new Thread(audioLoop);
        audioThread.start();
    }

    private long getSleepPeriodMilis() {
        return 3000;
    }

    public void destroy() {
        threadKeepAlive = false;
    }

    public void resume() {
        if (!isPaused) {
            return;
        }
        audioThread = new Thread(audioLoop);
        audioThread.start();
        isPaused = false;
    }

    public void pause() {
        if (isPaused) {
            return;
        }
        isPaused = true;
        audioThread.interrupt();
    }
}