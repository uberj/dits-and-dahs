package com.example.uberj.test1.storage;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

@Dao
public interface CompetencyWeightsDAO {
    @Query("SELECT * FROM CompetencyWeights ORDER BY createdAtEpocMillis DESC")
    LiveData<List<CompetencyWeights>> getAllCompetencyWeights();

    @Insert
    void insertCompetencyWeights(CompetencyWeights competencyWeights);

    default void getLatestSession(Consumer<Optional<CompetencyWeights>> observerCallback) {
        LiveData<List<CompetencyWeights>> getCallback = getAllCompetencyWeights();
        getCallback.observeForever((allWeights) -> {
            if (allWeights == null || allWeights.isEmpty()) {
                observerCallback.accept(Optional.empty());
            } else {
                observerCallback.accept(Optional.of(allWeights.get(0)));
            }
        });
    }
}
