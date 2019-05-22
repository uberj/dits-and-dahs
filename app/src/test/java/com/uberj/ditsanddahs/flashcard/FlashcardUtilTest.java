package com.uberj.ditsanddahs.flashcard;

import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardEngineEvent;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class FlashcardUtilTest {
    @Test
    public void testFirstGuessAccuracy1() {
        List<FlashcardEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(1, FlashcardEngineEvent.messageChosen("me")));
        events.add(eventAt(2, FlashcardEngineEvent.messageDonePlaying("me")));
        events.add(eventAt(3, FlashcardEngineEvent.correctGuessSubmitted("me")));
        Assert.assertEquals(1, FlashcardUtil.calcFirstGuessAccuracy(events), 0);
    }

    @Test
    public void testFirstGuessAccuracy2() {
        List<FlashcardEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(1, FlashcardEngineEvent.messageChosen("me")));
        events.add(eventAt(2, FlashcardEngineEvent.messageDonePlaying("me")));
        events.add(eventAt(3, FlashcardEngineEvent.incorrectGuessSubmitted("me")));
        Assert.assertEquals(0, FlashcardUtil.calcFirstGuessAccuracy(events), 0);
    }

    @Test
    public void testFirstGuessAccuracy3() {
        List<FlashcardEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(1, FlashcardEngineEvent.messageChosen("me")));
        events.add(eventAt(2, FlashcardEngineEvent.messageDonePlaying("me")));
        events.add(eventAt(3, FlashcardEngineEvent.incorrectGuessSubmitted("me")));
        events.add(eventAt(4, FlashcardEngineEvent.correctGuessSubmitted("me")));
        events.add(eventAt(5, FlashcardEngineEvent.messageChosen("me")));
        events.add(eventAt(4, FlashcardEngineEvent.messageDonePlaying("me")));
        events.add(eventAt(5, FlashcardEngineEvent.correctGuessSubmitted("me")));
        Assert.assertEquals(0.5, FlashcardUtil.calcFirstGuessAccuracy(events), 0);
    }

    private FlashcardEngineEvent eventAt(long i, FlashcardEngineEvent a) {
        a.eventAtEpoc = i;
        return a;
    }
}
