package com.uberj.pocketmorsepro.flashcard;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.uberj.pocketmorsepro.AudioManager;
import com.google.common.collect.Lists;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardEngineEvent;

import java.util.List;
import java.util.Optional;


public class FlashcardTrainingEngine {
    private static final int MISSED_LETTER_POINTS_REMOVED = 10;
    private static final int CORRECT_LETTER_POINTS_ADDED = 2;

    private static final String guessGate = "guessGate";
    private static final String pauseGate = "pauseGate";
    private static final String easyModePause = "easyModePause";
    private static final int LETTER_WEIGHT_MAX = 100;
    private static final int LETTER_WEIGHT_MIN = 0;
    private static final int INCLUSION_COMPETENCY_CUTOFF_WEIGHT = 50;

    private final AudioManager audioManager;
    private static volatile boolean audioThreadKeepAlive = true;
    private volatile boolean isPaused = false;
    private volatile boolean engineIsStarted = false;
    private volatile boolean shortCircuitGuessWait;
    private List<String> playableMessages;
    private String currentMessage;
    private Thread audioThread;
    private Runnable audioLoop;
    private final List<String> letterOrder;
    public final List<FlashcardEngineEvent> events = Lists.newArrayList();
    private volatile long sleepTime = -1;
    private final Handler guessHandler;
    private final HandlerThread guessSoundHandlerThread;
    private static final int SUBMIT_GUESS = 100;
    private static final int SKIP = 101;

    public FlashcardTrainingEngine(AudioManager audioManager, List<String> letterOrder, int wpm, List<String> playableMessages) {
        this.audioManager = audioManager;
        this.letterOrder = letterOrder;
        this.playableMessages = playableMessages;
        int playLetterWPM = wpm;
        guessSoundHandlerThread = new HandlerThread("GuessSoundHandler", HandlerThread.MAX_PRIORITY);
        guessSoundHandlerThread.start();
        guessHandler = new Handler(guessSoundHandlerThread.getLooper(), this::guessCallBack);
        this.chooseDifferentMessage();
    }

    private boolean guessCallBack(Message message) {
        if (message.what == SUBMIT_GUESS) {
            String guess = (String) message.obj;
        } else if (message.what == SKIP) {
            chooseDifferentMessage();
        }
        return true;
    }

    public void skip() {
        Message msg = new Message();
        msg.what = SKIP;
        guessHandler.sendMessage(msg);
    }

    public void submitGuess(String guess) {
        Message msg = new Message();
        msg.obj = guess;
        msg.what = SUBMIT_GUESS;
        guessHandler.sendMessage(msg);
    }

    private long getGuessWaitTimeMillis() {
        return 3000;
    }

    private void chooseDifferentMessage() {
        events.add(FlashcardEngineEvent.letterChosen(currentMessage));
        throw new RuntimeException("dump");
    }

    public void prime() {
        chooseDifferentMessage();
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
        audioManager.destroy();
        events.add(FlashcardEngineEvent.destroyed());
    }

    public void resume() {
        if (!isPaused || !engineIsStarted) {
            return;
        }
        events.add(FlashcardEngineEvent.resumed());
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
        events.add(FlashcardEngineEvent.paused());
        isPaused = true;
        sleepTime = 0;

        synchronized (guessGate) {
            guessGate.notify();
        }
        synchronized (easyModePause) {
            easyModePause.notifyAll();
        }
    }

    public void setPlayableMessages(List<String> playableMessages) {
        this.playableMessages = playableMessages;
        if (!playableMessages.contains(currentMessage)) {
            chooseDifferentMessage();
        }
    }

    public boolean isValidGuess(String letter) {
        return playableMessages.contains(letter);
    }

    public Optional<List<String>> introduceLetter() {
        int furthestLetterIdx = -1;
        for (String playableKey : playableMessages) {
            furthestLetterIdx = Math.max(furthestLetterIdx, letterOrder.indexOf(playableKey));
        }
        if (furthestLetterIdx > letterOrder.size()) {
            return Optional.empty();
        }
        String nextLetter = letterOrder.get(furthestLetterIdx + 1);
        this.playableMessages.add(nextLetter);
        return Optional.of(this.playableMessages);
    }

    public void playLetter(String letter) {
        audioManager.playMessage(letter);
    }
}
