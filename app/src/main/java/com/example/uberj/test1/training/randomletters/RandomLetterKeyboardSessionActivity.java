package com.example.uberj.test1.training.randomletters;

import com.example.uberj.test1.KochLetterSequence;
import com.example.uberj.test1.keyboards.KeyConfig;
import com.example.uberj.test1.transcribe.TranscribeKeyboardSessionActivity;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionType;
import com.google.common.collect.ImmutableList;


public class RandomLetterKeyboardSessionActivity extends TranscribeKeyboardSessionActivity {

    @Override
    protected TranscribeSessionType getSessionType() {
        return TranscribeSessionType.RANDOM_LETTER_ONLY;
    }

    @Override
    public ImmutableList<ImmutableList<KeyConfig>> getKeys() {
        return KochLetterSequence.keyboard().getKeys();
    }
}
