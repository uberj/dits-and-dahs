package com.uberj.ditsanddahs.transcribe;

import android.os.Bundle;

import com.uberj.ditsanddahs.ResetAwareEnabledPreferenceCompat;

public abstract class TranscribePreferenceFragment extends ResetAwareEnabledPreferenceCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(getSettingsResource(), rootKey);
        super.onCreatePreferences(savedInstanceState, rootKey);
    }

    protected abstract int getSettingsResource();
}
