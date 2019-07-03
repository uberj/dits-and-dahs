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

import java.util.Random;

import it.sephiroth.android.library.numberpicker.NumberPicker;

class RandomQSOStartScreenFragment extends Fragment {
    private static final Random r = new Random();
    private static final double SUGGEST_ADDING_MORE_LETTERS_ACCURACY_CUTOFF = 89;
    private static final double SUGGEST_REMOVING_LETTERS_ACCURACY_CUTOFF = 45;
    private NumberPicker minutesPicker;
    private NumberPicker letterWpmNumberPicker;
    private NumberPicker effectiveWpmNumberPicker;
    private TranscribeTrainingMainScreenViewModel sessionViewModel;
    private TranscribeSessionType sessionType;
    private TextView selectedStringsContainer;
    private TextView suggestAddLettersHelpText;
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
        bundle.putInt(TranscribeKeyboardSessionActivity.LETTER_WPM_REQUESTED, letterWpmNumberPicker.getProgress());
        bundle.putInt(TranscribeKeyboardSessionActivity.EFFECTIVE_WPM_REQUESTED, effectiveWpmNumberPicker.getProgress());
        boolean targetIssueStrings = preferences.getBoolean(getResources().getString(R.string.setting_transcribe_target_issue_letters), false);
        int audioToneFrequency = preferences.getInt(getResources().getString(R.string.setting_transcribe_audio_tone), 440);
        int secondsBetweenStationTransmissions = preferences.getInt(getResources().getString(R.string.qso_simulator_seconds_between_station_transmissions), 1);
        int secondAudioToneFrequency = preferences.getInt(getResources().getString(R.string.second_station_setting_transcribe_audio_tone), 410);
        int startDelaySeconds = preferences.getInt(getResources().getString(R.string.setting_transcribe_start_delay_seconds), 3);
        int endDelaySeconds = preferences.getInt(getResources().getString(R.string.setting_transcribe_end_delay_seconds), 3);
        if (endDelaySeconds == Integer.valueOf(getResources().getString(R.string.setting_transcribe_end_delay_seconds_max_value))) {
            endDelaySeconds = -1;
        }
        int fadeInOutPercentage = preferences.getInt(getResources().getString(R.string.setting_fade_in_out_percentage), 30);

        bundle.putBoolean(TranscribeKeyboardSessionActivity.TARGET_ISSUE_STRINGS, targetIssueStrings);
        bundle.putInt(TranscribeKeyboardSessionActivity.AUDIO_TONE_FREQUENCY, audioToneFrequency);
        bundle.putInt(TranscribeKeyboardSessionActivity.SECOND_AUDIO_TONE_FREQUENCY, secondAudioToneFrequency);
        bundle.putInt(TranscribeKeyboardSessionActivity.SECONDS_BETWEEN_STATION_TRANSMISSIONS, secondsBetweenStationTransmissions);
        bundle.putInt(TranscribeKeyboardSessionActivity.SESSION_START_DELAY_SECONDS, startDelaySeconds);
        bundle.putInt(TranscribeKeyboardSessionActivity.SESSION_END_DELAY_SECONDS, endDelaySeconds);
        bundle.putInt(TranscribeKeyboardSessionActivity.FADE_IN_OUT_PERCENTAGE, fadeInOutPercentage);
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
