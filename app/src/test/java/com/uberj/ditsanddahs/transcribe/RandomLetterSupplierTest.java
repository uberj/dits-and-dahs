package com.uberj.ditsanddahs.transcribe;

import com.google.common.collect.Lists;

import org.apache.commons.lang3.tuple.Pair;
import org.junit.Test;

public class RandomLetterSupplierTest {

    @Test
    public void testOutputVisually() {
        RandomLetterSupplier randomLetterSupplier = new RandomLetterSupplier(Lists.newArrayList(
                Pair.of("A", 1D),
                Pair.of("B", 1D),
                Pair.of("C", 1D),
                Pair.of("D", 1D)
        ), null);

        for (int i = 0; i < 100; i++) {
            System.out.println(randomLetterSupplier.get());
        }
    }

}