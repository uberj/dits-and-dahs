package com.example.uberj.test1.transcribe;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.example.uberj.test1.DynamicKeyboard;
import com.example.uberj.test1.R;
import com.example.uberj.test1.keyboards.Keys;
import com.example.uberj.test1.socratic.storage.SocraticTrainingEngineSettings;
import com.example.uberj.test1.socratic.storage.SocraticSessionType;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import static com.example.uberj.test1.socratic.SocraticKeyboardSessionActivity.DURATION_REQUESTED_MINUTES;
import static com.example.uberj.test1.socratic.SocraticKeyboardSessionActivity.WPM_REQUESTED;

public abstract class TranscribeKeyboardSessionActivity extends AppCompatActivity implements Keys, DialogInterface.OnDismissListener {

    private TranscribeTrainingSessionViewModel viewModel;
    private DynamicKeyboard keyboard;

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
        viewModel = ViewModelProviders.of(this,
                new TranscribeTrainingSessionViewModel.Factory(
                        this.getApplication(),
                        durationMinutesRequested,
                        wpmRequested,
                        getSessionType(),
                        this)
        ).get(TranscribeTrainingSessionViewModel.class);

        viewModel.getLatestEngineSetting().observe(this, (prevSettings) -> {
            SocraticTrainingEngineSettings settings;
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
                    })
                    .setProgressBarCallback((button, progressBar) -> {
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
    public void onDismiss(DialogInterface dialog) {
    }

    private boolean playableKeyLongClickHandler(View view) {
        return false;
    }

    private void keyboardButtonClicked(View view) {
    }


    protected abstract SocraticSessionType getSessionType();

}
