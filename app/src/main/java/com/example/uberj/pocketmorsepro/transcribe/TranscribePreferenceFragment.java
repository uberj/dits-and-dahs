package com.example.uberj.morsepocketpro.transcribe;

import android.os.Bundle;

import com.example.uberj.morsepocketpro.R;

import androidx.preference.PreferenceFragmentCompat;

public abstract class TranscribePreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.transcribe_settings, rootKey);
    }
}
