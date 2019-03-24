package com.uberj.pocketmorsepro.socratic.storage;

import com.uberj.pocketmorsepro.storage.converters.EventTypeConverter;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

@Entity
public class SocraticEngineEvent {

    @PrimaryKey(autoGenerate = true)
    public int id;

    public int sessionId;

    @Nonnull
    @TypeConverters(EventTypeConverter.class)
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
        INCORRECT_GUESS(6);

        public final int code;

        EventType(int code) {
            this.code = code;
        }
    }

    public static SocraticEngineEvent letterDonePlaying() {
        SocraticEngineEvent engineEvent = new SocraticEngineEvent();
        engineEvent.eventType = EventType.DONE_PLAYING;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        return engineEvent;
    }

    public static SocraticEngineEvent resumed() {
        SocraticEngineEvent engineEvent = new SocraticEngineEvent();
        engineEvent.eventType = EventType.RESUME;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        return engineEvent;
    }

    public static SocraticEngineEvent paused() {
        SocraticEngineEvent engineEvent = new SocraticEngineEvent();
        engineEvent.eventType = EventType.PAUSE;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        return engineEvent;
    }

    public static SocraticEngineEvent letterChosen(String currentLetter) {
        SocraticEngineEvent engineEvent = new SocraticEngineEvent();
        engineEvent.eventType = EventType.LETTER_CHOSEN;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        engineEvent.info = currentLetter;
        return engineEvent;
    }

    public static SocraticEngineEvent correctGuess(String currentLetter) {
        SocraticEngineEvent engineEvent = new SocraticEngineEvent();
        engineEvent.eventType = EventType.CORRECT_GUESS;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        engineEvent.info = currentLetter;
        return engineEvent;
    }

    public static SocraticEngineEvent incorrectGuess(String currentLetter) {
        SocraticEngineEvent engineEvent = new SocraticEngineEvent();
        engineEvent.eventType = EventType.INCORRECT_GUESS;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        engineEvent.info = currentLetter;
        return engineEvent;
    }
    public static SocraticEngineEvent destroyed() {
        SocraticEngineEvent engineEvent = new SocraticEngineEvent();
        engineEvent.eventType = EventType.DESTROYED;
        engineEvent.eventAtEpoc = System.currentTimeMillis();
        return engineEvent;
    }
}
