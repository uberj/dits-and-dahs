package com.uberj.pocketmorsepro.flashcard;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uberj.pocketmorsepro.AudioManager;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardEngineEvent;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardTrainingSessionWithEvents;
import com.uberj.pocketmorsepro.keyboards.KeyConfig;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.Nullable;

public class FlashcardUtil {

    protected static List<String> findLatestCardInput(List<String> inputStrings) {
        List<String> latestGuessInput = Lists.newArrayList();
        List<String> reversedStrings = Lists.newArrayList(inputStrings);
        Collections.reverse(reversedStrings);
        for (String outputString : reversedStrings) {
            if (outputString.equals(KeyConfig.ControlType.SKIP.keyName) || outputString.equals(KeyConfig.ControlType.SUBMIT.keyName)) {
                break;
            }
            latestGuessInput.add(0, outputString);
        }
        return latestGuessInput;
    }

    public static String convertKeyPressesToString(List<String> enteredStrings) {
        List<String> stringsToDisplay = Lists.newArrayList();
        List<String> latestCardInput = findLatestCardInput(enteredStrings);
        for (String transcribedString : latestCardInput) {
            Optional<KeyConfig.ControlType> controlType = KeyConfig.ControlType.fromKeyName(transcribedString);
            if (controlType.isPresent()) {
                if (controlType.get().equals(KeyConfig.ControlType.DELETE)) {
                    stringsToDisplay.remove(stringsToDisplay.size() - 1);
                } else if (controlType.get().equals(KeyConfig.ControlType.SPACE)) {
                    stringsToDisplay.add(" ");
                } else if (controlType.get().equals(KeyConfig.ControlType.AGAIN)) {
                } else if (controlType.get().equals(KeyConfig.ControlType.SUBMIT)) {
                } else {
                    throw new RuntimeException("unhandled control eventType " + transcribedString);
                }
            } else {
                stringsToDisplay.add(transcribedString);
            }
        }
        return Joiner.on("").join(stringsToDisplay);
    }

    public static Analysis analyseSession(FlashcardTrainingSessionWithEvents session) {
        Analysis analysis = new Analysis();
        analysis.messageAnalysis = buildIndividualCardAnalysis(session);
        double overallAccuracy = 0;
        int totalCorrectGuesses = 0;
        double totalInPlay = 0;
        for (SymbolAnalysis sa : analysis.messageAnalysis) {
            if (sa.accuracy == null) {
                continue;
            }
            totalInPlay += 1;
            overallAccuracy += sa.accuracy;
            totalCorrectGuesses += sa.hits;
        }

        analysis.overAllAccuracy = overallAccuracy / totalInPlay;
        analysis.wpmAverage = calcWpmAverage(session, totalCorrectGuesses);
        analysis.averageNumberOfIncorrectGuessesBeforeCorrectGuess = calcAverageNumberOfIncorrectGuessesBeforeCorrectGuess(analysis.messageAnalysis);
        analysis.overallAverageNumberPlaysBeforeCorrectGuess = calcAverageNumberPlaysBeforeCorrectGuess(analysis.messageAnalysis);
        analysis.overallAverageSecondsBeforeCorrectGuessSeconds = calcOverallAverageSecondsBeforeCorrectGuessSeconds(analysis.messageAnalysis);

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

    public static List<SymbolAnalysis> buildIndividualCardAnalysis(FlashcardTrainingSessionWithEvents session) {
        List<SymbolAnalysis> l = Lists.newArrayList();
        Map<String, List<List<FlashcardEngineEvent>>> allSegments = parseSegments(session.events);
        for (Map.Entry<String, List<List<FlashcardEngineEvent>>> entry : allSegments.entrySet()) {
            String symbol = entry.getKey();
            SymbolAnalysis sa = new SymbolAnalysis();
            sa.message = symbol;
            List<List<FlashcardEngineEvent>> segments = entry.getValue();
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

    private static List<List<FlashcardEngineEvent>> skipIncompleteIfNoUserInput(List<List<FlashcardEngineEvent>> segments) {
        List<List<FlashcardEngineEvent>> cleanedUpSegments = Lists.newArrayList();
        for (List<FlashcardEngineEvent> segment : segments) {
            List<FlashcardEngineEvent> cleanedUpEvents = Lists.newArrayList();
            boolean wasDestroyedInSegment = false;
            boolean userInputDetected = false;
            for (FlashcardEngineEvent event : segment) {
                // We don't want to make conclusions about a user's performance based on segments that were incomplete
                // For example, if the engine was destroyed before the user had a chance to input a correct guess.
                // This loop goes through each segment and ensures that it gave the user a chance to perform
                cleanedUpEvents.add(event);
                if (event.eventType == FlashcardEngineEvent.EventType.CORRECT_GUESS || event.eventType == FlashcardEngineEvent.EventType.INCORRECT_GUESS) {
                    // Once the correct letter was chosen, everything after the event is noise
                    userInputDetected = true;
                }

                if (event.eventType == FlashcardEngineEvent.EventType.DESTROYED) {
                    wasDestroyedInSegment = true;
                }
            }

            if (!wasDestroyedInSegment || userInputDetected) {
                cleanedUpSegments.add(cleanedUpEvents);
            }
        }

        return cleanedUpSegments;
    }

    private static List<List<FlashcardEngineEvent>> skipIncompleteSegments(List<List<FlashcardEngineEvent>> segments) {
        List<List<FlashcardEngineEvent>> cleanedUpSegments = Lists.newArrayList();
        for (List<FlashcardEngineEvent> segment : segments) {
            List<FlashcardEngineEvent> cleanedUpEvents = Lists.newArrayList();
            boolean wasDestroyedInSegment = false;
            boolean correctGuessMade = false;
            for (FlashcardEngineEvent event : segment) {
                // We don't want to make conclusions about a user's performance based on segments that were incomplete
                // For example, if the engine was destroyed before the user had a chance to input a correct guess.
                // This loop goes through each segment and ensures that it gave the user a chance to perform
                cleanedUpEvents.add(event);
                if (event.eventType == FlashcardEngineEvent.EventType.CORRECT_GUESS) {
                    // Once the correct letter was chosen, everything after the event is noise
                    correctGuessMade = true;
                    break;
                }

                if (event.eventType == FlashcardEngineEvent.EventType.DESTROYED) {
                    wasDestroyedInSegment = true;
                }
            }

            if (!wasDestroyedInSegment || correctGuessMade) {
                cleanedUpSegments.add(cleanedUpEvents);
            }
        }

        return cleanedUpSegments;
    }

    protected static Map<String, List<List<FlashcardEngineEvent>>> parseSegments(List<FlashcardEngineEvent> events) {
        Map<String, List<List<FlashcardEngineEvent>>> output = Maps.newHashMap();
        String currentLetter = null;
        List<FlashcardEngineEvent> currentLetterEvents = Lists.newArrayList();
        // This loop will segment the event stream by chopping where ever there is a MESSAGE_CHOSEN event
        for (FlashcardEngineEvent event : events) {
            if (event.eventType == FlashcardEngineEvent.EventType.MESSAGE_CHOSEN) {
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
            } else if (event.eventType == FlashcardEngineEvent.EventType.DONE_PLAYING) {
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

    private static Pair<Integer, Integer> calcHitsChances(List<List<FlashcardEngineEvent>> segments) {
        int hitCounter = 0;
        int chanceCounter = 0;
        for (List<FlashcardEngineEvent> events : segments) {
            FlashcardEngineEvent firstDonePlaying = findFirst(FlashcardEngineEvent.EventType.DONE_PLAYING, events);
            if (firstDonePlaying == null) {
                continue;
            }
            // We know it was played at least once
            chanceCounter += 1;
            List<FlashcardEngineEvent> inScopeEvents = events.stream().filter(event -> event.eventAtEpoc >= firstDonePlaying.eventAtEpoc)
                    .collect(Collectors.toList());
            for (FlashcardEngineEvent event : inScopeEvents) {
                // If we see a miss before we see a correct, its a miss. else, its not a miss;
                if (event.eventType == FlashcardEngineEvent.EventType.INCORRECT_GUESS) {
                    break;
                }

                if (event.eventType == FlashcardEngineEvent.EventType.CORRECT_GUESS) {
                    hitCounter += 1;
                    break;
                }

                // Don't count an error if the player didn't explicitly commit one
                if (event.eventType == FlashcardEngineEvent.EventType.DESTROYED) {
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

    private static List<String> calcTopFive(List<List<FlashcardEngineEvent>> segments) {
        Map<String, Integer> misCounter = Maps.newHashMap();
        for (List<FlashcardEngineEvent> events : segments) {
            FlashcardEngineEvent firstDonePlaying = findFirst(FlashcardEngineEvent.EventType.DONE_PLAYING, events);
            if (firstDonePlaying == null) {
                continue;
            }
            List<FlashcardEngineEvent> inScopeEvents = events.stream()
                    .filter(event -> event.eventAtEpoc >= firstDonePlaying.eventAtEpoc)
                    .collect(Collectors.toList());
            for (FlashcardEngineEvent event : inScopeEvents) {
                if (event.eventType == FlashcardEngineEvent.EventType.INCORRECT_GUESS) {
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

    private static Double calcAPBCG(List<List<FlashcardEngineEvent>> segments) {
        Stream<List<FlashcardEngineEvent>> validEvents = segments.stream().map(events -> {
            if (events.isEmpty()) {
                return Lists.newArrayList();
            }
            FlashcardEngineEvent firstDonePlaying = findFirst(FlashcardEngineEvent.EventType.DONE_PLAYING, events);
            FlashcardEngineEvent firstCorrectGuess = findFirst(FlashcardEngineEvent.EventType.CORRECT_GUESS, events);
            if (firstCorrectGuess == null || firstDonePlaying == null) {
                return Lists.newArrayList();
            }
            return events.stream()
                    .filter(event -> event.eventAtEpoc >= firstDonePlaying.eventAtEpoc && event.eventAtEpoc < firstCorrectGuess.eventAtEpoc)
                    .collect(Collectors.toList());
        });
        OptionalDouble average = validEvents
                .filter(events -> !events.isEmpty())
                .map((List<FlashcardEngineEvent> inScopeEvents) -> {
                    double count = 0D;
                    for (FlashcardEngineEvent event : inScopeEvents) {
                        if (event.eventType == FlashcardEngineEvent.EventType.DONE_PLAYING) {
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

    private static int calcNumPlays(List<List<FlashcardEngineEvent>> segments) {
        return segments.stream().map(events -> {
            if (events.isEmpty()) {
                return 0L;
            }
            int count = 0;
            for (FlashcardEngineEvent event : events) {
                if (event.eventType == FlashcardEngineEvent.EventType.CORRECT_GUESS) {
                    break;
                }
                if (event.eventType == FlashcardEngineEvent.EventType.DONE_PLAYING) {
                    count += 1;
                }
            }
            return count;
        }).mapToInt(Number::intValue).sum();
    }

    private static Double calcASBCG(List<List<FlashcardEngineEvent>> segments) {
        if (segments.isEmpty()) {
            return null;
        }

        OptionalDouble average = segments.stream().map(events -> {
            if (events.isEmpty()) {
                return 0L;
            }
            FlashcardEngineEvent firstDonePlaying = findFirst(FlashcardEngineEvent.EventType.DONE_PLAYING, events);
            FlashcardEngineEvent firstCorrectGuess = findFirst(FlashcardEngineEvent.EventType.CORRECT_GUESS, events);
            if (firstCorrectGuess == null || firstDonePlaying == null) {
                return 0L;
            }
            long totalDuration = firstCorrectGuess.eventAtEpoc - firstDonePlaying.eventAtEpoc;
            List<FlashcardEngineEvent> inScopeEvents = events.stream()
                    .filter(event -> event.eventAtEpoc >= firstDonePlaying.eventAtEpoc)
                    .filter(event -> event.eventAtEpoc < firstCorrectGuess.eventAtEpoc)
                    .collect(Collectors.toList());
            long pausedTime = calcTotalTimePaused(inScopeEvents);
            return totalDuration - pausedTime;
        }).mapToDouble(Double::valueOf).average();

        if (!average.isPresent()) {
            return null;
        } else {
            return toSeconds(average.getAsDouble());
        }
    }

    private static long calcTotalTimePaused(List<FlashcardEngineEvent> events) {
        long totalPausedTime = 0;
        boolean isPaused = false;
        FlashcardEngineEvent mostRecentPauseEvent = null;
        for (FlashcardEngineEvent event : events) {
            if (event.eventType == FlashcardEngineEvent.EventType.PAUSE) {
                isPaused = true;
                mostRecentPauseEvent = event;
            }

            if (isPaused && event.eventType == FlashcardEngineEvent.EventType.RESUME) {
                isPaused = false;
                totalPausedTime += event.eventAtEpoc - mostRecentPauseEvent.eventAtEpoc;
            }
        }
        return totalPausedTime;
    }

    private static Double calcICGBCG(List<List<FlashcardEngineEvent>> segments) {
        if (segments.isEmpty()) {
            return null;
        }

        OptionalDouble average = segments.stream()
                .map(
                        events -> events.stream()
                                .filter(e -> e.eventType == FlashcardEngineEvent.EventType.INCORRECT_GUESS)
                                .count()
                )
                .mapToDouble(Double::valueOf)
                .average();

        if (average.isPresent()) {
            return average.getAsDouble();
        } else {
            return null;
        }
    }

    private static FlashcardEngineEvent findFirst(FlashcardEngineEvent.EventType eventType, List<FlashcardEngineEvent> events) {
        for (FlashcardEngineEvent event : events) {
            if (event.eventType == eventType) {
                return event;
            }
        }

        return null;
    }

    private static int calcTotalAccurateSymbolsGuessed(List<FlashcardEngineEvent> events) {
        int totalCorrectSymbols = 0;
        for (FlashcardEngineEvent event : events) {
            if (event.eventType.equals(FlashcardEngineEvent.EventType.CORRECT_GUESS)) {
                totalCorrectSymbols += AudioManager.numSymbolsForStringNoFarnsworth(event.info);
            }
        }
        return totalCorrectSymbols;
    }

    private static double calcWpmAverage(FlashcardTrainingSessionWithEvents s, int totalCorrectGuesses) {
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
        public String message;
        public int numberPlays = -1;
        public int correctGuesses = 0;
        @Nullable
        public Double averagePlaysBeforeCorrect = null;
        @Nullable
        public Double averageSecondsBeforeGuess = null;
        @Nullable
        public Double skipRate = null;
    }

    public static class Analysis {
        public int overallCardsShown;
        public int overallCorrectGuesses;
        public double overAllSymbolAccuracy;
        public List<SymbolAnalysis> messageAnalysis;
        public double overallAverageSecondsBeforeGuess;
        public double overallSkipRate;
        public double cardsPlayed;
    }
}
