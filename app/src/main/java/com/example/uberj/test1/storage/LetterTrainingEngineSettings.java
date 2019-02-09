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

    @ColumnInfo(name = "createdAtEpocMillis")
    @NonNull
    public long createdAtEpocMillis;

    @ColumnInfo(name = "weights")
    public Map<String, Integer> weights;

    @ColumnInfo(name = "activeLetters")
    public List<String> activeLetters;

    @ColumnInfo(name = "playLetterWPM")
    public int playLetterWPM;

    @ColumnInfo(name = "durationRequestedMillis")
    @NonNull
    public Long durationRequestedMillis;
}
