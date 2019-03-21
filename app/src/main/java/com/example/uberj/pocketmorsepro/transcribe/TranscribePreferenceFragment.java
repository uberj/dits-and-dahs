package com.example.uberj.pocketmorsepro.transcribe;

import android.os.Bundle;

import com.example.uberj.pocketmorsepro.R;

import androidx.preference.PreferenceFragmentCompat;

public abstract class TranscribePreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.transcribe_settings, rootKey);
    }
}
