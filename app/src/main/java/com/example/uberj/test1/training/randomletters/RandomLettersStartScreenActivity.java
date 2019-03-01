package com.example.uberj.test1.training.randomletters;

import com.example.uberj.test1.transcribe.TranscribeStartScreenActivity;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionType;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class RandomLettersStartScreenActivity extends TranscribeStartScreenActivity {

    @Override
    public TranscribeSessionType getSessionType() {
        return TranscribeSessionType.RANDOM_LETTER_ONLY;
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
