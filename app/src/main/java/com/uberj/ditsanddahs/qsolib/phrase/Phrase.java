package com.uberj.ditsanddahs.qsolib.phrase;

import com.google.common.base.Supplier;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.qsolib.RandomUtil;
import com.uberj.ditsanddahs.qsolib.StationState;
import com.uberj.ditsanddahs.qsolib.StuffSaid;
import com.uberj.ditsanddahs.qsolib.data.RandomCityGenerator;
import com.uberj.ditsanddahs.qsolib.data.RandomNameGenerator;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.text.StringSubstitutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.ImmutableList.of;
import static com.uberj.ditsanddahs.qsolib.RandomUtil.choose;
import static com.uberj.ditsanddahs.qsolib.RandomUtil.randomGuard;

public interface Phrase {

    class Location {
        public final Phrase parent;
        public final Phrase prev;


        public Location(Phrase parent, Phrase prev) {
            this.parent = parent;
            this.prev = prev;
        }
    }

    default String resolveToString() {
        throw new RuntimeException("Only leaf phrases can resolve to a string");
    }

    default List<Phrase> reduce(Location location) {
        return Lists.newArrayList(this);
    }

    default boolean objectToBtJoin(Phrase next) {
        return false;
    }

    class LeafPhrase implements Phrase {
        private final String message;
        private final Supplier<Map<String, String>> facts;

        public LeafPhrase(String message) {
            this.message = message;
            this.facts = HashMap::new;
        }

        public LeafPhrase(String message, Supplier<Map<String, String>> facts) {
            this.message = message;
            this.facts = facts;
        }

        @Override
        public String resolveToString() {
            return StringSubstitutor.replace(message, facts.get());
        }
    }

    class CallCQ implements Phrase {
        private final StationState stationState;
        public String callSign;

        public CallCQ(StationState stationState) {
            this.stationState = stationState;
            this.callSign = stationState.callSign;
        }

        @Override
        public List<Phrase> reduce(Location location) {
            return of(new LeafPhrase("CQ CQ CQ DE ${myCallSign} ${myCallSign} K", this::resolveFacts));
        }

        private Map<String, String> resolveFacts() {
            return ImmutableMap.of("myCallSign", stationState.callSign);
        }

    }

    class AnswerCQ implements Phrase {
        private final ToFrom toFromStanza;
        private final Greeting greeting;
        private final RSTReport signalReport;
        private final Qth qth;
        private final Name name;
        private final EndToFrom end;

        public AnswerCQ(StationState me) {
            toFromStanza = new ToFrom(me);
            greeting = new Greeting(me);
            signalReport = new RSTReport();
            qth = new Qth();
            name = new Name(me);
            end = new EndToFrom(me);
        }

        @Override
        public List<Phrase> reduce(Location location) {
            return Lists.newArrayList(
                    toFromStanza, greeting, signalReport, qth, name, end
            );
        }
    }

    class Greeting implements Phrase {
        public final String greetingShort;
        private String theirName;

        public Greeting(StationState stationState) {
            StuffSaid stuffSaid = stationState.stuffSaid;
            String srtGreeting = null;
            for (Phrase phrase : PhraseUtil.collectAll(stuffSaid.allStuffTheySaid)) {
                if (phrase instanceof Greeting) {
                    Greeting theirGreeting = (Greeting) phrase;
                    srtGreeting = theirGreeting.greetingShort;
                }

                if (phrase instanceof Name) {
                    Name name = (Name) phrase;
                    theirName = name.name;
                }
            }

            if (srtGreeting == null) {
                srtGreeting = choose(Lists.newArrayList("GM", "GA", "GE"));
            }

            greetingShort = srtGreeting;
        }

        @Override
        public boolean objectToBtJoin(Phrase next) {
            return randomGuard(0.50);
        }

        private Map<String, String> resolveFacts() {
            if (theirName == null) {
                return ImmutableMap.of(
                        "greetingShort", greetingShort
                );
            } else {
                return ImmutableMap.of(
                        "greetingShort", greetingShort,
                        "theirName", theirName
                );
            }
        }

        @Override
        public List<Phrase> reduce(Location location) {
            if (theirName == null) {
                return of(new LeafPhrase(choose(Lists.newArrayList(
                        "${greetingShort} TNX CALL",
                        "${greetingShort} TNX FER CALL",
                        "${greetingShort} TNX FER UR CALL"
                )), this::resolveFacts));
            } else {
                // GM JOHN NICE TO MEET U
                return of(new LeafPhrase(choose(Lists.newArrayList(
                        "${greetingShort} ${theirName} NICE TO MEET U",
                        "${greetingShort} ${theirName} NICE TO MEET U",
                        "${greetingShort} ${theirName} GLAD TO TALK"
                )), this::resolveFacts));
            }
        }
    }

    class AllReceived implements Phrase {
        @Override
        public List<Phrase> reduce(Location location) {
            return of(new LeafPhrase(choose(
                    "ALL RCVD",
                    "CFM RCVD"
            )));
        }

    }

    class ThankForReport implements Phrase {
        @Override
        public List<Phrase> reduce(Location location) {
            return of(new LeafPhrase(choose(
                    "TNX FER RPRT",
                    "TU FER RPRT",
                    "TKS FER RPRT",

                    "TKS FER RST",
                    "TU FER RST",
                    "TNX FER RST"
            )));
        }

    }

    class RSTReport implements Phrase {
        private String observedRST;
        private boolean objectedToBtJoin;

        public RSTReport() {
            observedRST = choose(Lists.newArrayList(
                    "599",
                    "569",
                    "589",
                    "599",
                    "569",
                    "589"
            ));
        }

        private Map<String, String> resolveFacts() {
            return ImmutableMap.of("observedRST", observedRST);
        }

        @Override
        public List<Phrase> reduce(Location location) {
            return of(new LeafPhrase(choose(Lists.newArrayList(
                    "RST ${observedRST} ${observedRST}",
                    "UR ${observedRST} ${observedRST}",
                    "UR RST ${observedRST} ${observedRST}"
            )), this::resolveFacts));
        }

        @Override
        public boolean objectToBtJoin(Phrase next) {
            objectedToBtJoin = randomGuard(0.50);
            return objectedToBtJoin;
        }
    }

    class Qth implements Phrase {
        private String city;
        private String state;

        public Qth() {
            Pair<String, String> cityState = RandomCityGenerator.getCity();
            city = cityState.getKey();
            state = cityState.getValue();
        }

        private Map<String, String> resolveFacts() {
            return ImmutableMap.of("myCity", city, "myState", state);
        }

        @Override
        public List<Phrase> reduce(Location location) {
            if (location.prev instanceof RSTReport) {
                RSTReport prev = (RSTReport) location.prev;
                if (prev.objectedToBtJoin) {
                    return of(new LeafPhrase("IN ${myCity}, ${myState} ${myCity}, ${myState}", this::resolveFacts));
                }
            }

            return of(new LeafPhrase(choose(Lists.newArrayList(
                    "IN ${myCity}, ${myState} ${myCity}, ${myState}",
                    "QTH ${myCity}, ${myState} ${myCity}, ${myState}",
                    "QTH QTH ${myCity}, ${myState} ${myCity}, ${myState}"
            )), this::resolveFacts));
        }

    }

    class KorKN implements Phrase {
        @Override
        public List<Phrase> reduce(Location location) {
            return Lists.newArrayList(new LeafPhrase(choose(Lists.newArrayList(
                    "KN",
                    "K"
            ))));
        }
    }

    class DoingFB implements Phrase {
        public DoingFB(StationState stationState) {

        }

        @Override
        public List<Phrase> reduce(Location location) {
            return of(new LeafPhrase(choose(Lists.newArrayList(
                    "HW?",
                    "HW CPY?",
                    "HW CPI?",
                    "SO HW?"
            ))));
        }

    }

    class RequestRxConfirm implements Phrase {
        @Override
        public List<Phrase> reduce(Location location) {
            return of(new LeafPhrase(choose(Lists.newArrayList(
                    "HW?",
                    "HW CPY?",
                    "HW CPI?",
                    "SO HW?"
            ))));
        }

    }

    class EndToFrom implements Phrase {
        private final String theirName;
        private final List<Phrase> phrases;

        public EndToFrom(StationState me) {
            this.theirName = Name.findName(me.stuffSaid.allStuffTheySaid);
            if (theirName != null && randomGuard(0.1) ) {
                ToFrom from = new ToFrom(me);
                LeafPhrase backToYou = new LeafPhrase("BACK TO YOU " + theirName);

                KorKN k = new KorKN();
                ProsignAR ar = new ProsignAR();
                phrases = Lists.newArrayList(backToYou, ar, from, k);
            } else {
                ToFrom from = new ToFrom(me);
                ProsignAR ar = new ProsignAR();
                KorKN k = new KorKN();
                if (randomGuard(0.5)) {
                    RequestRxConfirm rxConfirm = new RequestRxConfirm();
                    phrases = Lists.newArrayList(ar, rxConfirm, from, k);
                } else {
                    phrases = Lists.newArrayList(ar, from, k);
                }
            }
        }

        @Override
        public List<Phrase> reduce(Location location) {
            return phrases;
        }

    }

    class ProsignAR implements Phrase {
        @Override
        public boolean objectToBtJoin(Phrase next) {
            return true;
        }

        @Override
        public List<Phrase> reduce(Location location) {
            return of(new LeafPhrase("AR"));
        }
    }

    class ToFrom implements Phrase {
        public final String callSign;
        public String theirCallSign;

        private final StationState stationState;

        public ToFrom(StationState me) {
            this.stationState = me;
            this.callSign = stationState.callSign;
            for (Phrase phrase : PhraseUtil.collectAll(stationState.stuffSaid.allStuffTheySaid)) {
                if (phrase instanceof CallCQ) {
                    theirCallSign = ((CallCQ) phrase).callSign;
                } else if (phrase instanceof ToFrom) {
                    theirCallSign = ((ToFrom) phrase).callSign;
                }
            }
        }

        @Override
        public boolean objectToBtJoin(Phrase next) {
            if (next instanceof KorKN) {
                return true;
            }

            return false;
        }

        private Map<String, String> resolveFacts() {
            return ImmutableMap.of(
                    "theirCallSign", theirCallSign,
                    "myCallSign", callSign
            );
        }

        @Override
        public List<Phrase> reduce(Location location) {
            return of(new LeafPhrase("${theirCallSign} DE ${myCallSign}", this::resolveFacts));
        }
    }

    class Name implements Phrase {
        private final String name;

        public static String findName(List<Phrase> stuffSaid) {
            for (Phrase phrase : PhraseUtil.collectAll(stuffSaid)) {
                if (phrase instanceof Name) {
                    Name name = (Name) phrase;
                    return name.name;
                }
            }
            return null;
        }

        public Name(StationState me) {
            String theirName = findName(me.stuffSaid.allStuffISaid);

            name = RandomNameGenerator.getName(theirName);
        }

        private Map<String, String> resolveFacts() {
            return ImmutableMap.of("myName", name);
        }

        @Override
        public List<Phrase> reduce(Location location) {
            return of(new LeafPhrase(choose(Lists.newArrayList(
                    "NAME IS ${myName} ${myName}",
                    "NAME NAME IS ${myName} ${myName}"
            )), this::resolveFacts));
        }
    }

}
