package com.example.uberj.test1.storage;

import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.Query;

import java.util.List;

@Dao
public interface TrainingSessionDAO {
    @Query("SELECT * FROM TrainingSession")
    List<TrainingSession> getAllSessions();

    @Query("SELECT * FROM TrainingSession WHERE session_type=:session_type")
    List<TrainingSession> getSessionByType(String session_type);

    @Insert
    void insertSession(TrainingSession trainingSession);
}
