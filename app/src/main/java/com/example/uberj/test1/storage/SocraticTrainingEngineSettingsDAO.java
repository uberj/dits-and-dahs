package com.example.uberj.test1.storage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface SocraticTrainingEngineSettingsDAO {
    @Query("SELECT * FROM SocraticTrainingEngineSettings WHERE sessionType = :sessionType ORDER BY createdAtEpocMillis DESC")
    LiveData<List<SocraticTrainingEngineSettings>> getAllEngineSettings(String sessionType);

    @Query("SELECT * FROM SocraticTrainingEngineSettings WHERE sessionType = :sessionType ORDER BY createdAtEpocMillis DESC LIMIT 1")
    LiveData<List<SocraticTrainingEngineSettings>> getLatestEngineSetting(String sessionType);

    @Insert
    void insertEngineSettings(SocraticTrainingEngineSettings engineSettings);
}
