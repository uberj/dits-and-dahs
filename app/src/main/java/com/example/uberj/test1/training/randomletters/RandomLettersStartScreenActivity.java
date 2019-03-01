package com.example.uberj.test1.training.randomletters;

import com.example.uberj.test1.KochLetterSequence;
import com.example.uberj.test1.keyboards.KeyConfig;
import com.example.uberj.test1.transcribe.TranscribeStartScreenActivity;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionType;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class RandomLettersStartScreenActivity extends TranscribeStartScreenActivity {

    @Override
    public TranscribeSessionType getSessionType() {
        return TranscribeSessionType.RANDOM_LETTER_ONLY;
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
        return null;
    }

}
