package com.uberj.ditsanddahs.training.simple;

import com.uberj.ditsanddahs.simplesocratic.SocraticPreferenceFragment;
import com.uberj.ditsanddahs.simplesocratic.SocraticSettingsActivity;

public class SimpleLetterSettingsActivity extends SocraticSettingsActivity {
    @Override
    public SocraticPreferenceFragment getPreferenceFragment() {
        return new SimpleLetterPreferenceFragment();
    }

    public static class SimpleLetterPreferenceFragment extends SocraticPreferenceFragment {
    }
}
