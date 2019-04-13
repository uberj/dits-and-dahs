package com.uberj.pocketmorsepro.flashcard.storage;


import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

public class FlashcardTrainingSessionWithEvents {
    @Embedded
    public FlashcardTrainingSession session;

    @Relation(parentColumn = "uid", entityColumn = "sessionId", entity = FlashcardEngineEvent.class)
    public List<FlashcardEngineEvent> events;
}
