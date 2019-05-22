package com.uberj.ditsanddahs.simplesocratic;

import android.os.Bundle;

import com.uberj.ditsanddahs.R;

import androidx.preference.PreferenceFragmentCompat;

public abstract class SocraticPreferenceFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.socratic_settings, rootKey);
    }
}
