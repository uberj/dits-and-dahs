package com.uberj.ditsanddahs.flashcard;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardEngineEvent;
import com.uberj.ditsanddahs.keyboards.KeyConfig;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
        return (int) events.stream().filter(e -> e.eventType == FlashcardEngineEvent.EventType.CORRECT_GUESS).count();
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
}
