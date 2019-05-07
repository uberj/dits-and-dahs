package com.uberj.pocketmorsepro;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;

import com.crashlytics.android.Crashlytics;
import com.uberj.pocketmorsepro.training.randomletters.RandomLettersStartScreenActivity;
import com.uberj.pocketmorsepro.training.simplewordflashcards.SimpleWordFlashcardStartScreenActivity;
import com.uberj.pocketmorsepro.training.simple.SimpleStartScreenActivity;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import io.fabric.sdk.android.Fabric;
import timber.log.Timber;

import java.util.ArrayList;


public class TrainingActivityList extends Activity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        } else {
            Timber.plant(new ReleaseTree());
        }
        setContentView(R.layout.activity_training_list);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
        mRecyclerView = findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        ArrayList<TrainingCardData> trainingActivities = new ArrayList<>();
        Intent simpleStartScreen = new Intent(this, SimpleStartScreenActivity.class);
        simpleStartScreen.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        TrainingCardData letter_training = new TrainingCardData(
                "Learn the Characters",
                R.string.letter_training_description_what,
                R.string.letter_training_description_why,
                simpleStartScreen
        );
        trainingActivities.add(letter_training);

        Intent randomLettersStartScreen = new Intent(this, RandomLettersStartScreenActivity.class);
        randomLettersStartScreen.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        TrainingCardData letter_groups = new TrainingCardData(
                "Letter Groups",
                R.string.random_letters_description_what,
                R.string.random_letters_description_why,
                randomLettersStartScreen
        );
        trainingActivities.add(letter_groups);

        Intent simpleWordFlashcard = new Intent(this, SimpleWordFlashcardStartScreenActivity.class);
        simpleWordFlashcard.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        TrainingCardData random_words = new TrainingCardData(
                "Random Words",
                R.string.random_words_description_what,
                R.string.random_words_description_why,
                simpleWordFlashcard
        );
        trainingActivities.add(random_words);

        mAdapter = new TrainingActivityAdapter(trainingActivities);
        mRecyclerView.setAdapter(mAdapter);
    }
}
