package com.example.uberj.morsepocketpro.storage;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.example.uberj.morsepocketpro.socratic.storage.SocraticTrainingEngineSettings;
import com.example.uberj.morsepocketpro.socratic.storage.SocraticTrainingEngineSettingsDAO;
import com.example.uberj.morsepocketpro.socratic.storage.SocraticTrainingSession;
import com.example.uberj.morsepocketpro.socratic.storage.SocraticTrainingSessionDAO;
import com.example.uberj.morsepocketpro.storage.converters.StringListConverter;
import com.example.uberj.morsepocketpro.storage.converters.StringToIntegerMapConverter;
import com.example.uberj.morsepocketpro.transcribe.storage.TranscribeSessionDAO;
import com.example.uberj.morsepocketpro.transcribe.storage.TranscribeTrainingSession;

@Database(entities = {
        SocraticTrainingEngineSettings.class,
        SocraticTrainingSession.class,
        TranscribeTrainingSession.class
}, version = 1)
@TypeConverters({
        StringToIntegerMapConverter.class,
        StringListConverter.class
})
public abstract class TheDatabase extends RoomDatabase {
    public static final String THE_DATABASE_NAME = "the_database";

    public abstract SocraticTrainingSessionDAO socraticTrainingSessionDAO();
    public abstract SocraticTrainingEngineSettingsDAO socraticEngineSettingsDAO();
    public abstract TranscribeSessionDAO transcribeTrainingSessionDAO();
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
