package com.example.uberj.test1.storage;

import android.content.Context;
import android.os.AsyncTask;

public class Repository {
    public final LetterTrainingSessionDAO letterTrainingSessionDAO;
    public final CompetencyWeightsDAO competencyWeightsDAO;

    public Repository(Context context) {
        TheDatabase database = TheDatabase.getDatabase(context);
        letterTrainingSessionDAO = database.trainingSessionDAO();
        competencyWeightsDAO = database.competencyWeightsDAO();

    }

    public void insertMostRecentCompetencyWeights(final CompetencyWeights competencyWeights) {
        competencyWeights.createdAtEpocMillis = System.currentTimeMillis();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                competencyWeightsDAO.insertCompetencyWeights(competencyWeights);
                return null;
            }
        }.execute();
    }

    public void insertLetterTrainingSession(final LetterTrainingSession session) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                letterTrainingSessionDAO.insertSession(session);
                return null;
            }
        }.execute();
    }
}
