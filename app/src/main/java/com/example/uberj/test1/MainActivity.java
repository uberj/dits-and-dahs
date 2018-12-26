package com.example.uberj.test1;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.uberj.test1.ui.mainscreen.MainScreenFragment;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.container, MainScreenFragment.newInstance())
                    .commitNow();
        }

        Button buttonTraining = findViewById(R.id.btnTraining);
        buttonTraining.setOnClickListener((view) -> {
            this.startActivity(new Intent(this, TrainingActivityList.class));
        });
    }
}
