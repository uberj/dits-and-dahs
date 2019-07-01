package com.uberj.ditsanddahs.transcribe;

import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Supplier;
import com.uberj.ditsanddahs.AudioManager;

import java.util.concurrent.Callable;

import timber.log.Timber;

public class TranscribeTrainingEngine {
    private final Runnable audioLoop;
    private final AudioManager audioManager;
    private final String pauseGate = "pauseGate";
    private final String farnsworthPause = "farnsworthPause";
    private final Consumer<String> letterPlayedCallback;
    private final Supplier<String> letterSupplier;
    private final Callable<Void> messageFinishedPlayingCallback;
    private final AudioManager.MorseConfig morseConfig;
    private final long stationSwitchDelay = 1000; // TODO, make this configurable

    private boolean audioThreadKeepAlive;
    private boolean isPaused;
    private Thread audioThread;
    private boolean engineIsStarted = false;
    private boolean awaitingShutdown = false;

    public TranscribeTrainingEngine(AudioManager audioManager, int startDelaySeconds, Consumer<String> letterPlayedCallback, Supplier<String> letterSupplier, Callable<Void> messageFinishedPlayingCallback, AudioManager.MorseConfig morseConfig) {
        this.letterSupplier = letterSupplier;

        this.letterPlayedCallback = letterPlayedCallback;
        this.audioManager = audioManager;
        this.messageFinishedPlayingCallback = messageFinishedPlayingCallback;
        this.morseConfig = morseConfig;
        this.audioLoop = () -> {
            try {
                synchronized (pauseGate) {
                    pauseGate.wait(startDelaySeconds * 1000);
                }

                while (Thread.currentThread() == audioThread) {
                    while (isPaused)  {
                        synchronized (pauseGate) {
                            pauseGate.wait();
                        }
                    }

                    // play next letter
                    if (!awaitingShutdown && audioThreadKeepAlive) {
                        String currentLetter = this.letterSupplier.get();
                        if (currentLetter == null) {
                            this.messageFinishedPlayingCallback.call();
                            return;
                        }

                        this.letterPlayedCallback.accept(currentLetter);
                        if (currentLetter.equals(QSOWordSupplier.STATION_SWITCH_MARKER)) {
                            Timber.d("Station switch marker");
                            // start the callback timer to play again
                            synchronized (farnsworthPause) {
                                farnsworthPause.wait(stationSwitchDelay);
                            }
                        } else {
                            Timber.d("Playing letter: '%s'", currentLetter);
                            audioManager.playMessage(currentLetter, this.morseConfig);
                        }
                    }

                    // The session is ending soon
                    if (awaitingShutdown) {
                        return;
                    }

                    // start the callback timer to play again
                    synchronized (farnsworthPause) {
                        long millis = audioManager.wordSpaceToMillis(morseConfig);
                        farnsworthPause.wait(millis);
                    }
                }
            } catch (Exception e) {
                Timber.d(e, "Audio loop exiting");
                return;
            }
            Timber.d("Audio loop exiting outside of loop");
        };
    }


    public void prime() {
        audioThread = new Thread(audioLoop);
    }

    public void start() {
        audioThreadKeepAlive = true;
        audioThread.start();
        engineIsStarted = true;
        isPaused = false;
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
            audioThread = null;
        }
        audioManager.destroy();
    }

    public void prepareForShutdown() {
        awaitingShutdown = true;
    }
}
