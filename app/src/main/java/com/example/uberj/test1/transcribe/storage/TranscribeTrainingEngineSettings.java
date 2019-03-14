package com.example.uberj.test1.transcribe.storage;

import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity
public class TranscribeTrainingEngineSettings {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @NonNull
    public long createdAtEpocMillis;

    @NonNull
    public int letterWpmRequested;

    @NonNull
    public int effectiveWpmRequested;

    @NonNull
    public Long durationRequestedMillis;

    @NonNull
    public String sessionType;

    @NonNull
    public List<String> selectedStrings;

    @NonNull
    public boolean targetIssueLetters;
}
