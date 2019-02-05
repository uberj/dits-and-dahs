package com.example.uberj.test1;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;

import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.tuple.Pair;

/*
This class is for exploring dynamicly creating a keyboard at runtime using the java layout apis
 */
public class DynamicKeyboardActivity extends Activity {
    private static final Pair<Float, String> HSM = Pair.of(0.5f, null);
    ImmutableList<ImmutableList<Pair<Float, String>>> keys = ImmutableList.of(
            ImmutableList.of(s1("1"), s1("2"), s1("3"), s1("4"), s1("5"), s1("6"), s1("7"), s1("8"), s1("9"), s1("0"), s1("/"), s1("=")),
            ImmutableList.of(HSM, s1("q"), s1("w"), s1("e"), s1("r"), s1("t"), s1("y"), s1("u"), s1("i"), s1("o"), s1("p"), s1("."), HSM),
            ImmutableList.of(HSM, HSM, s1("a"), s1("s"), s1("d"), s1("f"), s1("g"), s1("h"), s1("j"), s1("k"), s1("l"), s1("?"), HSM, HSM),
            ImmutableList.of(HSM, HSM, HSM, s1("z"), s1("x"), s1("c"), s1("v"), s1("b"), s1("n"), s1("m"), s1(","), s1("."), HSM, HSM, HSM),
            ImmutableList.of(HSM, HSM, HSM, s1("QRL"), s1("QRM"), s1("QRN"), s1("QRQ"), s1("QRS"), s1("QRZ"), s1("QTH"), s1("QSB"), s1("QSY"), HSM, HSM, HSM)
    );

    private Pair<Float, String> s1(String s) {
        return Pair.of(1f, s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyboard_activity_v2);
        LinearLayout rootView = findViewById(R.id.dynamic_keyboard);
        KeyboardBuilder keyboardBuilder = new KeyboardBuilder(this, keys);
        keyboardBuilder.buildAtRoot(rootView);
    }

}
