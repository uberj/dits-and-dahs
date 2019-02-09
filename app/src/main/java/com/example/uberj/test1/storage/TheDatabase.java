package com.example.uberj.test1.storage;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.example.uberj.test1.storage.converters.StringListConverter;
import com.example.uberj.test1.storage.converters.StringToIntegerMapConverter;

@Database(entities = {
        LetterTrainingEngineSettings.class,
        LetterTrainingSession.class
}, version = 6)
@TypeConverters({
        StringToIntegerMapConverter.class,
        StringListConverter.class
})
public abstract class TheDatabase extends RoomDatabase {
    public static final String THE_DATABASE_NAME = "the_database";

    public abstract LetterTrainingSessionDAO trainingSessionDAO();
    public abstract LetterTrainingEngineSettingsDAO engineSettingsDAO();
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
