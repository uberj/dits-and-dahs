package com.example.uberj.test1.storage;

import android.content.Context;
import android.os.AsyncTask;

public class Repository {
    private final TrainingSessionDAO trainingSessionDAO;
    private final CompetencyWeightsDAO competencyWeightsDAO;

    public Repository(Context context) {
        TheDatabase database = TheDatabase.getDatabase(context);
        trainingSessionDAO = database.trainingSessionDAO();
        competencyWeightsDAO = database.competencyWeightsDAO();

    }

    public void insertLetterTrainingSession(final LetterTrainingSession session) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                trainingSessionDAO.insertSession(session);
                return null;
            }
        }.execute();
    }
}
