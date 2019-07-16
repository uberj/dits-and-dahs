package com.uberj.ditsanddahs.flashcard;

import android.os.Bundle;

import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.ResetAwareEnabledPreferenceCompat;

public abstract class FlashcardPreferenceFragment extends ResetAwareEnabledPreferenceCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.flashcard_settings, rootKey);
        super.onCreatePreferences(savedInstanceState, rootKey);
    }
}
