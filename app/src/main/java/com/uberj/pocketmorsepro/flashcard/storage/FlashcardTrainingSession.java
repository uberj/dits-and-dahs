package com.uberj.pocketmorsepro.flashcard.storage;

import java.util.List;

import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.annotation.NonNull;

@Entity
public class FlashcardTrainingSession {
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
    public String sessionType;

    @NonNull
    public List<String> cards;
}
