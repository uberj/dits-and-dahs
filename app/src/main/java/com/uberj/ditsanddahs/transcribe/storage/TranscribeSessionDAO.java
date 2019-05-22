package com.uberj.ditsanddahs.transcribe.storage;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface TranscribeSessionDAO {
    @Query("SELECT * FROM TranscribeTrainingSession WHERE sessionType = :sessionType ORDER BY endTimeEpocMillis DESC")
    LiveData<List<TranscribeTrainingSession>> getAllSessions(String sessionType);

    @Query("SELECT * FROM TranscribeTrainingSession WHERE sessionType = :sessionType ORDER BY endTimeEpocMillis DESC LIMIT 1")
    LiveData<List<TranscribeTrainingSession>> getLatestSession(String sessionType);

    @Insert
    void insertSession(TranscribeTrainingSession trainingSession);

}
