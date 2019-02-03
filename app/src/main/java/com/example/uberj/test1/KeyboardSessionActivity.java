package com.example.uberj.test1;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Locale;

public abstract class KeyboardSessionActivity extends AppCompatActivity {
    public static final String DURATION_REQUESTED_MINUTES = "duration-requested-minutes";
    public static final String DURATION_REQUESTED_SECONDS = "duration-requested-seconds";
    public static final String SESSION_TYPE = "session-type";
    public static final String WPM_REQUESTED = "wpm-requested";
    private int durationMinutesRequested;
    private int durationSecondsRequested;
    protected long durationRemainingMillis;
    protected long durationRequestedMillis;
    private CountDownTimer countDownTimer;
    private Menu menu;

    public abstract void keyboardButtonClicked(View button);

    private boolean isPlaying;

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

    @Override
    public void onPause() {
        super.onPause();
        pauseTimer();
    }

    @Override
    public void onResume() {
        if (countDownTimer.isPaused()) {
            resumeTimer();
        }
        super.onResume();
    }

    protected View getLetterProgressBar(String letter) {
        int progressBarId = getResources().getIdentifier("progressBarForKey" + buttonLetterToIdName(letter), "id", getApplicationContext().getPackageName());
        return findViewById(progressBarId);
    }

    protected String getButtonLetter(View v) {
        String buttonId = getResources().getResourceEntryName(v.getId());
        if (!buttonId.startsWith("key")) {
            throw new RuntimeException("unknown button " + buttonId);
        }

        return idNameToButtonLetter(buttonId.replace("key", ""));
    }


    private static String idNameToButtonLetter(String idName) {
        if (idName.equals("SLASH")) {
            return "/";
        } else {
            return idName;
        }
    }

    private static String buttonLetterToIdName(String buttonLetter) {
        if (buttonLetter.equals("/")) {
            return "SLASH";
        } else {
            return buttonLetter;
        }
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyboard_activity);
        Toolbar keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        keyboardToolbar.inflateMenu(R.menu.keyboard);
        setSupportActionBar(keyboardToolbar);

        Bundle receiveBundle = getIntent().getExtras();
        assert receiveBundle != null;
        durationMinutesRequested = receiveBundle.getInt(DURATION_REQUESTED_MINUTES, 0);
        durationSecondsRequested = receiveBundle.getInt(DURATION_REQUESTED_SECONDS, 0);
        durationRequestedMillis = 1000 * (durationMinutesRequested * 60 + durationSecondsRequested);
        isPlaying = true;
        countDownTimer = buildCountDownTimer(1000 * (durationMinutesRequested * 60 + durationSecondsRequested + 1));
    }

    private CountDownTimer buildCountDownTimer(long durationsMillis) {
        TextView timeRemainingView = findViewById(R.id.toolbar_title_time_remaining);
        return new CountDownTimer(durationsMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                long secondsUntilFinished = millisUntilFinished / 1000;
                long minutesRemaining = secondsUntilFinished / 60;
                long secondsRemaining = secondsUntilFinished % 60;
                timeRemainingView.setText(String.format(Locale.ENGLISH, "%02d:%02d remaining", minutesRemaining, secondsRemaining));
                durationRemainingMillis = millisUntilFinished;
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

    protected abstract void finishSession(Bundle data);

    private Intent buildResultIntent() {
        // TODO, clean this up
        Intent intent = new Intent();
        Bundle sendBundle = new Bundle();
        intent.putExtras(sendBundle);
        return intent;
    }

    @Override
    public void onBackPressed() {
        if (durationRemainingMillis != 0) {
            // Manage internal state
            pauseTimer();
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
        if (isPlaying) {
            // Pause
            m.setIcon(R.mipmap.ic_play);
            pauseTimer();
            pauseSession();
        } else {
            m.setIcon(R.mipmap.ic_pause);
            resumeTimer();
            resumeSession();
        }
        isPlaying = !isPlaying;
    }


    protected void startTimer() {
        countDownTimer.start();
    }

    protected void pauseTimer() {
        countDownTimer.pause();
    }

    protected void resumeTimer() {
        countDownTimer.pause();
    }

    protected abstract void resumeSession();

    protected abstract void pauseSession();
}
