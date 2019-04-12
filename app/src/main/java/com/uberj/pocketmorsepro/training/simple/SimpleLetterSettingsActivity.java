package com.uberj.pocketmorsepro.training.simple;

import com.uberj.pocketmorsepro.simplesocratic.SocraticPreferenceFragment;
import com.uberj.pocketmorsepro.simplesocratic.SocraticSettingsActivity;

public class SimpleLetterSettingsActivity extends SocraticSettingsActivity {
    @Override
    public SocraticPreferenceFragment getPreferenceFragment() {
        return new SimpleLetterPreferenceFragment();
    }

    public static class SimpleLetterPreferenceFragment extends SocraticPreferenceFragment {
    }
}
