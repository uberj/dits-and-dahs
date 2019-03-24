package com.uberj.pocketmorsepro.storage;

import androidx.arch.core.executor.testing.InstantTaskExecutorRule;
import androidx.lifecycle.LiveData;
import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.uberj.pocketmorsepro.TestObserver;
import com.uberj.pocketmorsepro.socratic.storage.SocraticEngineEvent;
import com.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingEngineSettings;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingEngineSettingsDAO;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSession;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSessionDAO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSessionWithEvents;

import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;



import java.util.List;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class TheDatabaseTest {
    @Rule
    public InstantTaskExecutorRule instantTaskExecutorRule = new InstantTaskExecutorRule();

    private static TheDatabase theDatabase;
    private static SocraticTrainingSessionDAO socraticLetterTrainingSessionDAO;
    private static SocraticTrainingEngineSettingsDAO competencyWeightsDAO;

    @BeforeClass
    public static void setUp() {
        final Context context = InstrumentationRegistry.getTargetContext();
        context.deleteDatabase(TheDatabase.THE_DATABASE_NAME);
        theDatabase = TheDatabase.getDatabase(context);
        socraticLetterTrainingSessionDAO = theDatabase.socraticTrainingSessionDAO();
        competencyWeightsDAO = theDatabase.socraticEngineSettingsDAO();
    }

    @Test
    public void testCreateReadLetterTrainingSession() throws Throwable {
        {
            List<SocraticTrainingSession> trainingSessions = LiveDataTestUtil.getValue(socraticLetterTrainingSessionDAO.getAllSessions(SocraticSessionType.LETTER_ONLY.name()));
            Assert.assertTrue(trainingSessions.isEmpty());
        }

        final SocraticTrainingSession trainingSession = new SocraticTrainingSession();
        trainingSession.endTimeEpocMillis = 444l;
        trainingSession.completed = true;
        trainingSession.durationWorkedMillis = 100l;
        trainingSession.durationRequestedMillis = 99l;
        trainingSession.sessionType = SocraticSessionType.LETTER_ONLY.name();
        List<SocraticEngineEvent> events = Lists.newArrayList();
        events.add(SocraticEngineEvent.correctGuess("L"));
        events.add(SocraticEngineEvent.incorrectGuess("K"));
        events.add(SocraticEngineEvent.destroyed());
        socraticLetterTrainingSessionDAO.insertSessionAndEvents(trainingSession, events);

        List<SocraticTrainingSessionWithEvents> trainingSessions = LiveDataTestUtil.getValue(socraticLetterTrainingSessionDAO.getLatestSessionAndEvents(SocraticSessionType.LETTER_ONLY.name()));
        Assert.assertEquals(1, trainingSessions.size());
        SocraticTrainingSessionWithEvents ss = trainingSessions.get(0);
        Assert.assertEquals(trainingSession.durationWorkedMillis, ss.session.durationWorkedMillis);
        Assert.assertEquals(trainingSession.completed, ss.session.completed);
        Assert.assertEquals(trainingSession.endTimeEpocMillis, ss.session.endTimeEpocMillis);
        Assert.assertEquals(trainingSession.durationRequestedMillis, ss.session.durationRequestedMillis);
        Assert.assertEquals(events.size(), ss.events.size());
        Assert.assertEquals(SocraticEngineEvent.EventType.CORRECT_GUESS, ss.events.get(0).eventType);
        Assert.assertEquals("L", ss.events.get(0).info);

    }

    @Test
    public void testCRUDCompetencyWeights() throws InterruptedException {
        {
            LiveData<List<SocraticTrainingEngineSettings>> competencyWeights = competencyWeightsDAO.getAllEngineSettings(SocraticSessionType.LETTER_ONLY.name());
            TestObserver.test(competencyWeights)
                    .awaitValue()
                    .assertValue(List::isEmpty);
        }

        SocraticTrainingEngineSettings engineSettings = new SocraticTrainingEngineSettings();
        Map<String, Integer> weights = Maps.newHashMap();
        weights.put("A", 1);
        weights.put("B", 2);
        weights.put("C", 3);
        List<String> activeLetters = Lists.newArrayList("1", "2", "3");
        engineSettings.weights = weights;
        engineSettings.activeLetters = activeLetters;
        engineSettings.createdAtEpocMillis = System.currentTimeMillis();
        competencyWeightsDAO.insertEngineSettings(engineSettings);

        engineSettings.createdAtEpocMillis = System.currentTimeMillis();
        competencyWeightsDAO.insertEngineSettings(engineSettings);

        LiveData<List<SocraticTrainingEngineSettings>> allCompetencyWeights = competencyWeightsDAO.getAllEngineSettings(SocraticSessionType.LETTER_ONLY.name());
        TestObserver.test(allCompetencyWeights)
                .awaitNextValue()
                .assertValue((ss) -> ss.size() == 2)
                .assertValue((ss) -> ss.get(0).createdAtEpocMillis == engineSettings.createdAtEpocMillis)
                .assertValue((ss) -> ss.get(0).weights.entrySet().equals(engineSettings.weights.entrySet()))
                .assertValue((ss) -> ss.get(0).activeLetters.equals(engineSettings.activeLetters));
    }

}