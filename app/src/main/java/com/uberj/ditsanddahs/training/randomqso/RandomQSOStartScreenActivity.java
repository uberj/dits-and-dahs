package com.uberj.ditsanddahs.training.randomqso;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.google.common.collect.ImmutableList;
import com.uberj.ditsanddahs.KochLetterSequence;
import com.uberj.ditsanddahs.transcribe.TranscribeSettingsActivity;
import com.uberj.ditsanddahs.transcribe.TranscribeStartScreenActivity;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeSessionType;

import java.util.List;

public class RandomQSOStartScreenActivity extends TranscribeStartScreenActivity {

    @Override
    public TranscribeSessionType getSessionType() {
        return TranscribeSessionType.RANDOM_QSO;
    }

    @Override
    public Class<? extends TranscribeSettingsActivity> getSettingsActivity() {
        return RandomQSOSettingsActivity.class;
    }

    @Override
    protected List<String> initialSelectedStrings() {
        return ImmutableList.of();
    }

    @Override
    protected List<String> getPossibleStrings() {
        return KochLetterSequence.sequence;
    }

    @Override
    public Class<? extends FragmentActivity> getSessionActivityClass() {
        return RandomQSOKeyboardSessionActivity.class;
    }

    @Override
    public DialogFragment getHelpDialog() {
        return new RandomQSOStartScreenHelpDialog();
    }

    @Override
    protected Fragment getStartScreenFragment(TranscribeSessionType sessionType, Class<? extends FragmentActivity> sessionActivityClass) {
        return RandomQSOStartScreenFragment.newInstance(sessionType, sessionActivityClass);
    }

}
