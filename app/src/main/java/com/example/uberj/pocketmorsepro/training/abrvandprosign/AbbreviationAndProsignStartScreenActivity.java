package com.example.uberj.pocketmorsepro.training.abrvandprosign;

import com.example.uberj.pocketmorsepro.socratic.SocraticKeyboardSessionActivity;
import com.example.uberj.pocketmorsepro.socratic.SocraticStartScreenActivity;
import com.example.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;

import androidx.fragment.app.DialogFragment;

public class AbbreviationAndProsignStartScreenActivity extends SocraticStartScreenActivity {
    @Override
    protected SocraticSessionType getSessionType() {
        return SocraticSessionType.ABBREVIATION_AND_PROSIGN;
    }

    @Override
    public Class<? extends SocraticKeyboardSessionActivity> getSessionActivityClass() {
        return AbbreviationAndProsignSessionTrainingActivity.class;
    }

    @Override
    public DialogFragment getHelpDialog() {
        return null;
    }
}
