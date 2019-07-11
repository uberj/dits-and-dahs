package com.uberj.ditsanddahs.transcribe;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.uberj.ditsanddahs.DynamicKeyboard;
import com.uberj.ditsanddahs.GlobalSettings;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.keyboards.Keys;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeSessionType;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeTrainingSession;

import java.util.ArrayList;
import java.util.List;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProviders;

import timber.log.Timber;

import static com.uberj.ditsanddahs.simplesocratic.SocraticKeyboardSessionActivity.DISABLED_BUTTON_ALPHA;

public abstract class TranscribeKeyboardSessionActivity extends AppCompatActivity implements Keys, DialogInterface.OnDismissListener {
    public static final String STRINGS_REQUESTED = "strings-requested";

    private TranscribeTrainingSessionViewModel viewModel;
    private Menu menu;
    private EditText transcribeTextArea;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transcribe_keyboard_activity);
        Bundle receiveBundle = getIntent().getExtras();
        assert receiveBundle != null;
        TextView keyboardToolbarTitle = findViewById(R.id.keyboard_toolbar_title);
        Toolbar keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        keyboardToolbar.inflateMenu(R.menu.socratic_keyboard);
        setSupportActionBar(keyboardToolbar);

        ArrayList<String> stringsRequested = receiveBundle.getStringArrayList(STRINGS_REQUESTED);
        TranscribeSessionType sessionType = getSessionType();

        GlobalSettings globalSettings = GlobalSettings.fromContext(getApplicationContext());
        TranscribeSettings settings = TranscribeSettings.fromContext(getApplicationContext());
        TranscribeTrainingSessionViewModel.Params.Builder params = TranscribeTrainingSessionViewModel.Params.builder();

        final int startDelaySeconds = settings.startDelaySeconds;
        int endDelaySeconds = settings.endDelaySeconds;
        if (endDelaySeconds == Integer.valueOf(getResources().getString(R.string.setting_transcribe_end_delay_seconds_max_value))) {
            endDelaySeconds = -1;
        }

        params.setDurationMinutesRequested(settings.durationMinutesRequested);
        params.setLetterWpmRequested(settings.letterWpmRequested);
        params.setEffectiveWpmRequested(settings.letterWpmRequested);
        params.setTargetIssueLetters(settings.targetIssueStrings);
        params.setAudioToneFrequency(settings.audioToneFrequency);
        params.setSessionType(sessionType);
        params.setSecondAudioToneFrequency(settings.secondAudioToneFrequency);
        params.setSecondsBetweenStationTransmissions(settings.secondsBetweenStationTransmissions);
        params.setStartDelaySeconds(startDelaySeconds);
        params.setEndDelaySeconds(endDelaySeconds);
        params.setFadeInOutPercentage(globalSettings.getFadeInOutPercentage());
        params.setStringsRequested(stringsRequested);

        viewModel = ViewModelProviders
                .of(this, new TranscribeTrainingSessionViewModel.Factory(this.getApplication(), params.build()))
                .get(TranscribeTrainingSessionViewModel.class);

        ConstraintLayout keyboardContainer = findViewById(R.id.nested_transcribe_keyboard);
        ProgressBar timerProgressBar = findViewById(R.id.timer_progress_bar);
        FrameLayout timerProgressBarContainer = findViewById(R.id.timer_progress_bar_container);

        if (sessionType.equals(TranscribeSessionType.RANDOM_LETTER_ONLY)) {
            ArrayList<View> inPlayButtons = DynamicKeyboard.getViewsByTag(keyboardContainer, "inplay");
            for (View inPlayButton : inPlayButtons) {
                Button button = (Button) inPlayButton;
                if (!stringsRequested.contains(button.getText().toString())) {
                    button.setAlpha(DISABLED_BUTTON_ALPHA);
                }
            }
        } else if (sessionType.equals(TranscribeSessionType.RANDOM_QSO)) {
            timerProgressBarContainer.setVisibility(View.GONE);
        } else {
            throw new RuntimeException(("Unknown session type: ") + sessionType);
        }


        viewModel.titleText.observe(this, keyboardToolbarTitle::setText);

        viewModel.getLatestTrainingSession().observe(this, (possibleSession) -> {
            TranscribeTrainingSession session;
            if (possibleSession == null || possibleSession.isEmpty()) {
                session = null;
            } else {
                session = possibleSession.get(0);
            }

            if (startDelaySeconds > 0) {
                viewModel.primeEngineWithCountdown(session);
            } else {
                viewModel.primeTheEngine(session, GlobalSettings.fromContext(getApplicationContext()));
                viewModel.startTheEngine();
            }
        });

        viewModel.sessionIsFinished.observe(this, (isFinished) -> {
            if (isFinished) {
                finish();
            }
        });

        viewModel.endTimerInProgress.observe(this, inProgress -> {
            if (inProgress) {
                invalidateOptionsMenu();
            }
        });

        viewModel.sessionHasBeenStarted.observe(this, hasStarted -> {
            if (hasStarted) {
                invalidateOptionsMenu();
            }
        });

        viewModel.durationRemainingMillis.observe(this, (remainingMillis) -> {
            if (remainingMillis < 0) {
                // invalid state
                Timber.d("Invalid duration timer");
                return;
            }

            int progress = Math.round((((float) remainingMillis / (float) viewModel.getDurationRequestedMillis())) * 1000f);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                timerProgressBar.setProgress(progress, true);
            } else {
                timerProgressBar.setProgress(progress);
            }

            if (remainingMillis == 0) {
                viewModel.messageDonePlaying.postValue(true);
            }
        });

        viewModel.messageDonePlaying.observe(this, (donePlaying) -> {
            if (donePlaying) {
                viewModel.finishSessionWithTimer();
            }
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
        MenuItem item = menu.findItem(R.id.keyboard_pause_play);
        if (!viewModel.sessionHasBeenStarted.getValue() || viewModel.endTimerInProgress.getValue()) {
            item.setEnabled(false);
        } else {
            item.setEnabled(true);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        if (!viewModel.hasEngineBeenPrimed()) {
            super.onBackPressed();
        } else if (viewModel.durationRemainingMillis.getValue() != 0 && !viewModel.sessionIsFinished.getValue()) {
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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            view.performHapticFeedback(HapticFeedbackConstants.KEYBOARD_PRESS);
        } else {
            getWindow().getDecorView().performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY);
        }
        String buttonLetter = DynamicKeyboard.getButtonLetter(getApplicationContext(), view);
        List<String> transcribedStrings = viewModel.transcribedMessage.getValue();
        transcribedStrings.add(buttonLetter);
        viewModel.transcribedMessage.setValue(transcribedStrings);
    }


    protected abstract TranscribeSessionType getSessionType();
}
