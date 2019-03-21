package com.uberj.pocketmorsepro.storage;

import android.content.Context;
import android.os.AsyncTask;

import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingEngineSettings;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingEngineSettingsDAO;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSession;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSessionDAO;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeSessionDAO;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeTrainingSession;

public class Repository {
    public final SocraticTrainingSessionDAO socraticTrainingSessionDAO;
    public final SocraticTrainingEngineSettingsDAO socraticEngineSettingsDAO;
    public final TranscribeSessionDAO transcribeTrainingSessionDAO;

    public Repository(Context context) {
        TheDatabase database = TheDatabase.getDatabase(context);
        socraticTrainingSessionDAO = database.socraticTrainingSessionDAO();
        socraticEngineSettingsDAO = database.socraticEngineSettingsDAO();
        transcribeTrainingSessionDAO = database.transcribeTrainingSessionDAO();

    }

    public void insertSocraticEngineSettings(final SocraticTrainingEngineSettings engineSettings) {
        engineSettings.createdAtEpocMillis = System.currentTimeMillis();
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                socraticEngineSettingsDAO.insertEngineSettings(engineSettings);
                return null;
            }
        }.execute();
    }

    public void insertSocraticTrainingSession(final SocraticTrainingSession session) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                socraticTrainingSessionDAO.insertSession(session);
                return null;
            }
        }.execute();
    }

    public void insertTranscribeTrainingSession(final TranscribeTrainingSession session) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                transcribeTrainingSessionDAO.insertSession(session);
                return null;
            }
        }.execute();
    }
}
