package com.example.uberj.test1.storage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import java.util.Map;

@Entity
public class CompetencyWeights {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "createdAtEpocMillis")
    @NonNull
    public long createdAtEpocMillis;

    @ColumnInfo(name = "weights")
    public Map<String, Integer> weights;
}
