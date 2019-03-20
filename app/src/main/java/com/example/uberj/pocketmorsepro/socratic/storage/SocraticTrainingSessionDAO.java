package com.example.uberj.morsepocketpro.socratic.storage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SocraticTrainingSessionDAO {
    @Query("SELECT * FROM SocraticTrainingSession WHERE sessionType = :sessionType ORDER BY endTimeEpocMillis DESC")
    LiveData<List<SocraticTrainingSession>> getAllSessions(String sessionType);

    @Query("SELECT * FROM SocraticTrainingSession WHERE sessionType = :sessionType ORDER BY endTimeEpocMillis DESC LIMIT 1")
    LiveData<List<SocraticTrainingSession>> getLatestSession(String sessionType);

    @Insert
    void insertSession(SocraticTrainingSession trainingSession);

}
