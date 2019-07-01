package com.uberj.ditsanddahs.training.randomletters;

import com.uberj.ditsanddahs.KochLetterSequence;
import com.uberj.ditsanddahs.transcribe.TranscribeSettingsActivity;
import com.uberj.ditsanddahs.transcribe.TranscribeStartScreenActivity;
import com.uberj.ditsanddahs.transcribe.TranscribeStartScreenFragment;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeSessionType;
import com.google.common.collect.ImmutableList;

import java.util.List;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

public class RandomLettersStartScreenActivity extends TranscribeStartScreenActivity {

    @Override
    public TranscribeSessionType getSessionType() {
        return TranscribeSessionType.RANDOM_LETTER_ONLY;
    }

    @Override
    protected Fragment getStartScreenFragment(TranscribeSessionType sessionType, Class<? extends FragmentActivity> sessionActivityClass) {
        return TranscribeStartScreenFragment.newInstance(sessionType, sessionActivityClass);
    }

    @Override
    public Class<? extends TranscribeSettingsActivity> getSettingsActivity() {
        return RandomLetterSettingsActivity.class;
    }

    @Override
    protected List<String> initialSelectedStrings() {
        return ImmutableList.of(
                KochLetterSequence.sequence.get(0),
                KochLetterSequence.sequence.get(1)
        );
    }

    @Override
    protected List<String> getPossibleStrings() {
        return KochLetterSequence.sequence;
    }

    @Override
    public Class<? extends FragmentActivity> getSessionActivityClass() {
        return RandomLetterKeyboardSessionActivity.class;
    }

    @Override
    public DialogFragment getHelpDialog() {
        return new RandomLetterStartScreenHelpDialog();
    }

}
