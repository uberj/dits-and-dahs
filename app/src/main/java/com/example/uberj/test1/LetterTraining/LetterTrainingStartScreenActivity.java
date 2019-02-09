package com.example.uberj.test1.LetterTraining;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.Barrier;
import androidx.constraintlayout.widget.Group;
import androidx.constraintlayout.widget.Guideline;
import it.sephiroth.android.library.numberpicker.NumberPicker;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
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

//        setPreviousDetails();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//        if (resultCode == RESULT_OK && requestCode == KEYBOARD_REQUEST_CODE) {
//            setPreviousDetails();
//        }
    }

    public void goToCharacterAnalysis(View view) {
        this.startActivity(new Intent(this, CharacterAnalysis.class));
    }
}
