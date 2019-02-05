package com.example.uberj.test1;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.LinearLayout;

import com.example.uberj.test1.keyboards.KeyConfig;
import com.google.common.collect.ImmutableList;

import org.apache.commons.lang3.tuple.Pair;

import static com.example.uberj.test1.keyboards.KeyConfig.l;
import static com.example.uberj.test1.keyboards.KeyConfig.p;
import static com.example.uberj.test1.keyboards.KeyConfig.s;

/*
This class is for exploring dynamicly creating a keyboard at runtime using the java layout apis
 */
public class DynamicKeyboardActivity extends AppCompatActivity {
    private static final Pair<Float, String> HSM = Pair.of(0.5f, null);
    ImmutableList<ImmutableList<KeyConfig>> keys = ImmutableList.of(
            ImmutableList.of(l("1"), l("2"), l("3"), l("4"), l("5"), l("6"), l("7"), l("8"), l("9"), l("0"), l("/"), l("=")),
            ImmutableList.of(s(), l("q"), l("w"), l("e"), l("r"), l("t"), l("y"), l("u"), l("i"), l("o"), l("p"), l("."), s()),
            ImmutableList.of(s(), s(), l("a"), l("s"), l("d"), l("f"), l("g"), l("h"), l("j"), l("l"), l("l"), l("?"), s(), s()),
            ImmutableList.of(s(), s(), s(), l("z"), l("x"), l("c"), l("v"), l("b"), l("n"), l("m"), l(","), l("."), s(), s(), s()),
            ImmutableList.of(s(), s(), s(), p("QRL"), p("QRM"), p("QRN"), p("QRQ"), p("QRS"), p("QRZ"), p("QTH"), p("QSB"), p("QSY"), s(), s(), s())
    );

    private Pair<Float, String> s1(String s) {
        return Pair.of(1f, s);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.keyboard_activity_v2);
        LinearLayout rootView = findViewById(R.id.dynamic_keyboard);
        DynamicKeyboard dynamicKeyboardBuilder = new DynamicKeyboard.Builder()
                .setContext(this)
                .setKeys(keys).setButtonOnClickListener((x) -> {})
                .setButtonLongClickListener((x) -> true)
                .createKeyboardBuilder();
        dynamicKeyboardBuilder.buildAtRoot(rootView);
    }

}
