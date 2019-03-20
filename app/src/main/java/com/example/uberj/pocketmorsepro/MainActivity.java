package com.example.uberj.morsepocketpro;

import android.content.Intent;
import androidx.appcompat.app.AppCompatActivity;
import timber.log.Timber;

import android.os.Bundle;
import android.widget.Button;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_activity);
        Button buttonTraining = findViewById(R.id.btnTraining);
        buttonTraining.setOnClickListener((view) -> {
            this.startActivity(new Intent(this, TrainingActivityList.class));
        });

        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }
    }
}
