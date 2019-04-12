package com.uberj.pocketmorsepro.simplesocratic;

import com.google.common.collect.Lists;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticEngineEvent;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticTrainingSessionWithEvents;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class SocraticUtilTest {
    @Test
    public void playpausetest() {
        SocraticTrainingSessionWithEvents session = new SocraticTrainingSessionWithEvents();
        List<SocraticEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(0, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(1, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(2, SocraticEngineEvent.paused()));
        events.add(eventAt(10, SocraticEngineEvent.resumed()));
        events.add(eventAt(12, SocraticEngineEvent.paused()));
        events.add(eventAt(20, SocraticEngineEvent.resumed()));
        events.add(eventAt(21, SocraticEngineEvent.correctGuess("A")));
        session.events = events;
        List<SocraticUtil.SymbolAnalysis> symbolAnalyses = SocraticUtil.buildIndividualSymbolAnalysis(session);
        SocraticUtil.SymbolAnalysis sa = symbolAnalyses.get(0);
        Assert.assertNotNull(sa.averageSecondsBeforeCorrectGuessSeconds);
        Assert.assertEquals(0.004, sa.averageSecondsBeforeCorrectGuessSeconds, 0);
    }

    @Test
    public void segregationTestRaw1() {
        List<SocraticEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(0, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(1, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(2, SocraticEngineEvent.incorrectGuess("B")));
        events.add(eventAt(3, SocraticEngineEvent.correctGuess("A")));
        events.add(eventAt(4, SocraticEngineEvent.letterDonePlaying("A")));
        Map<String, List<List<SocraticEngineEvent>>> segmentMap = SocraticUtil.parseSegments(events);
        Assert.assertTrue(segmentMap.containsKey("A"));
        Assert.assertFalse(segmentMap.containsKey("B"));
        List<List<SocraticEngineEvent>> symbolASegments = segmentMap.get("A");
        Assert.assertEquals(1, symbolASegments.size());
        Assert.assertEquals(5, symbolASegments.get(0).size());
    }

    @Test
    public void segregationTest1() {
        SocraticTrainingSessionWithEvents session = new SocraticTrainingSessionWithEvents();
        List<SocraticEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(0, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(1, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(2, SocraticEngineEvent.incorrectGuess("B")));
        events.add(eventAt(3, SocraticEngineEvent.correctGuess("A")));
        events.add(eventAt(4, SocraticEngineEvent.letterDonePlaying("A")));
        session.events = events;
        List<SocraticUtil.SymbolAnalysis> symbolAnalyses = SocraticUtil.buildIndividualSymbolAnalysis(session);
        Assert.assertEquals(1, symbolAnalyses.size());
        SocraticUtil.SymbolAnalysis sa = symbolAnalyses.get(0);
        Assert.assertEquals("A", sa.symbol);
        Assert.assertEquals(0.002, sa.averageSecondsBeforeCorrectGuessSeconds, 0);
        Assert.assertEquals(1, sa.incorrectGuessesBeforeCorrectGuess, 0);
        Assert.assertEquals(1, sa.numberPlays);
        Assert.assertEquals(1, sa.averagePlaysBeforeCorrectGuess, 0);
        Assert.assertNotNull(sa.accuracy);
        Assert.assertEquals(0, sa.accuracy, 0);
        Assert.assertEquals(Lists.newArrayList("B"), sa.topFiveIncorrectGuesses);
    }

    @Test
    public void segregationTest2() {
        SocraticTrainingSessionWithEvents session = new SocraticTrainingSessionWithEvents();
        List<SocraticEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(0, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(1, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(2, SocraticEngineEvent.incorrectGuess("B")));
        events.add(eventAt(3, SocraticEngineEvent.correctGuess("A")));
        events.add(eventAt(4, SocraticEngineEvent.letterDonePlaying("A")));

        events.add(eventAt(5, SocraticEngineEvent.letterChosen("C")));
        events.add(eventAt(6, SocraticEngineEvent.letterDonePlaying("C")));
        events.add(eventAt(7, SocraticEngineEvent.incorrectGuess("A")));
        events.add(eventAt(8, SocraticEngineEvent.correctGuess("C")));
        events.add(eventAt(9, SocraticEngineEvent.letterDonePlaying("C")));

        events.add(eventAt(10, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(11, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(12, SocraticEngineEvent.incorrectGuess("C")));
        events.add(eventAt(13, SocraticEngineEvent.correctGuess("A")));
        events.add(eventAt(14, SocraticEngineEvent.letterDonePlaying("A")));

        session.events = events;
        List<SocraticUtil.SymbolAnalysis> symbolAnalyses = SocraticUtil.buildIndividualSymbolAnalysis(session);
        Assert.assertEquals(2, symbolAnalyses.size());
        SocraticUtil.SymbolAnalysis sa = symbolAnalyses.get(0);
        Assert.assertEquals("A", sa.symbol);
        Assert.assertEquals(0.002, sa.averageSecondsBeforeCorrectGuessSeconds, 0);
        Assert.assertEquals(1, sa.incorrectGuessesBeforeCorrectGuess, 0);
        Assert.assertEquals(2, sa.numberPlays);
        Assert.assertEquals(1, sa.averagePlaysBeforeCorrectGuess, 0);
        Assert.assertNotNull(sa.accuracy);
        Assert.assertEquals(0, sa.accuracy, 0);
        Assert.assertEquals(Lists.newArrayList("B", "C"), sa.topFiveIncorrectGuesses);
    }

    @Test
    public void segregationTest3() {
        SocraticTrainingSessionWithEvents session = new SocraticTrainingSessionWithEvents();
        List<SocraticEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(0, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(1, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(2, SocraticEngineEvent.incorrectGuess("B")));
        events.add(eventAt(3, SocraticEngineEvent.correctGuess("A")));
        events.add(eventAt(4, SocraticEngineEvent.letterDonePlaying("A")));

        events.add(eventAt(5, SocraticEngineEvent.letterChosen("C")));
        events.add(eventAt(6, SocraticEngineEvent.letterDonePlaying("C")));
        events.add(eventAt(7, SocraticEngineEvent.incorrectGuess("A")));
        events.add(eventAt(8, SocraticEngineEvent.letterDonePlaying("C")));
        events.add(eventAt(9, SocraticEngineEvent.correctGuess("C")));

        events.add(eventAt(10, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(11, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(12, SocraticEngineEvent.incorrectGuess("C")));
        events.add(eventAt(13, SocraticEngineEvent.incorrectGuess("D")));
        events.add(eventAt(14, SocraticEngineEvent.incorrectGuess("F")));
        events.add(eventAt(19, SocraticEngineEvent.correctGuess("A")));
        events.add(eventAt(20, SocraticEngineEvent.letterDonePlaying("A")));

        session.events = events;
        List<SocraticUtil.SymbolAnalysis> symbolAnalyses = SocraticUtil.buildIndividualSymbolAnalysis(session);
        Assert.assertEquals(2, symbolAnalyses.size());
        SocraticUtil.SymbolAnalysis symbolA = getSymbol("A", symbolAnalyses);
        Assert.assertEquals("A", symbolA.symbol);
        Assert.assertEquals((1D + 3D)/2D, symbolA.incorrectGuessesBeforeCorrectGuess, 0);
        Assert.assertEquals(((2D + 8D)/2D)/1000, symbolA.averageSecondsBeforeCorrectGuessSeconds, 0.00001);
        Assert.assertEquals(2, symbolA.numberPlays, 0);
        Assert.assertEquals(1, symbolA.averagePlaysBeforeCorrectGuess, 0);
        Assert.assertNotNull(symbolA.accuracy);
        Assert.assertEquals(0, symbolA.accuracy, 0);
        Assert.assertEquals(Lists.newArrayList("B", "C", "D", "F"), symbolA.topFiveIncorrectGuesses);

        SocraticUtil.SymbolAnalysis symbolC = getSymbol("C", symbolAnalyses);
        Assert.assertEquals("C", symbolC.symbol);
        Assert.assertEquals(1, symbolC.incorrectGuessesBeforeCorrectGuess.intValue());
        Assert.assertEquals(0.003F, symbolC.averageSecondsBeforeCorrectGuessSeconds, 0.00001);
        Assert.assertEquals(2, symbolC.numberPlays);
        Assert.assertEquals(2, symbolC.averagePlaysBeforeCorrectGuess, 0);
    }

    @Test
    public void segregationTest4() {
        SocraticTrainingSessionWithEvents session = new SocraticTrainingSessionWithEvents();
        List<SocraticEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(0, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(1, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(2, SocraticEngineEvent.incorrectGuess("B")));
        events.add(eventAt(3, SocraticEngineEvent.correctGuess("A")));
        events.add(eventAt(4, SocraticEngineEvent.letterDonePlaying("A")));

        events.add(eventAt(5, SocraticEngineEvent.letterChosen("C")));
        events.add(eventAt(6, SocraticEngineEvent.letterDonePlaying("C")));
        events.add(eventAt(7, SocraticEngineEvent.incorrectGuess("A")));
        events.add(eventAt(8, SocraticEngineEvent.letterDonePlaying("C")));
        events.add(eventAt(9, SocraticEngineEvent.correctGuess("C")));

        events.add(eventAt(10, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(11, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(12, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(13, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(14, SocraticEngineEvent.incorrectGuess("F")));
        events.add(eventAt(19, SocraticEngineEvent.correctGuess("A")));
        events.add(eventAt(20, SocraticEngineEvent.letterDonePlaying("A")));

        session.events = events;
        List<SocraticUtil.SymbolAnalysis> symbolAnalyses = SocraticUtil.buildIndividualSymbolAnalysis(session);
        SocraticUtil.SymbolAnalysis symbolC = getSymbol("C", symbolAnalyses);
        Assert.assertEquals((1D + 3D) / 2D, symbolC.averagePlaysBeforeCorrectGuess, 0);
    }

    @Test
    public void segregationTest5() {
        SocraticTrainingSessionWithEvents session = new SocraticTrainingSessionWithEvents();
        List<SocraticEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(0, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(1, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(2, SocraticEngineEvent.incorrectGuess("B")));
        events.add(eventAt(3, SocraticEngineEvent.correctGuess("A")));
        events.add(eventAt(4, SocraticEngineEvent.letterDonePlaying("A")));

        events.add(eventAt(5, SocraticEngineEvent.letterChosen("C")));
        events.add(eventAt(6, SocraticEngineEvent.letterDonePlaying("C")));
        events.add(eventAt(7, SocraticEngineEvent.incorrectGuess("A")));
        events.add(eventAt(8, SocraticEngineEvent.letterDonePlaying("C")));
        events.add(eventAt(9, SocraticEngineEvent.correctGuess("C")));

        events.add(eventAt(10, SocraticEngineEvent.letterChosen("J")));
        events.add(eventAt(11, SocraticEngineEvent.letterDonePlaying("J")));

        // 6 U
        events.add(eventAt(12, SocraticEngineEvent.incorrectGuess("U")));
        events.add(eventAt(13, SocraticEngineEvent.incorrectGuess("U")));
        events.add(eventAt(14, SocraticEngineEvent.incorrectGuess("U")));
        events.add(eventAt(14, SocraticEngineEvent.incorrectGuess("U")));
        events.add(eventAt(14, SocraticEngineEvent.incorrectGuess("U")));
        events.add(eventAt(15, SocraticEngineEvent.incorrectGuess("U")));

        // 5 I
        events.add(eventAt(16, SocraticEngineEvent.incorrectGuess("I")));
        events.add(eventAt(17, SocraticEngineEvent.incorrectGuess("I")));
        events.add(eventAt(17, SocraticEngineEvent.incorrectGuess("I")));
        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("I")));
        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("I")));

        // 4 P
        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("P")));
        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("P")));
        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("P")));
        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("P")));

        // 3 K
        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("K")));
        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("K")));
        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("K")));

        // 2 T
        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("T")));
        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("T")));

        events.add(eventAt(18, SocraticEngineEvent.incorrectGuess("O")));

        events.add(eventAt(20, SocraticEngineEvent.correctGuess("A")));
        events.add(eventAt(21, SocraticEngineEvent.letterDonePlaying("J")));

        session.events = events;
        List<SocraticUtil.SymbolAnalysis> symbolAnalyses = SocraticUtil.buildIndividualSymbolAnalysis(session);
        SocraticUtil.SymbolAnalysis symbolJ = getSymbol("J", symbolAnalyses);
        Assert.assertEquals(5, symbolJ.topFiveIncorrectGuesses.size());
        Assert.assertEquals(Lists.newArrayList("U", "I", "P", "K", "T"), symbolJ.topFiveIncorrectGuesses);
    }

    @Test
    public void segregationTest6() {
        SocraticTrainingSessionWithEvents session = new SocraticTrainingSessionWithEvents();
        List<SocraticEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(0, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(1, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(3, SocraticEngineEvent.correctGuess("A")));

        events.add(eventAt(5, SocraticEngineEvent.letterChosen("C")));
        events.add(eventAt(6, SocraticEngineEvent.letterDonePlaying("C")));
        events.add(eventAt(9, SocraticEngineEvent.correctGuess("C")));

        events.add(eventAt(10, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(11, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(12, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(13, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(19, SocraticEngineEvent.correctGuess("A")));

        events.add(eventAt(20, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(21, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(22, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(23, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(29, SocraticEngineEvent.incorrectGuess("A")));
        events.add(eventAt(30, SocraticEngineEvent.destroyed()));

        session.events = events;
        List<SocraticUtil.SymbolAnalysis> symbolAnalyses = SocraticUtil.buildIndividualSymbolAnalysis(session);
        SocraticUtil.SymbolAnalysis symbolC = getSymbol("C", symbolAnalyses);
        Assert.assertNotNull(symbolC.accuracy);
        Assert.assertEquals(1, symbolC.accuracy, 0);

        SocraticUtil.SymbolAnalysis symbolA = getSymbol("A", symbolAnalyses);
        Assert.assertNotNull(symbolA.accuracy);
        Assert.assertEquals(2D/3D, symbolA.accuracy, 0.000000001);
    }

    @Test
    public void segregationTest7() {
        SocraticTrainingSessionWithEvents session = new SocraticTrainingSessionWithEvents();
        List<SocraticEngineEvent> events = Lists.newArrayList();
        events.add(eventAt(0, SocraticEngineEvent.letterChosen("A")));
        events.add(eventAt(1, SocraticEngineEvent.letterDonePlaying("A")));
        events.add(eventAt(2, SocraticEngineEvent.destroyed()));
        session.events = events;
        List<SocraticUtil.SymbolAnalysis> sas = SocraticUtil.buildIndividualSymbolAnalysis(session);
        Assert.assertEquals(1, sas.size());
        SocraticUtil.SymbolAnalysis sa = sas.get(0);
        Assert.assertEquals(0, sa.chances);

        Assert.assertEquals("A", sa.symbol);
        Assert.assertEquals(0, sa.numberPlays);
        Assert.assertNull(sa.averagePlaysBeforeCorrectGuess);
        Assert.assertNull(sa.incorrectGuessesBeforeCorrectGuess);
        Assert.assertNull(sa.averageSecondsBeforeCorrectGuessSeconds);
        Assert.assertTrue(sa.topFiveIncorrectGuesses.isEmpty());
        Assert.assertNull(sa.accuracy);
        Assert.assertEquals(0, sa.chances);
        Assert.assertEquals(0, sa.hits);
    }


    private SocraticUtil.SymbolAnalysis getSymbol(String a, List<SocraticUtil.SymbolAnalysis> symbolAnalyses) {
        for (SocraticUtil.SymbolAnalysis sa : symbolAnalyses) {
            if (sa.symbol.equals(a)) {
                return sa;
            }
        }

        throw new RuntimeException("Couldn't find symbol analysis for symbol "+ a);
    }

    private SocraticEngineEvent eventAt(long i, SocraticEngineEvent a) {
        a.eventAtEpoc = i;
        return a;
    }


}