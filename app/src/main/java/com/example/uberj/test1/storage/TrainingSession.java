package com.example.uberj.test1.storage;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

@Entity
public class TrainingSession {
    @PrimaryKey(autoGenerate = true)
    public int uid;

    @ColumnInfo(name = "end_time_epoch")
    @NonNull
    public Integer endTimeEpoc;

    @ColumnInfo(name = "session_type")
    @NonNull
    public String sessionType;

    @ColumnInfo(name = "duration_worked")
    @NonNull
    public Long duration_worked;

    @ColumnInfo(name = "completed")
    @NonNull
    public Boolean completed;

}
