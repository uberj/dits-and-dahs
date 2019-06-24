package com.uberj.ditsanddahs.qsolib.phrase;

import com.google.common.collect.ImmutableMap;
import com.uberj.ditsanddahs.qsolib.data.RandomJobGenerator;

import java.util.List;
import java.util.Map;
import java.util.Random;

import static com.google.common.collect.ImmutableList.of;
import static com.uberj.ditsanddahs.qsolib.RandomUtil.choose;

public class Job implements Phrase {
    private static final Random r = new Random();
    private final String job;

    public Job() {
        job = RandomJobGenerator.getJob();
    }

    private Map<String, String> resolveFacts() {
        return ImmutableMap.of(
                "job", job
        );
    }


    @Override
    public List<Phrase> reduce(Location location) {
        String aAn = starsWithVowel(job) ? "AN" : "A";
        return of(new LeafPhrase(choose(
                "CURRENT JOB IS ${job}",
                "I WORK AS " + aAn + " ${job}",
                "PROFESSION IS ${job}",
                "I AM " + aAn + " ${job}",
                "I AM A PROFESSIONAL ${job}"
        ), this::resolveFacts));
    }

    private static boolean starsWithVowel(String job) {
        return job.startsWith("A") ||
                job.startsWith("E") ||
                job.startsWith("I") ||
                job.startsWith("O") ||
                job.startsWith("U");
    }
}
