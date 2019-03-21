package com.uberj.pocketmorsepro.training.randomletters;

import com.uberj.pocketmorsepro.KochLetterSequence;
import com.uberj.pocketmorsepro.transcribe.TranscribeSettingsActivity;
import com.uberj.pocketmorsepro.transcribe.TranscribeStartScreenActivity;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeSessionType;
import com.google.common.collect.ImmutableList;

import java.util.List;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class RandomLettersStartScreenActivity extends TranscribeStartScreenActivity {

    @Override
    public TranscribeSessionType getSessionType() {
        return TranscribeSessionType.RANDOM_LETTER_ONLY;
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
