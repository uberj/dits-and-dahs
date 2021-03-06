package com.uberj.ditsanddahs.training.randomletters;

import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.transcribe.TranscribePreferenceFragment;
import com.uberj.ditsanddahs.transcribe.TranscribeSettingsActivity;

public class RandomLetterSettingsActivity extends TranscribeSettingsActivity {
    public static class RandomLetterPreferenceFragment extends TranscribePreferenceFragment {
        @Override
        protected int getSettingsResource() {
            return R.xml.transcribe_settings;
        }
    }

    @Override
    public TranscribePreferenceFragment getPreferenceFragment() {
        return new RandomLetterPreferenceFragment();
    }
}
