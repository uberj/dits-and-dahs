package com.example.uberj.test1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import java.util.Locale;

public class KeyboardActivity extends AppCompatActivity {
    public static final String DURATION_MINUTES = "duration-minutes";
    public static final String DURATION_SECONDS = "duration-seconds";
    public static final String SESSION_TYPE = "session-type";
    public static final String WPM_AVERAGE = "wpm-average";
    public static final String ERROR_RATE = "error-rate";
    private String sessionType;
    private int durationMinutesRequested;
    private int durationSecondsRequested;
    private long durationMilisRemaining;
    private float wpmAverage = -1;
    private float errorRate = -1;
    private CountDownTimer countDownTimer;
    private long durationMilisRequested;

    public enum SessionType {
        LETTER_TRAINING,
        GROUP_TRAINING
    }

    public boolean isPlaying;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyboard_activity);
        Toolbar keyboardToolbar = findViewById(R.id.keyboard_toolbar);
        keyboardToolbar.inflateMenu(R.menu.keyboard);
        Bundle receiveBundle = getIntent().getExtras();
        sessionType = receiveBundle.getString(SESSION_TYPE);
        durationMinutesRequested = receiveBundle.getInt(DURATION_MINUTES, 0);
        durationSecondsRequested = receiveBundle.getInt(DURATION_SECONDS, 0);
        durationMilisRequested = 1000 * (durationMinutesRequested * 60 + durationSecondsRequested);
        isPlaying = true;
        countDownTimer = buildCountDownTimer(1000 * (durationMinutesRequested * 60 + durationSecondsRequested));
        countDownTimer.start();
    }

    private CountDownTimer buildCountDownTimer(long durationsMillis) {
        TextView timeRemainingView = findViewById(R.id.toolbar_title_time_remaining);
        KeyboardActivity curActivity = this;
        return new CountDownTimer(durationsMillis, 1000) {
            public void onTick(long millisUntilFinished) {
                long secondsUntilFinished = millisUntilFinished / 1000;
                long minutesRemaining = secondsUntilFinished / 60;
                long secondsRemaining = secondsUntilFinished % 60;
                timeRemainingView.setText(String.format(Locale.ENGLISH, "%02d:%02d remaining", minutesRemaining, secondsRemaining));
                durationMilisRemaining = millisUntilFinished;
            }

            public void onFinish() {
                Intent intent = new Intent(curActivity, LetterTrainingStartScreenActivity.class);
                Bundle sendBundle = new Bundle();
                // TODO, send over stats after session is done
                sendBundle.putString(KeyboardActivity.SESSION_TYPE, sessionType);
                sendBundle.putInt(KeyboardActivity.DURATION_MINUTES, durationMinutesRequested);
                sendBundle.putInt(KeyboardActivity.DURATION_SECONDS, durationSecondsRequested);
                sendBundle.putFloat(KeyboardActivity.WPM_AVERAGE, wpmAverage);
                sendBundle.putFloat(KeyboardActivity.WPM_AVERAGE, errorRate);
                intent.putExtras(sendBundle);
                startActivity(intent);
            }
        };
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.keyboard, menu);
        return super.onCreateOptionsMenu(menu);
    }


    public void onClickPlayPauseHandler(MenuItem m) {
        if (isPlaying) {
            m.setIcon(R.mipmap.ic_play);
            countDownTimer.pause();
        } else {
            m.setIcon(R.mipmap.ic_pause);
            countDownTimer.resume();
        }
        isPlaying = !isPlaying;
    }
}
