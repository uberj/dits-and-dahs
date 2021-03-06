package com.uberj.ditsanddahs.training.simple;

import com.uberj.ditsanddahs.keyboards.KeyConfig;
import com.uberj.ditsanddahs.keyboards.Keys;
import com.google.common.collect.ImmutableList;

import static com.uberj.ditsanddahs.keyboards.KeyConfig.l;
import static com.uberj.ditsanddahs.keyboards.KeyConfig.p;
import static com.uberj.ditsanddahs.keyboards.KeyConfig.s;

public class SimpleLetterKeys implements Keys {
    @Override
    public ImmutableList<ImmutableList<KeyConfig>> getKeys() {
        return ImmutableList.of(
                ImmutableList.of(l("1"), l("2"), l("3"), l("4"), l("5"), l("6"), l("7"), l("8"), l("9"), l("0"), l("/"), p("=")),
                ImmutableList.of(s(), l("Q"), l("W"), l("E"), l("R"), l("T"), l("Y"), l("U"), l("I"), l("O"), l("P"), s(1), s()),
                ImmutableList.of(s(1), l("A"), l("S"), l("D"), l("F"), l("G"), l("H"), l("J"), l("K"), l("L"), l("?"), s(1)),
                ImmutableList.of(s(), l(","), l("Z"), l("X"), l("C"), l("V"), l("B"), l("N"), l("M"), l("."), s(2), s())
        );
    }
}
