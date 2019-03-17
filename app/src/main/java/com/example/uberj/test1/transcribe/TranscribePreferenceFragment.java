package com.example.uberj.test1.transcribe;

import android.os.Bundle;

import com.example.uberj.test1.R;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public abstract class TranscribePreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        PreferenceManager.setDefaultValues(getActivity(), R.xml.transcribe_settings, false);
        setPreferencesFromResource(R.xml.transcribe_settings, rootKey);
    }
}
