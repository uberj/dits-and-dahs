package com.example.uberj.test1.transcribe.storage;

import java.util.List;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

@Dao
public interface TranscribeTrainingEngineSettingsDAO {
    @Query("SELECT * FROM TranscribeTrainingEngineSettings WHERE sessionType = :sessionType ORDER BY createdAtEpocMillis DESC")
    LiveData<List<TranscribeTrainingEngineSettings>> getAllEngineSettings(String sessionType);

    @Query("SELECT * FROM TranscribeTrainingEngineSettings WHERE sessionType = :sessionType ORDER BY createdAtEpocMillis DESC LIMIT 1")
    LiveData<List<TranscribeTrainingEngineSettings>> getLatestEngineSetting(String sessionType);

    @Insert
    void insertEngineSettings(TranscribeTrainingEngineSettings engineSettings);
}
