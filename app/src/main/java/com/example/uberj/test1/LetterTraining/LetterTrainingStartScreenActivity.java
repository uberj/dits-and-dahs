package com.example.uberj.test1.LetterTraining;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.example.uberj.test1.CharacterAnalysis;
import com.example.uberj.test1.KeyboardSessionActivity;
import com.example.uberj.test1.R;
import com.example.uberj.test1.storage.TheDatabase;
import com.example.uberj.test1.storage.TrainingSessionDAO;
import com.example.uberj.test1.storage.TrainingSessionType;

import java.util.Locale;

public class LetterTrainingStartScreenActivity extends AppCompatActivity {

    private static final int KEYBOARD_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_group_training_start_screen);
        setPreviousDetails();

        NumberPicker minutesPicker = findViewById(R.id.number_picker_minutes);
        minutesPicker.setMaxValue(60);
        minutesPicker.setMinValue(0);
        minutesPicker.setValue(1);
        minutesPicker.setFormatter(i -> String.format("%02d", i));

        NumberPicker secondsPicker = findViewById(R.id.number_picker_seconds);
        secondsPicker.setMaxValue(59);
        secondsPicker.setMinValue(0);
        secondsPicker.setValue(0);
        secondsPicker.setFormatter(i -> String.format(Locale.ENGLISH, "%02d", i));

        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> {
            Intent sendIntent = new Intent(getApplicationContext(), LetterTrainingKeyboardSessionActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(KeyboardSessionActivity.WPM_REQUESTED, 20);
            bundle.putInt(KeyboardSessionActivity.DURATION_REQUESTED_SECONDS, secondsPicker.getValue());
            bundle.putInt(KeyboardSessionActivity.DURATION_REQUESTED_MINUTES, minutesPicker.getValue());
            bundle.putString(KeyboardSessionActivity.SESSION_TYPE, TrainingSessionType.LETTER_TRAINING.name());
            sendIntent.putExtras(bundle);
            startActivityForResult(sendIntent, KEYBOARD_REQUEST_CODE);  // NOTE: Ignore request code for now. might become important later
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == KEYBOARD_REQUEST_CODE) {
            setPreviousDetails();
        }
    }

    private void setPreviousDetails() {
        TrainingSessionDAO trainingSessionDAO = TheDatabase.getDatabase(this).trainingSessionDAO();
        trainingSessionDAO.getLatestSession((latestSession) -> {
            float wpmAverage = latestSession.map(ts -> ts.wpmAverage).orElse(-1f);
            float errorRate = latestSession.map(ts -> ts.errorRate).orElse(-1f);
            long prevDurationMillis = latestSession.map((ts) -> ts.durationWorkedMillis).orElse(-1l);
            long prevDurationMinutes = (prevDurationMillis / 1000) / 60;
            long prevDurationSeconds = (prevDurationMillis / 1000) % 60;
            ((TextView) findViewById(R.id.prev_session_duration_time)).setText(
                    prevDurationMinutes >= 0 && prevDurationSeconds >= 0 ?
                            String.format(Locale.ENGLISH, "%02d:%02d", prevDurationMinutes, prevDurationSeconds) :
                            "N/A"
            );
            ((TextView) findViewById(R.id.prev_session_wpm_average)).setText(
                    wpmAverage >= 0 ? String.format(Locale.ENGLISH, "%.2f", wpmAverage) : "N/A"
            );
            ((TextView) findViewById(R.id.prev_session_error_rate)).setText(
                    errorRate >= 0 ? (int) (100 * errorRate) + "%" : "N/A"
            );
        });
    }

    public void goToCharacterAnalysis(View view) {
        this.startActivity(new Intent(this, CharacterAnalysis.class));
    }
}
