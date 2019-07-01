package com.uberj.ditsanddahs.transcribe;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Supplier;
import com.google.common.collect.ImmutableList;

import org.apache.commons.math3.distribution.EnumeratedDistribution;
import org.apache.commons.math3.util.Pair;

import java.util.List;

import timber.log.Timber;

public class RandomLetterSupplier implements Supplier<String> {
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

    public RandomLetterSupplier(List<org.apache.commons.lang3.tuple.Pair<String, Double>> inPlayLetters) {
        this.nextLetterDistribution = new EnumeratedDistribution<>(letterWeights(inPlayLetters));
    }

    private List<Pair<String, Double>> letterWeights(List<org.apache.commons.lang3.tuple.Pair<String, Double>> inPlayLetters) {
        return Stream.of(inPlayLetters).map(pair -> Pair.create(pair.getKey(), pair.getValue())).collect(Collectors.toList());
    }


    @Override
    public String get() {
        if (lettersLeftInGroup <= 0) {
            lettersLeftInGroup = LENGTH_DISTRIBUTION.sample();
            Timber.d("Planning on playing %s letters", lettersLeftInGroup);
            return " ";
        }

        lettersLeftInGroup -= 1;

        return nextLetterDistribution.sample();
    }
}
