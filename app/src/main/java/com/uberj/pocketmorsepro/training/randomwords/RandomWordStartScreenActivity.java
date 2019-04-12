package com.uberj.pocketmorsepro.training.randomwords;

import com.google.common.collect.ImmutableList;
import com.uberj.pocketmorsepro.CommonWords;
import com.uberj.pocketmorsepro.KochLetterSequence;
import com.uberj.pocketmorsepro.transcribe.TranscribeSettingsActivity;
import com.uberj.pocketmorsepro.transcribe.TranscribeStartScreenActivity;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeSessionType;

import java.util.List;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class RandomWordStartScreenActivity extends TranscribeStartScreenActivity {

    @Override
    public TranscribeSessionType getSessionType() {
        return TranscribeSessionType.RANDOM_WORD;
    }

    @Override
    public Class<? extends TranscribeSettingsActivity> getSettingsActivity() {
        return RandomWordSettingsActivity.class;
    }

    @Override
    protected List<String> initialSelectedStrings() {
        return ImmutableList.of(
                CommonWords.sequence.get(0),
                CommonWords.sequence.get(1)
        );
    }

    @Override
    protected List<String> getPossibleStrings() {
        return CommonWords.sequence;
    }

    @Override
    public Class<? extends FragmentActivity> getSessionActivityClass() {
        return RandomWordKeyboardSessionActivity.class;
    }

    @Override
    public DialogFragment getHelpDialog() {
        return new RandomWordStartScreenHelpDialog();
    }

}
