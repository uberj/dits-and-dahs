package com.example.uberj.test1.lettertraining.simple;

import com.example.uberj.test1.keyboards.Keys;
import com.example.uberj.test1.lettertraining.BaseKeyboardSessionActivity;
import com.example.uberj.test1.storage.SessionType;

public class SimpleLetterTrainingActivity extends BaseKeyboardSessionActivity {
    @Override
    protected Keys getSessionKeys() {
        return new SimpleLetterKeys();
    }

    @Override
    public SessionType getSessionType() {
        return SessionType.LETTER_ONLY;
    }
}
