package com.example.uberj.morsepocketpro.training.randomletters;

import com.example.uberj.morsepocketpro.transcribe.TranscribePreferenceFragment;
import com.example.uberj.morsepocketpro.transcribe.TranscribeSettingsActivity;

public class RandomLetterSettingsActivity extends TranscribeSettingsActivity {
    public static class RandomLetterPreferenceFragment extends TranscribePreferenceFragment {
    }
    @Override
    public TranscribePreferenceFragment getPreferenceFragment() {
        return new RandomLetterPreferenceFragment();
    }
}
