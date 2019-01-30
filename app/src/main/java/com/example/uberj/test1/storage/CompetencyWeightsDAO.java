package com.example.uberj.test1.storage;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface CompetencyWeightsDAO {
    @Query("SELECT * FROM CompetencyWeights ORDER BY createdAtEpocMillis DESC")
    LiveData<List<CompetencyWeights>> getAllCompetencyWeights();

    @Insert
    void insertCompetencyWeights(CompetencyWeights competencyWeights);
}
