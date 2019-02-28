package com.example.uberj.test1.lettertraining.randomletters;

import com.example.uberj.test1.keyboards.KeyConfig;
import com.example.uberj.test1.transcribe.TranscribeKeyboardSessionActivity;
import com.example.uberj.test1.socratic.storage.SocraticSessionType;
import com.google.common.collect.ImmutableList;


public class RandomLetterKeyboardSessionActivity extends TranscribeKeyboardSessionActivity {

    @Override
    protected SocraticSessionType getSessionType() {
        return null;
    }

    @Override
    public ImmutableList<ImmutableList<KeyConfig>> getKeys() {
        return null;
    }
}
