package com.uberj.ditsanddahs.qsolib;

import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.qsolib.phrase.Phrase;

import java.util.List;

public class StuffSaid {
    public final List<List<Phrase>> stuffTheySaid;
    public final List<List<Phrase>> stuffISaid;

    public final List<Phrase> allStuffTheySaid = Lists.newArrayList();
    public final List<Phrase> allStuffISaid = Lists.newArrayList();

    public StuffSaid(List<List<Phrase>> stuffTheySaid, List<List<Phrase>> stuffISaid) {
        this.stuffTheySaid = stuffTheySaid;
        this.stuffISaid = stuffISaid;

        for (List<Phrase> phrases : stuffTheySaid) {
            allStuffTheySaid.addAll(phrases);
        }

        for (List<Phrase> phrases : stuffISaid) {
            allStuffISaid.addAll(phrases);
        }
    }
}
