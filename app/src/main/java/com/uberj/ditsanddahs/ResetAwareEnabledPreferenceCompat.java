package com.uberj.ditsanddahs;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

public class ResetAwareEnabledPreferenceCompat extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Preference reset = findPreference(getResources().getString(R.string.setting_reset_to_defaults));
        if (reset != null) {
            reset.setOnPreferenceClickListener(this::onResetRequestClick);
        }
    }

    private boolean onResetRequestClick(Preference preference) {
        AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
        dialog.setTitle("Reset all settings?");
        dialog.setMessage("This action will reset all settings for the entire app. Are you sure you want to continue?");
        dialog.setCancelable(true);
        dialog.setPositiveButton("Reset", (dialog1, which) -> {
            // User selected OK
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
            SharedPreferences.Editor editor = preferences.edit();
            editor.clear();
            editor.apply();
            getActivity().finish();
            Toast.makeText(getContext(), "Settings reset!", Toast.LENGTH_SHORT).show();
        });

        dialog.setNegativeButton("Cancel", (dlg, which) -> dlg.cancel());

        AlertDialog al = dialog.create();
        al.show();
        return true;
    }
}
