package com.uberj.pocketmorsepro.training.simplewordflashcards;

import com.google.common.collect.ImmutableList;
import com.uberj.pocketmorsepro.CommonWords;
import com.uberj.pocketmorsepro.KochLetterSequence;
import com.uberj.pocketmorsepro.flashcard.FlashcardKeyboardSessionActivity;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardSessionType;
import com.uberj.pocketmorsepro.keyboards.KeyConfig;
import com.uberj.pocketmorsepro.keyboards.Keys;

import java.util.Objects;
import java.util.Optional;

import androidx.fragment.app.DialogFragment;


public class SimpleWordFlashcardKeyboardSessionActivity extends FlashcardKeyboardSessionActivity {

    @Override
    protected Keys getSessionKeys() {
        return () -> {
            ImmutableList<ImmutableList<KeyConfig>> baseKeys = CommonWords.keyboard().getKeys();
            Optional<Float> firstRowWeightTotal = baseKeys.get(0).stream().map(kc -> kc.weight).reduce((l, r) -> l + r);
            if (!firstRowWeightTotal.isPresent()) throw new AssertionError("No first row weight present");
            ImmutableList.Builder<ImmutableList<KeyConfig>> builder = ImmutableList.builder();
            builder.addAll(baseKeys);

            ImmutableList<KeyConfig> controlRow = ImmutableList.of(
                    KeyConfig.s(4), KeyConfig.ctrl(KeyConfig.ControlType.SPACE), KeyConfig.s(2), KeyConfig.ctrl(KeyConfig.ControlType.DELETE)
            );
            ImmutableList<KeyConfig> cardControlRow = ImmutableList.of(
                    KeyConfig.ctrl(KeyConfig.ControlType.AGAIN), KeyConfig.s(7), KeyConfig.ctrl(KeyConfig.ControlType.SKIP), KeyConfig.ctrl(KeyConfig.ControlType.SUBMIT)
            );
            Optional<Float> controlRowWeight = controlRow.stream().map(kc -> kc.weight).reduce((l, r) -> l + r);
            if (!controlRowWeight.isPresent()) throw new AssertionError("Control weight not present");
            if (!Objects.equals(controlRowWeight.get(), firstRowWeightTotal.get())) throw new AssertionError("Unequal Weights");

            Optional<Float> cardControl = cardControlRow.stream().map(kc -> kc.weight).reduce((l, r) -> l + r);
            if (!cardControl.isPresent()) throw new AssertionError("Card control weight not present");
            if (!Objects.equals(cardControl.get(), firstRowWeightTotal.get())) throw new AssertionError("Unequal Weights");

            builder.add(controlRow);
            builder.add(cardControlRow);
            return builder.build();
        };
    }

    @Override
    public FlashcardSessionType getSessionType() {
        return FlashcardSessionType.RANDOM_WORDS;
    }

    @Override
    protected DialogFragment getHelpDialog() {
        return new SimpleWordFlashcardKeyboardSessionHelpDialog();
    }
}
