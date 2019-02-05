package com.example.uberj.test1.keyboards;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.List;

import static com.example.uberj.test1.keyboards.KeyConfig.l;
import static com.example.uberj.test1.keyboards.KeyConfig.p;
import static com.example.uberj.test1.keyboards.KeyConfig.s;

public class SimpleLetters {
    public static final ImmutableList<ImmutableList<KeyConfig>> keys = ImmutableList.of(
            ImmutableList.of(l("1"), l("2"), l("3"), l("4"), l("5"), l("6"), l("7"), l("8"), l("9"), l("0"), l("/"), p("BT")),
            ImmutableList.of(s(), l("Q"), l("W"), l("e"), l("r"), l("T"), l("Y"), l("U"), l("I"), l("O"), l("P"), p("73"), s()),
            ImmutableList.of(s(), s(), l("A"), l("S"), l("D"), l("F"), l("G"), l("H"), l("J"), l("K"), l("L"), l("?"), s(), s()),
            ImmutableList.of(s(), l(","), l("Z"), l("X"), l("C"), l("V"), l("B"), l("N"), l("M"), p("SK"), l("."), p("AR"), s())
    );


    public static List<String> allPlayableKeysNames() {
        List<String> inPlayLetters = Lists.newArrayList();
        for (ImmutableList<KeyConfig> row : keys) {
            for (KeyConfig keyConfig : row) {
                if (keyConfig.isPlayable) {
                    inPlayLetters.add(keyConfig.textName);
                }
            }
        }
        return inPlayLetters;
    }
}
