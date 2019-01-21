package com.example.uberj.test1;

import java.util.List;
import java.util.Random;

public class LetterTrainingEngine {
    private final Random r = new Random();
    private final CWToneManager cwToneManager;
    private List<String> playableKeys;
    private String currentLetter = null;
    private Thread audioThread;
    private volatile boolean threadKeepAlive = true;
    private volatile boolean isPaused = true;
    private Runnable audioLoop;

    public LetterTrainingEngine(List<String> playableKeys) {
        this.cwToneManager = new CWToneManager();
        this.playableKeys = playableKeys;
    }

    public void guess(String guess) {
        // If its right
        if (isPaused) {
            return;
        }

        if (guess.equals(currentLetter)) {
            currentLetter = playableKeys.get(r.nextInt(playableKeys.size()));
        }

        audioThread.interrupt();
        audioThread = new Thread(audioLoop);
        audioThread.start();
    }

    public void initStart() {
        currentLetter = playableKeys.get(r.nextInt(playableKeys.size()));
        audioLoop = () -> {
            while (true) {
                if (!threadKeepAlive) {
                    return;
                }
                // pick a letter
                // play it
                cwToneManager.playLetter(currentLetter);
                // initStart the callback timer to play again
                try {
                    Thread.sleep(getSleepPeriodMilis());
                } catch (InterruptedException e) {
                    return;
                }

            }
        };
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
        audioThread = new Thread(audioLoop);
        audioThread.start();
        isPaused = false;
    }

    public void pause() {
        isPaused = true;
        audioThread.interrupt();
    }
}
