package com.uberj.ditsanddahs.flashcard.storage;

import com.uberj.ditsanddahs.storage.converters.FlashcardEventTypeConverter;

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

    public static FlashcardEngineEvent correctGuessSubmitted(String guess) {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.CORRECT_GUESS;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        engineEvent.info = guess;
        return engineEvent;
    }

    public static FlashcardEngineEvent incorrectGuessSubmitted(String guess) {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.INCORRECT_GUESS;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        engineEvent.info = guess;
        return engineEvent;
    }

    public enum EventType {
        DONE_PLAYING(0),
        RESUME(1),
        PAUSE(2),
        MESSAGE_CHOSEN(3),
        DESTROYED(4),
        REPEAT(5),
        SKIP(6),
        CORRECT_GUESS(7),
        INCORRECT_GUESS(8);

        public final int code;

        EventType(int code) {
            this.code = code;
        }
    }

    public static FlashcardEngineEvent messageDonePlaying(String message) {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.DONE_PLAYING;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        engineEvent.info = message;
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

    public static FlashcardEngineEvent messageChosen(String currentLetter) {
        FlashcardEngineEvent engineEvent = new FlashcardEngineEvent();
        engineEvent.eventType = EventType.MESSAGE_CHOSEN;
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

    public static FlashcardEngineEvent repeat() {
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
