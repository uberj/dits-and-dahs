package com.example.uberj.test1;

import android.content.Intent;

class TrainingCardData {
    private final String name;
    private final int descriptionId;
    private final Intent onClickIntent;

    public TrainingCardData(String name, int descriptionId, Intent intent) {
        this.name = name;
        this.descriptionId = descriptionId;
        this.onClickIntent = intent;
    }

    public String getName() {
        return name;
    }

    public int getDescription() {
        return descriptionId;
    }

    public Intent getOnClickIntent() {
        return onClickIntent;
    }
}
