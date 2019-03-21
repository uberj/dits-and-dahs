package com.example.uberj.pocketmorsepro.training.simple;

import com.example.uberj.pocketmorsepro.KochLetterSequence;
import com.example.uberj.pocketmorsepro.keyboards.Keys;
import com.example.uberj.pocketmorsepro.socratic.SocraticKeyboardSessionActivity;
import com.example.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;

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
