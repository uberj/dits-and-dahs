package com.example.uberj.test1.storage;

import android.content.Context;
import android.os.AsyncTask;

public class LetterTrainingSessionRepository {

    private final TrainingSessionDAO trainingSessionDAO;

    public LetterTrainingSessionRepository(Context context) {
        trainingSessionDAO = TheDatabase.getDatabase(context).trainingSessionDAO();

    }

    public void insertSession(final LetterTrainingSession session) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                trainingSessionDAO.insertSession(session);
                return null;
            }
        }.execute();
    }
}
