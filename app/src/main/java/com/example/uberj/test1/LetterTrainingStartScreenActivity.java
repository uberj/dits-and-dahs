package com.example.uberj.test1;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.NumberPicker;

public class LetterTrainingStartScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_letter_group_training_start_screen);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        NumberPicker minutesPicker = findViewById(R.id.number_picker_minutes);
        minutesPicker.setMaxValue(60);
        minutesPicker.setMinValue(0);
        minutesPicker.setValue(1);
        minutesPicker.setFormatter(i -> String.format("%02d", i));

        NumberPicker secondsPicker = findViewById(R.id.number_picker_seconds);
        secondsPicker.setMaxValue(60);
        secondsPicker.setMinValue(0);
        secondsPicker.setValue(0);
        secondsPicker.setFormatter(i -> String.format("%02d", i));

        Button startButton = findViewById(R.id.start_button);
        startButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), KeyboardActivity.class);
            Bundle bundle = new Bundle();
            bundle.putInt(KeyboardActivity.DURATION_SECONDS, secondsPicker.getValue());
            bundle.putInt(KeyboardActivity.DURATION_MINUTES, minutesPicker.getValue());
            bundle.putString(KeyboardActivity.SESSION_TYPE, KeyboardActivity.SessionType.LETTER_TRAINING.name());
            startActivity(intent);
        });



    }

    public void goToCharacterAnalysis(View view) {
        this.startActivity(new Intent(this, CharacterAnalysis.class));
    }
}
