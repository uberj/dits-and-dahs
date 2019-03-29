package com.uberj.pocketmorsepro.socratic;

import com.crashlytics.android.Crashlytics;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uberj.pocketmorsepro.CWToneManager;
import com.uberj.pocketmorsepro.socratic.storage.SocraticEngineEvent;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSessionWithEvents;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SocraticUtil {
    public static Analysis analyseSession(SocraticTrainingSessionWithEvents session) {
        List<SocraticEngineEvent> events = session.events;
        int numLettersPlayed = countNumberOfLettersChosenAndPlayed(events);
        int numCorrect = countNumberOfCorrectGuesses(events);

        Analysis analysis = new Analysis();
        analysis.overAllAccuracy = (double) numCorrect / (double) numLettersPlayed;
        analysis.wpmAverage = calcWpmAverage(session, numCorrect);
        analysis.symbolAnalysis = buildIndividualSymbolAnalysis(session);

        return analysis;
    }

    public static List<SymbolAnalysis> buildIndividualSymbolAnalysis(SocraticTrainingSessionWithEvents session) {
        List<SymbolAnalysis> l = Lists.newArrayList();
        Map<String, List<List<SocraticEngineEvent>>> allSegments = parseSegments(session.events);
        for (Map.Entry<String, List<List<SocraticEngineEvent>>> entry : allSegments.entrySet()) {
            String symbol = entry.getKey();
            SymbolAnalysis sa = new SymbolAnalysis();
            sa.symbol = symbol;
            List<List<SocraticEngineEvent>> segments = entry.getValue();
            sa.incorrectGuessesBeforeCorrectGuess = calcICGBCG(segments);
            sa.averageTimeBeforeCorrectGuessSeconds = calcATBCG(segments);
            sa.numberPlays = calcNumPlays(segments);
            sa.averagePlaysBeforeCorrectGuess = calcAPBCG(segments);
            sa.topFiveIncorrectGuesses = calcTopFive(segments);
            l.add(sa);
        }

        return l;
    }

    private static List<String> calcTopFive(List<List<SocraticEngineEvent>> segments) {
        Map<String, Integer> misCounter = Maps.newHashMap();
        for (List<SocraticEngineEvent> events : segments) {
            SocraticEngineEvent firstDonePlaying = findFirst(SocraticEngineEvent.EventType.DONE_PLAYING, events);
            SocraticEngineEvent firstCorrectGuess = findFirst(SocraticEngineEvent.EventType.CORRECT_GUESS, events);
            if (firstCorrectGuess == null || firstDonePlaying == null) {
                continue;
            }
            List<SocraticEngineEvent> inScopeEvents = events.stream().filter(event -> event.eventAtEpoc >= firstDonePlaying.eventAtEpoc && event.eventAtEpoc < firstCorrectGuess.eventAtEpoc)
                    .collect(Collectors.toList());
            for (SocraticEngineEvent event : inScopeEvents) {
                if (event.eventType == SocraticEngineEvent.EventType.INCORRECT_GUESS) {
                    misCounter.putIfAbsent(event.info, 0);
                    misCounter.compute(event.info, (l, cur) -> cur + 1);
                }
            }
        }

        List<String> topMisses = misCounter.entrySet()
                .stream()
                .sorted(Collections.reverseOrder(Map.Entry.comparingByValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        if (topMisses.size() <= 5) {
            return topMisses;
        }
        return topMisses.subList(0, 5);
    }

    private static double calcAPBCG(List<List<SocraticEngineEvent>> segments) {
        return segments.stream().map(events -> {
            if (events.isEmpty()) {
                return 0L;
            }
            SocraticEngineEvent firstDonePlaying = findFirst(SocraticEngineEvent.EventType.DONE_PLAYING, events);
            SocraticEngineEvent firstCorrectGuess = findFirst(SocraticEngineEvent.EventType.CORRECT_GUESS, events);
            if (firstCorrectGuess == null || firstDonePlaying == null) {
                return 0L;
            }
            List<SocraticEngineEvent> inScopeEvents = events.stream().filter(event -> event.eventAtEpoc >= firstDonePlaying.eventAtEpoc && event.eventAtEpoc < firstCorrectGuess.eventAtEpoc)
                    .collect(Collectors.toList());
            long count = 0L;

            for (SocraticEngineEvent event : inScopeEvents) {
                if (event.eventType == SocraticEngineEvent.EventType.DONE_PLAYING) {
                    count += 1;
                }
            }
            return count;
        }).mapToDouble(Double::valueOf).average().orElse(-1D);
    }

    private static int calcNumPlays(List<List<SocraticEngineEvent>> segments) {
        return segments.stream().map(events -> {
            if (events.isEmpty()) {
                return 0L;
            }
            int count = 0;
            for (SocraticEngineEvent event : events) {
                if (event.eventType == SocraticEngineEvent.EventType.CORRECT_GUESS) {
                    break;
                }
                if (event.eventType == SocraticEngineEvent.EventType.DONE_PLAYING) {
                    count += 1;
                }
            }
            return count;
        }).mapToInt(Number::intValue).sum();
    }

    private static double calcATBCG(List<List<SocraticEngineEvent>> segments) {
        return segments.stream().map(events -> {
            if (events.isEmpty()) {
                return 0L;
            }
            SocraticEngineEvent firstDonePlaying = findFirst(SocraticEngineEvent.EventType.DONE_PLAYING, events);
            SocraticEngineEvent firstCorrectGuess = findFirst(SocraticEngineEvent.EventType.CORRECT_GUESS, events);
            if (firstCorrectGuess == null || firstDonePlaying == null) {
                return 0L;
            }
            long l1 = firstCorrectGuess.eventAtEpoc - firstDonePlaying.eventAtEpoc;
            return l1;
        }).mapToDouble(Double::valueOf).average().orElse(-1D);
    }

    private static int calcICGBCG(List<List<SocraticEngineEvent>> segments) {
        return segments.stream().map(
                    events -> events.stream()
                            .filter(e -> e.eventType == SocraticEngineEvent.EventType.INCORRECT_GUESS)
                            .count()
            ).mapToInt(Long::intValue).sum();
    }

    private static SocraticEngineEvent findFirst(SocraticEngineEvent.EventType eventType, List<SocraticEngineEvent> events) {
        for (SocraticEngineEvent event : events) {
            if (event.eventType == eventType) {
                return event;
            }
        }

        return null;
    }

    private static Map<String, List<List<SocraticEngineEvent>>> parseSegments(List<SocraticEngineEvent> events) {
        Map<String, List<List<SocraticEngineEvent>>> allSegments = Maps.newHashMap();
        String currentLetter = null;
        List<SocraticEngineEvent> currentLetterEvents = Lists.newArrayList();
        for (SocraticEngineEvent event : events) {
            if (event.eventType == SocraticEngineEvent.EventType.LETTER_CHOSEN) {
                if (currentLetter != null) {
                    if (!allSegments.containsKey(currentLetter)) {
                        allSegments.put(currentLetter, Lists.newArrayList());
                    }

                    allSegments.get(currentLetter).add(currentLetterEvents);
                }
                currentLetter = event.info;
                currentLetterEvents = Lists.newArrayList();
            } else if (event.eventType == SocraticEngineEvent.EventType.DONE_PLAYING) {
                if (event.info == null || !event.info.equals(currentLetter)) {
                    // TODO, dump json into this error message so we can debug it
                    Crashlytics.log("DONE_PLAYING logged with a different currentLetter");
                    continue;
                }
            }

            currentLetterEvents.add(event);
        }

        if (currentLetter != null) {
            if (!allSegments.containsKey(currentLetter)) {
                allSegments.put(currentLetter, Lists.newArrayList());
            }

            allSegments.get(currentLetter).add(currentLetterEvents);
        }

        return allSegments;
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

    public static class SymbolAnalysis {
        public String symbol;
        public int numberPlays;
        public double averagePlaysBeforeCorrectGuess;
        public int incorrectGuessesBeforeCorrectGuess;
        public double averageTimeBeforeCorrectGuessSeconds;
        public List<String> topFiveIncorrectGuesses;
        public double accuracy;
    }

    public static class Analysis {
        public double wpmAverage;
        public double overAllAccuracy;
        public List<SymbolAnalysis> symbolAnalysis;
    }
}
