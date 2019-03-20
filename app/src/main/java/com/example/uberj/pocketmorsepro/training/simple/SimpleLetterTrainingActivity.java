package com.example.uberj.morsepocketpro.training.simple;

import com.example.uberj.morsepocketpro.KochLetterSequence;
import com.example.uberj.morsepocketpro.keyboards.Keys;
import com.example.uberj.morsepocketpro.socratic.SocraticKeyboardSessionActivity;
import com.example.uberj.morsepocketpro.socratic.storage.SocraticSessionType;

import androidx.fragment.app.DialogFragment;

public class SimpleLetterTrainingActivity extends SocraticKeyboardSessionActivity {
    @Override
    protected Keys getSessionKeys() {
        return KochLetterSequence.keyboard();
    }

    @Override
    public SocraticSessionType getSessionType() {
        return SocraticSessionType.LETTER_ONLY;
    }

    @Override
    protected DialogFragment getHelpDialog() {
        return new SimpleLetterTrainingHelpDialog();
    }
}
