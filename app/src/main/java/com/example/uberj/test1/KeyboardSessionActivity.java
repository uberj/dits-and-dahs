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

abstract class KeyboardSessionActivity extends AppCompatActivity {
    public static final String DURATION_REQUESTED_MINUTES = "duration-requested-minutes";
    public static final String DURATION_REQUESTED_SECONDS = "duration-requested-seconds";
    public static final String SESSION_TYPE = "session-type";
    public static final String WPM_AVERAGE = "wpm-average";
    public static final String ERROR_RATE = "error-rate";
    public static final String DURATION_REMAINING_MILIS = "duration-remaining-milis";
    public static final String DURATION_REQUESTED_MILIS = "duration-requested-milis";
    private String sessionType;
    private int durationMinutesRequested;
    private int durationSecondsRequested;
    private long durationMilisRemaining;
    private float wpmAverage = -1;
    private float errorRate = -1;
    private CountDownTimer countDownTimer;
    private long durationMilisRequested;
    private Menu menu;

    public abstract void keyboardButtonClicked(View button);

    public enum SessionType {
        LETTER_TRAINING,
        GROUP_TRAINING
    }

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyboard_activity);
        Toolbar keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        keyboardToolbar.inflateMenu(R.menu.keyboard);
        setSupportActionBar(keyboardToolbar);

        Bundle receiveBundle = getIntent().getExtras();
        assert receiveBundle != null;
        sessionType = receiveBundle.getString(SESSION_TYPE);
        durationMinutesRequested = receiveBundle.getInt(DURATION_REQUESTED_MINUTES, 0);
        durationSecondsRequested = receiveBundle.getInt(DURATION_REQUESTED_SECONDS, 0);
        durationMilisRequested = 1000 * (durationMinutesRequested * 60 + durationSecondsRequested);
        isPlaying = true;
        countDownTimer = buildCountDownTimer(1000 * (durationMinutesRequested * 60 + durationSecondsRequested + 1));
        countDownTimer.start();

    }

    private CountDownTimer buildCountDownTimer(long durationsMillis) {
        TextView timeRemainingView = findViewById(R.id.toolbar_title_time_remaining);
        return new CountDownTimer(durationsMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                long secondsUntilFinished = millisUntilFinished / 1000;
                long minutesRemaining = secondsUntilFinished / 60;
                long secondsRemaining = secondsUntilFinished % 60;
                timeRemainingView.setText(String.format(Locale.ENGLISH, "%02d:%02d remaining", minutesRemaining, secondsRemaining));
                durationMilisRemaining = millisUntilFinished;
            }

            public void onFinish() {
                durationMilisRemaining = 0;
                Intent data = buildResultIntent();
                setResult(Activity.RESULT_OK, data);
                finish();
            }
        };
    }

    private Intent buildResultIntent() {
        Intent intent = new Intent();
        Bundle sendBundle = new Bundle();
        sendBundle.putString(KeyboardSessionActivity.SESSION_TYPE, sessionType);
        sendBundle.putLong(KeyboardSessionActivity.DURATION_REMAINING_MILIS, durationMilisRemaining);
        sendBundle.putLong(KeyboardSessionActivity.DURATION_REQUESTED_MILIS, durationMilisRequested);
        sendBundle.putFloat(KeyboardSessionActivity.WPM_AVERAGE, wpmAverage);
        sendBundle.putFloat(KeyboardSessionActivity.WPM_AVERAGE, errorRate);
        intent.putExtras(sendBundle);
        return intent;
    }

    @Override
    public void onBackPressed() {
        new CWToneManager().playSound();
        if (durationMilisRemaining != 0) {
            countDownTimer.pause();
            isPlaying = false;
            MenuItem playPauseIcon = menu.findItem(R.id.keyboard_pause_play);
            playPauseIcon.setIcon(R.mipmap.ic_play);

            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setCancelable(false);
            builder.setMessage("Do you want to end this session?");
            builder.setPositiveButton("Yes", (dialog, which) -> {
                durationMilisRemaining -= 1000; // Duration always seems to be off by -1s when back is pressed
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

    public void onClickPlayPauseHandler(MenuItem m) {
        if (isPlaying) {
            // Pause
            m.setIcon(R.mipmap.ic_play);
            countDownTimer.pause();
            pauseSession();
        } else {
            m.setIcon(R.mipmap.ic_pause);
            countDownTimer.resume();
            resumeSession();
        }
        isPlaying = !isPlaying;
    }

    protected abstract void resumeSession();

    protected abstract void pauseSession();
}
