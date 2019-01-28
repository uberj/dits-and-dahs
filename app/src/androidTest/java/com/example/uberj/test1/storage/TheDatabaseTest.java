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
        Assert.assertTrue(trainingSessionDAO.getAllSessions().getValue().isEmpty());
        LetterTrainingSession trainingSession = new LetterTrainingSession();
        trainingSession.endTimeEpocMilis = 444l;
        trainingSession.completed = true;
        trainingSession.durationWorkedMilis = 100l;
        trainingSessionDAO.insertSession(trainingSession);

        List<LetterTrainingSession> allSessions = trainingSessionDAO.getAllSessions().getValue();
        Assert.assertEquals(1, allSessions.size());
        LetterTrainingSession trainingSession1 = allSessions.get(0);
        Assert.assertEquals(trainingSession.durationWorkedMilis, trainingSession1.durationWorkedMilis);
        Assert.assertEquals(trainingSession.completed, trainingSession1.completed);
        Assert.assertEquals(trainingSession.endTimeEpocMilis, trainingSession1.endTimeEpocMilis);
    }

}