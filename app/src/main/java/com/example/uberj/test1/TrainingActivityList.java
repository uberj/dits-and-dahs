package com.example.uberj.test1;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;


public class TrainingActivityList extends Activity {
    private RecyclerView mRecyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_training_list);
        mRecyclerView = (RecyclerView) findViewById(R.id.my_recycler_view);

        // use this setting to improve performance if you know that changes
        // in content do not change the layout size of the RecyclerView
        mRecyclerView.setHasFixedSize(true);

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // specify an adapter (see also next example)
        ArrayList<TrainingCardData> trainingActivities = new ArrayList();
        trainingActivities.add(new TrainingCardData("Letter Groups", "na", new Intent(this, KeyboardActivity.class)));
        trainingActivities.add(new TrainingCardData("Letter Training", "na", new Intent(this, MainActivity.class)));
        mAdapter = new TrainingActivityAdapter(trainingActivities);
        mRecyclerView.setAdapter(mAdapter);
    }

}
