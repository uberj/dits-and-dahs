package com.uberj.ditsanddahs.transcribe;

import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Supplier;
import com.uberj.ditsanddahs.AudioManager;

import org.apache.commons.lang3.tuple.Pair;

import java.util.concurrent.Callable;

import timber.log.Timber;

public class TranscribeTrainingEngine {
    private final Runnable audioLoop;
    private final AudioManager audioManager;
    private final String pauseGate = "pauseGate";
    private final String farnsworthPause = "farnsworthPause";
    private final Consumer<String> letterPlayedCallback;
    private final Callable<Void> messageFinishedPlayingCallback;
    private final Supplier<Pair<String, AudioManager.MorseConfig>> letterSupplier;

    private volatile boolean audioThreadKeepAlive;
    private volatile boolean isPaused;
    private Thread audioThread;
    private volatile boolean engineIsStarted = false;
    private volatile boolean awaitingShutdown = false;

    public TranscribeTrainingEngine(AudioManager audioManager, Consumer<String> letterPlayedCallback, Supplier<Pair<String, AudioManager.MorseConfig>> letterSupplier, Callable<Void> messageFinishedPlayingCallback, int secondsBetweenStationTransmissions) {
        this.letterSupplier = letterSupplier;

        this.letterPlayedCallback = letterPlayedCallback;
        this.audioManager = audioManager;
        this.messageFinishedPlayingCallback = messageFinishedPlayingCallback;
        this.audioLoop = () -> {
            try {
                while (Thread.currentThread() == audioThread) {
                    while (isPaused)  {
                        synchronized (pauseGate) {
                            pauseGate.wait();
                        }
                    }

                    // play next letter
                    Pair<String, AudioManager.MorseConfig> pair = this.letterSupplier.get();
                    if (pair == null) {
                        this.messageFinishedPlayingCallback.call();
                        return;
                    }
                    String currentLetter = pair.getKey();
                    AudioManager.MorseConfig morseConfig = pair.getValue();

                    if (!awaitingShutdown && audioThreadKeepAlive) {
                        this.letterPlayedCallback.accept(currentLetter);
                        if (currentLetter.equals(QSOWordSupplier.STATION_SWITCH_MARKER)) {
                            Timber.d("Station switch marker");
                            // start the callback timer to play again
                            synchronized (farnsworthPause) {
                                farnsworthPause.wait(secondsBetweenStationTransmissions * 1000);
                            }
                        } else {
                            Timber.d("Playing letter: '%s'", currentLetter);
                            audioManager.playMessage(currentLetter, morseConfig);
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
