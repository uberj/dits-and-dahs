package com.uberj.pocketmorsepro.flashcard.storage;

import com.uberj.pocketmorsepro.storage.converters.FlashcardEventTypeConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity
public class FlashcardEngineEvent {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int sessionId;

    @Nonnull
    @TypeConverters(FlashcardEventTypeConverter.class)
    public EventType eventType;

    @Nonnull
    public Long eventAtEpoc;

    @Nullable
    public String info;

    public enum EventType {
        DONE_PLAYING(0),
        RESUME(1),
        PAUSE(2),
        LETTER_CHOSEN(3),
        CORRECT_GUESS(4),
        DESTROYED(5),
        INCORRECT_GUESS(6),
        GUESS_SUBMITTED(7), REPEAT(8), SKIP(9);

        public final int code;

        EventType(int code) {
            this.code = code;
        }
    }

    public static FlashcardEngineEvent letterDonePlaying(String currentLetter) {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.DONE_PLAYING;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        engineEvent.info = currentLetter;
        return engineEvent;
    }

    public static FlashcardEngineEvent resumed() {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.RESUME;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        return engineEvent;
    }

    public static FlashcardEngineEvent paused() {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.PAUSE;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        return engineEvent;
    }

    public static FlashcardEngineEvent letterChosen(String currentLetter) {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.LETTER_CHOSEN;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        engineEvent.info = currentLetter;
        return engineEvent;
    }

    public static FlashcardEngineEvent correctGuess(String currentLetter) {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.CORRECT_GUESS;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        engineEvent.info = currentLetter;
        return engineEvent;
    }

    public static FlashcardEngineEvent incorrectGuess(String currentLetter) {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.INCORRECT_GUESS;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        engineEvent.info = currentLetter;
        return engineEvent;
    }
    public static FlashcardEngineEvent destroyed() {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.DESTROYED;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        return engineEvent;
    }

    public static FlashcardEngineEvent guessSubmitted(String guess) {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.GUESS_SUBMITTED;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        engineEvent.info = guess;
        return engineEvent;
    }

    public static FlashcardEngineEvent repeat(String currentMessage) {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.REPEAT;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        return engineEvent;
    }

    public static FlashcardEngineEvent skip() {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.SKIP;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        return engineEvent;
    }
}
