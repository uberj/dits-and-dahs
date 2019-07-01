package com.uberj.ditsanddahs.training.randomqso;

import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.google.common.collect.ImmutableList;
import com.uberj.ditsanddahs.KochLetterSequence;
import com.uberj.ditsanddahs.keyboards.KeyConfig;
import com.uberj.ditsanddahs.transcribe.TranscribeKeyboardSessionActivity;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeSessionType;

import java.util.Objects;


public class RandomQSOKeyboardSessionActivity extends TranscribeKeyboardSessionActivity {

    @Override
    protected TranscribeSessionType getSessionType() {
        return TranscribeSessionType.RANDOM_QSO;
    }

    @Override
    public ImmutableList<ImmutableList<KeyConfig>> getKeys() {
        ImmutableList<ImmutableList<KeyConfig>> baseKeys = KochLetterSequence.keyboard().getKeys();
        Optional<Float> firstRowWeightTotal = Stream.of(baseKeys.get(0)).map(kc -> kc.weight).reduce((l, r) -> l + r);
        if (!firstRowWeightTotal.isPresent()) throw new AssertionError("No first row weight present");
        ImmutableList.Builder<ImmutableList<KeyConfig>> builder = ImmutableList.builder();
        builder.addAll(baseKeys);

        ImmutableList<KeyConfig> controlRow = ImmutableList.of(
                KeyConfig.s(4), KeyConfig.ctrl(KeyConfig.ControlType.SPACE), KeyConfig.s(2), KeyConfig.ctrl(KeyConfig.ControlType.DELETE)
        );
        Optional<Float> controlRowWeight = Stream.of(controlRow).map(kc -> kc.weight).reduce((l, r) -> l + r);
        if (!controlRowWeight.isPresent()) throw new AssertionError("Control weight not present");
        if (!Objects.equals(controlRowWeight.get(), firstRowWeightTotal.get())) throw new AssertionError("No first row weight present");

        builder.add(controlRow);
        return builder.build();
    }
}
