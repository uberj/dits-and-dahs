package com.example.uberj.test1.storage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Dao
public interface LetterTrainingSessionDAO {
    @Query("SELECT * FROM LetterTrainingSession WHERE sessionType = :sessionType ORDER BY endTimeEpocMillis DESC")
    LiveData<List<LetterTrainingSession>> getAllSessions(String sessionType);

    @Query("SELECT * FROM LetterTrainingSession WHERE sessionType = :sessionType ORDER BY endTimeEpocMillis DESC LIMIT 1")
    LiveData<List<LetterTrainingSession>> getLatestSession(String sessionType);

    @Insert
    void insertSession(LetterTrainingSession letterTrainingSession);

}
