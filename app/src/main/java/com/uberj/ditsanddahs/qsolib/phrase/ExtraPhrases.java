package com.uberj.ditsanddahs.qsolib.phrase;

import com.annimon.stream.function.Function;
import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.qsolib.StationState;

import org.apache.commons.lang3.tuple.Pair;

import java.util.Collections;
import java.util.List;

public class ExtraPhrases {
    private static final List<Pair<Class<? extends Phrase>, Function<StationState, Phrase>>> allExtraPhrases = Lists.newArrayList(
            Pair.of(Wx.class, (ss) -> new Wx()),
            Pair.of(Job.class, (ss) -> new Job()),
            Pair.of(Equipment.class, (ss) -> new Equipment())
    );

    public static List<Phrase> get(StationState stationState, int extraPhraseCount, List<Phrase> stuffIWillBeSaying) {
        List<Class<? extends Phrase>> stuffIShouldntSay = Lists.newArrayList();
        for (Phrase phrase : stationState.stuffSaid.allStuffISaid) {
            stuffIShouldntSay.add(phrase.getClass());
        }

        for (Phrase phrase : stuffIWillBeSaying) {
            stuffIShouldntSay.add(phrase.getClass());
        }

        List<Phrase> extraPhrases = Lists.newArrayList();
        Collections.shuffle(allExtraPhrases);

        for (Pair<Class<? extends Phrase>, Function<StationState, Phrase>> phraseMap : allExtraPhrases) {
            Class<? extends Phrase> phraseClassName = phraseMap.getKey();
            if (stuffIShouldntSay.contains(phraseClassName)) {
                continue;
            }

            extraPhrases.add(phraseMap.getValue().apply(stationState));
            if (extraPhrases.size() == extraPhraseCount) {
                break;
            }
        }

        return extraPhrases;
    }

    public static List<Phrase> getPoliteResponses(StationState stationState) {
        List<Phrase> politeResponses = Lists.newArrayList();
        for (Pair<Class<? extends Phrase>, Function<StationState, Phrase>> allExtraPhrase : allExtraPhrases) {
            Class<? extends Phrase> phraseClass = allExtraPhrase.getKey();
            boolean theyHaveMentionedIt = PhraseUtil.hasMentionedPhrase(stationState.stuffSaid.allStuffTheySaid, phraseClass);
            boolean iHaveMentionedIt = PhraseUtil.hasMentionedPhrase(stationState.stuffSaid.allStuffISaid, phraseClass);
            if (theyHaveMentionedIt && !iHaveMentionedIt) {
                politeResponses.add(allExtraPhrase.getValue().apply(stationState));
            }
        }
        return politeResponses;
    }
}
