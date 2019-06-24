package com.uberj.ditsanddahs.qsolib;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.flashcard.RandomCallSignGenerator;
import com.uberj.ditsanddahs.qsolib.phrase.Goodbye;
import com.uberj.ditsanddahs.qsolib.phrase.Phrase;
import com.uberj.ditsanddahs.qsolib.phrase.PhraseUtil;
import com.uberj.ditsanddahs.qsolib.phrase.Sentence;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static com.uberj.ditsanddahs.qsolib.phrase.PhraseUtil.hasRecentlyMentionedPhrase;

public class RandomCWQSOTest {

    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void doABunchOfQSOs() {
        int numRuns = 100;
        for (int i = 0; i < numRuns; i++) {
            List<String> generate = generate();
            for (String s : generate) {
                Assert.assertFalse(s, s.contains("$"));
                System.out.println(s);
            }
            System.out.println();
            System.out.println();
        }
    }

    private static List<String> generate() {
        List<String> lines = Lists.newArrayList();
        String station0Call = RandomCallSignGenerator.getCall();
        String station1Call = RandomCallSignGenerator.getCall();
        int round = 0;
        List<List<Phrase>> responses0 = Lists.newArrayList();
        List<List<Phrase>> responses1 = Lists.newArrayList();
        while(true) {
            boolean station0SKed = hasRecentlyMentionedPhrase(responses0, Goodbye.class);
            boolean station1SKed = hasRecentlyMentionedPhrase(responses1, Goodbye.class);
            if (station0SKed && station1SKed) {
                break;
            }
            if (round % 2 == 0) { // Station0
                List<Phrase> response = p(lines, new Sentence(round, new StationState(station0Call, new StuffSaid(responses1, responses0))));
                responses0 = ImmutableList.<List<Phrase>>builder()
                        .addAll(responses0)
                        .add(PhraseUtil.collectAll(response))
                        .build();
            } else { // Station1
                List<Phrase> response = p(lines, new Sentence(round, new StationState(station1Call, new StuffSaid(responses0, responses1))));
                responses1 = ImmutableList.<List<Phrase>>builder()
                        .addAll(responses1)
                        .add(PhraseUtil.collectAll(response))
                        .build();
            }
            round += 1;
        }
        return lines;
    }

    private static List<Phrase> p(List<String> lines, Sentence sentence) {
        List<Phrase> phrases = sentence.toPhrases();
        String s = PhraseUtil.reduceToString(phrases);
        lines.add(s);
        return phrases;
    }
}