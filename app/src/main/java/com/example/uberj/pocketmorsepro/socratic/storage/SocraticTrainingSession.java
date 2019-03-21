package com.example.uberj.pocketmorsepro.socratic.storage;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity
public class SocraticTrainingSession {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @NonNull
    public Long endTimeEpocMillis;

    @NonNull
    public Long durationWorkedMillis;

    @NonNull
    public Long durationRequestedMillis;

    @NonNull
    public Boolean completed;

    @NonNull
    public float wpmAverage;

    @NonNull
    public double accuracy;

    @NonNull
    public String sessionType;
}
