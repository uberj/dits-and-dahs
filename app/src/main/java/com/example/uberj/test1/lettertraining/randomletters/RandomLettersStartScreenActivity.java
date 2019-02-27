package com.example.uberj.test1.lettertraining.randomletters;

import com.example.uberj.test1.keyboards.KeyConfig;
import com.example.uberj.test1.keyboards.Keys;
import com.example.uberj.test1.lettertraining.SocraticKeyboardSessionActivity;
import com.example.uberj.test1.lettertraining.BaseStartScreenActivity;
import com.example.uberj.test1.storage.SessionType;
import com.google.common.collect.ImmutableList;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class RandomLettersStartScreenActivity extends BaseStartScreenActivity {

    @Override
    public SessionType getSessionType() {
        return SessionType.LETTER_ONLY_GROUPS;
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
