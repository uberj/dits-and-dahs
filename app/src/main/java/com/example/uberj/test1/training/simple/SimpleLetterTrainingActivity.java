package com.example.uberj.test1.training.simple;

import com.example.uberj.test1.KochLetterSequence;
import com.example.uberj.test1.keyboards.Keys;
import com.example.uberj.test1.socratic.SocraticKeyboardSessionActivity;
import com.example.uberj.test1.socratic.storage.SocraticSessionType;

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
