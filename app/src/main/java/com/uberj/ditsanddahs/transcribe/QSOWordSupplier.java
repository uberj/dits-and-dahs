package com.uberj.ditsanddahs.transcribe;

import com.annimon.stream.function.Supplier;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

public class QSOWordSupplier implements Supplier<String> {
    public static final String STATION_SWITCH_MARKER = "@STATION_SWITCH_MARKER@";
    private final List<List<String>> messages;
    private int sentenceIdx = 0;
    private boolean pumpSpace = false;

    public QSOWordSupplier(List<String> passedMessages) {
        messages = Lists.newArrayList();
        for (String passedMessage : passedMessages) {
            messages.add(Arrays.asList(passedMessage.split(" ")));
        }
    }

    @Override
    public synchronized String get() {
        if (messages.isEmpty()) {
            return null;
        }

        List<String> currSentence = messages.get(0);
        if (sentenceIdx >= currSentence.size()) {
            messages.remove(0);
            sentenceIdx = 0;
            pumpSpace = false;
            return messages.isEmpty() ? null : STATION_SWITCH_MARKER;
        }

        if (pumpSpace) {
            pumpSpace = false;
            return " ";
        }

        String s = String.valueOf(currSentence.get(sentenceIdx));
        sentenceIdx++;
        pumpSpace = true;
        return s;
    }
}
