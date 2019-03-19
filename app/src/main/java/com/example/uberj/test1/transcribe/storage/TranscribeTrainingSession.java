package com.example.uberj.test1.transcribe.storage;

import java.util.ArrayList;
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
    public Long durationRequestedMillis;

    @NonNull
    public Boolean completed;

    @NonNull
    public Long effectiveWpm;

    @NonNull
    public Long letterWpm;

    @NonNull
    public String sessionType;

    @NonNull
    public List<String> playedMessage;

    @NonNull
    public List<String> enteredKeys;

    @NonNull
    public List<String> stringsRequested;

    @NonNull
    public Boolean targetIssueLetters;

    @NonNull
    public Integer audioToneFrequency;

    @NonNull
    public Integer startDelaySeconds;

    @NonNull
    public Integer endDelaySeconds;
}
