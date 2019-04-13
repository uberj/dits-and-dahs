package com.uberj.pocketmorsepro.training.simplewordflashcards;


import com.uberj.pocketmorsepro.flashcard.FlashcardPreferenceFragment;
import com.uberj.pocketmorsepro.flashcard.FlashcardSettingsActivity;

public class SimpleWordFlashcardSettingsActivity extends FlashcardSettingsActivity {
    public static class PreferenceFragment extends FlashcardPreferenceFragment {
    }

    @Override
    public FlashcardPreferenceFragment getPreferenceFragment() {
        return new PreferenceFragment();
    }
}
