package com.uberj.pocketmorsepro.simplesocratic.storage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.List;

@Dao
public interface SocraticTrainingSessionDAO {
    @Query("SELECT * FROM SocraticTrainingSession WHERE sessionType = :sessionType ORDER BY endTimeEpocMillis DESC")
    LiveData<List<SocraticTrainingSession>> getAllSessions(String sessionType);

    @Query("SELECT * FROM SocraticTrainingSession WHERE sessionType = :sessionType ORDER BY endTimeEpocMillis DESC LIMIT 1")
    @Transaction
    LiveData<List<SocraticTrainingSessionWithEvents>> getLatestSessionAndEvents(String sessionType);

    @Insert
    long insertSession(SocraticTrainingSession trainingSession);

    @Insert
    void insertEvents(List<SocraticEngineEvent> events);

    default void insertSessionAndEvents(SocraticTrainingSession trainingSession, List<SocraticEngineEvent> events) {
        int sessionId = (int) insertSession(trainingSession);
        for (SocraticEngineEvent event : events) {
            event.sessionId = sessionId;
        }
        insertEvents(events);
    }

}
