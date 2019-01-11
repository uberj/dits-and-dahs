package com.example.uberj.test1;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class KeyboardActivity extends AppCompatActivity {
    public static final String DURATION_MINUTES = "duration-minutes";
    public static final String DURATION_SECONDS = "duration-seconds";
    public static final String SESSION_TYPE = "session-type";
    public static final String WPM_AVERAGE = "wpm-average";
    public static final String ERROR_RATE = "error-rate";
    private String sessionType;
    private int durationMinutes;
    private int durationSeconds;
    private float wpmAverage = -1;
    private float errorRate = -1;

    public enum SessionType {
        LETTER_TRAINING,
        GROUP_TRAINING
    }

    public boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.keyboard_activity);
        Toolbar keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        keyboardToolbar.inflateMenu(R.menu.keyboard);
        Bundle receiveBundle = getIntent().getExtras();
        sessionType = receiveBundle.getString(SESSION_TYPE);
        durationMinutes = receiveBundle.getInt(DURATION_MINUTES, 0);
        durationSeconds = receiveBundle.getInt(DURATION_SECONDS, 0);
        isPlaying = true;
        startSessionTimer(durationMinutes * durationSeconds * 1000);
    }

    private void startSessionTimer(int durationsMillis) {
        TextView timeRemainingView = findViewById(R.id.toolbar_title_time_remaining);
        KeyboardActivity curActivity = this;
        new CountDownTimer(durationsMillis, 1000) {

            public void onTick(long millisUntilFinished) {
                long minutesRemaining = (millisUntilFinished / 1000) / 60;
                long secondsRemaining = (millisUntilFinished / 1000) % 60;
                timeRemainingView.setText(String.format("%02d:%02d remaining", minutesRemaining, secondsRemaining) );
            }

            public void onFinish() {
                Intent intent = new Intent(curActivity, LetterTrainingStartScreenActivity.class);
                Bundle sendBundle = new Bundle();
                // TODO, send over stats after session is done
                sendBundle.putString(KeyboardActivity.SESSION_TYPE, sessionType);
                sendBundle.putInt(KeyboardActivity.DURATION_MINUTES, durationMinutes);
                sendBundle.putInt(KeyboardActivity.DURATION_SECONDS, durationSeconds);
                sendBundle.putFloat(KeyboardActivity.WPM_AVERAGE, wpmAverage);
                sendBundle.putFloat(KeyboardActivity.WPM_AVERAGE, errorRate);
                intent.putExtras(sendBundle);
                startActivity(intent);
            }
        }.start();
    }

    private void setPausePlayIcon(MenuItem v) {
        if (isPlaying) {
            v.setIcon(R.mipmap.ic_play);
        } else {
            v.setIcon(R.mipmap.ic_pause);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.keyboard, menu);
        return super.onCreateOptionsMenu(menu);
    }


    public void onClickPlayPauseHandler(MenuItem m) {
        isPlaying = !isPlaying;
        setPausePlayIcon(m);
    }
}
