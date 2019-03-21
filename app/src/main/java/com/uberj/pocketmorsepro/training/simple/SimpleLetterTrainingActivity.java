package com.uberj.pocketmorsepro.training.simple;

import com.uberj.pocketmorsepro.KochLetterSequence;
import com.uberj.pocketmorsepro.keyboards.Keys;
import com.uberj.pocketmorsepro.socratic.SocraticKeyboardSessionActivity;
import com.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;

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
