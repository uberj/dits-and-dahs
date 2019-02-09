package com.example.uberj.test1.LetterTraining;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import it.sephiroth.android.library.numberpicker.NumberPicker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.uberj.test1.CharacterAnalysis;
import com.example.uberj.test1.R;
import com.example.uberj.test1.storage.LetterTrainingSessionDAO;
import com.example.uberj.test1.storage.TheDatabase;

import java.util.Locale;

public class LetterTrainingStartScreenActivity extends AppCompatActivity {

    private static final int KEYBOARD_REQUEST_CODE = 0;
    private NumberPicker minutesPicker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_group_training_start_screen);

        minutesPicker = findViewById(R.id.number_picker_minutes);
        minutesPicker.setMaxValue(60);
        minutesPicker.setMinValue(0);

        setPreviousDetails();

        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> {
            Intent sendIntent = new Intent(getApplicationContext(), LetterTrainingKeyboardSessionActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(LetterTrainingKeyboardSessionActivity.WPM_REQUESTED, 20);
            bundle.putInt(LetterTrainingKeyboardSessionActivity.DURATION_REQUESTED_MINUTES, minutesPicker.getProgress());
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
        LetterTrainingSessionDAO letterTrainingSessionDAO = TheDatabase.getDatabase(this).trainingSessionDAO();
        letterTrainingSessionDAO.getLatestSession((latestSession) -> {
            float wpmAverage = latestSession.map(ts -> ts.wpmAverage).orElse(-1f);
            float errorRate = latestSession.map(ts -> ts.errorRate).orElse(-1f);
            long prevDurationRequestedMillis = latestSession.map((ts) -> ts.durationRequestedMillis).orElse(-1l);
            long prevDurationMillis = latestSession.map((ts) -> ts.durationWorkedMillis).orElse(-1l);
            long prevDurationMinutes = (prevDurationMillis / 1000) / 60;
            long prevDurationSeconds = (prevDurationMillis / 1000) % 60;
            if (prevDurationRequestedMillis >= 0) {
                long prevDurationRequestedMinutes = (prevDurationRequestedMillis / 1000) / 60;
                minutesPicker.setProgress((int) prevDurationRequestedMinutes);
            } else {
                minutesPicker.setProgress(1);
            }

            /*
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
            */
        });
    }

    public void goToCharacterAnalysis(View view) {
        this.startActivity(new Intent(this, CharacterAnalysis.class));
    }
}
