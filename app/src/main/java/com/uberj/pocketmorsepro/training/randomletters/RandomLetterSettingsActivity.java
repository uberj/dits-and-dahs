package com.uberj.pocketmorsepro.training.randomletters;

import com.uberj.pocketmorsepro.transcribe.TranscribePreferenceFragment;
import com.uberj.pocketmorsepro.transcribe.TranscribeSettingsActivity;

public class RandomLetterSettingsActivity extends TranscribeSettingsActivity {
    public static class RandomLetterPreferenceFragment extends TranscribePreferenceFragment {
    }
    @Override
    public TranscribePreferenceFragment getPreferenceFragment() {
        return new RandomLetterPreferenceFragment();
    }
}
