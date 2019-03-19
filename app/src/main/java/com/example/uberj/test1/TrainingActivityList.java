package com.example.uberj.test1;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.example.uberj.test1.training.randomletters.RandomLettersStartScreenActivity;
import com.example.uberj.test1.training.simple.SimpleStartScreenActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;


public class TrainingActivityList extends Activity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_list);
        mRecyclerView = findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        ArrayList<TrainingCardData> trainingActivities = new ArrayList<>();
        TrainingCardData letter_training = new TrainingCardData(
                "Letters",
                R.string.letter_training_description_what,
                R.string.letter_training_description_why,
                new Intent(this, SimpleStartScreenActivity.class)
        );
        trainingActivities.add(letter_training);
        TrainingCardData letter_groups = new TrainingCardData(
                "Letter Groups",
                R.string.random_letters_description_what,
                R.string.random_letters_description_why,
                new Intent(this, RandomLettersStartScreenActivity.class)
        );
        trainingActivities.add(letter_groups);
        mAdapter = new TrainingActivityAdapter(trainingActivities);
        mRecyclerView.setAdapter(mAdapter);
    }

}
