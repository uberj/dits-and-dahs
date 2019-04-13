package com.uberj.pocketmorsepro.flashcard;

import android.os.Bundle;

import com.uberj.pocketmorsepro.R;

import androidx.preference.PreferenceFragmentCompat;

public abstract class FlashcardPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.flashcard_settings, rootKey);
    }
}
