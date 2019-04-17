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
                    if (!stringsToDisplay.isEmpty()) {
                        stringsToDisplay.remove(stringsToDisplay.size() - 1);
                    }
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
//        for (SymbolAnalysis sa : analysis.messageAnalysis) {
//            if (sa.accuracy == null) {
//                continue;
//            }
//            totalInPlay += 1;
//            overallAccuracy += sa.accuracy;
//            totalCorrectGuesses += sa.hits;
//        }
//
//
//        analysis.overAllAccuracy = overallAccuracy / totalInPlay;

        return analysis;
    }

    public static List<SymbolAnalysis> buildIndividualCardAnalysis(FlashcardTrainingSessionWithEvents session) {
        List<SymbolAnalysis> l = Lists.newArrayList();
        Map<String, List<List<FlashcardEngineEvent>>> allSegments = parseSegments(session.events);
        for (Map.Entry<String, List<List<FlashcardEngineEvent>>> entry : allSegments.entrySet()) {
            String symbol = entry.getKey();
            List<List<FlashcardEngineEvent>> segments = entry.getValue();
            SymbolAnalysis sa = new SymbolAnalysis();
            sa.message = symbol;
            sa.numberPlays = segments.size();
//            sa.correctGuesses = calcCorrectGuesses(segments);
//            sa.averagePlaysBeforeGuess = calcAveragePlaysBeforeGuess(segments);
//            sa.averageSecondsBeforeGuess = calcAverageSecondsBeforeGuess(segments);
//            sa.skipRate = calcSkipRate(segments);
            l.add(sa);
        }

        return l;
    }

    protected static Map<String, List<List<FlashcardEngineEvent>>> parseSegments(List<FlashcardEngineEvent> events) {
        Map<String, List<List<FlashcardEngineEvent>>> output = Maps.newHashMap();
        String currentMessage = null;
        List<FlashcardEngineEvent> currentLetterEvents = Lists.newArrayList();
        // This loop will segment the event stream by chopping where ever there is a MESSAGE_CHOSEN event
        for (FlashcardEngineEvent event : events) {
            if (event.eventType == FlashcardEngineEvent.EventType.MESSAGE_CHOSEN) {
                if (currentMessage != null) {
                    // Store the current letter's segment we have been tracking
                    if (!output.containsKey(currentMessage)) {
                        output.put(currentMessage, Lists.newArrayList());
                    }

                    output.get(currentMessage).add(currentLetterEvents);
                }

                // Set the new current letter
                currentMessage = event.info;
                currentLetterEvents = Lists.newArrayList();
            } else if (event.eventType == FlashcardEngineEvent.EventType.DONE_PLAYING) {
                if (event.info == null || !event.info.equals(currentMessage)) {
                    // TODO, dump json into this error message so we can debug it
                    Crashlytics.log("DONE_PLAYING logged with a different currentMessage");
                    continue;
                }
            }

            currentLetterEvents.add(event);
        }

        // We got to the end of the loop. We have to store the current letter's latest segment
        if (currentMessage != null) {
            if (!output.containsKey(currentMessage)) {
                output.put(currentMessage, Lists.newArrayList());
            }
            output.get(currentMessage).add(currentLetterEvents);
        }


        return output;
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

    private static FlashcardEngineEvent findFirst(FlashcardEngineEvent.EventType eventType, List<FlashcardEngineEvent> events) {
        for (FlashcardEngineEvent event : events) {
            if (event.eventType == eventType) {
                return event;
            }
        }

        return null;
    }

    public static class SymbolAnalysis {
        public String message;
        public int numberPlays = -1;
        public int correctGuesses = 0;
        @Nullable
        public Double averagePlaysBeforeGuess = null;
        @Nullable
        public Double averageSecondsBeforeGuess = null;
        @Nullable
        public Double skipRate = null;
    }

    public static class Analysis {
        public List<SymbolAnalysis> messageAnalysis;
    }
}
