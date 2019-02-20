package com.example.uberj.test1.storage;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

import java.util.List;
import java.util.Map;

@Entity
public class LetterTrainingEngineSettings {

    @PrimaryKey(autoGenerate = true)
    public int uid;

    @NonNull
    public long createdAtEpocMillis;

    public Map<String, Integer> weights;

    public List<String> activeLetters;

    public int playLetterWPM;

    @NonNull
    public Long durationRequestedMillis;

    @NonNull
    public String sessionType;
}
