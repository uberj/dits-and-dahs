package com.uberj.ditsanddahs.flashcard;

import android.os.Bundle;

import com.uberj.ditsanddahs.R;

import androidx.preference.PreferenceFragmentCompat;

public abstract class FlashcardPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.flashcard_settings, rootKey);
    }
}
