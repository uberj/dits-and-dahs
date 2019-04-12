package com.uberj.pocketmorsepro.training.randomwords;

import com.uberj.pocketmorsepro.transcribe.TranscribePreferenceFragment;
import com.uberj.pocketmorsepro.transcribe.TranscribeSettingsActivity;

public class RandomWordSettingsActivity extends TranscribeSettingsActivity {
    public static class PreferenceFragment extends TranscribePreferenceFragment {
    }
    @Override
    public TranscribePreferenceFragment getPreferenceFragment() {
        return new PreferenceFragment();
    }
}
