package com.example.uberj.test1.LetterTraining;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.uberj.test1.CWToneManager;
import com.example.uberj.test1.KeyboardSessionActivity;
import com.example.uberj.test1.KochLetterSequence;
import com.example.uberj.test1.ProgressGradient;
import com.example.uberj.test1.storage.CompetencyWeights;
import com.example.uberj.test1.storage.LetterTrainingSession;
import com.example.uberj.test1.storage.Repository;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LetterTrainingKeyboardSessionActivity extends KeyboardSessionActivity {
    private static final String TAG = "LetterTrainingKeyboardSessionActivity";

    private static final float ENABLED_BUTTON_ALPHA = 1f;
    private static final float ENABLED_PROGRESS_BAR_ALPHA = 0.75f;

    private static final float DISABLED_BUTTON_ALPHA = 0.35f;
    private static final float DISABLED_PROGRESS_BAR_ALPHA = 0.25f;

    private int totalUniqueLettersChosen;
    private int totalCorrectGuesses;
    private int totalAccurateSymbolsGuessed;
    private int totalIncorrectGuesses;
    private long endTimeEpocMillis = -1;
    private final Repository repository = new Repository(this);

    private LetterTrainingEngine engine;
    private Map<String, Integer> competencyWeights = null;
    private List<Button> allPlayableButtons;

    private List<Button> getButtonsTagedAsPlayable() {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        ArrayList<View> inplay = KeyboardSessionActivity.getViewsByTag((ViewGroup) rootView.getParent(), "inplay");
        List<Button> playableButtons = inplay.stream().map(v -> ((Button) v)).collect(Collectors.toList());
        playableButtons
                .forEach((btn) -> btn.setOnLongClickListener(this::playableKeyLongClickHandler));

        return playableButtons;
    }

    private boolean playableKeyLongClickHandler(View view) {
        /*
        If a playable key is held down the user is indicating that they want to include all
        letters up to, and including, that letter in the sequence they have provided (Default is
        Koch
        */
        String buttonLetter = getButtonLetter(view);

        List<String> lettersToBePlayedFromNowOn = Lists.newArrayList();
        for (String letter : KochLetterSequence.sequence) {
            lettersToBePlayedFromNowOn.add(letter);
            if (letter.equals(buttonLetter)) {
                break;
            }
        }

        updateLayoutUsingTheseLetters(lettersToBePlayedFromNowOn);
        engine.setPlayableKeys(lettersToBePlayedFromNowOn);
        return true;
    }

    private void updateLayoutUsingTheseLetters(List<String> updatedInPlayLetters) {
        for (Button button : allPlayableButtons) {
            String buttonLetter = button.getText().toString();
            View progressBar = getLetterProgressBar(buttonLetter);
            if (updatedInPlayLetters.contains(buttonLetter)) {
                updateProgressBarForLetter(buttonLetter);
                button.setAlpha(ENABLED_BUTTON_ALPHA);
                progressBar.setAlpha(ENABLED_PROGRESS_BAR_ALPHA);
            } else {
                button.setAlpha(DISABLED_BUTTON_ALPHA);
                progressBar.setAlpha(DISABLED_PROGRESS_BAR_ALPHA);
            }
        }
    }

    @Override
    public void keyboardButtonClicked(View v) {
        String letter = getButtonLetter(v);
        Optional<Boolean> guess = engine.guess(letter);
        guess.ifPresent(wasCorrectGuess -> updateCompetencyWeights(letter, wasCorrectGuess));
    }

    private void updateCompetencyWeights(String letter, boolean wasCorrectGuess) {
        if (wasCorrectGuess) {
            totalCorrectGuesses++;
            totalAccurateSymbolsGuessed += CWToneManager.numSymbols(letter);
        } else {
            totalIncorrectGuesses++;
        }

        updateProgressBarForLetter(letter);
    }

    private void updateProgressBarForLetter(String letter) {
        View progressBar = getLetterProgressBar(letter);
        Integer competencyWeight = engine.getCompetencyWeight(letter);
        Integer color = ProgressGradient.forWeight(competencyWeight);
        progressBar.setBackgroundColor(color);
        progressBar.setAlpha(ENABLED_PROGRESS_BAR_ALPHA);
    }

    @Override
    public void onDestroy() {
        engine.destroy();
        if (endTimeEpocMillis < 0) {
            endTimeEpocMillis = System.currentTimeMillis();
        }
        super.onDestroy();
    }

    @Override
    public void onPause() {
        engine.pause();
        endTimeEpocMillis = System.currentTimeMillis();
        super.onPause();
    }

    @Override
    public void onResume() {
        if (engine != null) {
            engine.resume();
            endTimeEpocMillis = -1;
        }
        super.onResume();
    }

    @Override
    protected void resumeSession() {
        if (engine != null) {
            engine.resume();
        }
    }

    @Override
    protected void pauseSession() {
        if (engine != null) {
            engine.pause();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        allPlayableButtons = getButtonsTagedAsPlayable();

        Bundle receiveBundle = getIntent().getExtras();
        assert receiveBundle != null;
        int wpmRequested = receiveBundle.getInt(WPM_REQUESTED);
        Log.d(TAG, "onCreated Called in LTKS");

        final List<String> allInPlayKeyNames = allPlayableButtons.stream().map(btn -> btn.getText().toString()).collect(Collectors.toList());
        repository.competencyWeightsDAO.getLatestSession((previousWeight) -> {
            Log.d(TAG, "Latest session loaded");
            List<String> inPlayKeyNames;
            if (previousWeight.isPresent()) {
                competencyWeights = buildInitialCompetencyWeights(previousWeight.get(), allInPlayKeyNames);
                inPlayKeyNames = buildInitialInPlayKeyNames(previousWeight.get());
                Log.d(TAG, "No previous session weights");
            } else {
                competencyWeights = buildBlankWeights(allInPlayKeyNames);
                inPlayKeyNames = buildFirstTwoKeyList();
                Log.d(TAG, "Using previous session\nInplay: " + Joiner.on(", ").join(inPlayKeyNames));
            }

            engine = new LetterTrainingEngine(wpmRequested, this::letterChosenCallback, inPlayKeyNames, competencyWeights);
            updateLayoutUsingTheseLetters(inPlayKeyNames);
            for (String letter : competencyWeights.keySet()) {
                updateProgressBarForLetter(letter);
            }

            engine.initEngine();
            startTimer();
        });
    }

    private List<String> buildFirstTwoKeyList() {
        return Lists.newArrayList(
                KochLetterSequence.sequence.get(0),
                KochLetterSequence.sequence.get(1)
        );
    }

    private List<String> buildInitialInPlayKeyNames(CompetencyWeights competencyWeights) {
        if (competencyWeights.activeLetters == null || competencyWeights.activeLetters.size() == 0) {
            return buildFirstTwoKeyList();
        }
        return competencyWeights.activeLetters;
    }

    private Map<String,Integer> buildBlankWeights(List<String> playableKeys) {
        Map<String, Integer> competencyWeights = Maps.newHashMap();
        for (String playableKey : playableKeys) {
            competencyWeights.put(playableKey, 0);
        }
        return competencyWeights;
    }

    private Map<String, Integer> buildInitialCompetencyWeights(CompetencyWeights weights, List<String> playableKeys) {
        Map<String, Integer> competencyWeights;
        if (weights.weights.size() == 0) {
            competencyWeights = buildBlankWeights(playableKeys);
        } else {
            competencyWeights = weights.weights;
            for (String playableKey : playableKeys) {
                if (!competencyWeights.containsKey(playableKey)) {
                    competencyWeights.put(playableKey, 0);
                }
            }
        }

        return competencyWeights;
    }

    private void letterChosenCallback(String letterChosen) {
        totalUniqueLettersChosen++;
    }

    @Override
    protected void finishSession(Bundle data) {
        engine.destroy();
        LetterTrainingSession trainingSession = new LetterTrainingSession();

        if (endTimeEpocMillis < 0) {
            trainingSession.endTimeEpocMillis = System.currentTimeMillis();
        } else {
            trainingSession.endTimeEpocMillis = endTimeEpocMillis;
        }
        long durationWorkedMillis = durationRequestedMillis - durationRemainingMillis;

        trainingSession.endTimeEpocMillis = System.currentTimeMillis();
        trainingSession.durationRequestedMillis = durationRequestedMillis;
        trainingSession.durationWorkedMillis = durationWorkedMillis;
        trainingSession.completed = durationWorkedMillis == 0;
        trainingSession.wpmAverage = calcWpmAverage(durationWorkedMillis);
        trainingSession.errorRate = (float) totalIncorrectGuesses / (float) (totalCorrectGuesses + totalIncorrectGuesses);
        if (Float.isNaN(trainingSession.errorRate)) {
            trainingSession.errorRate = -1;
        }

        Log.d(TAG, "totalCorrectGuesses: " + totalCorrectGuesses);
        Log.d(TAG, "totalIncorrectGuesses: " + totalCorrectGuesses);
        Log.d(TAG, "totalUniqueLettersChosen: " + totalUniqueLettersChosen);
        repository.insertLetterTrainingSession(trainingSession);

        CompetencyWeights endWeights = new CompetencyWeights();
        endWeights.weights = competencyWeights;
        endWeights.activeLetters = engine.getPlayableKeys();
        repository.insertMostRecentCompetencyWeights(endWeights);
    }

    private float calcWpmAverage(long durationWorkedMillis) {
        int spacesBetweenLetters = (totalCorrectGuesses - 1) * 3;
        // accurateWords = (accurateSymbols / 50)
        float accurateSymbols = (float) (totalAccurateSymbolsGuessed + spacesBetweenLetters);
        float accurateWords = accurateSymbols / 50f;
        // wpmAverage = accurateWords / minutes
        float minutesWorked = (float) (durationWorkedMillis / 1000) / 60;
        return accurateWords / minutesWorked;
    }
}
