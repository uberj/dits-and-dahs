package com.example.uberj.test1.lettertraining.simple;

import com.example.uberj.test1.lettertraining.BaseKeyboardSessionActivity;
import com.example.uberj.test1.lettertraining.BaseStartScreenActivity;
import com.example.uberj.test1.storage.SessionType;

import androidx.fragment.app.DialogFragment;

public class SimpleStartScreenActivity extends BaseStartScreenActivity {
    @Override
    protected SessionType getSessionType() {
        return SessionType.LETTER_ONLY;
    }

    @Override
    public Class<? extends BaseKeyboardSessionActivity> getSessionActivityClass() {
        return SimpleLetterTrainingActivity.class;
    }

    @Override
    public DialogFragment getHelpDialog() {
        return new SimpleLetterTrainingStartScreenHelpDialog();
    }
}
