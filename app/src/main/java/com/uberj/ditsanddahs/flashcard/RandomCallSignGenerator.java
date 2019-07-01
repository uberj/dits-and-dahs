package com.uberj.ditsanddahs.flashcard;

import com.google.common.collect.ImmutableList;

import java.util.Random;

public class RandomCallSignGenerator {
    private static final ImmutableList.Builder<String> callsBuilder = new ImmutableList.Builder<>();
    private static final ImmutableList<String> calls;
    private static final Random r = new Random();

    static {
        callsBuilder.add("KC3HGW");
        callsBuilder.add("KF4OJG");
        callsBuilder.add("N4TBR");
        callsBuilder.add("WB5QBG");
        callsBuilder.add("KB1VWC");
        callsBuilder.add("KB9ACW");
        callsBuilder.add("K4MSS");
        callsBuilder.add("KI7SBZ");
        callsBuilder.add("KB9PGY");
        callsBuilder.add("K8DEM");
        callsBuilder.add("WP4OOT");
        callsBuilder.add("KC2NJT");
        callsBuilder.add("N0PG");
        callsBuilder.add("KB8ZGF");
        callsBuilder.add("KC6SQM");
        callsBuilder.add("K3KAA");
        callsBuilder.add("KG7CMN");
        callsBuilder.add("KC2BYD");
        callsBuilder.add("N7UNN");
        callsBuilder.add("N4QWK");
        callsBuilder.add("KE7IMZ");
        callsBuilder.add("KC3ISA");
        callsBuilder.add("KF4FZR");
        callsBuilder.add("W9KOJ");
        callsBuilder.add("WA1HAH");
        callsBuilder.add("KA1ZUO");
        callsBuilder.add("KB5IE");
        callsBuilder.add("N5EFH");
        callsBuilder.add("KE7OKY");
        callsBuilder.add("KA3WQO");
        callsBuilder.add("W7EOZ");
        callsBuilder.add("KJ6POI");
        callsBuilder.add("WB4EAK");
        callsBuilder.add("KI4KGP");
        callsBuilder.add("KB1WQU");
        callsBuilder.add("KA8CVT");
        callsBuilder.add("N8KMQ");
        callsBuilder.add("KC3BGZ");
        callsBuilder.add("AJ4OH");
        callsBuilder.add("N9GOA");
        callsBuilder.add("KB8SAV");
        callsBuilder.add("N1TCE");
        callsBuilder.add("KB0SLW");
        callsBuilder.add("N8BCH");
        callsBuilder.add("KI5AJH");
        callsBuilder.add("K2INH");
        callsBuilder.add("W8JO");
        callsBuilder.add("N1LZU");
        callsBuilder.add("KB0YKW");
        callsBuilder.add("KA9NIU");
        callsBuilder.add("N9SYI");
        callsBuilder.add("N0WQY");
        callsBuilder.add("KB5HQS");
        callsBuilder.add("KE6WEZ");
        callsBuilder.add("KF4CZY");
        callsBuilder.add("KB5YOE");
        callsBuilder.add("KX7EMT");
        callsBuilder.add("KC9MCM");
        callsBuilder.add("KC0KWK");
        callsBuilder.add("K9NMR");
        callsBuilder.add("AB2JD");
        callsBuilder.add("KM4UTL");
        callsBuilder.add("KI4LLG");
        callsBuilder.add("KF4PFS");
        callsBuilder.add("N3XEJ");
        callsBuilder.add("KC3LLR");
        callsBuilder.add("KC9LTJ");
        callsBuilder.add("W3UJO");
        callsBuilder.add("KE7QLN");
        callsBuilder.add("KC4HGR");
        callsBuilder.add("KD8POG");
        callsBuilder.add("N0UQU");
        callsBuilder.add("KD8ZOK");
        callsBuilder.add("KI4ONB");
        callsBuilder.add("KG6NJO");
        callsBuilder.add("KB9LXQ");
        callsBuilder.add("N6JXN");
        callsBuilder.add("N6PTQ");
        callsBuilder.add("KA5VVT");
        callsBuilder.add("KG6KIF");
        callsBuilder.add("KM4MBS");
        callsBuilder.add("KC1FNW");
        callsBuilder.add("KI6JCW");
        callsBuilder.add("KN4PQY");
        callsBuilder.add("K7HJ");
        callsBuilder.add("AD5SF");
        callsBuilder.add("N0WZC");
        callsBuilder.add("WB8BJX");
        callsBuilder.add("KI4GQT");
        callsBuilder.add("N4PZR");
        callsBuilder.add("K4MMD");
        callsBuilder.add("KC2MDY");
        callsBuilder.add("KA3JQB");
        callsBuilder.add("KE5JKZ");
        callsBuilder.add("NE6DP");
        callsBuilder.add("N2CRU");
        callsBuilder.add("KD4GJD");
        callsBuilder.add("KJ4THD");
        callsBuilder.add("KE7RMY");
        callsBuilder.add("KA7HTO");
        callsBuilder.add("WP4QFI");
        callsBuilder.add("KN4LFH");
        callsBuilder.add("KG7WYQ");
        callsBuilder.add("N4ORX");
        callsBuilder.add("KE8GBQ");
        callsBuilder.add("KF7KLL");
        callsBuilder.add("KM4TFG");
        callsBuilder.add("N6LGX");
        callsBuilder.add("KI4YFY");
        callsBuilder.add("KE0EIA");
        callsBuilder.add("KB7NEZ");
        callsBuilder.add("KF6HRY");
        callsBuilder.add("K9BJL");
        callsBuilder.add("KD4GIH");
        callsBuilder.add("KC2FLO");
        callsBuilder.add("KI6MVV");
        callsBuilder.add("N9SZP");
        callsBuilder.add("KG4CBP");
        callsBuilder.add("N1WFN");
        callsBuilder.add("WB0TYZ");
        callsBuilder.add("WB6JMM");
        callsBuilder.add("KD2PXB");
        callsBuilder.add("KG4RSI");
        calls = callsBuilder.build();
    }

    public static String getCall() {
        return calls.get(r.nextInt(calls.size()));
    }
}
