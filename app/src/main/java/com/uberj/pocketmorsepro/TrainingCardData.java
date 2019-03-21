package com.uberj.pocketmorsepro;

import android.content.Intent;

class TrainingCardData {
    private final String name;
    private final int whatDescriptionId;
    private final int whyDescriptionId;
    private final Intent onClickIntent;

    public TrainingCardData(String name, int whatDescriptionId, int whyDescriptionId, Intent intent) {
        this.name = name;
        this.whatDescriptionId = whatDescriptionId;
        this.whyDescriptionId = whyDescriptionId;
        this.onClickIntent = intent;
    }

    public String getName() {
        return name;
    }

    public int getWhatDescription() {
        return whatDescriptionId;
    }

    public int getWhyDescription() {
        return whyDescriptionId;
    }

    public Intent getOnClickIntent() {
        return onClickIntent;
    }
}
