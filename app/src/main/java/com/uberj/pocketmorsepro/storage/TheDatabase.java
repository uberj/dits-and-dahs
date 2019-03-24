package com.uberj.pocketmorsepro.storage;


import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import android.content.Context;

import com.uberj.pocketmorsepro.socratic.storage.SocraticEngineEvent;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingEngineSettings;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingEngineSettingsDAO;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSession;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSessionDAO;
import com.uberj.pocketmorsepro.storage.converters.StringListConverter;
import com.uberj.pocketmorsepro.storage.converters.StringToIntegerMapConverter;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeSessionDAO;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeTrainingSession;

@Database(entities = {
        SocraticTrainingEngineSettings.class,
        SocraticTrainingSession.class,
        SocraticEngineEvent.class,
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
