package com.uberj.pocketmorsepro.storage;


import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import com.uberj.pocketmorsepro.flashcard.storage.FlashcardEngineEvent;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardTrainingSession;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardTrainingSessionDAO;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticEngineEvent;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingEngineSettings;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingEngineSettingsDAO;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingSession;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingSessionDAO;
import com.uberj.pocketmorsepro.storage.converters.StringListConverter;
import com.uberj.pocketmorsepro.storage.converters.StringToIntegerMapConverter;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeSessionDAO;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeTrainingSession;

@Database(entities = {
        FlashcardTrainingSession.class,
        FlashcardEngineEvent.class,
        SocraticTrainingEngineSettings.class,
        SocraticTrainingSession.class,
        SocraticEngineEvent.class,
        TranscribeTrainingSession.class
}, version = 3)
@TypeConverters({
        StringToIntegerMapConverter.class,
        StringListConverter.class
})
public abstract class TheDatabase extends RoomDatabase {
    public static final String THE_DATABASE_NAME = "the_database";
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE SocraticTrainingSession "
                    + " ADD COLUMN easyMode INTEGER DEFAULT 0 NOT NULL");
        }
    };

    public abstract SocraticTrainingSessionDAO socraticTrainingSessionDAO();
    public abstract SocraticTrainingEngineSettingsDAO socraticEngineSettingsDAO();
    public abstract TranscribeSessionDAO transcribeTrainingSessionDAO();
    public abstract FlashcardTrainingSessionDAO flashcardTrainingSessionDAO();
    private static TheDatabase INSTANCE;

    public static TheDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (TheDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(), TheDatabase.class, THE_DATABASE_NAME)
                            .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
