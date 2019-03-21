package com.uberj.pocketmorsepro.training.randomletters;

import com.uberj.pocketmorsepro.KochLetterSequence;
import com.uberj.pocketmorsepro.keyboards.KeyConfig;
import com.uberj.pocketmorsepro.transcribe.TranscribeKeyboardSessionActivity;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeSessionType;
import com.google.common.collect.ImmutableList;

import java.util.Objects;
import java.util.Optional;


public class RandomLetterKeyboardSessionActivity extends TranscribeKeyboardSessionActivity {

    @Override
    protected TranscribeSessionType getSessionType() {
        return TranscribeSessionType.RANDOM_LETTER_ONLY;
    }

    @Override
    public ImmutableList<ImmutableList<KeyConfig>> getKeys() {
        ImmutableList<ImmutableList<KeyConfig>> baseKeys = KochLetterSequence.keyboard().getKeys();
        Optional<Float> firstRowWeightTotal = baseKeys.get(0).stream().map(kc -> kc.weight).reduce((l, r) -> l + r);
        if (!firstRowWeightTotal.isPresent()) throw new AssertionError("No first row weight present");
        ImmutableList.Builder<ImmutableList<KeyConfig>> builder = ImmutableList.builder();
        builder.addAll(baseKeys);

        ImmutableList<KeyConfig> controlRow = ImmutableList.of(
                KeyConfig.s(4), KeyConfig.ctrl(KeyConfig.ControlType.SPACE), KeyConfig.s(2), KeyConfig.ctrl(KeyConfig.ControlType.DELETE)
        );
        Optional<Float> controlRowWeight = controlRow.stream().map(kc -> kc.weight).reduce((l, r) -> l + r);
        if (!controlRowWeight.isPresent()) throw new AssertionError("Control weight not present");
        if (!Objects.equals(controlRowWeight.get(), firstRowWeightTotal.get())) throw new AssertionError("No first row weight present");

        builder.add(controlRow);
        return builder.build();
    }
}
