package com.uberj.ditsanddahs.training.simplewordflashcards;


import com.uberj.ditsanddahs.flashcard.FlashcardPreferenceFragment;
import com.uberj.ditsanddahs.flashcard.FlashcardSettingsActivity;

public class SimpleWordFlashcardSettingsActivity extends FlashcardSettingsActivity {
    public static class PreferenceFragment extends FlashcardPreferenceFragment {
    }

    @Override
    public FlashcardPreferenceFragment getPreferenceFragment() {
        return new PreferenceFragment();
    }
}
