package com.uberj.ditsanddahs.transcribe;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public abstract class TranscribePreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(getSettingsResource(), rootKey);
    }

    protected abstract int getSettingsResource();
}
