package com.example.uberj.test1.LetterTraining;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.uberj.test1.CWToneManager;
import com.example.uberj.test1.KeyboardSessionActivity;
import com.example.uberj.test1.ProgressGradient;
import com.example.uberj.test1.storage.LetterTrainingSession;
import com.example.uberj.test1.storage.LetterTrainingSessionRepository;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LetterTrainingKeyboardSessionActivity extends KeyboardSessionActivity {
    private static final String TAG = "LetterTrainingKeyboardSessionActivity";
    private static final int MISSED_LETTER_POINTS_REMOVED = 10;
    private static final int CORRECT_LETTER_POINTS_ADDED = 5;
    private int totalUniqueLettersChosen;
    private int totalCorrectGuesses;
    private int totalAccurateSymbolsGuessed;
    private int totalIncorrectGuesses;
    private long endTimeEpocMilis = -1;
    private final LetterTrainingSessionRepository sessionRepository = new LetterTrainingSessionRepository(this);

    private LetterTrainingEngine engine;
    HashMap<String, Integer> competencyWeights = null;

    private List<String> getPlayableKeys() {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        ArrayList<View> inplay = KeyboardSessionActivity.getViewsByTag((ViewGroup) rootView.getParent(), "inplay");
        return inplay.stream().map(v -> ((Button) v).getText().toString()).collect(Collectors.toList());
    }

    @Override
    public void keyboardButtonClicked(View v) {
        String buttonId = getResources().getResourceEntryName(v.getId());
        if (!buttonId.startsWith("key")) {
            throw new RuntimeException("unknown button " + buttonId);
        }

        String letter = buttonId.replace("key", "");
        Optional<Boolean> guess = engine.guess(letter);
        guess.ifPresent(wasCorrectGuess -> updateCompetencyWeights(letter, wasCorrectGuess));
    }

    private void updateCompetencyWeights(String letter, boolean wasCorrectGuess) {
        ensureCompetencyWeight(letter);
        if (wasCorrectGuess) {
            totalCorrectGuesses++;
            totalAccurateSymbolsGuessed += CWToneManager.numSymbols(letter);
            competencyWeights.computeIfPresent(letter,
                    (cLetter, existingCompetency) -> Math.min(100, existingCompetency + CORRECT_LETTER_POINTS_ADDED));
        } else {
            totalIncorrectGuesses++;
            competencyWeights.computeIfPresent(letter,
                    (cLetter, existingCompetency) -> Math.max(0, existingCompetency - MISSED_LETTER_POINTS_REMOVED));
        }

        updateProgressBarForLetter(letter);
    }

    private void updateProgressBarForLetter(String letter) {
        int id = getResources().getIdentifier("progressBarForKey" + letter, "id", getApplicationContext().getPackageName());
        View progressBar = findViewById(id);
        ensureCompetencyWeight(letter);
        Integer competencyWeight = competencyWeights.get(letter);
        Integer color = ProgressGradient.forWeight(competencyWeight);
        progressBar.setBackgroundColor(color);
    }

    private void ensureCompetencyWeight(String letter) {
        if (!competencyWeights.containsKey(letter)) {
            throw new RuntimeException("No competency weight for " + letter);
        }
    }

    @Override
    public void onDestroy() {
        engine.destroy();
        if (endTimeEpocMilis < 0) {
            endTimeEpocMilis = System.currentTimeMillis();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        engine.pause();
        endTimeEpocMilis = System.currentTimeMillis();
        super.onPause();
    }

    @Override
    public void onResume() {
        engine.resume();
        endTimeEpocMilis = -1;
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Starts the timer
        super.onCreate(savedInstanceState);
        List<String> playableKeys = getPlayableKeys();
        competencyWeights = getInitialCompetencyWeights(playableKeys);
        for (String letter : competencyWeights.keySet()) {
            updateProgressBarForLetter(letter);
        }

        Bundle receiveBundle = getIntent().getExtras();
        assert receiveBundle != null;
        int wpmRequested = receiveBundle.getInt(WPM_REQUESTED);

        engine = new LetterTrainingEngine(wpmRequested, this::letterPlayedCallback, this::letterChosenCallback, playableKeys);
        engine.initEngine();
    }

    private void letterChosenCallback(String letterChosen) {
        totalUniqueLettersChosen++;
        Log.d(TAG, "new letter: " + totalUniqueLettersChosen);
    }

    private void letterPlayedCallback(String letterPlayed) {
    }

    @Override
    protected void finishSession(Bundle data) {
        LetterTrainingSession trainingSession = new LetterTrainingSession();

        if (endTimeEpocMilis < 0) {
            trainingSession.endTimeEpocMilis = System.currentTimeMillis();
        } else {
            trainingSession.endTimeEpocMilis = endTimeEpocMilis;
        }
        long durationWorkedMilis = durationMilisRequested - durationMilisRemaining;

        trainingSession.endTimeEpocMilis = System.currentTimeMillis();
        trainingSession.durationWorkedMilis = durationWorkedMilis;
        trainingSession.completed = durationWorkedMilis == 0;
        trainingSession.wpmAverage = calcWpmAverage(durationWorkedMilis);
        trainingSession.errorRate = (float) totalIncorrectGuesses / (float) (totalCorrectGuesses + totalIncorrectGuesses);
        if (trainingSession.errorRate == Float.NaN) {
            trainingSession.errorRate = -1;
        }

        Log.d(TAG, "totalCorrectGuesses: " + totalCorrectGuesses);
        Log.d(TAG, "totalIncorrectGuesses: " + totalCorrectGuesses);
        Log.d(TAG, "totalUniqueLettersChosen: " + totalUniqueLettersChosen);
        sessionRepository.insertSession(trainingSession);
    }

    private float calcWpmAverage(long durationWorkedMilis) {
        int spacesBetweenLetters = (totalCorrectGuesses - 1) * 3;
        // accurateWords = (accurateSymbols / 50)
        float accurateSymbols = (float) (totalAccurateSymbolsGuessed + spacesBetweenLetters);
        float accurateWords = accurateSymbols / 50f;
        // wpmAverage = accurateWords / minutes
        float minutesWorked = (float) (durationWorkedMilis / 1000) / 60;
        return accurateWords / minutesWorked;
    }

    private HashMap<String, Integer> getInitialCompetencyWeights(List<String> playableKeys) {
        // TODO, load old weights from w/e they come from
        HashMap<String, Integer> map = Maps.newHashMap();
        for (String playableKey : playableKeys) {
            map.put(playableKey, 0);
        }
        return map;
    }

    @Override
    protected void resumeSession() {
        engine.resume();
    }

    @Override
    protected void pauseSession() {
        engine.pause();
    }
}
