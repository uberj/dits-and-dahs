package com.uberj.pocketmorsepro.simplesocratic.storage;

import java.util.List;

import androidx.room.Embedded;
import androidx.room.Relation;

public class SocraticTrainingSessionWithEvents {
    @Embedded
    public SocraticTrainingSession session;

    @Relation(parentColumn = "uid", entityColumn = "sessionId", entity = SocraticEngineEvent.class)
    public List<SocraticEngineEvent> events;
}
