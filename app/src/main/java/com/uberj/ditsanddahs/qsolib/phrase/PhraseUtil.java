package com.uberj.ditsanddahs.qsolib.phrase;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

public class PhraseUtil {
    public static List<Phrase> collectAll(List<Phrase> allPhrases) {
        List<Phrase> all = Lists.newArrayList();
        Phrase prevPhrase = null;
        for (Phrase phrase : allPhrases) {
            all.addAll(collectAll(phrase, null, prevPhrase));
            prevPhrase = phrase;
        }
        return all;
    }

    public static String reduceToString(List<Phrase> phrases) {
        return reduceToString(new Phrase() {
            @Override
            public List<Phrase> reduce(Location location) {
                return phrases;
            }
        }, null, null);
    }

    private static String reduceToString(Phrase phrase, Phrase parent, Phrase prev) {
        if (phrase instanceof Phrase.LeafPhrase) {
            return phrase.resolveToString();
        }

        StringBuilder message = new StringBuilder();
        List<Phrase> reduce = phrase.reduce(new Phrase.Location(parent, prev));
        for (int i = 0; i < reduce.size(); i++) {
            Phrase curPhrase = reduce.get(i);
            boolean objectToBtJoin;
            if (i != reduce.size() - 1) {
                // not the last one
                Phrase next = reduce.get(i + 1);
                objectToBtJoin = curPhrase.objectToBtJoin(next);
                message.append(reduceToString(curPhrase, phrase, prev));
                if (objectToBtJoin) {
                    message.append(" ");
                } else {
                    message.append(" = ");
                }
            } else {
                message.append(reduceToString(curPhrase, phrase, prev));
            }

            prev = curPhrase;
        }
        return message.toString();
    }

    private static List<Phrase> collectAll(Phrase phrase, Phrase parentPhrase, Phrase prevPhrase) {
        ArrayList<Phrase> all = Lists.newArrayList();
        List<Phrase> reducedList = phrase.reduce(new Phrase.Location(parentPhrase, prevPhrase));
        for (Phrase reduced : reducedList) {
            if (reduced instanceof Phrase.LeafPhrase) {
                all.add(phrase);
            } else {
                all.addAll(collectAll(reduced, phrase, prevPhrase));
            }
            prevPhrase = reduced;
        }
        return all;
    }

    public static boolean hasMentionedPhrase(List<Phrase> stuffSaid, Class<? extends Phrase> stanzaClass) {
        for (Phrase phrase : PhraseUtil.collectAll(stuffSaid)) {
            if (stanzaClass.equals(phrase.getClass())) {
                return true;
            }
        }

        return false;
    }

    public static boolean hasRecentlyMentionedPhrase(List<List<Phrase>> stuffTheySaid, Class<? extends Phrase> phrase) {
        if (stuffTheySaid.isEmpty()) {
            return false;
        }
        List<Phrase> stuffJustSaid = stuffTheySaid.get(stuffTheySaid.size() - 1);
        return hasMentionedPhrase(stuffJustSaid, phrase);
    }

    static <T> Optional<T> findIfPresent(List<Phrase> allStuffISaid, Class<T> greetingClass) {
        for (Phrase phrase : allStuffISaid) {
            if (phrase.getClass().equals(greetingClass)) {
                return (Optional<T>) Optional.of(phrase);
            }
        }
        return Optional.absent();
    }

}
