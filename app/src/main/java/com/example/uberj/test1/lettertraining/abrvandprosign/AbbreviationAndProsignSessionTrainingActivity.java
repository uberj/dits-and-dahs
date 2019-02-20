package com.example.uberj.test1.lettertraining.abrvandprosign;

import com.example.uberj.test1.keyboards.Keys;
import com.example.uberj.test1.lettertraining.BaseKeyboardSessionActivity;
import com.example.uberj.test1.storage.SessionType;

public class AbbreviationAndProsignSessionTrainingActivity extends BaseKeyboardSessionActivity {
    @Override
    protected Keys getSessionKeys() {
        return new AbbreviationAndProsignKeys();
    }

    @Override
    public SessionType getSessionType() {
        return SessionType.LETTER_ONLY;
    }
}
