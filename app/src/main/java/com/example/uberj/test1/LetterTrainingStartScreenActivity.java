package com.example.uberj.test1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.example.uberj.test1.storage.TrainingSessionType;

import java.util.Locale;
import java.util.Optional;

public class LetterTrainingStartScreenActivity extends AppCompatActivity {

    private static final int KEYBOARD_REQUEST_CODE = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_group_training_start_screen);
        Optional<Bundle> receiveBundle = Optional.ofNullable(getIntent().getExtras());
        receiveBundle.ifPresent(this::setPreviousDetails);

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
            Intent sendBundle = new Intent(getApplicationContext(), LetterTrainingKeyboardSessionActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(KeyboardSessionActivity.DURATION_REQUESTED_SECONDS, secondsPicker.getValue());
            bundle.putInt(KeyboardSessionActivity.DURATION_REQUESTED_MINUTES, minutesPicker.getValue());
            bundle.putString(KeyboardSessionActivity.SESSION_TYPE, TrainingSessionType.LETTER_TRAINING.name());
            sendBundle.putExtras(bundle);
            startActivityForResult(sendBundle, KEYBOARD_REQUEST_CODE);  // NOTE: Ignore request code for now. might become important later
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == KEYBOARD_REQUEST_CODE) {
            Optional<Bundle> receiveBundle = Optional.ofNullable(data.getExtras());
            receiveBundle.ifPresent(this::setPreviousDetails);
        }
    }

    private void setPreviousDetails(Bundle bundle) {
        long prevDurationRemainingMilis = bundle.getLong(KeyboardSessionActivity.DURATION_REMAINING_MILIS, -1);
        long prevDurationRequestedMilis = bundle.getLong(KeyboardSessionActivity.DURATION_REQUESTED_MILIS, -1);
        float wpmAverage = bundle.getFloat(KeyboardSessionActivity.WPM_AVERAGE, -1);
        float errorRate = bundle.getFloat(KeyboardSessionActivity.ERROR_RATE, -1);
        long prevDurationMilis = prevDurationRequestedMilis - prevDurationRemainingMilis;
        long prevDurationMinutes = (prevDurationMilis / 1000) / 60;
        long prevDurationSeconds = (prevDurationMilis / 1000) % 60;
        ((TextView) findViewById(R.id.prev_session_duration_time)).setText(
                prevDurationMinutes >= 0 && prevDurationSeconds >= 0 ?
                        String.format(Locale.ENGLISH, "%02d:%02d", prevDurationMinutes, prevDurationSeconds) :
                        "N/A"
        );
        ((TextView) findViewById(R.id.prev_session_wpm_average)).setText(
                wpmAverage >= 0 ? String.format(Locale.ENGLISH, "%.2f", wpmAverage) : "N/A"
        );
        ((TextView) findViewById(R.id.prev_session_error_rate)).setText(
                errorRate >= 0 ? String.format(Locale.ENGLISH, "%.2f", errorRate) : "N/A"
        );
    }

    public void goToCharacterAnalysis(View view) {
        this.startActivity(new Intent(this, CharacterAnalysis.class));
    }
}
