package com.example.uberj.test1.LetterTraining;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.TransitionDrawable;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;

import com.example.uberj.test1.CWToneManager;
import com.example.uberj.test1.CountDownTimer;
import com.example.uberj.test1.DynamicKeyboard;
import com.example.uberj.test1.KochLetterSequence;
import com.example.uberj.test1.ProgressGradient;
import com.example.uberj.test1.R;
import com.example.uberj.test1.keyboards.SimpleLetters;
import com.example.uberj.test1.storage.LetterTrainingEngineSettings;
import com.example.uberj.test1.storage.LetterTrainingSession;
import com.example.uberj.test1.storage.Repository;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class LetterTrainingKeyboardSessionActivity extends AppCompatActivity {
    private static final String engineMutex = "engineMutex";
    public static final String DURATION_REQUESTED_MINUTES = "duration-requested-minutes";
    public static final String WPM_REQUESTED = "wpm-requested";
    private int durationMinutesRequested;
    protected long durationRemainingMillis;
    protected long durationRequestedMillis;
    private CountDownTimer countDownTimer;
    private Menu menu;
    private boolean isPlaying;

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
    private List<Button> allPlayableButtons;
    private DynamicKeyboard keyboard;
    private int wpmRequested;

    public static ArrayList<View> getViewsByTag(ViewGroup root, String tag) {
        ArrayList<View> views = new ArrayList<View>();
        final int childCount = root.getChildCount();
        for (int i = 0; i < childCount; i++) {
            final View child = root.getChildAt(i);
            if (child instanceof ViewGroup) {
                views.addAll(getViewsByTag((ViewGroup) child, tag));
            }

            final Object tagObj = child.getTag();
            if (tagObj != null && tagObj.equals(tag)) {
                views.add(child);
            }

        }

        return views;
    }

    private List<Button> getButtonsTaggedAsPlayable() {
        View rootView = getWindow().getDecorView().findViewById(android.R.id.content);
        ArrayList<View> inplay = getViewsByTag((ViewGroup) rootView.getParent(), "inplay");
        return inplay.stream().map(v -> ((Button) v)).collect(Collectors.toList());
    }

    private boolean playableKeyLongClickHandler(View view) {
        /*
        If a playable key is held down the user is indicating that they want to include all
        letters up to, and including, that letter in the sequence they have provided (Default is
        Koch
        */
        String buttonLetter = keyboard.getButtonLetter(view);

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
            View progressBar = keyboard.getLetterProgressBar(buttonLetter);
            if (updatedInPlayLetters.contains(buttonLetter)) {
                updateProgressBarColorForLetter(buttonLetter);
                button.setAlpha(ENABLED_BUTTON_ALPHA);
                progressBar.setAlpha(ENABLED_PROGRESS_BAR_ALPHA);
            } else {
                button.setAlpha(DISABLED_BUTTON_ALPHA);
                progressBar.setAlpha(DISABLED_PROGRESS_BAR_ALPHA);
                progressBar.setBackgroundColor(ProgressGradient.DISABLED);
            }
        }
    }

    public void keyboardButtonClicked(View v) {
        String letter = keyboard.getButtonLetter(v);
        if (!engine.isValidGuess(letter)) {
            return;
        }

        Optional<Boolean> guess = engine.guess(letter);

        guess.ifPresent(wasCorrectGuess -> {
            ProgressBar timerProgressBar = findViewById(R.id.timer_progress_bar);
            if (!wasCorrectGuess) {
                TransitionDrawable background = (TransitionDrawable) timerProgressBar.getProgressDrawable();
                background.startTransition(0);
                background.reverseTransition(500);
            }
            updateCompetencyWeights(letter, wasCorrectGuess);
            synchronized (engineMutex) {
                if (engine.shouldIntroduceNewLetter()) {
                    Optional<List<String>> updatedLetters = engine.introduceLetter();
                    updatedLetters.ifPresent(this::updateLayoutUsingTheseLetters);
                }
            }
        });
    }

    private void updateCompetencyWeights(String letter, boolean wasCorrectGuess) {
        if (wasCorrectGuess) {
            totalCorrectGuesses++;
            totalAccurateSymbolsGuessed += CWToneManager.numSymbols(letter);
        } else {
            totalIncorrectGuesses++;
        }

        updateProgressBarColorForLetter(letter);
    }

    private void updateProgressBarColorForLetter(String letter) {
        View progressBar = keyboard.getLetterProgressBar(letter);
        Integer competencyWeight = engine.getCompetencyWeight(letter);
        Integer color = ProgressGradient.forWeight(competencyWeight);
        progressBar.setBackgroundColor(color);
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
        if (countDownTimer != null) {
            countDownTimer.pause();
        }
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
        if (countDownTimer != null && countDownTimer.isPaused()) {
            countDownTimer.resume();
        }
        super.onResume();
    }

    private void resumeSession() {
        if (engine != null) {
            engine.resume();
        }
    }

    private void pauseSession() {
        if (engine != null) {
            engine.pause();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyboard_activity);
        Bundle receiveBundle = getIntent().getExtras();
        assert receiveBundle != null;
        durationMinutesRequested = receiveBundle.getInt(DURATION_REQUESTED_MINUTES, 0);
        durationRequestedMillis = 1000 * (durationMinutesRequested * 60);
        wpmRequested = receiveBundle.getInt(WPM_REQUESTED);
        if (savedInstanceState == null) {
            repository.competencyWeightsDAO.getLatestEngineSetting(this::buildAndStartSession);
        } else {
            System.out.println("wtf");
        }
    }

    private void buildAndStartSession(Optional<LetterTrainingEngineSettings> previousWeight) {
        Map<String, Integer> competencyWeights = buildInitialCompetencyWeights(previousWeight.orElse(null));
        List<String> inPlayKeyNames = buildInitialInPlayKeyNames(previousWeight.orElse(null));
        keyboard = new DynamicKeyboard.Builder()
                .setContext(this)
                .setKeys(SimpleLetters.keys)
                .setButtonOnClickListener(this::keyboardButtonClicked)
                .setButtonLongClickListener(this::playableKeyLongClickHandler)
                .setButtonCallback((button) -> {
                    if (inPlayKeyNames.contains(button.getText().toString())) {
                        button.setAlpha(ENABLED_BUTTON_ALPHA);
                    } else {
                        button.setAlpha(DISABLED_BUTTON_ALPHA);
                    }
                })
                .setProgressBarCallback((button, progressBar) -> {
                    progressBar.setBackgroundColor(ProgressGradient.DISABLED);
                    progressBar.setAlpha(ENABLED_PROGRESS_BAR_ALPHA);
                })
                .createKeyboardBuilder();
        keyboard.buildAtRoot(findViewById(R.id.keyboard_base));

        allPlayableButtons = getButtonsTaggedAsPlayable();

        Toolbar keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        keyboardToolbar.inflateMenu(R.menu.keyboard);
        setSupportActionBar(keyboardToolbar);

        countDownTimer = buildCountDownTimer(1000 * (durationMinutesRequested * 60 + 1));

        engine = new LetterTrainingEngine(KochLetterSequence.sequence, wpmRequested, this::letterChosenCallback, inPlayKeyNames, competencyWeights);
        inPlayKeyNames.forEach(this::updateProgressBarColorForLetter);

        engine.initEngine();
        isPlaying = true;
        countDownTimer.start();
    }

    private CountDownTimer buildCountDownTimer(long durationsMillis) {
        ProgressBar timerProgressBar = findViewById(R.id.timer_progress_bar);
        return new CountDownTimer(durationsMillis, 50) {
            public void onTick(long millisUntilFinished) {
                durationRemainingMillis = millisUntilFinished;
                int progress = Math.round((((float) millisUntilFinished / (float) durationRequestedMillis)) * 1000f);
                timerProgressBar.setProgress(progress, true);
            }

            public void onFinish() {
                durationRemainingMillis = 0;
                Intent data = buildResultIntent();
                setResult(Activity.RESULT_OK, data);
                finishSession(data.getExtras());
                finish();
            }
        };
    }

    @Override
    public void onBackPressed() {
        if (durationRemainingMillis != 0) {
            // Manage internal state
            countDownTimer.pause();
            isPlaying = false;
            // Call subclasses to pause themselves
            pauseSession();

            // Update UI to indicate paused session. Player will need to manually trigger play to resume
            MenuItem playPauseIcon = menu.findItem(R.id.keyboard_pause_play);
            playPauseIcon.setIcon(R.mipmap.ic_play);

            // Build alert and show to user for exit confirmation
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("Do you want to end this session?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                durationRemainingMillis -= 1000; // Duration always seems to be off by -1s when back is pressed
                Intent data = buildResultIntent();
                setResult(Activity.RESULT_OK, data);
                finishSession(data.getExtras());
                finish();
            });
            builder.setNegativeButton("No", (dialog, which) -> dialog.cancel());
            AlertDialog alert = builder.create();
            alert.show();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.keyboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    public void onClickPlayPauseHandler(MenuItem m) {
        // TODO: use timer state instead of isPlaying, then remove isPlaying
        if (!countDownTimer.isPaused()) {
            // User wants pause
            m.setIcon(R.mipmap.ic_play);
            countDownTimer.pause();
            pauseSession();
        } else {
            // User wants play
            m.setIcon(R.mipmap.ic_pause);
            countDownTimer.resume();
            resumeSession();
        }
        isPlaying = !isPlaying;
    }

    private Intent buildResultIntent() {
        // TODO, clean this up
        Intent intent = new Intent();
        Bundle sendBundle = new Bundle();
        intent.putExtras(sendBundle);
        return intent;
    }

    private List<String> buildFirstTwoKeyList() {
        return Lists.newArrayList(
                KochLetterSequence.sequence.get(0),
                KochLetterSequence.sequence.get(1)
        );
    }

    private List<String> buildInitialInPlayKeyNames(LetterTrainingEngineSettings engineSettings) {
        if (engineSettings != null && engineSettings.activeLetters != null && engineSettings.activeLetters.size() != 0) {
            return engineSettings.activeLetters;
        }
        return buildFirstTwoKeyList();
    }

    private Map<String,Integer> buildBlankWeights(List<String> playableKeys) {
        Map<String, Integer> competencyWeights = Maps.newHashMap();
        for (String playableKey : playableKeys) {
            competencyWeights.put(playableKey, 0);
        }
        return competencyWeights;
    }

    private Map<String, Integer> buildInitialCompetencyWeights(LetterTrainingEngineSettings weights) {
        if (weights == null) {
            return buildBlankWeights(SimpleLetters.allPlayableKeysNames());
        }

        Map<String, Integer> competencyWeights;
        List<String> playableKeys = SimpleLetters.allPlayableKeysNames();
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

    private void finishSession(Bundle data) {
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

        repository.insertLetterTrainingSession(trainingSession);

        LetterTrainingEngineSettings settings = engine.getSettings();
        settings.durationRequestedMillis = durationRequestedMillis;
        repository.insertMostRecentCompetencyWeights(settings);
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
