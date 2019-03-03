package com.example.uberj.test1.transcribe;

import com.example.uberj.test1.CWToneManager;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingEngineSettings;

import java.util.List;

import kotlin.random.Random;
import timber.log.Timber;

class TranscribeTrainingEngine {
    private final Runnable audioLoop;
    private final CWToneManager cwToneManager;
    private final String pauseGate = "pauseGate";
    private final String farnsworthPause = "farnsworthPause";
    private final List<String> inPlayLetters;

    private boolean audioThreadKeepAlive;
    private boolean isPaused;
    private Thread audioThread;
    private boolean engineIsStarted = false;

    public TranscribeTrainingEngine(int wpmRequested, int farnsworth, List<String> inPlayLetters) {
        this.inPlayLetters = inPlayLetters;
        this.cwToneManager = new CWToneManager(wpmRequested, farnsworth);
        this.audioLoop = () -> {
            try {
                while (Thread.currentThread() == audioThread) {
                    while (isPaused)  {
                        synchronized (pauseGate) {
                            pauseGate.wait();
                        }
                    }

                    // play next letter
                    if (audioThreadKeepAlive) {
                        String currentLetter = nextLetter();
                        Timber.d("Playing letter: %s", currentLetter);
                        cwToneManager.playLetter(currentLetter);
                    }

                    // start the callback timer to play again

                    synchronized (farnsworthPause) {
                        long millis = cwToneManager.spaceToMillisWithFarnsworthScale();
                        Timber.d("Waiting: " + millis);
                        farnsworthPause.wait();
                    }
                }
            } catch (InterruptedException e) {
                Timber.d(e, "Audio loop exiting");
                return;
            }
            Timber.d("Audio loop exiting outside of loop");
        };
    }

    private String nextLetter() {
        // TODO, put in spaces
        return inPlayLetters.get(Math.abs(Random.Default.nextInt()) % inPlayLetters.size());
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

    public void destroy() {
        audioThreadKeepAlive = false;
        audioThread.interrupt();
        audioThread = null;
        cwToneManager.destroy();
    }

    public TranscribeTrainingEngineSettings getSettings() {
        return null;
    }
}
