package com.uberj.pocketmorsepro.training.abrvandprosign;

import android.app.Activity;

import com.uberj.pocketmorsepro.socratic.SocraticKeyboardSessionActivity;
import com.uberj.pocketmorsepro.socratic.SocraticStartScreenActivity;
import com.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;

import androidx.fragment.app.DialogFragment;

public class AbbreviationAndProsignStartScreenActivity extends SocraticStartScreenActivity {
    @Override
    protected SocraticSessionType getSessionType() {
        return SocraticSessionType.ABBREVIATION_AND_PROSIGN;
    }

    @Override
    public Class<? extends Activity> getSettingsActivity() {
        return null;
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
