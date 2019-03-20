package com.example.uberj.morsepocketpro.training.abrvandprosign;

import com.example.uberj.morsepocketpro.keyboards.KeyConfig;
import com.example.uberj.morsepocketpro.keyboards.Keys;
import com.google.common.collect.ImmutableList;

import static com.example.uberj.morsepocketpro.keyboards.KeyConfig.l;
import static com.example.uberj.morsepocketpro.keyboards.KeyConfig.p;
import static com.example.uberj.morsepocketpro.keyboards.KeyConfig.s;

public class AbbreviationAndProsignKeys implements Keys {
    /*
    /
73
=
QRL
QRM
QRN
QRQ
QRS
QRZ
QSB
QSY
QTH
agn
ant
cq
dx
es
fb
ga
ge
hi
hr
hw
name
nr
om
pse
pwr
r
rst
rtu
tnx
tu
wx
     */
    @Override
    public ImmutableList<ImmutableList<KeyConfig>> getKeys() {
        return ImmutableList.of(
                ImmutableList.of(l("1"), l("2"), l("3"), l("4"), l("5"), l("6"), l("7"), l("8"), l("9"), l("0"), l("/"), p("=")),
                ImmutableList.of(s(), l("Q"), l("W"), l("e"), l("r"), l("T"), l("Y"), l("U"), l("I"), l("O"), l("P"), s(1), s()),
                ImmutableList.of(s(1), l("A"), l("S"), l("D"), l("F"), l("G"), l("H"), l("J"), l("K"), l("L"), l("?"), s(1)),
                ImmutableList.of(s(), l(","), l("Z"), l("X"), l("C"), l("V"), l("B"), l("N"), l("M"), l("."), s(1), s())
        );
    }
}
