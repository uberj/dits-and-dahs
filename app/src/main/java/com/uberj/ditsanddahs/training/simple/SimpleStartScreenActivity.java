package com.uberj.ditsanddahs.training.simple;

import android.app.Activity;

import com.uberj.ditsanddahs.simplesocratic.SocraticKeyboardSessionActivity;
import com.uberj.ditsanddahs.simplesocratic.SocraticStartScreenActivity;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticSessionType;

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
