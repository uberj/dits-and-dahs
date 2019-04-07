package com.uberj.pocketmorsepro.socratic;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.HandlerThread;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.uberj.pocketmorsepro.DynamicKeyboard;
import com.uberj.pocketmorsepro.KochLetterSequence;
import com.uberj.pocketmorsepro.ProgressGradient;
import com.uberj.pocketmorsepro.R;
import com.uberj.pocketmorsepro.keyboards.Keys;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingEngineSettings;
import com.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;
import com.google.common.collect.Lists;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class SocraticKeyboardSessionActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {
    public static final String EASY_MODE = "easy-mode";
    public static final String REQUEST_WEIGHTS_RESET = "request-weights-reset";
    public static final String DURATION_REQUESTED_MINUTES = "duration-requested-minutes";
    public static final String WPM_REQUESTED = "wpm-requested";
    public static final String TONE_FREQUENCY_HZ = "tone-frequency-hz";
    private static final String engineMutex = "engineMutex";
    private Menu menu;

    public static final float ENABLED_BUTTON_ALPHA = 1f;
    public static final float ENABLED_PROGRESS_BAR_ALPHA = 0.75f;

    public static final float DISABLED_BUTTON_ALPHA = 0.35f;
    public static final float DISABLED_PROGRESS_BAR_ALPHA = 0.25f;

    private DynamicKeyboard keyboard;

    private SocraticTrainingSessionViewModel viewModel;

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
        viewModel.getEngine().setPlayableKeys(lettersToBePlayedFromNowOn);
        return true;
    }

    private void updateLayoutUsingTheseLetters(List<String> updatedInPlayLetters) {
        for (Button button : getButtonsTaggedAsPlayable()) {
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
        if (viewModel.isPaused()) {
            viewModel.getEngine().playLetter(letter);
            return;
        }

        if (!viewModel.getEngine().isValidGuess(letter)) {
            return;
        }

        Optional<Boolean> guess = viewModel.getEngine().guess(letter);

        guess.ifPresent(wasCorrectGuess -> {
            ProgressBar timerProgressBar = findViewById(R.id.timer_progress_bar);
            if (!wasCorrectGuess) {
                Drawable incorrectDrawable = getResources().getDrawable(R.drawable.incorrect_guess_timer_bar_progress_background, getTheme());
                timerProgressBar.setProgressDrawable(incorrectDrawable);
                new Thread(() -> viewModel.playIncorrectSound()).start();
            } else {
                Drawable correctDrawable = getResources().getDrawable(R.drawable.correct_guess_timer_bar_progress_background, getTheme());
                timerProgressBar.setProgressDrawable(correctDrawable);
                new Thread(() -> viewModel.playCorrectSound()).start();
            }

            TransitionDrawable background = (TransitionDrawable) timerProgressBar.getProgressDrawable();
            background.startTransition(0);
            background.reverseTransition(500);

            viewModel.updateCompetencyWeights(letter, wasCorrectGuess);
            updateProgressBarColorForLetter(letter);

            synchronized (engineMutex) {
                if (viewModel.getEngine().shouldIntroduceNewLetter()) {
                    Optional<List<String>> updatedLetters = viewModel.getEngine().introduceLetter();
                    updatedLetters.ifPresent(this::updateLayoutUsingTheseLetters);
                }
            }
        });
    }

    private void updateProgressBarColorForLetter(String letter) {
        View progressBar = keyboard.getLetterProgressBar(letter);
        Integer competencyWeight = viewModel.getEngine().getCompetencyWeight(letter);
        Integer color = ProgressGradient.forWeight(competencyWeight);
        progressBar.setBackgroundColor(color);
    }


    @Override
    public void onPause() {
        viewModel.pause();
        super.onPause();
    }

    @Override
    public void onResume() {
        viewModel.resume();
        super.onResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.socratic_keyboard_activity);
        Bundle receiveBundle = getIntent().getExtras();
        assert receiveBundle != null;
        Toolbar keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        keyboardToolbar.inflateMenu(R.menu.keyboard);
        setSupportActionBar(keyboardToolbar);

        boolean resetWeights = receiveBundle.getBoolean(REQUEST_WEIGHTS_RESET, false);
        int durationMinutesRequested = receiveBundle.getInt(DURATION_REQUESTED_MINUTES, 0);
        int wpmRequested = receiveBundle.getInt(WPM_REQUESTED);
        int toneFrequency = receiveBundle.getInt(TONE_FREQUENCY_HZ, 440);
        boolean easyMode = receiveBundle.getBoolean(EASY_MODE, true);

        viewModel = ViewModelProviders.of(this,
                new SocraticTrainingSessionViewModel.Factory(
                        this.getApplication(),
                        resetWeights,
                        durationMinutesRequested,
                        wpmRequested,
                        toneFrequency,
                        easyMode,
                        getSessionType(),
                        getSessionKeys())
        ).get(SocraticTrainingSessionViewModel.class);

        viewModel.getLatestEngineSetting().observe(this, (prevSettings) -> {
            SocraticTrainingEngineSettings settings;
            if (prevSettings == null || prevSettings.size() == 0) {
                settings = null;
            } else {
                settings = prevSettings.get(0);
            }
            viewModel.primeTheEngine(settings);
            Keys sessionKeys = getSessionKeys();
            LinearLayout keyboardContainer = findViewById(R.id.keyboard_base);
            keyboard = new DynamicKeyboard.Builder()
                    .setContext(this)
                    .setRootView(keyboardContainer)
                    .setKeys(sessionKeys.getKeys())
                    .setButtonOnClickListener(this::keyboardButtonClicked)
                    .setButtonLongClickListener(this::playableKeyLongClickHandler)
                    .setButtonCallback((button, keyConfig) -> {
                        if (button instanceof Button) {
                            String letter = ((Button) button).getText().toString();
                            if (viewModel.getInPlayKeyNames().contains(letter)) {
                                button.setAlpha(ENABLED_BUTTON_ALPHA);
                            } else {
                                button.setAlpha(DISABLED_BUTTON_ALPHA);
                            }
                        }
                    })
                    .setProgressBarCallback((button, progressBar) -> {
                        progressBar.setBackgroundColor(ProgressGradient.DISABLED);
                        progressBar.setAlpha(ENABLED_PROGRESS_BAR_ALPHA);
                    })
                    .build();
            keyboard.buildAtRoot();
            for (String letter : viewModel.getInPlayKeyNames()) {
                updateProgressBarColorForLetter(letter);
            }
            viewModel.startTheEngine();
        });

        ProgressBar timerProgressBar = findViewById(R.id.timer_progress_bar);
        viewModel.getDurationRemainingMillis().observe(this, (remainingMillis) -> {
            if (remainingMillis == 0) {
                finish();
                return;
            }

            int progress = Math.round((((float) remainingMillis / (float) viewModel.getDurationRequestedMillis())) * 1000f);
            timerProgressBar.setProgress(progress, true);
        });

    }

    protected abstract Keys getSessionKeys();

    public abstract SocraticSessionType getSessionType();

    @Override
    public void onBackPressed() {
        if (viewModel.getDurationRemainingMillis().getValue() != 0) {
            viewModel.pause();

            // Update UI to indicate paused session. Player will need to manually trigger play to resume
            MenuItem playPauseIcon = menu.findItem(R.id.keyboard_pause_play);
            playPauseIcon.setIcon(R.mipmap.ic_play);

            // Build alert and show to user for exit confirmation
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("Do you want to end this session?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                // Duration always seems to be off by -1s when back is pressed
                viewModel.setDurationRemainingMillis(viewModel.getDurationRemainingMillis().getValue() - 1000 );
                Intent data = buildResultIntent();
                setResult(Activity.RESULT_OK, data);
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

    private Intent buildResultIntent() {
        // TODO, clean this up
        Intent intent = new Intent();
        Bundle sendBundle = new Bundle();
        intent.putExtras(sendBundle);
        return intent;
    }


    @Override
    public void onDismiss(DialogInterface dialog) {
        MenuItem item = menu.findItem(R.id.keyboard_pause_play);
        item.setIcon(R.mipmap.ic_pause);
        viewModel.resume();
    }

    public void onClickHelpButton(MenuItem item) {
        viewModel.pause();
        DialogFragment dialog = getHelpDialog();
        FragmentManager supportFragmentManager = getSupportFragmentManager();
        dialog.show(supportFragmentManager, dialog.getTag());
    }

    public void onClickPlayPauseHandler(MenuItem m) {
        if (!viewModel.isPaused()) {
            // User wants pause
            m.setIcon(R.mipmap.ic_play);
            viewModel.pause();
        } else {
            // User wants play
            m.setIcon(R.mipmap.ic_pause);
            viewModel.resume();
        }
    }


    protected abstract DialogFragment getHelpDialog();
}
