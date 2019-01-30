package com.example.uberj.test1.LetterTraining;

import com.example.uberj.test1.CWToneManager;

import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;

public class LetterTrainingEngine {
    private static final String guessGate = "guessGate";
    private static final String pauseGate = "pauseGate";
    private static final String audioGate = "audioGate";
    private static final String TAG = "LetterTrainingEngine";

    private final Random r = new Random();
    private final CWToneManager cwToneManager;
    private final Consumer<String> letterChosenCallback;
    private volatile boolean waitingForGuess;
    private List<String> playableKeys;
    private String currentLetter;
    private Thread audioThread;
    private volatile boolean threadKeepAlive = true;
    private volatile boolean isPaused = false;
    private Runnable audioLoop;

    public LetterTrainingEngine(int wpm, final Consumer<String> letterPlayedCallback, Consumer<String> letterChosenCallback, List<String> playableKeys) {
        this.letterChosenCallback = letterChosenCallback;
        this.cwToneManager = new CWToneManager(wpm);
        this.playableKeys = playableKeys;
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
                letterPlayedCallback.accept(currentLetter);

                waitingForGuess = true;
                synchronized (guessGate) {
                    try {
                        guessGate.wait(getGuessWaitTimeMillis());
                    } catch (InterruptedException e) {
                        return;
                    }
                }
                waitingForGuess = false;
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
            currentLetter = playableKeys.get(r.nextInt(playableKeys.size()));
            letterChosenCallback.accept(currentLetter);
            isCorrectGuess = true;
        }

        synchronized (guessGate) {
            guessGate.notify();
        }

        return Optional.of(isCorrectGuess);
    }

    public void initEngine() {
        currentLetter = playableKeys.get(r.nextInt(playableKeys.size()));
        letterChosenCallback.accept(currentLetter);
        audioThread = new Thread(audioLoop);
        audioThread.start();
    }

    public void destroy() {
        audioThread.interrupt();
    }

    public void resume() {
        if (!isPaused) {
            return;
        }
        isPaused = false;
        synchronized (pauseGate) {
            pauseGate.notify();
        }
    }

    public void pause() {
        if (isPaused) {
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
}
