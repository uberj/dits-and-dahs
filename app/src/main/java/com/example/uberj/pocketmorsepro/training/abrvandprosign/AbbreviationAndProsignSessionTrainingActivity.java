package com.example.uberj.pocketmorsepro.training.abrvandprosign;

import com.example.uberj.pocketmorsepro.keyboards.Keys;
import com.example.uberj.pocketmorsepro.socratic.SocraticKeyboardSessionActivity;
import com.example.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;

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
