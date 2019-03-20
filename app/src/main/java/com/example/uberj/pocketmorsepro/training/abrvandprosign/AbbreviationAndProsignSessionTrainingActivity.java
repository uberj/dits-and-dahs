package com.example.uberj.morsepocketpro.training.abrvandprosign;

import com.example.uberj.morsepocketpro.keyboards.Keys;
import com.example.uberj.morsepocketpro.socratic.SocraticKeyboardSessionActivity;
import com.example.uberj.morsepocketpro.socratic.storage.SocraticSessionType;

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
