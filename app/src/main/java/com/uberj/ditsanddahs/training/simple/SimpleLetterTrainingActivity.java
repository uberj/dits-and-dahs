package com.uberj.ditsanddahs.training.simple;

import com.uberj.ditsanddahs.KochLetterSequence;
import com.uberj.ditsanddahs.keyboards.Keys;
import com.uberj.ditsanddahs.simplesocratic.SocraticKeyboardSessionActivity;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticSessionType;

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
