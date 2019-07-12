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
    private boolean pumpWordSpace = false;
    private boolean pumpLetterSpace = false;
    private boolean allDone = false;
    private boolean pumpStationSpace = false;

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
        AudioManager.MorseConfig config = sentenceIdx % 2 == 0 ? morseConfig0 : morseConfig1;

        // Invarient: Current run is safe. Its this methods job to make sure the next run is safe
        if (allDone || sentences.isEmpty()) {
            return null;
        }

        if (pumpLetterSpace) {
            pumpLetterSpace = false;
            return Pair.of(String.valueOf(AudioManager.LETTER_SPACE), config);
        }

        if (pumpWordSpace) {
            pumpWordSpace = false;
            return Pair.of(String.valueOf(AudioManager.WORD_SPACE), config);
        }

        if (pumpStationSpace) {
            pumpStationSpace = false;
            return Pair.of(STATION_SWITCH_MARKER, config);
        }

        List<String> currSentence = sentences.get(sentenceIdx);
        // Figure out what word we are on
        String out = setupNextRun(currSentence);
        return Pair.of(out, config);
    }

    private String setupNextRun(List<String> currSentence) {
        String curWord = currSentence.get(wordIdx);
        char curLetter = curWord.charAt(letterIdx);
        letterIdx++;

        // Side affects, hell yeah
        // Start with letter and work up to sentence
        pumpLetterSpace = !prosigns.contains(curWord);

        if (letterIdx >= curWord.length()) {
            pumpLetterSpace = false; // Word space takes precedence
            pumpWordSpace = true;
            letterIdx = 0;
            wordIdx++;
        }

        if (wordIdx >= currSentence.size()) {
            pumpLetterSpace = false; // Station space takes precedence
            pumpWordSpace = false; // Station space takes precedence
            pumpStationSpace = true;
            letterIdx = 0;
            wordIdx = 0;
            sentenceIdx++;
        }

        if (sentences.size() <= sentenceIdx) {
            allDone = true;
        }


        return String.valueOf(curLetter);
    }
}
