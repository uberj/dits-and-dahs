package com.example.uberj.test1.storage;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

@RunWith(AndroidJUnit4.class)
public class TheDatabaseTest {

    private TheDatabase theDatabase;
    private TrainingSessionDAO trainingSessionDAO;

    @Before
    public void setUp() {
        final Context context = InstrumentationRegistry.getTargetContext();
        context.deleteDatabase(TheDatabase.THE_DATABASE_NAME);
        theDatabase = TheDatabase.getDatabase(context);
        trainingSessionDAO = theDatabase.trainingSessionDAO();
    }

    @Test
    public void testCreateReadSession()  {
        Assert.assertTrue(trainingSessionDAO.getAllSessions().isEmpty());
        TrainingSession trainingSession = new TrainingSession();
        trainingSession.endTimeEpoc = 444;
        trainingSession.completed = true;
        trainingSession.duration_worked = 100l;
        trainingSession.sessionType = TrainingSessionType.LETTER_TRAINING.name();
        trainingSessionDAO.insertSession(trainingSession);

        List<TrainingSession> allSessions = trainingSessionDAO.getAllSessions();
        Assert.assertEquals(1, allSessions.size());
        TrainingSession trainingSession1 = allSessions.get(0);
        Assert.assertEquals(trainingSession.duration_worked, trainingSession1.duration_worked);
        Assert.assertEquals(trainingSession.completed, trainingSession1.completed);
        Assert.assertEquals(trainingSession.sessionType, trainingSession1.sessionType);
        Assert.assertEquals(trainingSession.endTimeEpoc, trainingSession1.endTimeEpoc);
    }

}