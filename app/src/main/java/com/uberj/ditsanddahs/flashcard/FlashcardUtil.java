package com.uberj.ditsanddahs.flashcard;

import android.content.res.Resources;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.crashlytics.android.Crashlytics;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uberj.ditsanddahs.KeyboardUtil;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardEngineEvent;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardSessionType;
import com.uberj.ditsanddahs.keyboards.KeyConfig;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class FlashcardUtil {

    protected static List<String> findLatestCardInput(List<String> inputStrings) {
        List<String> latestGuessInput = Lists.newArrayList();
        List<String> reversedStrings = Lists.newArrayList(inputStrings);
        Collections.reverse(reversedStrings);
        for (String outputString : reversedStrings) {
            KeyboardUtil.KeyPressV1 kp = KeyboardUtil.KeyPressV1.from(outputString);
            if (kp.name.equals(KeyConfig.ControlType.SKIP.keyName) || kp.name.equals(KeyConfig.ControlType.SUBMIT.keyName)) {
                break;
            }
            latestGuessInput.add(0, outputString);
        }
        return latestGuessInput;
    }

    public static Pair<Integer, String> convertKeyPressesToString(List<String> enteredStrings) {
        List<String> latestCardInput = findLatestCardInput(enteredStrings);
        return KeyboardUtil.convertKeyPressesToString(latestCardInput);
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

    public static long calcDurationMillis(List<FlashcardEngineEvent> events) {
        if (events.isEmpty()) {
            return -1;
        }

        long timePaused = calcTotalTimePaused(events);
        return events.get(events.size() - 1).eventAtEpoc - events.get(0).eventAtEpoc - timePaused;
    }

    public static int calcNumCardsCompleted(List<FlashcardEngineEvent> events) {
        return (int) Stream.of(events).filter(e -> e.eventType == FlashcardEngineEvent.EventType.CORRECT_GUESS).count();
    }

    public static double calcFirstGuessAccuracy(List<FlashcardEngineEvent> events) {
        Collection<List<List<FlashcardEngineEvent>>> segments = parseSegments(events).values();
        double correctFirstGuesses = 0;
        double incorrectFirstGuesses = 0;
        for (List<List<FlashcardEngineEvent>> segment : segments) {
            for (List<FlashcardEngineEvent> segmentEvents : segment) {
                for (FlashcardEngineEvent event : segmentEvents) {
                    if (event.eventType == FlashcardEngineEvent.EventType.CORRECT_GUESS) {
                        correctFirstGuesses += 1;
                        break;
                    }
                    if (event.eventType == FlashcardEngineEvent.EventType.INCORRECT_GUESS) {
                        incorrectFirstGuesses += 1;
                        break;
                    }
                }
            }
        }
        return correctFirstGuesses / (correctFirstGuesses + incorrectFirstGuesses);
    }

    public static int calcSkipCount(List<FlashcardEngineEvent> events) {
        Collection<List<List<FlashcardEngineEvent>>> segments = parseSegments(events).values();
        int skipCount = 0;
        for (List<List<FlashcardEngineEvent>> segment : segments) {
            for (List<FlashcardEngineEvent> segmentEvents : segment) {
                for (FlashcardEngineEvent event : segmentEvents) {
                    if (event.eventType == FlashcardEngineEvent.EventType.SKIP) {
                        skipCount += 1;
                        break;
                    }
                }
            }
        }
        return skipCount;
    }

    public static String getCardType(Resources resources, int pos) {
        String[] cardTypes = resources.getStringArray(R.array.flashcard_type);
        return cardTypes[pos];
    }

    public static int getCardTypePos(Resources resources, FlashcardSessionType sessionType) {
        String[] cardTypes = resources.getStringArray(R.array.flashcard_type);
        for (int i = 0; i < cardTypes.length; i++) {
            String cardType = cardTypes[i];
            if (sessionType.equals(FlashcardSessionType.RANDOM_FCC_CALLSIGNS) && cardType.equals(resources.getString(R.string.fcc_call_signs_flashcard_type))) {
                return i;
            }

            if (sessionType.equals(FlashcardSessionType.RANDOM_WORDS) && cardType.equals(resources.getString(R.string.common_words_flashcard_type))) {
                return i;
            }
        }
        throw new RuntimeException("Couldn't find session type in drop down strings: " + sessionType.name());
    }
}
