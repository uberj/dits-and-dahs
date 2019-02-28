package com.example.uberj.test1.training.simple;

import com.example.uberj.test1.socratic.SocraticKeyboardSessionActivity;
import com.example.uberj.test1.training.BaseStartScreenActivity;
import com.example.uberj.test1.socratic.storage.SocraticSessionType;

import androidx.fragment.app.DialogFragment;

public class SimpleStartScreenActivity extends BaseStartScreenActivity {
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
