package com.example.uberj.test1.transcribe;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.inputmethodservice.Keyboard;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.uberj.test1.DynamicKeyboard;
import com.example.uberj.test1.R;
import com.example.uberj.test1.keyboards.Keys;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionType;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingEngineSettings;

import java.util.ArrayList;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import static com.example.uberj.test1.socratic.SocraticKeyboardSessionActivity.DISABLED_BUTTON_ALPHA;

public abstract class TranscribeKeyboardSessionActivity extends AppCompatActivity implements Keys, DialogInterface.OnDismissListener {
    public static final String DURATION_REQUESTED_MINUTES = "duration-requested-minutes";
    public static final String WPM_REQUESTED = "wpm-requested";
    public static final String FARNSWORTH_SPACES = "farnsworth-spaces";
    public static final String STRINGS_REQUESTED = "strings-requested";

    private TranscribeTrainingSessionViewModel viewModel;
    private DynamicKeyboard keyboard;
    private Menu menu;

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
        int wpmRequested = receiveBundle.getInt(WPM_REQUESTED);
        int fransworth = receiveBundle.getInt(FARNSWORTH_SPACES);
        ArrayList<String> stringsRequested = receiveBundle.getStringArrayList(STRINGS_REQUESTED);
        viewModel = ViewModelProviders.of(this,
                new TranscribeTrainingSessionViewModel.Factory(
                        this.getApplication(),
                        durationMinutesRequested,
                        wpmRequested,
                        stringsRequested,
                        fransworth,
                        getSessionType(),
                        this)
        ).get(TranscribeTrainingSessionViewModel.class);

        viewModel.getLatestEngineSetting().observe(this, (prevSettings) -> {
            TranscribeTrainingEngineSettings settings;
            if (prevSettings == null || prevSettings.size() == 0) {
                settings = null;
            } else {
                settings = prevSettings.get(0);
            }
            viewModel.primeTheEngine(settings);
            LinearLayout keyboardContainer = findViewById(R.id.keyboard_base);
            keyboard = new DynamicKeyboard.Builder()
                    .setContext(this)
                    .setRootView(keyboardContainer)
                    .setDrawProgressBar(false)
                    .setKeys(getKeys())
                    .setButtonOnClickListener(this::keyboardButtonClicked)
                    .setButtonLongClickListener(this::playableKeyLongClickHandler)
                    .setButtonCallback((button) -> {
                        assert stringsRequested != null;
                        if (!stringsRequested.contains(button.getText().toString())) {
                            button.setAlpha(DISABLED_BUTTON_ALPHA);
                        }
                    })
                    .build();
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
    }


    protected abstract TranscribeSessionType getSessionType();

}
