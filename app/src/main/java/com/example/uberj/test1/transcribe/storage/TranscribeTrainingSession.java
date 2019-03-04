package com.example.uberj.test1.transcribe.storage;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TranscribeTrainingSession {
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
    public float errorRate;

    @NonNull
    public Long transmitWpm;

    @NonNull
    public Long letterWpm;

    @NonNull
    public String sessionType;

    @NonNull
    public List<String> playedKeys;

    @NonNull
    public List<String> enteredKeys;
}
