package com.uberj.ditsanddahs.qsolib.phrase;

import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.qsolib.StationState;
import com.uberj.ditsanddahs.qsolib.StuffSaid;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static com.uberj.ditsanddahs.qsolib.RandomUtil.choose;
import static com.uberj.ditsanddahs.qsolib.RandomUtil.randomGuard;
import static com.uberj.ditsanddahs.qsolib.phrase.PhraseUtil.hasMentionedPhrase;
import static com.uberj.ditsanddahs.qsolib.phrase.PhraseUtil.hasRecentlyMentionedPhrase;

public class Sentence {
    private final StuffSaid stuffSaid;
    private final StationState stationState;
    private final int round;

    public Sentence(int round, StationState stationState) {
        this.round = round;
        this.stuffSaid = stationState.stuffSaid;
        this.stationState = stationState;
    }

    public List<Phrase> toPhrases() {
        if (stuffSaid.allStuffTheySaid.isEmpty() && stuffSaid.allStuffISaid.isEmpty()) {
            return of(new Phrase.CallCQ(stationState));
        }

        if (!stuffSaid.allStuffTheySaid.isEmpty() && hasMentionedPhrase(stuffSaid.allStuffTheySaid, Phrase.CallCQ.class) && stuffSaid.allStuffISaid.isEmpty()) {
            return of (new Phrase.AnswerCQ(stationState));
        }

        List<Phrase> phrases = Lists.newArrayList();
        phrases.add(new Phrase.ToFrom(stationState));

        if (shouldGreet()) {
            phrases.add(new Phrase.Greeting(stationState));
        }

        if (hasRecentlyMentionedPhrase(stuffSaid.stuffTheySaid, Phrase.RequestRxConfirm.class)) {
            phrases.add(new Phrase.AllReceived());
        }

        if (hasReportedMyRST(stuffSaid.allStuffTheySaid) && !hasThankedForReport()) {
            phrases.add(new Phrase.ThankForReport());
        }

        if (shouldReportRST()) {
            phrases.add(new Phrase.RSTReport());
        }

        boolean iHaveNotMentionedMyName = !hasMentionedPhrase(stuffSaid.allStuffISaid, Phrase.Name.class);
        boolean theyHaveMentionedTheirName = hasMentionedPhrase(stuffSaid.allStuffTheySaid, Phrase.Name.class);
        if (iHaveNotMentionedMyName && theyHaveMentionedTheirName) {
            phrases.add(new Phrase.Name(stationState));
        }

        if (!theyHaveMentionedTheirName && !iHaveNotMentionedMyName && randomGuard(0.9)) {
            phrases.add(new Phrase.Name(stationState));
        }

        boolean iHaveMentionedMyQTH = hasMentionedPhrase(stuffSaid.allStuffISaid, Phrase.Qth.class);
        boolean theyHaveMentionedTheirQTH = hasMentionedPhrase(stuffSaid.allStuffISaid, Phrase.Qth.class);
        if (theyHaveMentionedTheirQTH && !iHaveMentionedMyQTH) {
            phrases.add(new Phrase.Qth());
        }

        if (!theyHaveMentionedTheirQTH && !iHaveMentionedMyQTH && randomGuard(0.9)) {
            phrases.add(new Phrase.Qth());
        }

        List<Class<? extends Phrase>> topLevelPhrasesSoFar = toClasses(phrases);
        boolean tooEarlyToSK = false;
        if (topLevelPhrasesSoFar.contains(Phrase.Qth.class) || topLevelPhrasesSoFar.contains(Phrase.Name.class)) {
            tooEarlyToSK = true;
        }


        List<Phrase> politeResponses = ExtraPhrases.getPoliteResponses(stationState);
        phrases.addAll(politeResponses);

        if (hasRecentlyMentionedPhrase(stuffSaid.stuffTheySaid, Goodbye.class)) {
            phrases.add(new Goodbye(stationState));
        } else {
            if (tooEarlyToSK || randomGuard(0.2)) {
                int phrasesToSay = choose(4, 5);
                int numPhrasesSoFar = phrases.size();
                int extraPhraseCount = phrasesToSay - numPhrasesSoFar;

                // Maybe add some more stuff
                if (extraPhraseCount > 0) {
                    List<Phrase> extraPhrases = ExtraPhrases.get(stationState, extraPhraseCount, phrases);
                    if (extraPhrases.size() == 0) {
                        phrases.add(new Goodbye(stationState));
                    } else {
                        phrases.addAll(extraPhrases);
                    }
                }
                phrases.add(new Phrase.EndToFrom(stationState));
            } else {
                phrases.add(new Goodbye(stationState));
            }
        }

        return phrases;
    }

    private List<Class<? extends Phrase>> toClasses(List<Phrase> phrases) {
        List<Class<? extends Phrase>> classes = Lists.newArrayList();
        for (Phrase phrase : phrases) {
            classes.add(phrase.getClass());
        }
        return classes;
    }

    private boolean hasReportedMyRST(List<Phrase> stuffTheySaid) {
        return hasMentionedPhrase(stuffTheySaid, Phrase.RSTReport.class);
    }


    private boolean hasThankedForReport() {
        return hasMentionedPhrase(stuffSaid.allStuffISaid, Phrase.ThankForReport.class);
    }

    private boolean shouldReportRST() {
        return !hasMentionedPhrase(stuffSaid.allStuffISaid, Phrase.RSTReport.class);
    }

    private boolean shouldGreet() {
        return !hasMentionedPhrase(stuffSaid.allStuffISaid, Phrase.Greeting.class);
    }
}
