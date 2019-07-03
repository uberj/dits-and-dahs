package com.uberj.ditsanddahs.transcribe;

import com.annimon.stream.function.Supplier;
import com.uberj.ditsanddahs.AudioManager;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public class QSOWordSupplier implements Supplier<Pair<String, AudioManager.MorseConfig>> {
    public static final String STATION_SWITCH_MARKER = "@";
    private final List<String> messages;
    private int sentenceIdx = 0;
    private int wordIdx = 0;
    private final AudioManager.MorseConfig morseConfig0;
    private final AudioManager.MorseConfig morseConfig1;

    public QSOWordSupplier(List<String> passedMessages, AudioManager.MorseConfig morseConfig0, AudioManager.MorseConfig morseConfig1) {
        this.messages = passedMessages;
        this.morseConfig0 = morseConfig0;
        this.morseConfig1 = morseConfig1;
    }

    @Override
    public synchronized Pair<String, AudioManager.MorseConfig> get() {
        if (sentenceIdx == 1) {
            return null;
        }
        if (messages.isEmpty()) {
            return null;
        }

        if (messages.size() <= sentenceIdx) {
            // We've exhausted our sentences
            return null;
        }

        String currSentence = messages.get(sentenceIdx);
        if (currSentence.length() <= wordIdx) {
            sentenceIdx++;
            wordIdx = 0;
            if (messages.size() <= sentenceIdx) {
                // We are going to be done next round. just end it now
                return null;
            } else {
                return Pair.of(STATION_SWITCH_MARKER, sentenceIdx % 2 == 0 ? morseConfig0 : morseConfig1);
            }
        }

        char curChar = currSentence.charAt(wordIdx);
        wordIdx++;
        return Pair.of(String.valueOf(curChar), sentenceIdx % 2 == 0 ? morseConfig0 : morseConfig1);
    }
}
