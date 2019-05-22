package com.uberj.ditsanddahs.transcribe;

import android.os.Bundle;

import com.uberj.ditsanddahs.R;

import androidx.preference.PreferenceFragmentCompat;

public abstract class TranscribePreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.transcribe_settings, rootKey);
    }
}
