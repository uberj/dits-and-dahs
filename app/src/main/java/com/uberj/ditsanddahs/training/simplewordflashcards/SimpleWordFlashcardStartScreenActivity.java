package com.uberj.ditsanddahs.training.simplewordflashcards;

import com.google.common.collect.ImmutableList;
import com.uberj.ditsanddahs.CommonWords;
import com.uberj.ditsanddahs.flashcard.FlashcardSettingsActivity;
import com.uberj.ditsanddahs.flashcard.FlashcardStartScreenActivity;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardSessionType;

import java.util.List;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class SimpleWordFlashcardStartScreenActivity extends FlashcardStartScreenActivity {

    @Override
    public Class<? extends FlashcardSettingsActivity> getSettingsActivity() {
        return SimpleWordFlashcardSettingsActivity.class;
    }

    @Override
    protected List<String> initialSelectedStrings() {
        return ImmutableList.of(
                CommonWords.sequence.get(0),
                CommonWords.sequence.get(1),
                CommonWords.sequence.get(2),
                CommonWords.sequence.get(3)
        );
    }

    @Override
    protected List<String> getPossibleStrings() {
        return CommonWords.sequence;
    }

    @Override
    public Class<? extends FragmentActivity> getSessionActivityClass() {
        return SimpleWordFlashcardKeyboardSessionActivity.class;
    }

    @Override
    public DialogFragment getHelpDialog() {
        return new SimpleWordFlashcardStartScreenHelpDialog();
    }

}
