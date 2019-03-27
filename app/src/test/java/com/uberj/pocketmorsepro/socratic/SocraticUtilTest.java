package com.uberj.pocketmorsepro.socratic;

import com.google.common.collect.Lists;
import com.uberj.pocketmorsepro.socratic.storage.SocraticEngineEvent;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSessionWithEvents;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class SocraticUtilTest {
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
        Assert.assertEquals(2, sa.averageTimeBeforeCorrectGuessSeconds, 0);
        Assert.assertEquals(1, sa.incorrectGuessesBeforeCorrectGuess, 0);
        Assert.assertEquals(1, sa.numberPlays);
        Assert.assertEquals(1, sa.averagePlaysBeforeCorrectGuess, 0);
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
        Assert.assertEquals(2, sa.averageTimeBeforeCorrectGuessSeconds, 0);
        Assert.assertEquals(2, sa.incorrectGuessesBeforeCorrectGuess, 0);
        Assert.assertEquals(2, sa.numberPlays);
        Assert.assertEquals(1, sa.averagePlaysBeforeCorrectGuess, 0);
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
        Assert.assertEquals(4, symbolA.incorrectGuessesBeforeCorrectGuess);
        Assert.assertEquals((2D + 8D)/2D, symbolA.averageTimeBeforeCorrectGuessSeconds, 0);
        Assert.assertEquals(2, symbolA.numberPlays, 0);
        Assert.assertEquals(1, symbolA.averagePlaysBeforeCorrectGuess, 0);
        Assert.assertEquals(Lists.newArrayList("B", "C", "D", "F"), symbolA.topFiveIncorrectGuesses);

        SocraticUtil.SymbolAnalysis symbolC = getSymbol("C", symbolAnalyses);
        Assert.assertEquals("C", symbolC.symbol);
        Assert.assertEquals(1, symbolC.incorrectGuessesBeforeCorrectGuess);
        Assert.assertEquals(3F, symbolC.averageTimeBeforeCorrectGuessSeconds, 0);
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