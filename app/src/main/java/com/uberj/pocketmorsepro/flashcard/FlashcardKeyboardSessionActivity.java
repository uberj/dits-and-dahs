package com.uberj.pocketmorsepro.flashcard;

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
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.common.base.Joiner;
import com.uberj.pocketmorsepro.DynamicKeyboard;
import com.uberj.pocketmorsepro.R;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardSessionType;
import com.uberj.pocketmorsepro.keyboards.KeyConfig;
import com.uberj.pocketmorsepro.keyboards.Keys;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public abstract class FlashcardKeyboardSessionActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {
    public static final String DURATION_UNITS_REQUESTED = "duration-requested-minutes";
    public static final String WPM_REQUESTED = "wpm-requested";
    public static final String TONE_FREQUENCY_HZ = "tone-frequency-hz";
    public static final String MESSAGES_REQUESTED = "messages-requested";
    public static final String DURATION_UNIT = "duration-unit";
    private Menu menu;

    private DynamicKeyboard keyboard;

    private FlashcardTrainingSessionViewModel viewModel;
    private EditText transcribeTextArea;
    private String durationUnit;

    public void keyboardButtonClicked(View v) {
        String buttonLetter = keyboard.getButtonLetter(v).toUpperCase();
        String currentGuess = transcribeTextArea.getText().toString();
        Optional<KeyConfig.ControlType> controlType = KeyConfig.ControlType.fromKeyName(buttonLetter);
        if (controlType.isPresent()) {
            KeyConfig.ControlType type = controlType.get();
            if (type.keyName.equals(KeyConfig.ControlType.AGAIN.keyName)) {
                viewModel.getEngine().repeat();
            } else if (type.keyName.equals(KeyConfig.ControlType.SKIP.keyName)) {
                viewModel.getEngine().skip();
            } else if (type.keyName.equals(KeyConfig.ControlType.SUBMIT.keyName)) {
                boolean wasCorrectGuess = viewModel.getEngine().submitGuess(currentGuess);
                ProgressBar timerProgressBar = findViewById(R.id.timer_progress_bar);
                if (!wasCorrectGuess) {
                    Drawable incorrectDrawable = getResources().getDrawable(R.drawable.incorrect_guess_timer_bar_progress_background, getTheme());
                    timerProgressBar.setProgressDrawable(incorrectDrawable);
                } else {
                    Drawable correctDrawable = getResources().getDrawable(R.drawable.correct_guess_timer_bar_progress_background, getTheme());
                    timerProgressBar.setProgressDrawable(correctDrawable);
                }

                TransitionDrawable background = (TransitionDrawable) timerProgressBar.getProgressDrawable();
                background.startTransition(0);
                background.reverseTransition(500);
            }
        }
        List<String> transcribedStrings = viewModel.transcribedMessage.getValue();
        transcribedStrings.add(buttonLetter);
        viewModel.transcribedMessage.setValue(transcribedStrings);
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
        setContentView(R.layout.flashcard_keyboard_activity);
        Bundle receiveBundle = getIntent().getExtras();
        assert receiveBundle != null;
        Toolbar keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        keyboardToolbar.inflateMenu(R.menu.socratic_keyboard);
        setSupportActionBar(keyboardToolbar);

        int durationUnitsRequested = receiveBundle.getInt(DURATION_UNITS_REQUESTED, 0);
        durationUnit = receiveBundle.getString(DURATION_UNIT, "num_cards");
        int wpmRequested = receiveBundle.getInt(WPM_REQUESTED);
        int toneFrequency = receiveBundle.getInt(TONE_FREQUENCY_HZ, 440);
        ArrayList<String> requestedMessages = receiveBundle.getStringArrayList(MESSAGES_REQUESTED);

        viewModel = ViewModelProviders.of(this,
                new FlashcardTrainingSessionViewModel.Factory(
                        this.getApplication(),
                        requestedMessages,
                        durationUnitsRequested,
                        durationUnit,
                        wpmRequested,
                        toneFrequency,
                        getSessionType())
        ).get(FlashcardTrainingSessionViewModel.class);

        Keys sessionKeys = getSessionKeys();
        LinearLayout keyboardContainer = findViewById(R.id.keyboard_base);
        keyboard = new DynamicKeyboard.Builder()
                .setContext(this)
                .setRootView(keyboardContainer)
                .setKeys(sessionKeys.getKeys())
                .setButtonOnClickListener(this::keyboardButtonClicked)
                .setDrawProgressBar(false)
                .setButtonCallback((button, keyConfig) -> {
                })
                .setProgressBarCallback((button, progressBar) -> {
                })
                .build();
        keyboard.buildAtRoot();

        ProgressBar timerProgressBar = findViewById(R.id.timer_progress_bar);
        viewModel.getDurationUnitsRemaining().observe(this, (remainingParts) -> {
            if (remainingParts == 0) {
                finish();
                return;
            }
            int progress;
            if (durationUnit.equals(FlashcardTrainingSessionViewModel.TIME_LIMITED_SESSION_TYPE)) {
                float millisRequested = durationUnitsRequested * 60 * 1000;
                System.out.println("remainingParts: " + remainingParts);
                System.out.println("durationRequested: " + millisRequested);
                progress = Math.round(((float) remainingParts / millisRequested) * 1000f);
            } else {
                progress = Math.round(((float) remainingParts / (float) durationUnitsRequested) * 1000f);
            }
            timerProgressBar.setProgress(progress, true);
        });

        transcribeTextArea = findViewById(R.id.transcribe_text_area);
        transcribeTextArea.requestFocus();
        transcribeTextArea.setShowSoftInputOnFocus(false);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        viewModel.transcribedMessage.observe(this, (enteredStrings) -> {
            String message = FlashcardUtil.convertKeyPressesToString(enteredStrings);
            transcribeTextArea.setText(message);
            transcribeTextArea.setSelection(transcribeTextArea.getText().length());
        });
    }

    protected abstract Keys getSessionKeys();

    public abstract FlashcardSessionType getSessionType();

    @Override
    public void onBackPressed() {
        if (viewModel.getDurationUnitsRemaining().getValue() != 0) {
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
                viewModel.setDurationUnitsRemainingMillis(viewModel.getDurationUnitsRemaining().getValue() - 1000 );
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
        if (!durationUnit.equals(FlashcardTrainingSessionViewModel.TIME_LIMITED_SESSION_TYPE)) {
            // TODO, ignore the pause/play button
        }
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
