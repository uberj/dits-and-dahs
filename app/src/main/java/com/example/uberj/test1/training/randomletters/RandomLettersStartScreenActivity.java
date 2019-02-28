package com.example.uberj.test1.training.randomletters;

import com.example.uberj.test1.training.BaseStartScreenActivity;
import com.example.uberj.test1.socratic.storage.SocraticSessionType;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class RandomLettersStartScreenActivity extends BaseStartScreenActivity {

    @Override
    public SocraticSessionType getSessionType() {
        return SocraticSessionType.LETTER_ONLY_GROUPS;
    }

    @Override
    public Class<? extends FragmentActivity> getSessionActivityClass() {
        return RandomLetterKeyboardSessionActivity.class;
    }

    @Override
    public DialogFragment getHelpDialog() {
        return null;
    }

}
