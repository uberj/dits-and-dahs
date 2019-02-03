package com.example.uberj.test1.storage;

import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import com.example.uberj.test1.TestObserver;
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
    private static LetterTrainingSessionDAO letterTrainingSessionDAO;
    private static CompetencyWeightsDAO competencyWeightsDAO;

    @BeforeClass
    public static void setUp() {
        final Context context = InstrumentationRegistry.getTargetContext();
        context.deleteDatabase(TheDatabase.THE_DATABASE_NAME);
        theDatabase = TheDatabase.getDatabase(context);
        letterTrainingSessionDAO = theDatabase.trainingSessionDAO();
        competencyWeightsDAO = theDatabase.competencyWeightsDAO();
    }

    @Test
    public void testCreateReadLetterTrainingSession() throws InterruptedException {
        TestObserver.test(letterTrainingSessionDAO.getAllSessions())
                .awaitValue()
                .assertValue(List::isEmpty);
        final LetterTrainingSession trainingSession = new LetterTrainingSession();
        trainingSession.endTimeEpocMillis = 444l;
        trainingSession.completed = true;
        trainingSession.durationWorkedMillis = 100l;
        trainingSession.durationRequestedMillis = 99l;
        letterTrainingSessionDAO.insertSession(trainingSession);

        TestObserver.test(letterTrainingSessionDAO.getAllSessions())
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
            LiveData<List<CompetencyWeights>> competencyWeights = competencyWeightsDAO.getAllCompetencyWeights();
            TestObserver.test(competencyWeights)
                    .awaitValue()
                    .assertValue(List::isEmpty);
        }

        CompetencyWeights competencyWeights = new CompetencyWeights();
        Map<String, Integer> weights = Maps.newHashMap();
        weights.put("A", 1);
        weights.put("B", 2);
        weights.put("C", 3);
        List<String> activeLetters = Lists.newArrayList("1", "2", "3");
        competencyWeights.weights = weights;
        competencyWeights.activeLetters = activeLetters;
        competencyWeights.createdAtEpocMillis = System.currentTimeMillis();
        competencyWeightsDAO.insertCompetencyWeights(competencyWeights);

        competencyWeights.createdAtEpocMillis = System.currentTimeMillis();
        competencyWeightsDAO.insertCompetencyWeights(competencyWeights);

        LiveData<List<CompetencyWeights>> allCompetencyWeights = competencyWeightsDAO.getAllCompetencyWeights();
        TestObserver.test(allCompetencyWeights)
                .awaitNextValue()
                .assertValue((ss) -> ss.size() == 2)
                .assertValue((ss) -> ss.get(0).createdAtEpocMillis == competencyWeights.createdAtEpocMillis)
                .assertValue((ss) -> ss.get(0).weights.entrySet().equals(competencyWeights.weights.entrySet()))
                .assertValue((ss) -> ss.get(0).activeLetters.equals(competencyWeights.activeLetters));
    }

}