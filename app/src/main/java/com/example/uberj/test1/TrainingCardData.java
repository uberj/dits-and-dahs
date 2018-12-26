package com.example.uberj.test1;

import android.content.Intent;

class TrainingCardData {
    private final String name;
    private final String description;
    private final Intent onClickIntent;

    public TrainingCardData(String name, String description, Intent intent) {
        this.name = name;
        this.description = description;
        this.onClickIntent = intent;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Intent getOnClickIntent() {
        return onClickIntent;
    }
}
