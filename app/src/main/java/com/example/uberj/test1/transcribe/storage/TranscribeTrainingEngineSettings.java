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

    public List<String> activeLetters;

    public int playLetterWPM;

    @NonNull
    public Long durationRequestedMillis;

    @NonNull
    public String sessionType;

    @NonNull
    public List<String> selectedStrings;
}
