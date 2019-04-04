package com.uberj.pocketmorsepro.socratic;

import com.crashlytics.android.Crashlytics;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uberj.pocketmorsepro.CWToneManager;
import com.uberj.pocketmorsepro.socratic.storage.SocraticEngineEvent;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSessionWithEvents;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class SocraticUtil {
    public static Analysis analyseSession(SocraticTrainingSessionWithEvents session) {
        Analysis analysis = new Analysis();
        analysis.symbolAnalysis = buildIndividualSymbolAnalysis(session);
        double overallAccuracy = 0;
        int totalCorrectGuesses = 0;
        double totalInPlay = 0;
        for (SymbolAnalysis sa : analysis.symbolAnalysis) {
            if (sa.accuracy == null) {
                continue;
            }
            totalInPlay += 1;
            overallAccuracy += sa.accuracy;
            totalCorrectGuesses += sa.hits;
        }

        analysis.overAllAccuracy = overallAccuracy / totalInPlay;
        analysis.wpmAverage = calcWpmAverage(session, totalCorrectGuesses);
        analysis.averageNumberOfIncorrectGuessesBeforeCorrectGuess = calcAverageNumberOfIncorrectGuessesBeforeCorrectGuess(analysis.symbolAnalysis);
        analysis.overallAverageNumberPlaysBeforeCorrectGuess = calcAverageNumberPlaysBeforeCorrectGuess(analysis.symbolAnalysis);
        analysis.overallAverageSecondsBeforeCorrectGuessSeconds = calcOverallAverageSecondsBeforeCorrectGuessSeconds(analysis.symbolAnalysis);

        return analysis;
    }

    private static double calcAverageNumberPlaysBeforeCorrectGuess(List<SymbolAnalysis> symbolAnalysis) {
        return symbolAnalysis.stream()
                .filter(sa -> sa.averagePlaysBeforeCorrectGuess != null)
                .mapToDouble(sa -> sa.averagePlaysBeforeCorrectGuess)
                .average()
                .orElse(-1D);
    }

    private static double calcOverallAverageSecondsBeforeCorrectGuessSeconds(List<SymbolAnalysis> symbolAnalysis) {
        return symbolAnalysis.stream()
                .filter(sa -> sa.averageSecondsBeforeCorrectGuessSeconds != null)
                .mapToDouble(sa -> sa.averageSecondsBeforeCorrectGuessSeconds)
                .average()
                .orElse(-1D);
    }

    private static double calcAverageNumberOfIncorrectGuessesBeforeCorrectGuess(List<SymbolAnalysis> symbolAnalysis) {
        return symbolAnalysis.stream()
                .filter(sa -> sa.incorrectGuessesBeforeCorrectGuess != null)
                .mapToDouble(sa -> sa.incorrectGuessesBeforeCorrectGuess)
                .average()
                .orElse(-1D);
    }

    public static List<SymbolAnalysis> buildIndividualSymbolAnalysis(SocraticTrainingSessionWithEvents session) {
        List<SymbolAnalysis> l = Lists.newArrayList();
        Map<String, List<List<SocraticEngineEvent>>> allSegments = parseSegments(session.events);
        for (Map.Entry<String, List<List<SocraticEngineEvent>>> entry : allSegments.entrySet()) {
            String symbol = entry.getKey();
            SymbolAnalysis sa = new SymbolAnalysis();
            sa.symbol = symbol;
            List<List<SocraticEngineEvent>> segments = entry.getValue();
            sa.incorrectGuessesBeforeCorrectGuess = calcICGBCG(skipIncompleteIfNoUserInput(segments));
            sa.averageSecondsBeforeCorrectGuessSeconds = calcASBCG(skipIncompleteSegments(segments));
            sa.numberPlays = calcNumPlays(skipIncompleteIfNoUserInput(segments));
            sa.averagePlaysBeforeCorrectGuess = calcAPBCG(skipIncompleteSegments(segments));
            sa.topFiveIncorrectGuesses = calcTopFive(segments);
            Pair<Integer, Integer> hitsChances = calcHitsChances(skipIncompleteIfNoUserInput(segments));
            sa.accuracy = calcAccuracy(hitsChances);
            sa.hits = hitsChances.getLeft();
            sa.chances = hitsChances.getRight();
            l.add(sa);
        }

        return l;
    }

    private static List<List<SocraticEngineEvent>> skipIncompleteIfNoUserInput(List<List<SocraticEngineEvent>> segments) {
        List<List<SocraticEngineEvent>> cleanedUpSegments = Lists.newArrayList();
        for (List<SocraticEngineEvent> segment : segments) {
            List<SocraticEngineEvent> cleanedUpEvents = Lists.newArrayList();
            boolean wasDestroyedInSegment = false;
            boolean userInputDetected = false;
            for (SocraticEngineEvent event : segment) {
                // We don't want to make conclusions about a user's performance based on segments that were incomplete
                // For example, if the engine was destroyed before the user had a chance to input a correct guess.
                // This loop goes through each segment and ensures that it gave the user a chance to perform
                cleanedUpEvents.add(event);
                if (event.eventType == SocraticEngineEvent.EventType.CORRECT_GUESS || event.eventType == SocraticEngineEvent.EventType.INCORRECT_GUESS) {
                    // Once the correct letter was chosen, everything after the event is noise
                    userInputDetected = true;
                }

                if (event.eventType == SocraticEngineEvent.EventType.DESTROYED) {
                    wasDestroyedInSegment = true;
                }
            }

            if (!wasDestroyedInSegment || userInputDetected) {
                cleanedUpSegments.add(cleanedUpEvents);
            }
        }

        return cleanedUpSegments;
    }

    private static List<List<SocraticEngineEvent>> skipIncompleteSegments(List<List<SocraticEngineEvent>> segments) {
        List<List<SocraticEngineEvent>> cleanedUpSegments = Lists.newArrayList();
        for (List<SocraticEngineEvent> segment : segments) {
            List<SocraticEngineEvent> cleanedUpEvents = Lists.newArrayList();
            boolean wasDestroyedInSegment = false;
            boolean correctGuessMade = false;
            for (SocraticEngineEvent event : segment) {
                // We don't want to make conclusions about a user's performance based on segments that were incomplete
                // For example, if the engine was destroyed before the user had a chance to input a correct guess.
                // This loop goes through each segment and ensures that it gave the user a chance to perform
                cleanedUpEvents.add(event);
                if (event.eventType == SocraticEngineEvent.EventType.CORRECT_GUESS) {
                    // Once the correct letter was chosen, everything after the event is noise
                    correctGuessMade = true;
                    break;
                }

                if (event.eventType == SocraticEngineEvent.EventType.DESTROYED) {
                    wasDestroyedInSegment = true;
                }
            }

            if (!wasDestroyedInSegment || correctGuessMade) {
                cleanedUpSegments.add(cleanedUpEvents);
            }
        }

        return cleanedUpSegments;
    }

    protected static Map<String, List<List<SocraticEngineEvent>>> parseSegments(List<SocraticEngineEvent> events) {
        Map<String, List<List<SocraticEngineEvent>>> output = Maps.newHashMap();
        String currentLetter = null;
        List<SocraticEngineEvent> currentLetterEvents = Lists.newArrayList();
        // This loop will segment the event stream by chopping where ever there is a LETTER_CHOSEN event
        for (SocraticEngineEvent event : events) {
            if (event.eventType == SocraticEngineEvent.EventType.LETTER_CHOSEN) {
                if (currentLetter != null) {
                    // Store the current letter's segment we have been tracking
                    if (!output.containsKey(currentLetter)) {
                        output.put(currentLetter, Lists.newArrayList());
                    }

                    output.get(currentLetter).add(currentLetterEvents);
                }

                // Set the new current letter
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

        // We got to the end of the loop. We have to store the current letter's latest segment
        if (currentLetter != null) {
            if (!output.containsKey(currentLetter)) {
                output.put(currentLetter, Lists.newArrayList());
            }
            output.get(currentLetter).add(currentLetterEvents);
        }


        return output;
    }

    private static Pair<Integer, Integer> calcHitsChances(List<List<SocraticEngineEvent>> segments) {
        int hitCounter = 0;
        int chanceCounter = 0;
        for (List<SocraticEngineEvent> events : segments) {
            SocraticEngineEvent firstDonePlaying = findFirst(SocraticEngineEvent.EventType.DONE_PLAYING, events);
            if (firstDonePlaying == null) {
                continue;
            }
            // We know it was played at least once
            chanceCounter += 1;
            List<SocraticEngineEvent> inScopeEvents = events.stream().filter(event -> event.eventAtEpoc >= firstDonePlaying.eventAtEpoc)
                    .collect(Collectors.toList());
            for (SocraticEngineEvent event : inScopeEvents) {
                // If we see a miss before we see a correct, its a miss. else, its not a miss;
                if (event.eventType == SocraticEngineEvent.EventType.INCORRECT_GUESS) {
                    break;
                }

                if (event.eventType == SocraticEngineEvent.EventType.CORRECT_GUESS) {
                    hitCounter += 1;
                    break;
                }

                // Don't count an error if the player didn't explicitly commit one
                if (event.eventType == SocraticEngineEvent.EventType.DESTROYED) {
                    chanceCounter -= 1;
                    chanceCounter = Math.max(chanceCounter, 1);
                }
            }
        }
        return Pair.of(hitCounter, chanceCounter);
    }

    private static Double calcAccuracy(Pair<Integer, Integer> hitsChances) {
        Integer hits = hitsChances.getLeft();
        Integer chances = hitsChances.getRight();
        if (chances == 0) {
            return null;
        } else {
            return hits.doubleValue() / chances.doubleValue();
        }
    }

    private static List<String> calcTopFive(List<List<SocraticEngineEvent>> segments) {
        Map<String, Integer> misCounter = Maps.newHashMap();
        for (List<SocraticEngineEvent> events : segments) {
            SocraticEngineEvent firstDonePlaying = findFirst(SocraticEngineEvent.EventType.DONE_PLAYING, events);
            if (firstDonePlaying == null) {
                continue;
            }
            List<SocraticEngineEvent> inScopeEvents = events.stream()
                    .filter(event -> event.eventAtEpoc >= firstDonePlaying.eventAtEpoc)
                    .collect(Collectors.toList());
            for (SocraticEngineEvent event : inScopeEvents) {
                if (event.eventType == SocraticEngineEvent.EventType.INCORRECT_GUESS) {
                    String guess = event.info;
                    if (guess == null) {
                        // TODO, crashlytics
                        continue;
                    }
                    misCounter.putIfAbsent(guess, 0);
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

    private static Double calcAPBCG(List<List<SocraticEngineEvent>> segments) {
        Stream<List<SocraticEngineEvent>> validEvents = segments.stream().map(events -> {
            if (events.isEmpty()) {
                return Lists.newArrayList();
            }
            SocraticEngineEvent firstDonePlaying = findFirst(SocraticEngineEvent.EventType.DONE_PLAYING, events);
            SocraticEngineEvent firstCorrectGuess = findFirst(SocraticEngineEvent.EventType.CORRECT_GUESS, events);
            if (firstCorrectGuess == null || firstDonePlaying == null) {
                return Lists.newArrayList();
            }
            return events.stream()
                    .filter(event -> event.eventAtEpoc >= firstDonePlaying.eventAtEpoc && event.eventAtEpoc < firstCorrectGuess.eventAtEpoc)
                    .collect(Collectors.toList());
        });
        OptionalDouble average = validEvents
                .filter(events -> !events.isEmpty())
                .map((List<SocraticEngineEvent> inScopeEvents) -> {
                    double count = 0D;
                    for (SocraticEngineEvent event : inScopeEvents) {
                        if (event.eventType == SocraticEngineEvent.EventType.DONE_PLAYING) {
                            count += 1D;
                        }
                    }
                    return count;
                })
                .mapToDouble(Double::valueOf)
                .average();

        if (average.isPresent()) {
            return average.getAsDouble();
        } else {
            return null;
        }
    }

    private static double toSeconds(double millis) {
        if (millis > 0) {
            return millis / 1000;
        }

        return millis;
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

    private static Double calcASBCG(List<List<SocraticEngineEvent>> segments) {
        if (segments.isEmpty()) {
            return null;
        }

        return toSeconds(segments.stream().map(events -> {
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
        }).mapToDouble(Double::valueOf).average().orElse(-1D));
    }

    private static Integer calcICGBCG(List<List<SocraticEngineEvent>> segments) {
        if (segments.isEmpty()) {
            return null;
        }

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
        @Nullable
        public Double averagePlaysBeforeCorrectGuess = null;
        @Nullable
        public Integer incorrectGuessesBeforeCorrectGuess;
        @Nullable
        public Double averageSecondsBeforeCorrectGuessSeconds = null;
        public List<String> topFiveIncorrectGuesses;
        @Nullable
        public Double accuracy = null;
        public int chances;
        public int hits;
    }

    public static class Analysis {
        public double wpmAverage;
        public double overAllAccuracy;
        public List<SymbolAnalysis> symbolAnalysis;
        public double averageNumberOfIncorrectGuessesBeforeCorrectGuess;
        public double overallAverageSecondsBeforeCorrectGuessSeconds;
        public double overallAverageNumberPlaysBeforeCorrectGuess;
    }
}
