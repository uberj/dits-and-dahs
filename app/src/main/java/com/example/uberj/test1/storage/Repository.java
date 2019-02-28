package com.example.uberj.test1.storage;

import android.content.Context;
import android.os.AsyncTask;

public class Repository {
    public final SocraticTrainingSessionDAO letterTrainingSessionDAO;
    public final SocraticTrainingEngineSettingsDAO engineSettingsDAO;

    public Repository(Context context) {
        TheDatabase database = TheDatabase.getDatabase(context);
        letterTrainingSessionDAO = database.trainingSessionDAO();
        engineSettingsDAO = database.engineSettingsDAO();

    }

    public void insertMostRecentCompetencyWeights(final SocraticTrainingEngineSettings engineSettings) {
        engineSettings.createdAtEpocMillis = System.currentTimeMillis();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                engineSettingsDAO.insertEngineSettings(engineSettings);
                return null;
            }
        }.execute();
    }

    public void insertLetterTrainingSession(final SocraticTrainingSession session) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                letterTrainingSessionDAO.insertSession(session);
                return null;
            }
        }.execute();
    }
}
