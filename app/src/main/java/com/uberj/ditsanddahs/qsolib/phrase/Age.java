package com.uberj.ditsanddahs.qsolib.phrase;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.uberj.ditsanddahs.qsolib.RandomUtil;
import com.uberj.ditsanddahs.qsolib.StationState;

import java.util.List;

import static com.google.common.collect.ImmutableList.of;
import static com.uberj.ditsanddahs.qsolib.RandomUtil.choose;
import static com.uberj.ditsanddahs.qsolib.RandomUtil.randomGuard;

public class Age implements Phrase {
    private final int age;
    private final boolean sameAgeResponse;

    public Age(StationState ss)  {
        Optional<Age> ageOptional = PhraseUtil.findIfPresent(ss.stuffSaid.allStuffTheySaid, Age.class);
        if (ageOptional.isPresent() && randomGuard(0.2)) {
            age = ageOptional.get().age;
            sameAgeResponse = true;
        } else {
            age = RandomUtil.intBetween(17, 99);
            sameAgeResponse = false;
        }
    }

    @Override
    public boolean objectToBtJoin(Phrase next) {
        return randomGuard(0.7);
    }

    @Override
    public List<Phrase> reduce(Location location) {
        if (sameAgeResponse) {
            return of(new LeafPhrase(choose(
                    "I AM ALSO ${age}",
                    "SAME AGE AS YOU",
                    "WE ARE SAME AGE"
            ), this::resolveFacts));
        }

        return of(new LeafPhrase(choose(
                "AGE HR ${age}",
                "AGE ${age}",
                "MY AGE ${age}"
        ), this::resolveFacts));
    }

    public ImmutableMap<String, String> resolveFacts() {
        return ImmutableMap.of("age", String.valueOf(age));
    }
}
