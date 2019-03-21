package com.uberj.pocketmorsepro.storage;

import androidx.lifecycle.LiveData;
import android.content.Context;
import androidx.test.InstrumentationRegistry;
import androidx.test.runner.AndroidJUnit4;

import com.uberj.pocketmorsepro.TestObserver;
import com.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingEngineSettings;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingEngineSettingsDAO;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSession;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSessionDAO;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;
import java.util.Map;

@RunWith(AndroidJUnit4.class)
public class TheDatabaseTest {

    private static TheDatabase theDatabase;
    private static SocraticTrainingSessionDAO letterTrainingSessionDAO;
    private static SocraticTrainingEngineSettingsDAO competencyWeightsDAO;

    @BeforeClass
    public static void setUp() {
        final Context context = InstrumentationRegistry.getTargetContext();
        context.deleteDatabase(TheDatabase.THE_DATABASE_NAME);
        theDatabase = TheDatabase.getDatabase(context);
        letterTrainingSessionDAO = theDatabase.socraticTrainingSessionDAO();
        competencyWeightsDAO = theDatabase.socraticEngineSettingsDAO();
    }

    @Test
    public void testCreateReadLetterTrainingSession() throws InterruptedException {
        TestObserver.test(letterTrainingSessionDAO.getAllSessions(SocraticSessionType.LETTER_ONLY.name()))
                .awaitValue()
                .assertValue(List::isEmpty);
        final SocraticTrainingSession trainingSession = new SocraticTrainingSession();
        trainingSession.endTimeEpocMillis = 444l;
        trainingSession.completed = true;
        trainingSession.durationWorkedMillis = 100l;
        trainingSession.durationRequestedMillis = 99l;
        letterTrainingSessionDAO.insertSession(trainingSession);

        TestObserver.test(letterTrainingSessionDAO.getAllSessions(SocraticSessionType.LETTER_ONLY.name()))
                .awaitValue()
                .assertValue((ss) -> ss.size() == 1)
                .assertValue((ss) -> ss.get(0).durationWorkedMillis.equals(trainingSession.durationWorkedMillis))
                .assertValue((ss) -> ss.get(0).completed == trainingSession.completed)
                .assertValue((ss) -> ss.get(0).endTimeEpocMillis.equals(trainingSession.endTimeEpocMillis))
                .assertValue((ss) -> ss.get(0).durationRequestedMillis.equals(trainingSession.durationRequestedMillis));
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