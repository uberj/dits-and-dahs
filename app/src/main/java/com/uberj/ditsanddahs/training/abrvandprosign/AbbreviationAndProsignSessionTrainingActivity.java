package com.uberj.ditsanddahs.training.abrvandprosign;

import com.uberj.ditsanddahs.keyboards.Keys;
import com.uberj.ditsanddahs.simplesocratic.SocraticKeyboardSessionActivity;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticSessionType;

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
