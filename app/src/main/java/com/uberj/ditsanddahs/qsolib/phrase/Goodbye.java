package com.uberj.ditsanddahs.qsolib.phrase;

import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.uberj.ditsanddahs.qsolib.StationState;

import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.of;
import static com.uberj.ditsanddahs.qsolib.RandomUtil.choose;
import static com.uberj.ditsanddahs.qsolib.RandomUtil.randomGuard;

public class Goodbye implements Phrase {
    private final StationState stationState;
    private final boolean theyHaveSaidGoodbye;
    private final String theirName;
    private final String greetingShort;
    private boolean includesExcuse;
    private List<Phrase> phrases = null;

    public Goodbye(StationState stationState) {
        this.stationState = stationState;
        this.theirName = Name.findName(stationState.stuffSaid.allStuffTheySaid);

        Optional<Goodbye> possibleGoodbye = PhraseUtil.findIfPresent(stationState.stuffSaid.allStuffTheySaid, Goodbye.class);
        Optional<Greeting> greeting = PhraseUtil.findIfPresent(stationState.stuffSaid.allStuffISaid, Greeting.class);
        if (greeting.isPresent()) {
            this.greetingShort = greeting.get().greetingShort;
        } else {
            this.greetingShort = null;
        }

        if (possibleGoodbye.isPresent()) {
            this.theyHaveSaidGoodbye = true;
            this.includesExcuse = false;
            Goodbye goodbye = possibleGoodbye.get();
            if (goodbye.includesExcuse) {
                if (greetingShort != null) {
                    phrases = of(new LeafPhrase(choose(
                            "OK ${theirName} WONT HOLD YOU TNX QSO HPE CUAGN VY 73 ${greetingShort} SK",
                            "OK OM WONT HOLD YOU TNX QSO HPE CUAGN VY 73 ${greetingShort} SK",
                            "OK OM WONT HOLD YOU TNX QSO 73 ${greetingShort} SK"
                    ), this::resolveFacts), new ToFrom(stationState), new LeafPhrase("E E"));
                } else {
                    phrases = of(new LeafPhrase(choose(
                            "OK OM WONT HOLD YOU TNX QSO HPE CUAGN VY 73 SK"
                    ), this::resolveFacts), new ToFrom(stationState), new LeafPhrase("E E"));
                }
            }
        } else {
            this.theyHaveSaidGoodbye = false;
            if (randomGuard(0.2)) {
                // Add an excuse
                this.includesExcuse = true;
                phrases = of(new LeafPhrase(choose(
                        "R FB ON ALL ${theirName} BT THE XYL SAYS SUPPER IS READY SO I MUST GO BT TNX QSO HPE CUL 73 GE SK",
                        "UR DOING FB = LEFT THE STOVE ON GOTTA 73 SK",
                        "UR DOING FB = I GOT A PHONE CALL, CUL 73 SK",
                        "UR DOING FB ${theirName} = DINNER HR 73 SK",
                        "UR DOING FB BUT I HAVE OTHER BIZ TO ATTEND 73 SK"
                ), this::resolveFacts), new ToFrom(stationState), new LeafPhrase("E E"));
            }
        }

        if (this.phrases == null) {
            this.includesExcuse = false;
            // Regular old good by
            phrases = of(new LeafPhrase(choose(
                    "R FB ON ALL ${theirName} = TNX QSO HPE CUL 73 GE SK",
                    "UR DOING FB 73 SK",
                    "FB TNX FER QSO 73 SK",
                    "FB ${theirName} TNX FER NICE QSO 73 ES BCNU AR SK"
            ), this::resolveFacts), new ToFrom(stationState), new LeafPhrase("E E"));
        }
    }

    @Override
    public List<Phrase> reduce(Location location) {
        return phrases;
    }

    private Map<String, String> resolveFacts() {
        ImmutableMap.Builder<String, String> factBuilder = ImmutableMap.builder();
        if (theirName != null) {
            factBuilder.put("theirName", theirName);
        } else {
            factBuilder.put("theirName", "OM");
        }

        if (greetingShort != null) {
            factBuilder.put("greetingShort", greetingShort);
        }
        return factBuilder.build();
    }
}
