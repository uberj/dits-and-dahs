package com.example.uberj.test1.storage;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Dao
public interface TrainingSessionDAO {
    @Query("SELECT * FROM LetterTrainingSession ORDER BY endTimeEpocMilis DESC")
    LiveData<List<LetterTrainingSession>> getAllSessions();

    default void getLatestSession(Consumer<Optional<LetterTrainingSession>> observerCallback) {
        LiveData<List<LetterTrainingSession>> getCallback = getAllSessions();
        getCallback.observeForever((allSessions) -> {
            if (allSessions.isEmpty()) {
                observerCallback.accept(Optional.empty());
            } else {
                observerCallback.accept(Optional.of(allSessions.get(0)));
            }
        });
    }

    @Insert
    void insertSession(LetterTrainingSession letterTrainingSession);
}
