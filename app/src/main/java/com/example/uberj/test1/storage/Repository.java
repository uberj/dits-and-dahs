package com.example.uberj.test1.storage;

import android.content.Context;
import android.os.AsyncTask;

import com.example.uberj.test1.socratic.storage.SocraticTrainingEngineSettings;
import com.example.uberj.test1.socratic.storage.SocraticTrainingEngineSettingsDAO;
import com.example.uberj.test1.socratic.storage.SocraticTrainingSession;
import com.example.uberj.test1.socratic.storage.SocraticTrainingSessionDAO;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionDAO;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingSession;

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
