package com.uberj.pocketmorsepro.training.simple;

import com.uberj.pocketmorsepro.KochLetterSequence;
import com.uberj.pocketmorsepro.keyboards.Keys;
import com.uberj.pocketmorsepro.simplesocratic.SocraticKeyboardSessionActivity;
import com.uberj.pocketmorsepro.simplesocratic.storage.SocraticSessionType;

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
