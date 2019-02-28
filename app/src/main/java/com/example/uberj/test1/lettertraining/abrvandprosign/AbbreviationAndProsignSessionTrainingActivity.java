package com.example.uberj.test1.lettertraining.abrvandprosign;

import com.example.uberj.test1.keyboards.Keys;
import com.example.uberj.test1.socratic.SocraticKeyboardSessionActivity;
import com.example.uberj.test1.storage.SocraticSessionType;

import androidx.fragment.app.DialogFragment;

public class AbbreviationAndProsignSessionTrainingActivity extends SocraticKeyboardSessionActivity {
    @Override
    protected Keys getSessionKeys() {
        return new AbbreviationAndProsignKeys();
    }

    @Override
    public SocraticSessionType getSessionType() {
        return SocraticSessionType.LETTER_ONLY;
    }

    @Override
    protected DialogFragment getHelpDialog() {
        // TODO
        return null;
    }
}
