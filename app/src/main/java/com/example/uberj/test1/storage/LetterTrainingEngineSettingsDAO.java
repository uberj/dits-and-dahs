package com.example.uberj.test1.storage;

import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Dao
public interface LetterTrainingEngineSettingsDAO {
    @Query("SELECT * FROM LetterTrainingEngineSettings ORDER BY createdAtEpocMillis DESC")
    LiveData<List<LetterTrainingEngineSettings>> getAllEngineSettings();

    @Query("SELECT * FROM LetterTrainingEngineSettings ORDER BY createdAtEpocMillis DESC LIMIT 1")
    LiveData<List<LetterTrainingEngineSettings>> getLatestEngineSetting();

    @Insert
    void insertEngineSettings(LetterTrainingEngineSettings engineSettings);
}
