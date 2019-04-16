package com.uberj.pocketmorsepro.flashcard;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.uberj.pocketmorsepro.AudioManager;
import com.google.common.collect.Lists;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardEngineEvent;

import java.util.List;
import java.util.Random;

import androidx.lifecycle.MutableLiveData;


public class FlashcardTrainingEngine {
    private static final Random r = new Random();
    private final AudioManager audioManager;
    private volatile MutableLiveData<Long> cardsRemaining;
    private volatile boolean isPaused = false;
    private volatile boolean engineIsStarted = false;
    private String currentMessage;
    private final List<String> messages;
    public final List<FlashcardEngineEvent> events = Lists.newArrayList();
    private final Handler eventHandler;
    private static final int SUBMIT_GUESS = 100;
    private static final int SKIP = 101;
    private static final int REPEAT = 102;
    private static final int PLAY_CURRENT_MESSAGE = 103;

    public FlashcardTrainingEngine(AudioManager audioManager, List<String> messages, MutableLiveData<Long> durationUnitsRemaining) {
        this.audioManager = audioManager;
        this.messages = messages;
        this.cardsRemaining = durationUnitsRemaining;
        HandlerThread eventHandlerThread = new HandlerThread("GuessSoundHandler", HandlerThread.MAX_PRIORITY);
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
        synchronized (eventHandler) {
            if (message.what == SUBMIT_GUESS) {
                String guess = (String) message.obj;
                events.add(FlashcardEngineEvent.guessSubmitted(guess));
                if (cardsRemaining != null) {
                    cardsRemaining.postValue(cardsRemaining.getValue() - 1);
                }
                chooseDifferentMessage();
                playMessage(currentMessage);
            } else if (message.what == PLAY_CURRENT_MESSAGE) {
                playMessage(currentMessage);
            } else if (message.what == REPEAT) {
                playMessage(currentMessage);
                events.add(FlashcardEngineEvent.repeat());
            } else if (message.what == SKIP) {
                if (cardsRemaining != null) {
                    cardsRemaining.postValue(cardsRemaining.getValue() - 1);
                }
                chooseDifferentMessage();
                playMessage(currentMessage);
                events.add(FlashcardEngineEvent.skip());
            }
            return true;
        }
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
    }

    public void start() {
        engineIsStarted = true;
        eventHandler.sendEmptyMessageDelayed(PLAY_CURRENT_MESSAGE, 500L);
    }

    public void destroy() {
        audioManager.destroy();
        events.add(FlashcardEngineEvent.destroyed());
    }

    public void resume() {
        if (!isPaused || !engineIsStarted) {
            return;
        }
        events.add(FlashcardEngineEvent.resumed());
        isPaused = false;
    }

    public void pause() {
        if (isPaused || !engineIsStarted) {
            return;
        }
        events.add(FlashcardEngineEvent.paused());
        isPaused = true;
    }

    public void playLetter(String letter) {
        audioManager.playMessage(letter);
    }
}
