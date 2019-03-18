package com.example.uberj.test1.transcribe;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.uberj.test1.DynamicKeyboard;
import com.example.uberj.test1.R;
import com.example.uberj.test1.keyboards.KeyConfig;
import com.example.uberj.test1.keyboards.Keys;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionType;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingSession;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import static com.example.uberj.test1.socratic.SocraticKeyboardSessionActivity.DISABLED_BUTTON_ALPHA;

public abstract class TranscribeKeyboardSessionActivity extends AppCompatActivity implements Keys, DialogInterface.OnDismissListener {
    public static final String DURATION_REQUESTED_MINUTES = "duration-requested-minutes";
    public static final String LETTER_WPM_REQUESTED = "letter-wpm-requested";
    public static final String FARNSWORTH_SPACES = "farnsworth-spaces";
    public static final String STRINGS_REQUESTED = "strings-requested";
    public static final String EFFECTIVE_WPM_REQUESTED = "effective-wpm-requested";
    public static final String TARGET_ISSUE_STRINGS = "target-issue-strings";
    public static final String AUDIO_TONE_FREQUENCY = "audio-tone-frequency";
    public static final String SESSION_START_DELAY_SECONDS = "session-start-delay-seconds";
    public static final String SESSION_END_DELAY_SECONDS = "session-end-delay-seconds";

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
        keyboardToolbar.inflateMenu(R.menu.keyboard);
        setSupportActionBar(keyboardToolbar);

        int durationMinutesRequested = receiveBundle.getInt(DURATION_REQUESTED_MINUTES, 0);
        int letterWpmRequested = receiveBundle.getInt(LETTER_WPM_REQUESTED);
        int effectiveWpmRequested = receiveBundle.getInt(EFFECTIVE_WPM_REQUESTED);
        int fransworth = receiveBundle.getInt(FARNSWORTH_SPACES);
        boolean targetIssueLetters = receiveBundle.getBoolean(TARGET_ISSUE_STRINGS);
        int audioToneFrequency = receiveBundle.getInt(AUDIO_TONE_FREQUENCY);
        int startDelaySeconds = receiveBundle.getInt(SESSION_START_DELAY_SECONDS);
        int endDelaySeconds = receiveBundle.getInt(SESSION_END_DELAY_SECONDS);
        ArrayList<String> stringsRequested = receiveBundle.getStringArrayList(STRINGS_REQUESTED);
        viewModel = ViewModelProviders.of(this,
                new TranscribeTrainingSessionViewModel.Factory(
                        this.getApplication(),
                        durationMinutesRequested,
                        letterWpmRequested,
                        effectiveWpmRequested,
                        stringsRequested,
                        fransworth,
                        targetIssueLetters,
                        audioToneFrequency,
                        startDelaySeconds,
                        endDelaySeconds,
                        getSessionType(),
                        this)
        ).get(TranscribeTrainingSessionViewModel.class);

        LinearLayout keyboardContainer = findViewById(R.id.keyboard_base);
        keyboard = new DynamicKeyboard.Builder()
                .setContext(this)
                .setRootView(keyboardContainer)
                .setDrawProgressBar(false)
                .setKeys(getKeys())
                .setButtonOnClickListener(this::keyboardButtonClicked)
                .setButtonLongClickListener(this::playableKeyLongClickHandler)
                .setButtonCallback((view, keyConfig) -> {
                    assert stringsRequested != null;
                    if (keyConfig.type == KeyConfig.KeyType.DELETE_KEY || keyConfig.type == KeyConfig.KeyType.SPACE_KEY) {
                        return;
                    }

                    if (!(view instanceof Button)) {
                        return;
                    }

                    Button button = (Button) view;
                    if (!stringsRequested.contains(button.getText().toString())) {
                        button.setAlpha(DISABLED_BUTTON_ALPHA);
                    }

                })
                .build();

        viewModel.getLatestTrainingSession().observe(this, (possibleSession) -> {
            TranscribeTrainingSession session;
            if (possibleSession == null || possibleSession.isEmpty()) {
                session = null;
            } else {
                session = possibleSession.get(0);
            }
            viewModel.primeTheEngine(session);
            keyboard.buildAtRoot();
            viewModel.startTheEngine();
        });

        ProgressBar timerProgressBar = findViewById(R.id.timer_progress_bar);
        viewModel.durationRemainingMillis.observe(this, (remainingMillis) -> {
            if (remainingMillis == 0) {
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
        getMenuInflater().inflate(R.menu.keyboard, menu);
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

    private boolean playableKeyLongClickHandler(View view) {
        return false;
    }

    private void keyboardButtonClicked(View view) {
        String buttonLetter = keyboard.getButtonLetter(view);
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
