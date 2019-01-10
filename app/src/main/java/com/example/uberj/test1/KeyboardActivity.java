package com.example.uberj.test1;

import android.content.pm.ActivityInfo;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

public class KeyboardActivity extends AppCompatActivity {
    public static final String DURATION_MINUTES = "duration-minutes";
    public static final String DURATION_SECONDS = "duration-seconds";
    public static final String SESSION_TYPE = "session-type";

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
        isPlaying = true;
        startSessionTimer()
    }

    private void startSessionTimer() {
        new CountDownTimer(30000, 1000) {

            public void onTick(long millisUntilFinished) {
                mTextField.setText("seconds remaining: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                mTextField.setText("done!");
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
