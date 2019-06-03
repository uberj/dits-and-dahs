package com.uberj.ditsanddahs.simplesocratic;

import com.crashlytics.android.Crashlytics;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingEngineSettings;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticSessionType;
import com.uberj.ditsanddahs.training.DialogFragmentProvider;

import androidx.annotation.NonNull;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import it.sephiroth.android.library.numberpicker.NumberPicker;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;


import static com.uberj.ditsanddahs.simplesocratic.SocraticStartScreenActivity.KEYBOARD_REQUEST_CODE;


public class SocraticStartScreenFragment extends Fragment {
    private NumberPicker minutesPicker;
    private NumberPicker wpmPicker;
    private CheckBox resetLetterWeights;
    private SocraticTrainingMainScreenViewModel sessionViewModel;
    private SocraticSessionType sessionType;
    private SharedPreferences preferences;
    private String sessionActivityClassName;


    public static SocraticStartScreenFragment newInstance(SocraticSessionType sessionType, Class<? extends FragmentActivity> sessionActivityClass) {
        SocraticStartScreenFragment fragment = new SocraticStartScreenFragment();
        fragment.setSessionActivityClass(sessionActivityClass);
        fragment.setSessionType(sessionType);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void setSessionActivityClass(Class<? extends FragmentActivity> sessionActivityClass) {
        this.sessionActivityClassName = sessionActivityClass.getName();
    }

    public void setSessionType(SocraticSessionType sessionType) {
        this.sessionType = sessionType;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("sessionType", sessionType.name());
        outState.putString("sessionActivityClassName", sessionActivityClassName);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        resetLetterWeights.setChecked(false);
        super.onResume();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            sessionType = SocraticSessionType.valueOf(savedInstanceState.getString("sessionType"));
            sessionActivityClassName = savedInstanceState.getString("sessionActivityClassName");
        }

        View rootView = inflater.inflate(R.layout.socratic_training_start_screen_fragment, container, false);
        ImageView helpWPM = rootView.findViewById(R.id.wpmhelp);
        helpWPM.setOnClickListener((l) -> {
            DialogFragmentProvider provider = (DialogFragmentProvider) getActivity();
            DialogFragment dialog = provider.getHelpDialog();
            FragmentManager supportFragmentManager = provider.getHelpDialogFragmentManager();
            dialog.show(supportFragmentManager, dialog.getTag());
        });


        PreferenceManager.setDefaultValues(getActivity().getApplicationContext(), R.xml.socratic_settings, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        minutesPicker = rootView.findViewById(R.id.number_picker_minutes);
        wpmPicker = rootView.findViewById(R.id.wpm_number_picker);
        resetLetterWeights = rootView.findViewById(R.id.reset_weights);
        sessionViewModel = ViewModelProviders.of(this).get(SocraticTrainingMainScreenViewModel.class);
        sessionViewModel.getLatestEngineSettings(sessionType).observe(this, (mostRecentSettings) -> {
            int playLetterWPM = -1;
            long prevDurationRequestedMillis = -1L;
            if (!mostRecentSettings.isEmpty()) {
                SocraticTrainingEngineSettings engineSettings = mostRecentSettings.get(0);
                playLetterWPM = engineSettings.playLetterWPM;
                prevDurationRequestedMillis = engineSettings.durationRequestedMillis;
            }

            if (playLetterWPM > 0) {
                wpmPicker.setProgress(playLetterWPM);
            } else {
                wpmPicker.setProgress(20);
            }

            if (prevDurationRequestedMillis > 0) {
                long prevDurationRequestedMinutes = (prevDurationRequestedMillis / 1000) / 60;
                minutesPicker.setProgress((int) prevDurationRequestedMinutes);
            } else {
                minutesPicker.setProgress(1);
            }
        });

        TextView additionalSettingsLink = rootView.findViewById(R.id.additional_settings);
        additionalSettingsLink.setOnClickListener(this::launchSettings);

        Button startButton = rootView.findViewById(R.id.start_button);
        startButton.setOnClickListener(this::handleStartButtonClick);

        resetLetterWeights.setChecked(false);

        return rootView;
    }

    private void handleStartButtonClick(View view) {
        int toneFrequency = preferences.getInt(getResources().getString(R.string.setting_socratic_audio_tone), 440);
        boolean easyMode = preferences.getBoolean(getResources().getString(R.string.setting_socratic_easy_mode), true);
        int fadeInOutPercentage = preferences.getInt(getResources().getString(R.string.setting_fade_in_out_percentage), 30);
        Class<? extends FragmentActivity> sessionActivityClass = null;
        try {
            sessionActivityClass = (Class<? extends FragmentActivity>) Class.forName(sessionActivityClassName);
        } catch (ClassNotFoundException e) {
            Crashlytics.log(String.format("Couldn't find sessionActivityClassName with '%s'", sessionActivityClassName));
        }
        Intent sendIntent = new Intent(view.getContext(), sessionActivityClass);
        Bundle bundle = new Bundle();
        bundle.putInt(SocraticKeyboardSessionActivity.WPM_REQUESTED, wpmPicker.getProgress());
        bundle.putInt(SocraticKeyboardSessionActivity.DURATION_REQUESTED_MINUTES, minutesPicker.getProgress());
        bundle.putBoolean(SocraticKeyboardSessionActivity.REQUEST_WEIGHTS_RESET, resetLetterWeights.isChecked());
        bundle.putBoolean(SocraticKeyboardSessionActivity.EASY_MODE, easyMode);
        bundle.putInt(SocraticKeyboardSessionActivity.FADE_IN_OUT_PERCENTAGE, fadeInOutPercentage);
        bundle.putInt(SocraticKeyboardSessionActivity.TONE_FREQUENCY_HZ, toneFrequency);
        sendIntent.putExtras(bundle);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(sendIntent, KEYBOARD_REQUEST_CODE);
    }

    private SocraticStartScreenActivity getSocraticActivity() {
        return ((SocraticStartScreenActivity)getActivity());
    }

    private void launchSettings(View view) {
        Intent intent = new Intent(view.getContext(), getSocraticActivity().getSettingsActivity());
        startActivity(intent);
    }
}
