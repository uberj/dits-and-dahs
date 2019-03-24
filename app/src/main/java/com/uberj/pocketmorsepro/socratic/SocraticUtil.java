package com.uberj.pocketmorsepro.socratic;

import com.uberj.pocketmorsepro.CWToneManager;
import com.uberj.pocketmorsepro.socratic.storage.SocraticEngineEvent;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSessionWithEvents;

import java.util.List;

class SocraticUtil {
    public static Analysis analyseSession(SocraticTrainingSessionWithEvents session) {
        List<SocraticEngineEvent> events = session.events;
        int numLettersPlayed = countNumberOfLettersChosenAndPlayed(events);
        int numCorrect = countNumberOfCorrectGuesses(events);

        Analysis analysis = new Analysis();
        analysis.overAllAccuracy = (double) numCorrect / (double) numLettersPlayed;
        analysis.wpmAverage = calcWpmAverage(session, numCorrect);

        return analysis;
    }

    private static int countNumberOfCorrectGuesses(List<SocraticEngineEvent> events) {
        int count = 0;
        for (SocraticEngineEvent event : events) {
            if (event.eventType.equals(SocraticEngineEvent.EventType.CORRECT_GUESS)) {
                count += 1;
            }
        }
        return count;
    }

    private static int countNumberOfLettersChosenAndPlayed(List<SocraticEngineEvent> events) {
        // Should make this smarter. but I'm feeling dumb
        int count = 0;
        for (SocraticEngineEvent event : events) {
            if (event.eventType.equals(SocraticEngineEvent.EventType.LETTER_CHOSEN)) {
                count += 1;
            }
        }
        return count;
    }

    private static int calcTotalAccurateSymbolsGuessed(List<SocraticEngineEvent> events) {
        int totalCorrectSymbols = 0;
        for (SocraticEngineEvent event : events) {
            if (event.eventType.equals(SocraticEngineEvent.EventType.CORRECT_GUESS)) {
                totalCorrectSymbols += CWToneManager.numSymbolsForStringNoFarnsworth(event.info);
            }
        }
        return totalCorrectSymbols;
    }

    private static double calcWpmAverage(SocraticTrainingSessionWithEvents s, int totalCorrectGuesses) {
        int totalAccurateSymbolsGuessed = calcTotalAccurateSymbolsGuessed(s.events);
        int spacesBetweenLetters = (totalCorrectGuesses - 1) * 3;
        // accurateWords = (accurateSymbols / 50)
        double accurateSymbols = (double) (totalAccurateSymbolsGuessed + spacesBetweenLetters);
        double accurateWords = accurateSymbols / 50f;
        // wpmAverage = accurateWords / minutes
        double minutesWorked = (double) (s.session.durationWorkedMillis / 1000) / 60;
        return accurateWords / minutesWorked;
    }

    public static class Analysis {
        public double wpmAverage;
        public double overAllAccuracy;
    }
}
