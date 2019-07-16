package com.uberj.ditsanddahs.simplesocratic;

import android.os.Bundle;

import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.ResetAwareEnabledPreferenceCompat;

public abstract class SocraticPreferenceFragment extends ResetAwareEnabledPreferenceCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.socratic_settings, rootKey);
        super.onCreatePreferences(savedInstanceState, rootKey);
    }

}
