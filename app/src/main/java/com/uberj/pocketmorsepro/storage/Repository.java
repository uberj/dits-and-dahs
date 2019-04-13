package com.uberj.pocketmorsepro.storage;

import android.content.Context;
import android.os.AsyncTask;

import com.uberj.pocketmorsepro.flashcard.storage.FlashcardEngineEvent;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardTrainingSession;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardTrainingSessionDAO;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticEngineEvent;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingEngineSettings;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingEngineSettingsDAO;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingSession;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingSessionDAO;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeSessionDAO;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeTrainingSession;

import java.util.List;

public class Repository {
    public final FlashcardTrainingSessionDAO flashcardTrainingSessionDAO;
    public final SocraticTrainingSessionDAO socraticTrainingSessionDAO;
    public final SocraticTrainingEngineSettingsDAO socraticEngineSettingsDAO;
    public final TranscribeSessionDAO transcribeTrainingSessionDAO;

    public Repository(Context context) {
        TheDatabase database = TheDatabase.getDatabase(context);
        flashcardTrainingSessionDAO = database.flashcardTrainingSessionDAO();
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

    public void insertSocraticTrainingSessionAndEvents(final SocraticTrainingSession session, final List<SocraticEngineEvent> events) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                socraticTrainingSessionDAO.insertSessionAndEvents(session, events);
                return null;
            }
        }.execute();
    }

    public void insertFlashcardTrainingSessionAndEvents(final FlashcardTrainingSession session, final List<FlashcardEngineEvent> events) {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... voids) {
                flashcardTrainingSessionDAO.insertSessionAndEvents(session, events);
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
