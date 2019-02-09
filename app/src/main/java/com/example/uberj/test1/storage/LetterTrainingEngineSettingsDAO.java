package com.example.uberj.test1.storage;

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

    @Insert
    void insertEngineSettings(LetterTrainingEngineSettings engineSettings);

    default void getLatestEngineSetting(Consumer<Optional<LetterTrainingEngineSettings>> observerCallback) {
        LiveData<List<LetterTrainingEngineSettings>> getCallback = getAllEngineSettings();
        getCallback.observeForever((allWeights) -> {
            if (allWeights == null || allWeights.isEmpty()) {
                observerCallback.accept(Optional.empty());
            } else {
                observerCallback.accept(Optional.of(allWeights.get(0)));
            }
        });
    }
}
