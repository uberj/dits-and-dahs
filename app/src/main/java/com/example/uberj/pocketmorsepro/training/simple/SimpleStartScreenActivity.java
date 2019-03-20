package com.example.uberj.morsepocketpro.training.simple;

import com.example.uberj.morsepocketpro.socratic.SocraticKeyboardSessionActivity;
import com.example.uberj.morsepocketpro.socratic.SocraticStartScreenActivity;
import com.example.uberj.morsepocketpro.socratic.storage.SocraticSessionType;

import androidx.fragment.app.DialogFragment;

public class SimpleStartScreenActivity extends SocraticStartScreenActivity {
    @Override
    protected SocraticSessionType getSessionType() {
        return SocraticSessionType.LETTER_ONLY;
    }

    @Override
    public Class<? extends SocraticKeyboardSessionActivity> getSessionActivityClass() {
        return SimpleLetterTrainingActivity.class;
    }

    @Override
    public DialogFragment getHelpDialog() {
        return new SimpleLetterTrainingStartScreenHelpDialog();
    }
}
