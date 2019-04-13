package com.uberj.pocketmorsepro.flashcard.storage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface FlashcardTrainingSessionDAO {
    @Query("SELECT * FROM FlashcardTrainingSession WHERE sessionType = :sessionType ORDER BY endTimeEpocMillis DESC")
    LiveData<List<FlashcardTrainingSession>> getAllSessions(String sessionType);

    @Query("SELECT * FROM FlashcardTrainingSession WHERE sessionType = :sessionType ORDER BY endTimeEpocMillis DESC LIMIT 1")
    @Transaction
    LiveData<List<FlashcardTrainingSessionWithEvents>> getLatestSessionAndEvents(String sessionType);

    @Insert
    long insertSession(FlashcardTrainingSession trainingSession);

    @Insert
    void insertEvents(List<FlashcardEngineEvent> events);

    default void insertSessionAndEvents(FlashcardTrainingSession trainingSession, List<FlashcardEngineEvent> events) {
        int sessionId = (int) insertSession(trainingSession);
        for (FlashcardEngineEvent event : events) {
            event.sessionId = sessionId;
        }
        insertEvents(events);
    }
}
