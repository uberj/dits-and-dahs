package com.uberj.ditsanddahs.storage;


import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import android.content.Context;

import com.uberj.ditsanddahs.flashcard.storage.FlashcardEngineEvent;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardTrainingSession;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardTrainingSessionDAO;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticEngineEvent;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingEngineSettings;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingEngineSettingsDAO;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingSession;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingSessionDAO;
import com.uberj.ditsanddahs.storage.converters.StringListConverter;
import com.uberj.ditsanddahs.storage.converters.StringToIntegerMapConverter;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeSessionDAO;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeTrainingSession;

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

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE IF NOT EXISTS `FlashcardTrainingSession` (`uid` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `endTimeEpocMillis` INTEGER NOT NULL, `sessionType` TEXT NOT NULL, `cards` TEXT NOT NULL, `durationUnitsRequested` INTEGER NOT NULL, `durationUnit` TEXT NOT NULL)");
            database.execSQL("CREATE TABLE IF NOT EXISTS `FlashcardEngineEvent` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `sessionId` INTEGER NOT NULL, `eventType` INTEGER, `eventAtEpoc` INTEGER, `info` TEXT)");
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
                            .addMigrations(MIGRATION_2_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
