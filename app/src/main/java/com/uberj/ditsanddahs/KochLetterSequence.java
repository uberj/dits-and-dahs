package com.uberj.ditsanddahs;

import com.uberj.ditsanddahs.keyboards.Keys;
import com.google.common.collect.ImmutableList;

import static com.uberj.ditsanddahs.keyboards.KeyConfig.l;
import static com.uberj.ditsanddahs.keyboards.KeyConfig.p;
import static com.uberj.ditsanddahs.keyboards.KeyConfig.s;

public class KochLetterSequence {
    public static final ImmutableList<String> sequence = new ImmutableList.Builder<String>()
            .add("K")
            .add("M")
            .add("U")
            .add("R")
            .add("E")
            .add("S")
            .add("N")
            .add("A")
            .add("P")
            .add("T")
            .add("L")
            .add("W")
            .add("I")
            .add(".")
            .add("J")
            .add("Z")
            .add("=")
            .add("F")
            .add("O")
            .add("Y")
            .add(",")
            .add("V")
            .add("G")
            .add("5")
            .add("/")
            .add("Q")
            .add("9")
            .add("2")
            .add("H")
            .add("3")
            .add("8")
            .add("B")
            .add("?")
            .add("4")
            .add("7")
            .add("C")
            .add("1")
            .add("D")
            .add("6")
            .add("0")
            .add("X")
            .build();

    public static Keys keyboard() {
        return () -> ImmutableList.of(
                ImmutableList.of(l("1"), l("2"), l("3"), l("4"), l("5"), l("6"), l("7"), l("8"), l("9"), l("0"), l("/"), p("=")),
                ImmutableList.of(s(), l("Q"), l("W"), l("e"), l("r"), l("T"), l("Y"), l("U"), l("I"), l("O"), l("P"), s(1), s()),
                ImmutableList.of(s(1), l("A"), l("S"), l("D"), l("F"), l("G"), l("H"), l("J"), l("K"), l("L"), l("?"), s(1)),
                ImmutableList.of(s(), l(","), l("Z"), l("X"), l("C"), l("V"), l("B"), l("N"), l("M"), l("."), s(2), s())
        );
    }

}
