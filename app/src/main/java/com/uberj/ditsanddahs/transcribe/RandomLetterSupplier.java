package com.uberj.ditsanddahs.transcribe;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Supplier;
import com.google.common.collect.ImmutableList;
import com.uberj.ditsanddahs.AudioManager;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.List;

import timber.log.Timber;

public class RandomLetterSupplier implements Supplier<org.apache.commons.lang3.tuple.Pair<String, AudioManager.MorseConfig>> {
    private final EnumeratedDistribution<String> nextLetterDistribution;
    private static final EnumeratedDistribution<Integer> LENGTH_DISTRIBUTION = new EnumeratedDistribution<>(ImmutableList.of(
            Pair.create(2, 3D),
            Pair.create(3, 4D),
            Pair.create(4, 5D),
            Pair.create(5, 5D),
            Pair.create(6, 5D),
            Pair.create(7, 5D),
            Pair.create(8, 3D),
            Pair.create(9, 3D),
            Pair.create(10, 1D),
            Pair.create(11, 1D),
            Pair.create(12, 1D)
    ));

    private int lettersLeftInGroup = LENGTH_DISTRIBUTION.sample();
    private final AudioManager.MorseConfig morseConfig;
    private boolean pumpLetterSpace = false;

    public RandomLetterSupplier(List<org.apache.commons.lang3.tuple.Pair<String, Double>> inPlayLetters, AudioManager.MorseConfig morseConfig) {
        this.nextLetterDistribution = new EnumeratedDistribution<>(letterWeights(inPlayLetters));
        this.morseConfig = morseConfig;
    }

    private List<Pair<String, Double>> letterWeights(List<org.apache.commons.lang3.tuple.Pair<String, Double>> inPlayLetters) {
        return Stream.of(inPlayLetters).map(pair -> Pair.create(pair.getKey(), pair.getValue())).collect(Collectors.toList());
    }


    @Override
    public org.apache.commons.lang3.tuple.Pair<String, AudioManager.MorseConfig> get() {
        if (lettersLeftInGroup <= 0) {
            lettersLeftInGroup = LENGTH_DISTRIBUTION.sample();
            Timber.d("Planning on playing %s letters", lettersLeftInGroup);
            pumpLetterSpace = false;
            return org.apache.commons.lang3.tuple.Pair.of(String.valueOf(AudioManager.WORD_SPACE), morseConfig);
        }

        if (pumpLetterSpace) {
            pumpLetterSpace = false;
            return org.apache.commons.lang3.tuple.Pair.of(String.valueOf(AudioManager.LETTER_SPACE), morseConfig);
        } else {
            lettersLeftInGroup -= 1;
            pumpLetterSpace = true;
        }

        return org.apache.commons.lang3.tuple.Pair.of(nextLetterDistribution.sample(), morseConfig);
    }
}
