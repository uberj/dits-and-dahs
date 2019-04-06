package com.uberj.pocketmorsepro.training.simple;

import com.uberj.pocketmorsepro.socratic.SocraticPreferenceFragment;
import com.uberj.pocketmorsepro.socratic.SocraticSettingsActivity;

public class SimpleLetterSettingsActivity extends SocraticSettingsActivity {
    @Override
    public SocraticPreferenceFragment getPreferenceFragment() {
        return new SimpleLetterPreferenceFragment();
    }

    public static class SimpleLetterPreferenceFragment extends SocraticPreferenceFragment {
    }
}
