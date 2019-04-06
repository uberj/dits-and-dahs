package com.uberj.pocketmorsepro.training.simple;

import android.app.Activity;

import com.uberj.pocketmorsepro.socratic.SocraticKeyboardSessionActivity;
import com.uberj.pocketmorsepro.socratic.SocraticStartScreenActivity;
import com.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;

import androidx.fragment.app.DialogFragment;

public class SimpleStartScreenActivity extends SocraticStartScreenActivity {
    @Override
    protected SocraticSessionType getSessionType() {
        return SocraticSessionType.LETTER_ONLY;
    }

    @Override
    public Class<? extends Activity> getSettingsActivity() {
        return SimpleLetterSettingsActivity.class;
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
