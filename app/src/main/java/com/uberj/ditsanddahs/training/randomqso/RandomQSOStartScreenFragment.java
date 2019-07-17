package com.uberj.ditsanddahs.training.randomqso;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;

import com.crashlytics.android.Crashlytics;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.training.DialogFragmentProvider;
import com.uberj.ditsanddahs.transcribe.TranscribeKeyboardSessionActivity;
import com.uberj.ditsanddahs.transcribe.TranscribeStartScreenActivity;
import com.uberj.ditsanddahs.transcribe.TranscribeTrainingMainScreenViewModel;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeSessionType;

import org.jetbrains.annotations.NotNull;

import it.sephiroth.android.library.numberpicker.NumberPicker;

public class RandomQSOStartScreenFragment extends Fragment {
    private NumberPicker letterWpmNumberPicker;
    private NumberPicker effectiveWpmNumberPicker;
    private TranscribeTrainingMainScreenViewModel sessionViewModel;
    private TranscribeSessionType sessionType;
    private TextView additionalSettingsLink;
    private SharedPreferences preferences;
    private String sessionActivityClassName;


    public static RandomQSOStartScreenFragment newInstance(TranscribeSessionType sessionType, Class<? extends FragmentActivity> sessionActivityClass) {
        RandomQSOStartScreenFragment fragment = new RandomQSOStartScreenFragment();
        fragment.setSessionActivityClass(sessionActivityClass);
        fragment.setSessionType(sessionType);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void setSessionActivityClass(Class<? extends FragmentActivity> sessionActivityClass) {
        this.sessionActivityClassName = sessionActivityClass.getName();
    }

    public void setSessionType(TranscribeSessionType sessionType) {
        this.sessionType = sessionType;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("sessionType", sessionType.name());
        outState.putString("sessionActivityClassName", sessionActivityClassName);
        super.onSaveInstanceState(outState);
    }

    private TranscribeStartScreenActivity getTranscribeActivity() {
        return ((TranscribeStartScreenActivity)getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            sessionType = TranscribeSessionType.valueOf(savedInstanceState.getString("sessionType"));
            sessionActivityClassName = savedInstanceState.getString("sessionActivityClassName");
        }
        View rootView = inflater.inflate(R.layout.qso_simulator_start_screen_fragment, container, false);
        ImageView helpWPM = rootView.findViewById(R.id.wpmhelp);
        helpWPM.setOnClickListener((l) -> {
            DialogFragmentProvider provider = (DialogFragmentProvider) getActivity();
            DialogFragment dialog = provider.getHelpDialog();
            FragmentManager supportFragmentManager = provider.getHelpDialogFragmentManager();
            dialog.show(supportFragmentManager, dialog.getTag());
        });


        PreferenceManager.setDefaultValues(getActivity().getApplicationContext(), R.xml.transcribe_settings, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        letterWpmNumberPicker = rootView.findViewById(R.id.letter_wpm_number_picker);
        effectiveWpmNumberPicker = rootView.findViewById(R.id.effective_wpm_number_picker);
        additionalSettingsLink = rootView.findViewById(R.id.additional_settings);
        sessionViewModel = ViewModelProviders.of(this).get(TranscribeTrainingMainScreenViewModel.class);
        registerOnChangeListeners();

        additionalSettingsLink.setOnClickListener(this::launchSettings);

        sessionViewModel.getLatestSession(sessionType).observe(this, mostRecentSession -> setupPreviousSettings());

        Button startButton = rootView.findViewById(R.id.start_button);
        startButton.setOnClickListener(this::handleStartButtonClick);

        return rootView;
    }

    private void launchSettings(View view) {
        Intent intent = new Intent(view.getContext(), getTranscribeActivity().getSettingsActivity());
        startActivity(intent);
    }

    private void registerOnChangeListeners() {
        // Letter WPM is the upper bound of the effective WPM
        letterWpmNumberPicker.setNumberPickerChangeListener(new NumberPicker.OnNumberPickerChangeListener() {
            @Override
            public void onProgressChanged(@NotNull NumberPicker numberPicker, int letterWpm, boolean b) {
                int effectiveWpm = effectiveWpmNumberPicker.getProgress();
                if (effectiveWpm > letterWpm) {
                    effectiveWpmNumberPicker.setProgress(letterWpm);
                }
                SharedPreferences.Editor edit = preferences.edit();
                edit.putInt(getResources().getString(R.string.setting_transcribe_letter_wpm), letterWpm);
                edit.apply();
            }

            @Override
            public void onStartTrackingTouch(@NotNull NumberPicker numberPicker) {

            }

            @Override
            public void onStopTrackingTouch(@NotNull NumberPicker numberPicker) {

            }
        });

        effectiveWpmNumberPicker.setNumberPickerChangeListener(new NumberPicker.OnNumberPickerChangeListener() {
            @Override
            public void onProgressChanged(@NotNull NumberPicker numberPicker, int effectiveWpm, boolean b) {
                int letterWpm = letterWpmNumberPicker.getProgress();
                if (effectiveWpm > letterWpm) {
                    letterWpmNumberPicker.setProgress(effectiveWpm);
                }
                SharedPreferences.Editor edit = preferences.edit();
                edit.putInt(getResources().getString(R.string.setting_transcribe_effective_wpm), effectiveWpm);
                edit.apply();
            }

            @Override
            public void onStartTrackingTouch(@NotNull NumberPicker numberPicker) {

            }

            @Override
            public void onStopTrackingTouch(@NotNull NumberPicker numberPicker) {

            }
        });
    }

    private void handleStartButtonClick(View view) {
        Class<? extends FragmentActivity> sessionActivityClass = null;
        try {
            sessionActivityClass = (Class<? extends FragmentActivity>) Class.forName(sessionActivityClassName);
        } catch (ClassNotFoundException e) {
            Crashlytics.log(String.format("Couldn't find sessionActivityClassName with '%s'", sessionActivityClassName));
        }
        Intent sendIntent = new Intent(view.getContext(), sessionActivityClass);
        Bundle bundle = new Bundle();
        bundle.putStringArrayList(
                TranscribeKeyboardSessionActivity.STRINGS_REQUESTED,
                sessionViewModel.selectedStrings.getValue()
        );
        sendIntent.putExtras(bundle);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(sendIntent, TranscribeStartScreenActivity.KEYBOARD_REQUEST_CODE);
    }

    private void setupPreviousSettings() {
        int letterWpm = preferences.getInt(getResources().getString(R.string.setting_transcribe_letter_wpm), -1);
        int effectiveWpm = preferences.getInt(getResources().getString(R.string.setting_transcribe_effective_wpm), -1);
        if (letterWpm > 0) {
            letterWpmNumberPicker.setProgress(letterWpm);
        } else {
            letterWpmNumberPicker.setProgress(20);
        }

        if (effectiveWpm > 0) {
            effectiveWpmNumberPicker.setProgress(effectiveWpm);
        } else {
            effectiveWpmNumberPicker.setProgress(6);
        }
    }
}
