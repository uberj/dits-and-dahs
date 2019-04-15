package com.uberj.pocketmorsepro.flashcard;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.uberj.pocketmorsepro.AudioManager;
import com.google.common.collect.Lists;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardEngineEvent;

import java.util.List;
import java.util.Random;


public class FlashcardTrainingEngine {

    private static final String guessGate = "guessGate";
    private static final String pauseGate = "pauseGate";
    private static final String easyModePause = "easyModePause";

    private static final Random r = new Random();
    private final AudioManager audioManager;
    private static volatile boolean audioThreadKeepAlive = true;
    private volatile boolean isPaused = false;
    private volatile boolean engineIsStarted = false;
    private volatile boolean shortCircuitGuessWait;
    private List<String> playableMessages;
    private String currentMessage;
    private Thread audioThread;
    private Runnable audioLoop;
    private final List<String> messages;
    public final List<FlashcardEngineEvent> events = Lists.newArrayList();
    private volatile long sleepTime = -1;
    private final Handler eventHandler;
    private final HandlerThread eventHandlerThread;
    private static final int SUBMIT_GUESS = 100;
    private static final int SKIP = 101;
    private static final int REPEAT = 102;
    private static final int PLAY_CURRENT_MESSAGE = 103;

    public FlashcardTrainingEngine(AudioManager audioManager, List<String> messages, List<String> playableMessages) {
        this.audioManager = audioManager;
        this.messages = messages;
        this.playableMessages = playableMessages;
        eventHandlerThread = new HandlerThread("GuessSoundHandler", HandlerThread.MAX_PRIORITY);
        eventHandlerThread.start();
        eventHandler = new Handler(eventHandlerThread.getLooper(), this::eventCallback);
        this.chooseDifferentMessage();
    }

    public void repeat() {
        Message msg = new Message();
        msg.what = REPEAT;
        eventHandler.sendMessage(msg);
    }

    private boolean eventCallback(Message message) {
        if (message.what == SUBMIT_GUESS) {
            String guess = (String) message.obj;
            events.add(FlashcardEngineEvent.guessSubmitted(guess));
            chooseDifferentMessage();
            playMessage(currentMessage);
            playMessage(currentMessage);
        } else if (message.what == PLAY_CURRENT_MESSAGE) {
            playMessage(currentMessage);
        } else if (message.what == REPEAT) {
            playMessage(currentMessage);
            events.add(FlashcardEngineEvent.repeat());
        } else if (message.what == SKIP) {
            chooseDifferentMessage();
            playMessage(currentMessage);
            events.add(FlashcardEngineEvent.skip());
        }
        return true;
    }

    private void playMessage(String currentMessage) {
        audioManager.playMessage(currentMessage);
        events.add(FlashcardEngineEvent.messageDonePlaying(currentMessage));
    }

    public void skip() {
        Message msg = new Message();
        msg.what = SKIP;
        eventHandler.sendMessage(msg);
    }

    public void submitGuess(String guess) {
        Message msg = new Message();
        msg.obj = guess;
        msg.what = SUBMIT_GUESS;
        eventHandler.sendMessage(msg);
    }

    private void chooseDifferentMessage() {
        // TODO, smartly pick message
        currentMessage = messages.get(r.nextInt(messages.size()));
        events.add(FlashcardEngineEvent.messageChosen(currentMessage));
    }

    public void prime() {
        chooseDifferentMessage();
        audioThread = new Thread(audioLoop);
    }

    public void start() {
        audioThread.start();
        engineIsStarted = true;
        audioThreadKeepAlive = true;
        eventHandler.sendEmptyMessageDelayed(PLAY_CURRENT_MESSAGE, 500L);
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

    public void playLetter(String letter) {
        audioManager.playMessage(letter);
    }
}
