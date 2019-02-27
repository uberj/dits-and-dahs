package com.example.uberj.test1.lettertraining.abrvandprosign;

import com.example.uberj.test1.lettertraining.BaseKeyboardSessionActivity;
import com.example.uberj.test1.lettertraining.BaseStartScreenActivity;
import com.example.uberj.test1.storage.SessionType;

import androidx.fragment.app.DialogFragment;

public class AbbreviationAndProsignStartScreenActivity extends BaseStartScreenActivity {
    @Override
    protected SessionType getSessionType() {
        return SessionType.ABBREVIATION_AND_PROSIGN;
    }

    @Override
    public Class<? extends BaseKeyboardSessionActivity> getSessionActivityClass() {
        return AbbreviationAndProsignSessionTrainingActivity.class;
    }

    @Override
    public DialogFragment getHelpDialog() {
        return null;
    }
}
