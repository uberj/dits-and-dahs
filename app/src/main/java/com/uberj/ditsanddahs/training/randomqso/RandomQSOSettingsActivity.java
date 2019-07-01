package com.uberj.ditsanddahs.training.randomqso;

import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.transcribe.TranscribePreferenceFragment;
import com.uberj.ditsanddahs.transcribe.TranscribeSettingsActivity;

public class RandomQSOSettingsActivity extends TranscribeSettingsActivity {
    public static class RandomQSOPreferenceFragment extends TranscribePreferenceFragment {
        @Override
        protected int getSettingsResource() {
            return R.xml.qso_simulator_settings;
        }
    }
    @Override
    public TranscribePreferenceFragment getPreferenceFragment() {
        return new RandomQSOPreferenceFragment();
    }
}
