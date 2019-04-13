package com.uberj.pocketmorsepro.training.simplewordflashcards;

import com.google.common.collect.ImmutableList;
import com.uberj.pocketmorsepro.CommonWords;
import com.uberj.pocketmorsepro.flashcard.FlashcardSettingsActivity;
import com.uberj.pocketmorsepro.flashcard.FlashcardStartScreenActivity;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardSessionType;

import java.util.List;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.FragmentActivity;

public class SimpleWordFlashcardStartScreenActivity extends FlashcardStartScreenActivity {

    @Override
    public FlashcardSessionType getSessionType() {
        return FlashcardSessionType.RANDOM_WORDS;
    }

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
