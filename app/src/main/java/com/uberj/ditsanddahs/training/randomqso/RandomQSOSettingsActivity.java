package com.uberj.ditsanddahs.training.randomqso;

import android.os.Bundle;

import androidx.preference.MultiSelectListPreference;
import androidx.preference.Preference;

import com.google.common.base.Joiner;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.transcribe.TranscribePreferenceFragment;
import com.uberj.ditsanddahs.transcribe.TranscribeSettingsActivity;

import java.util.Set;

public class RandomQSOSettingsActivity extends TranscribeSettingsActivity {
    public static class RandomQSOPreferenceFragment extends TranscribePreferenceFragment {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            super.onCreatePreferences(savedInstanceState, rootKey);
            MultiSelectListPreference proSignsPreference = findPreference(getResources().getString(R.string.global_setting_enabled_prosigns));
            setupSummary(proSignsPreference, proSignsPreference.getValues());
            proSignsPreference.setOnPreferenceChangeListener(this::proSignsChanged);
        }

        private boolean proSignsChanged(Preference preference, Object proSigns) {
            MultiSelectListPreference proSignsPreference = (MultiSelectListPreference) preference;
            setupSummary(proSignsPreference, (Set<String>) proSigns);
            return true;
        }

        @Override
        protected int getSettingsResource() {
            return R.xml.qso_simulator_settings;
        }

        public void setupSummary(MultiSelectListPreference proSignsPreference, Set<String> proSigns) {
            proSignsPreference.setSummary(Joiner.on(", ").join(proSigns));
        }
    }
    @Override
    public TranscribePreferenceFragment getPreferenceFragment() {
        return new RandomQSOPreferenceFragment();
    }
}
