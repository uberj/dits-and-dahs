package com.example.uberj.test1.training.randomletters;

import com.example.uberj.test1.transcribe.TranscribePreferenceFragment;
import com.example.uberj.test1.transcribe.TranscribeSettingsActivity;

public class RandomLetterSettingsActivity extends TranscribeSettingsActivity {
    public static class RandomLetterPreferenceFragment extends TranscribePreferenceFragment {
    }
    @Override
    public TranscribePreferenceFragment getPreferenceFragment() {
        return new RandomLetterPreferenceFragment();
    }
}
