package com.example.uberj.test1;

import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class LetterTrainingKeyboardSessionActivity extends KeyboardSessionActivity {
    private static final int MISSED_LETTER_POINTS_REMOVED = 10;
    private static final int CORRECT_LETTER_POINTS_ADDED = 5;

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
            competencyWeights.computeIfPresent(letter,
                    (cLetter, existingCompetency) -> Math.min(100, existingCompetency + CORRECT_LETTER_POINTS_ADDED));
        } else {
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
        super.onDestroy();
    }

    @Override
    public void onPause() {
        engine.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        engine.resume();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Starts the timer
        super.onCreate(savedInstanceState);
        List<String> playableKeys = getPlayableKeys();
        competencyWeights = getInitialCompentencyWeights(playableKeys);
        for (String letter : competencyWeights.keySet()) {
            updateProgressBarForLetter(letter);
        }

        engine = new LetterTrainingEngine(playableKeys);
        engine.initEngine();
    }

    private HashMap<String, Integer> getInitialCompentencyWeights(List<String> playableKeys) {
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
