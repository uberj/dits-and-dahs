package com.uberj.ditsanddahs.simplesocratic;

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
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.uberj.ditsanddahs.DynamicKeyboard;
import com.uberj.ditsanddahs.GlobalSettings;
import com.uberj.ditsanddahs.KochLetterSequence;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.keyboards.Keys;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingEngineSettings;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticSessionType;
import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.views.ProgressDots;

import java.util.ArrayList;
import java.util.List;

public abstract class SocraticKeyboardSessionActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {
    public static final String REQUEST_WEIGHTS_RESET = "request-weights-reset";
    private static final String engineMutex = "engineMutex";
    private Menu menu;

    public static final float ENABLED_BUTTON_ALPHA = 1f;
    public static final float ENABLED_PROGRESS_BAR_ALPHA = 0.75f;

    public static final float DISABLED_BUTTON_ALPHA = 0.15f;
    public static final float DISABLED_PROGRESS_BAR_ALPHA = 0.05f;

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
        return Stream.of(inplay).map(v -> ((Button) v)).collect(Collectors.toList());
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
            ProgressDots progressBar = keyboard.getLetterProgressBar(buttonLetter);
            if (updatedInPlayLetters.contains(buttonLetter)) {
                updateProgressBarColorForLetter(buttonLetter);
                button.setAlpha(ENABLED_BUTTON_ALPHA);
                progressBar.setAlpha(ENABLED_PROGRESS_BAR_ALPHA);
                progressBar.setShowSegments(true);
            } else {
                button.setAlpha(DISABLED_BUTTON_ALPHA);
                progressBar.setAlpha(DISABLED_PROGRESS_BAR_ALPHA);
                progressBar.setShowSegments(false);
            }
        }
    }

    public void keyboardButtonClicked(View v) {
        if (viewModel.enableHapticFeedback) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
                v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
            } else {
                getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.KEYBOARD_TAP);
            }
        }

        String letter = keyboard.getButtonLetter(v);
        if (viewModel.isPaused()) {
            viewModel.getEngine().playLetter(letter);
            return;
        }

        if (!viewModel.getEngine().isValidGuess(letter)) {
            return;
        }

        String currentLetter = viewModel.getEngine().getCurrentLetter();
        Optional<Boolean> guess = viewModel.getEngine().guess(letter);

        guess.ifPresent(wasCorrectGuess -> {
            ProgressBar timerProgressBar = findViewById(R.id.timer_progress_bar);
            if (!wasCorrectGuess) {
                Drawable incorrectDrawable = getResources().getDrawable(R.drawable.incorrect_guess_timer_bar_progress_background, getTheme());
                timerProgressBar.setProgressDrawable(incorrectDrawable);
                viewModel.playIncorrectSound();
            } else {
                Drawable correctDrawable = getResources().getDrawable(R.drawable.correct_guess_timer_bar_progress_background, getTheme());
                timerProgressBar.setProgressDrawable(correctDrawable);
                viewModel.playCorrectSound();
            }

            TransitionDrawable background = (TransitionDrawable) timerProgressBar.getProgressDrawable();
            background.startTransition(0);
            background.reverseTransition(500);


            synchronized (engineMutex) {
                updateProgressBarColorForLetter(currentLetter);
                if (viewModel.getEngine().shouldIntroduceNewLetter()) {
                    Optional<List<String>> updatedLetters = viewModel.getEngine().introduceLetter();
                    updatedLetters.ifPresent(this::updateLayoutUsingTheseLetters);
                }
            }
        });
    }

    private void updateProgressBarColorForLetter(String letter) {
        ProgressDots progressBar = keyboard.getLetterProgressBar(letter);
        Integer competencyWeight = viewModel.getEngine().getCompetencyWeight(letter);
        progressBar.setCompetencyWeight(competencyWeight);
        progressBar.setShowSegments(true);
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
        keyboardToolbar.inflateMenu(R.menu.socratic_keyboard);
        setSupportActionBar(keyboardToolbar);

        boolean resetWeights = receiveBundle.getBoolean(REQUEST_WEIGHTS_RESET, false);

        viewModel = ViewModelProviders.of(this,
                new SocraticTrainingSessionViewModel.Factory(
                        this.getApplication(),
                        resetWeights,
                        getSessionType(),
                        getSessionKeys(),
                        GlobalSettings.fromContext(getApplicationContext()),
                        SocraticSettings.fromContext(getApplicationContext()))
        ).get(SocraticTrainingSessionViewModel.class);

        ProgressBar timerProgressBar = findViewById(R.id.timer_progress_bar);
        viewModel.getLatestEngineSetting().observe(this, (prevSettings) -> {
            SocraticTrainingEngineSettings settings;
            if (prevSettings == null || prevSettings.size() == 0) {
                settings = null;
            } else {
                settings = prevSettings.get(0);
            }
            viewModel.setUp(settings, GlobalSettings.fromContext(getApplicationContext()));
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
                    })
                    .build();
            keyboard.buildAtRoot();
            for (String letter : viewModel.getInPlayKeyNames()) {
                updateProgressBarColorForLetter(letter);
            }
            viewModel.startTheEngine();
        });


        viewModel.getDurationRemainingMillis().observe(this, (remainingMillis) -> {
            if (remainingMillis == 0) {
                finish();
                return;
            }

            int progress = Math.round((((float) remainingMillis / (float) viewModel.getDurationRequestedMillis())) * 1000f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                timerProgressBar.setProgress(progress, true);
            } else {
                timerProgressBar.setProgress(progress);
            }
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
                viewModel.prepairShutDown();
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
        getMenuInflater().inflate(R.menu.socratic_keyboard, menu);
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
