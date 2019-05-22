package com.uberj.ditsanddahs.storage;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import androidx.room.testing.MigrationTestHelper;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory;
import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

@RunWith(AndroidJUnit4.class)
public class MigrationDatabaseTest {
    @Rule
    public MigrationTestHelper testHelper = new MigrationTestHelper(
            InstrumentationRegistry.getInstrumentation(),
            TheDatabase.class.getCanonicalName(),
            new FrameworkSQLiteOpenHelperFactory()
    );

    @Test
    public void testMigration1to2() throws IOException {

        SupportSQLiteDatabase db = testHelper.runMigrationsAndValidate(TheDatabase.THE_DATABASE_NAME, 2, true, TheDatabase.MIGRATION_1_2);
    }
    @Test
    public void testMigration2to3() throws IOException {

        SupportSQLiteDatabase db = testHelper.runMigrationsAndValidate(TheDatabase.THE_DATABASE_NAME, 3, true, TheDatabase.MIGRATION_1_2, TheDatabase.MIGRATION_2_3);
    }

}