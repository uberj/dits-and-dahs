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
    @Query("SELECT * FROM LetterTrainingSession ORDER BY endTimeEpocMillis DESC")
    LiveData<List<LetterTrainingSession>> getAllSessions();

    default void getLatestSession(Consumer<Optional<LetterTrainingSession>> observerCallback) {
        LiveData<List<LetterTrainingSession>> getCallback = getAllSessions();
        getCallback.observeForever((allSessions) -> {
            if (allSessions == null || allSessions.isEmpty()) {
                observerCallback.accept(Optional.empty());
            } else {
                observerCallback.accept(Optional.of(allSessions.get(0)));
            }
        });
    }

    @Insert
    void insertSession(LetterTrainingSession letterTrainingSession);
}
