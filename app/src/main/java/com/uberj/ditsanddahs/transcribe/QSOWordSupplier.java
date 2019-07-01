package com.uberj.ditsanddahs.transcribe;

import com.annimon.stream.function.Supplier;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class QSOWordSupplier implements Supplier<String> {
    public static final String STATION_SWITCH_MARKER = "@STATION_SWITCH_MARKER@";
    private final List<String> messages;
    private int sentenceIdx = 0;
    private int wordIdx = 0;

    public QSOWordSupplier(List<String> passedMessages) {
        this.messages = passedMessages;
    }

    @Override
    public synchronized String get() {
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
                return STATION_SWITCH_MARKER;
            }
        }

        char curChar = currSentence.charAt(wordIdx);
        wordIdx++;
        return String.valueOf(curChar);
    }
}
