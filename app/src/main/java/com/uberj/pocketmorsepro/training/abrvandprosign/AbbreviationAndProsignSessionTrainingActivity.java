package com.uberj.pocketmorsepro.training.abrvandprosign;

import com.uberj.pocketmorsepro.keyboards.Keys;
import com.uberj.pocketmorsepro.socratic.SocraticKeyboardSessionActivity;
import com.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;

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
