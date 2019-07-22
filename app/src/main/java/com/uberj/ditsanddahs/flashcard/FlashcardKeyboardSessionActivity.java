package com.uberj.ditsanddahs.flashcard;

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
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.annimon.stream.Optional;
import com.uberj.ditsanddahs.DynamicKeyboard;
import com.uberj.ditsanddahs.GlobalSettings;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardSessionType;
import com.uberj.ditsanddahs.keyboards.KeyConfig;
import com.uberj.ditsanddahs.keyboards.Keys;

import java.util.ArrayList;
import java.util.List;

public abstract class FlashcardKeyboardSessionActivity extends AppCompatActivity implements DialogInterface.OnDismissListener {
    public static final String DURATION_UNITS_REQUESTED = "duration-requested-minutes";
    public static final String WPM_REQUESTED = "wpm-requested";
    public static final String TONE_FREQUENCY_HZ = "tone-frequency-hz";
    public static final String MESSAGES_REQUESTED = "messages-requested";
    public static final String DURATION_UNIT = "duration-unit";
    public static final String SESSION_TYPE = "flashcard-session-type";
    private Menu menu;

    private FlashcardTrainingSessionViewModel viewModel;
    private EditText transcribeTextArea;
    private String durationUnit;
    private ProgressBar timerProgressBar;

    public void keyboardButtonClicked(View v) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            v.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
        } else {
            getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
        String buttonLetter = DynamicKeyboard.getButtonLetter(getApplicationContext(), v).toUpperCase();
        String currentGuess = transcribeTextArea.getText().toString();
        Optional<KeyConfig.ControlType> controlType = KeyConfig.ControlType.fromKeyName(buttonLetter);
        boolean includeInMessage = true;
        if (controlType.isPresent()) {
            KeyConfig.ControlType type = controlType.get();
            if (type.keyName.equals(KeyConfig.ControlType.AGAIN.keyName)) {
                viewModel.getEngine().repeat();
            } else if (type.keyName.equals(KeyConfig.ControlType.SKIP.keyName)) {
                viewModel.getEngine().skip();
            } else if (type.keyName.equals(KeyConfig.ControlType.SUBMIT.keyName)) {
                boolean wasCorrectGuess = viewModel.guess(currentGuess);
                if (wasCorrectGuess) {
                    Drawable correctDrawable = getResources().getDrawable(R.drawable.correct_guess_timer_bar_progress_background, getTheme());
                    timerProgressBar.setProgressDrawable(correctDrawable);
                } else {
                    Drawable incorrectDrawable = getResources().getDrawable(R.drawable.incorrect_guess_timer_bar_progress_background, getTheme());
                    timerProgressBar.setProgressDrawable(incorrectDrawable);
                    includeInMessage = false;
                }

                TransitionDrawable background = (TransitionDrawable) timerProgressBar.getProgressDrawable();
                background.startTransition(0);
                background.reverseTransition(500);
            }
        }

        if (includeInMessage) {
            List<String> transcribedStrings = viewModel.transcribedMessage.getValue();
            transcribedStrings.add(buttonLetter);
            viewModel.transcribedMessage.setValue(transcribedStrings);
        }
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
        TextView keyboardToolbarTitle = findViewById(R.id.keyboard_toolbar_title);
        timerProgressBar = findViewById(R.id.timer_progress_bar);

        int durationUnitsRequested = receiveBundle.getInt(DURATION_UNITS_REQUESTED, 0);
        durationUnit = receiveBundle.getString(DURATION_UNIT, "num_cards");
        int wpmRequested = receiveBundle.getInt(WPM_REQUESTED);
        int toneFrequency = receiveBundle.getInt(TONE_FREQUENCY_HZ, 440);

        GlobalSettings gSettings = GlobalSettings.fromContext(getApplicationContext());
        FlashcardSessionType flashcardSessionType = FlashcardSessionType.valueOf(receiveBundle.getString(SESSION_TYPE, FlashcardSessionType.RANDOM_WORDS.name()));
        ArrayList<String> requestedMessages = receiveBundle.getStringArrayList(MESSAGES_REQUESTED);

        viewModel = ViewModelProviders.of(this,
                new FlashcardTrainingSessionViewModel.Factory(
                        this.getApplication(),
                        requestedMessages,
                        durationUnitsRequested,
                        durationUnit,
                        wpmRequested,
                        toneFrequency,
                        flashcardSessionType,
                        gSettings
                )
        ).get(FlashcardTrainingSessionViewModel.class);

        if (keyboardToolbar != null) {
            keyboardToolbar.inflateMenu(R.menu.socratic_keyboard);
            setSupportActionBar(keyboardToolbar);
            viewModel.titleText.observe(this, keyboardToolbarTitle::setText);
        }

        viewModel.durationUnitsRemaining.observe(this, (remainingParts) -> updateProgressBar(remainingParts, durationUnitsRequested));

        transcribeTextArea = findViewById(R.id.transcribe_text_area);
        transcribeTextArea.requestFocus();
        transcribeTextArea.setShowSoftInputOnFocus(false);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        viewModel.transcribedMessage.observe(this, (enteredStrings) -> {
            String message = FlashcardUtil.convertKeyPressesToString(enteredStrings);
            transcribeTextArea.setText(message);
            transcribeTextArea.setSelection(transcribeTextArea.getText().length());
            boolean enableSubmit;
            enableSubmit = !message.isEmpty();
            findViewById(R.id.keySUBMIT).setEnabled(enableSubmit);

        });

        findViewById(R.id.keyDEL).setOnLongClickListener(v -> {
            List<String> transcribedStrings = viewModel.transcribedMessage.getValue();
            for (char ignored : transcribeTextArea.getText().toString().toCharArray()) {
                transcribedStrings.add("DEL");
            }
            viewModel.transcribedMessage.setValue(transcribedStrings);
            return false;
        });
    }

    private void updateProgressBar(long remainingParts, int durationUnitsRequested) {
        if (remainingParts == 0) {
            finish();
            return;
        }

        int progress;
        if (durationUnit.equals(FlashcardTrainingSessionViewModel.TIME_LIMITED_SESSION_TYPE)) {
            float millisRequested = durationUnitsRequested * 60 * 1000;
            progress = Math.round(((float) remainingParts / millisRequested) * 1000f);
        } else {
            progress = Math.round(((float) remainingParts / (float) durationUnitsRequested) * 1000f);
        }

        // You have to set the progress to zero because there is a bug in ProgressBar
        // https://stackoverflow.com/a/4352073
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            timerProgressBar.setProgress(0, false);
            timerProgressBar.setProgress(progress, true);
        } else {
            timerProgressBar.setProgress(0);
            timerProgressBar.setProgress(progress);
        }
    }

    protected abstract Keys getSessionKeys();

    @Override
    public void onBackPressed() {
        if (viewModel.durationUnitsRemaining.getValue() != 0) {
            viewModel.pause();

            // Update UI to indicate paused session. Player will need to manually trigger play to resume
            if (menu != null) {
                MenuItem playPauseIcon = menu.findItem(R.id.keyboard_pause_play);
                if (durationUnit.equals(FlashcardTrainingSessionViewModel.TIME_LIMITED_SESSION_TYPE)) {
                    playPauseIcon.setIcon(R.mipmap.ic_play);
                }
            }

            // Build alert and show to user for exit confirmation
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("Do you want to end this session?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                // Duration always seems to be off by -1s when back is pressed
                viewModel.setDurationUnitsRemainingMillis(viewModel.durationUnitsRemaining.getValue() - 1000 );
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
        if (durationUnit.equals(FlashcardTrainingSessionViewModel.TIME_LIMITED_SESSION_TYPE)) {
            getMenuInflater().inflate(R.menu.socratic_keyboard, menu);
        } else {
            getMenuInflater().inflate(R.menu.no_pause_keyboard, menu);
        }
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
        if (durationUnit.equals(FlashcardTrainingSessionViewModel.TIME_LIMITED_SESSION_TYPE)) {
            if (menu != null) {
                MenuItem item = menu.findItem(R.id.keyboard_pause_play);
                item.setIcon(R.mipmap.ic_pause);
            }
        }
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
            if (durationUnit.equals(FlashcardTrainingSessionViewModel.TIME_LIMITED_SESSION_TYPE)) {
                m.setIcon(R.mipmap.ic_play);
            }
            viewModel.pause();
        } else {
            // User wants play
            if (durationUnit.equals(FlashcardTrainingSessionViewModel.TIME_LIMITED_SESSION_TYPE)) {
                m.setIcon(R.mipmap.ic_pause);
            }
            viewModel.resume();
        }
    }


    protected abstract DialogFragment getHelpDialog();
}
