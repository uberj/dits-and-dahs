package com.example.uberj.test1.storage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class LetterTrainingSession {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "endTimeEpocMilis")
    @NonNull
    public Long endTimeEpocMilis;

    @ColumnInfo(name = "durationWorkedMilis")
    @NonNull
    public Long durationWorkedMilis;

    @ColumnInfo(name = "completed")
    @NonNull
    public Boolean completed;

    @ColumnInfo(name = "wpmAverage")
    @NonNull
    public float wpmAverage;

    @ColumnInfo(name = "errorRate")
    @NonNull
    public float errorRate;
}
