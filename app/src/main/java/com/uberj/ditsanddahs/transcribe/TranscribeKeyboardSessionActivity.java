package com.uberj.ditsanddahs.transcribe;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.uberj.ditsanddahs.DynamicKeyboard;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.keyboards.KeyConfig;
import com.uberj.ditsanddahs.keyboards.Keys;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeSessionType;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeTrainingSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;

import static com.uberj.ditsanddahs.simplesocratic.SocraticKeyboardSessionActivity.DISABLED_BUTTON_ALPHA;

public abstract class TranscribeKeyboardSessionActivity extends AppCompatActivity implements Keys, DialogInterface.OnDismissListener {
    public static final String DURATION_REQUESTED_MINUTES = "duration-requested-minutes";
    public static final String LETTER_WPM_REQUESTED = "letter-wpm-requested";
    public static final String STRINGS_REQUESTED = "strings-requested";
    public static final String EFFECTIVE_WPM_REQUESTED = "effective-wpm-requested";
    public static final String TARGET_ISSUE_STRINGS = "target-issue-strings";
    public static final String AUDIO_TONE_FREQUENCY = "audio-tone-frequency";
    public static final String SESSION_START_DELAY_SECONDS = "session-start-delay-seconds";
    public static final String SESSION_END_DELAY_SECONDS = "session-end-delay-seconds";
    public static final String FADE_IN_OUT_PERCENTAGE = "fade-in-out-percentage";

    private TranscribeTrainingSessionViewModel viewModel;
    private DynamicKeyboard keyboard;
    private Menu menu;
    private EditText transcribeTextArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transcribe_keyboard_activity);
        Bundle receiveBundle = getIntent().getExtras();
        assert receiveBundle != null;
        Toolbar keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        keyboardToolbar.inflateMenu(R.menu.socratic_keyboard);
        setSupportActionBar(keyboardToolbar);

        int durationMinutesRequested = receiveBundle.getInt(DURATION_REQUESTED_MINUTES, 0);
        int letterWpmRequested = receiveBundle.getInt(LETTER_WPM_REQUESTED);
        int effectiveWpmRequested = receiveBundle.getInt(EFFECTIVE_WPM_REQUESTED);
        boolean targetIssueLetters = receiveBundle.getBoolean(TARGET_ISSUE_STRINGS);
        int audioToneFrequency = receiveBundle.getInt(AUDIO_TONE_FREQUENCY);
        int startDelaySeconds = receiveBundle.getInt(SESSION_START_DELAY_SECONDS);
        int endDelaySeconds = receiveBundle.getInt(SESSION_END_DELAY_SECONDS);
        int fadeInOutPercentage = receiveBundle.getInt(FADE_IN_OUT_PERCENTAGE);
        ArrayList<String> stringsRequested = receiveBundle.getStringArrayList(STRINGS_REQUESTED);
        viewModel = ViewModelProviders.of(this,
                new TranscribeTrainingSessionViewModel.Factory(
                        this.getApplication(),
                        durationMinutesRequested,
                        letterWpmRequested,
                        effectiveWpmRequested,
                        stringsRequested,
                        targetIssueLetters,
                        audioToneFrequency,
                        startDelaySeconds,
                        endDelaySeconds,
                        getSessionType(),
                        fadeInOutPercentage)
        ).get(TranscribeTrainingSessionViewModel.class);

        ConstraintLayout keyboardContainer = findViewById(R.id.nested_transcribe_keyboard);
        ArrayList<View> inPlayButtons = DynamicKeyboard.getViewsByTag(keyboardContainer, "inplay");
        for (View inPlayButton : inPlayButtons) {
            Button button = (Button) inPlayButton;
            if (!stringsRequested.contains(button.getText().toString())) {
                button.setAlpha(DISABLED_BUTTON_ALPHA);
            }
        }

        viewModel.getLatestTrainingSession().observe(this, (possibleSession) -> {
            TranscribeTrainingSession session;
            if (possibleSession == null || possibleSession.isEmpty()) {
                session = null;
            } else {
                session = possibleSession.get(0);
            }
            viewModel.primeTheEngine(session);
            viewModel.startTheEngine();
        });

        ProgressBar timerProgressBar = findViewById(R.id.timer_progress_bar);
        viewModel.durationRemainingMillis.observe(this, (remainingMillis) -> {
            if (endDelaySeconds >= 0 && remainingMillis == 0) {
                finish();
                return;
            }

            int progress = Math.round((((float) remainingMillis / (float) viewModel.getDurationRequestedMillis())) * 1000f);
            timerProgressBar.setProgress(progress, true);
        });

        transcribeTextArea = findViewById(R.id.transcribe_text_area);
        transcribeTextArea.requestFocus();
        transcribeTextArea.setShowSoftInputOnFocus(false);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        viewModel.transcribedMessage.observe(this, (enteredStrings) -> {
            String message = TranscribeUtil.convertKeyPressesToString(enteredStrings);
            transcribeTextArea.setText(message);
            transcribeTextArea.setSelection(transcribeTextArea.getText().length());
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.transcribe_keyboard, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (viewModel.durationRemainingMillis.getValue() != 0) {
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
                viewModel.durationRemainingMillis.setValue(viewModel.durationRemainingMillis.getValue() - 1000 );
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

    private Intent buildResultIntent() {
        return new Intent();
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        MenuItem item = menu.findItem(R.id.keyboard_pause_play);
        item.setIcon(R.mipmap.ic_pause);
        viewModel.resume();
    }

    public void onClickHelpButton(MenuItem item) {
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

    // Called via xml
    public void keyboardButtonClicked(View view) {
        view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
        String buttonLetter = DynamicKeyboard.getButtonLetter(getApplicationContext(), view);
        Optional<KeyConfig.ControlType> controlType = KeyConfig.ControlType.fromKeyName(buttonLetter);
        if (!viewModel.isARequstedString(buttonLetter) && !controlType.isPresent()) {
            return;
        }
        List<String> transcribedStrings = viewModel.transcribedMessage.getValue();
        transcribedStrings.add(buttonLetter);
        viewModel.transcribedMessage.setValue(transcribedStrings);
    }


    protected abstract TranscribeSessionType getSessionType();
}
