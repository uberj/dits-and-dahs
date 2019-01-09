package com.example.uberj.test1;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class KeyboardActivity extends AppCompatActivity {
    public static final String DURATION_MINUTES = "duration-minutes";
    public static final String DURATION_SECONDS = "duration-seconds";
    public static final String SESSION_TYPE = "session-type";

    public enum SessionType {
        LETTER_TRAINING,
        GROUP_TRAINING
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.keyboard_activity);
    }
}
