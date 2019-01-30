package com.example.uberj.test1.storage;


import android.arch.persistence.room.Database;
import android.arch.persistence.room.Room;
import android.arch.persistence.room.RoomDatabase;
import android.arch.persistence.room.TypeConverters;
import android.content.Context;

import com.example.uberj.test1.storage.converters.StringToIntegerMapConverter;

@Database(entities = {LetterTrainingSession.class, CompetencyWeights.class}, version = 1)
@TypeConverters({StringToIntegerMapConverter.class})
public abstract class TheDatabase extends RoomDatabase {
    public static final String THE_DATABASE_NAME = "the_database";

    public abstract TrainingSessionDAO trainingSessionDAO();
    public abstract CompetencyWeightsDAO competencyWeightsDAO();
    private static TheDatabase INSTANCE;

    public static TheDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TheDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(), TheDatabase.class, THE_DATABASE_NAME
                    ).build();
                }
            }
        }
        return INSTANCE;
    }
}
