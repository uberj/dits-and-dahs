package com.example.uberj.pocketmorsepro.training.randomletters;

import com.example.uberj.pocketmorsepro.transcribe.TranscribePreferenceFragment;
import com.example.uberj.pocketmorsepro.transcribe.TranscribeSettingsActivity;

public class RandomLetterSettingsActivity extends TranscribeSettingsActivity {
    public static class RandomLetterPreferenceFragment extends TranscribePreferenceFragment {
    }
    @Override
    public TranscribePreferenceFragment getPreferenceFragment() {
        return new RandomLetterPreferenceFragment();
    }
}
