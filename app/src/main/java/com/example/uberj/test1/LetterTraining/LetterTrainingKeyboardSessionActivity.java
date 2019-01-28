package com.example.uberj.test1.LetterTraining;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

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
    private static final int MISSED_LETTER_POINTS_REMOVED = 10;
    private static final int CORRECT_LETTER_POINTS_ADDED = 5;
    private float wpmAverage = -1;
    private float errorRate = -1;
    private int totalUniqueLettersPlayed;
    private int totalCorrectGuesses;
    private int totalIncorrectGuesses;
    private long endTimeEpocMilis = -1;
    private String currentLetterPlaying = null;
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
        guess.ifPresent(aBoolean -> updateCompetencyWeights(letter, aBoolean));
    }

    private void updateCompetencyWeights(String letter, boolean wasCorrectGuess) {
        ensureCompetencyWeight(letter);
        if (wasCorrectGuess) {
            totalCorrectGuesses++;
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

        engine = new LetterTrainingEngine(this::letterPlayedCallback, playableKeys);
        engine.initEngine();
    }

    private void letterPlayedCallback(String letterPlayed) {
        // The engine will always call a different letter when the user gets the current one right.
        // So, to count the number of unique letters played we count the number of times the letterPlayed
        // changes and add one.
        if (!letterPlayed.equals(currentLetterPlaying)) {
            currentLetterPlaying = letterPlayed;
            totalUniqueLettersPlayed++;
        }
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
        trainingSession.wpmAverage = wpmAverage;
        trainingSession.errorRate = errorRate;

        sessionRepository.insertSession(trainingSession);
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
