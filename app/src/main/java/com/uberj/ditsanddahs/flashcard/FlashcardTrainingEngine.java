package com.uberj.ditsanddahs.flashcard;

import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;

import com.google.common.collect.Maps;
import com.uberj.ditsanddahs.AudioManager;
import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.WeightUtil;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardEngineEvent;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardSessionType;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.lang3.tuple.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import androidx.lifecycle.MutableLiveData;


public class FlashcardTrainingEngine {
    private final AudioManager audioManager;
    private final HandlerThread eventHandlerThread;
    private final FlashcardSessionType sessionType;
    private volatile MutableLiveData<Long> cardsRemaining;
    private volatile boolean isPaused = false;
    private volatile boolean engineIsStarted = false;
    private String currentMessage;
    public final List<FlashcardEngineEvent> events = Lists.newArrayList();
    public final Map<String, Integer> competencyWeights;
    private final Handler eventHandler;
    private static final int SUBMIT_GUESS = 100;
    private static final int SKIP = 101;
    private static final int REPEAT = 102;
    private static final int PLAY_CURRENT_MESSAGE = 103;

    public FlashcardTrainingEngine(AudioManager audioManager, FlashcardSessionType sessionType, List<String> messages, MutableLiveData<Long> durationUnitsRemaining) {
        this.audioManager = audioManager;
        this.cardsRemaining = durationUnitsRemaining;
        this.sessionType = sessionType;
        if (sessionType.equals(FlashcardSessionType.RANDOM_FCC_CALLSIGNS)) {
            this.competencyWeights = null;
        } else {
            this.competencyWeights = buildInitialCompetencyWeights(messages);
        }
        eventHandlerThread = new HandlerThread("GuessHandler", HandlerThread.MAX_PRIORITY);
        eventHandlerThread.start();
        eventHandler = new Handler(eventHandlerThread.getLooper(), this::eventCallback);
        this.chooseDifferentMessage();
    }

    private Map<String, Integer> buildInitialCompetencyWeights(List<String> messages) {
        Map<String, Integer> weights = Maps.newConcurrentMap();
        for (String message : messages) {
            weights.put(message, 100);
        }
        return weights;
    }

    public void repeat() {
        Message msg = new Message();
        msg.what = REPEAT;
        eventHandler.sendMessage(msg);
    }

    private boolean eventCallback(Message message) {
        synchronized (eventHandler) {
            if (message.what == SUBMIT_GUESS) {
                Pair<Boolean, String> obj = (Pair<Boolean, String>) message.obj;
                Boolean wasCorrect = obj.getLeft();
                String guess = obj.getRight();
                if (wasCorrect) {
                    if (competencyWeights != null) {
                        WeightUtil.increment(competencyWeights, currentMessage, 50);
                    }
                    events.add(FlashcardEngineEvent.correctGuessSubmitted(guess));
                    if (cardsRemaining != null) {
                        cardsRemaining.postValue(cardsRemaining.getValue() - 1);
                    }
                    audioManager.playCorrectTone();
                    chooseDifferentMessage();
                    playMessageAfterDelay();
                } else {
                    if (competencyWeights != null) {
                        WeightUtil.decrement(competencyWeights, currentMessage, 20);
                    }
                    events.add(FlashcardEngineEvent.incorrectGuessSubmitted(guess));
                    audioManager.playIncorrectTone();
                    if (cardsRemaining != null) {
                        cardsRemaining.postValue(cardsRemaining.getValue() - 1);
                    }
                }
            } else if (message.what == PLAY_CURRENT_MESSAGE) {
                playMessage(currentMessage);
            } else if (message.what == REPEAT) {
                playMessage(currentMessage);
                events.add(FlashcardEngineEvent.repeat());
            } else if (message.what == SKIP) {
                eventHandler.removeMessages(SKIP);
                if (competencyWeights != null) {
                    WeightUtil.decrement(competencyWeights, currentMessage, 20);
                }
                if (cardsRemaining != null) {
                    cardsRemaining.postValue(cardsRemaining.getValue() - 1);
                }
                chooseDifferentMessage();
                events.add(FlashcardEngineEvent.skip());
                playMessageAfterDelay();
            }
            return true;
        }
    }

    private void playMessageAfterDelay() {
        Message playAgainMsg = new Message();
        playAgainMsg.what = PLAY_CURRENT_MESSAGE;
        eventHandler.sendMessageDelayed(playAgainMsg, 500);
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

    public boolean submitGuess(String guess) {
        Message msg = new Message();
        msg.what = SUBMIT_GUESS;
        boolean isCorrect = currentMessage.equals(guess);
        msg.obj = Pair.of(isCorrect, guess);
        eventHandler.sendMessage(msg);
        return isCorrect;
    }

    private List<org.apache.commons.math3.util.Pair<String,Double>> buildPmfCompetencyWeights(String currentMessage) {
        ArrayList<org.apache.commons.math3.util.Pair<String, Double>> pmf = Lists.newArrayList();
        for (Map.Entry<String, Integer> entry : competencyWeights.entrySet()) {
            String letter = entry.getKey();
            if (letter.equals(currentMessage)) {
                // Don't include the current letter in the pmf
                continue;
            }
            int letterWeight = entry.getValue();
            pmf.add(new org.apache.commons.math3.util.Pair<>(letter, Math.max(1, (double) letterWeight)));
        }
        return pmf;
    }

    private void chooseDifferentMessage() {
        if (sessionType.equals(FlashcardSessionType.RANDOM_FCC_CALLSIGNS)) {
            currentMessage = RandomCallSignGenerator.getCall();
            events.add(FlashcardEngineEvent.messageChosen(currentMessage));
        } else {
            List<org.apache.commons.math3.util.Pair<String, Double>> pmfCompetencyWeights = buildPmfCompetencyWeights(currentMessage);
            currentMessage = new EnumeratedDistribution<>(pmfCompetencyWeights).sample();
            events.add(FlashcardEngineEvent.messageChosen(currentMessage));
        }
    }

    public void prime() {
        chooseDifferentMessage();
    }

    public void start() {
        engineIsStarted = true;
        eventHandler.sendEmptyMessageDelayed(PLAY_CURRENT_MESSAGE, 500L);
    }

    public void destroy() {
        synchronized (eventHandler) {
            eventHandlerThread.quit();
            audioManager.destroy();
            events.add(FlashcardEngineEvent.destroyed());
        }
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
}
