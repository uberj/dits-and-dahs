package com.uberj.ditsanddahs.transcribe;

import com.annimon.stream.function.Supplier;
import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.AudioManager;
import com.uberj.ditsanddahs.GlobalSettings;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class QSOWordSupplier implements Supplier<Pair<String, AudioManager.MorseConfig>> {
    public static final String STATION_SWITCH_MARKER = "@";
    private final List<List<String>> sentences;
    private final boolean collapseProSigns;
    private final Set<String> prosigns;
    private int sentenceIdx = 0;
    private int wordIdx = 0;
    private int letterIdx = 0;
    private final AudioManager.MorseConfig morseConfig0;
    private final AudioManager.MorseConfig morseConfig1;
    private boolean pumpLetterSpace = false;

    public QSOWordSupplier(List<String> passedMessages, GlobalSettings globalSettings, AudioManager.MorseConfig morseConfig0, AudioManager.MorseConfig morseConfig1) {
        this.sentences = wordSplit(passedMessages);
        this.morseConfig0 = morseConfig0;
        this.morseConfig1 = morseConfig1;
        this.collapseProSigns = globalSettings.shouldCollapseProSigns();
        this.prosigns = globalSettings.getEnabledProsigns();
    }

    private static List<List<String>> wordSplit(List<String> passedMessages) {
        List<List<String>> messages = Lists.newArrayList();
        for (String passedMessage : passedMessages) {
            messages.add(Arrays.asList(passedMessage.split(" ")));
        }
        return messages;
    }

    @Override
    public synchronized Pair<String, AudioManager.MorseConfig> get() {
        if (sentences.isEmpty()) {
            return null;
        }

        if (sentences.size() <= sentenceIdx) {
            // We've exhausted our sentences
            return null;
        }

        List<String> currSentence = sentences.get(sentenceIdx);
        if (wordIdx >= currSentence.size()) {
            sentenceIdx++;
            letterIdx = 0;
            wordIdx = 0;
            if (sentences.size() <= sentenceIdx) {
                // We are going to be done next round. just end it now
                return null;
            } else {
                pumpLetterSpace = false;
                return Pair.of(STATION_SWITCH_MARKER, sentenceIdx % 2 == 0 ? morseConfig0 : morseConfig1);
            }
        }


        if (pumpLetterSpace) {
            pumpLetterSpace = false;
            return Pair.of(String.valueOf(AudioManager.LETTER_SPACE), sentenceIdx % 2 == 0 ? morseConfig0 : morseConfig1);
        } else {
            String curWord = currSentence.get(wordIdx);

            if (letterIdx >= curWord.length()) {
                // This means a space is due.
                wordIdx++;
                letterIdx = 0;
                return Pair.of(String.valueOf(AudioManager.WORD_SPACE), sentenceIdx % 2 == 0 ? morseConfig0 : morseConfig1);
            } else {
                char curChar = curWord.charAt(letterIdx);
                pumpLetterSpace = !collapseProSigns || !prosigns.contains(curWord.toUpperCase());
                return Pair.of(String.valueOf(curChar), sentenceIdx % 2 == 0 ? morseConfig0 : morseConfig1);
            }
        }
    }
}
