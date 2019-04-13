package com.uberj.pocketmorsepro.flashcard;

import com.uberj.pocketmorsepro.R;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardSessionType;
import com.uberj.pocketmorsepro.training.DialogFragmentProvider;

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

import static com.uberj.pocketmorsepro.flashcard.FlashcardStartScreenActivity.KEYBOARD_REQUEST_CODE;


public class FlashcardStartScreenFragment extends Fragment {
    private NumberPicker minutesPicker;
    private NumberPicker wpmPicker;
    private CheckBox resetLetterWeights;
    private FlashcardTrainingMainScreenViewModel sessionViewModel;
    private Class<? extends FragmentActivity> sessionActivityClass;
    private FlashcardSessionType sessionType;
    private SharedPreferences preferences;


    public static FlashcardStartScreenFragment newInstance(FlashcardSessionType sessionType, Class<? extends FragmentActivity> sessionActivityClass) {
        FlashcardStartScreenFragment fragment = new FlashcardStartScreenFragment();
        fragment.setSessionActivityClass(sessionActivityClass);
        fragment.setSessionType(sessionType);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private void setSessionActivityClass(Class<? extends FragmentActivity> sessionActivityClass) {
        this.sessionActivityClass = sessionActivityClass;
    }

    public void setSessionType(FlashcardSessionType sessionType) {
        this.sessionType = sessionType;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString("sessionType", sessionType.name());
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            sessionType = FlashcardSessionType.valueOf(savedInstanceState.getString("sessionType"));
        }

        View rootView = inflater.inflate(R.layout.flashcard_training_start_screen_fragment, container, false);
        ImageView helpWPM = rootView.findViewById(R.id.wpmhelp);
        helpWPM.setOnClickListener((l) -> {
            DialogFragmentProvider provider = (DialogFragmentProvider) getActivity();
            DialogFragment dialog = provider.getHelpDialog();
            FragmentManager supportFragmentManager = provider.getHelpDialogFragmentManager();
            dialog.show(supportFragmentManager, dialog.getTag());
        });


        PreferenceManager.setDefaultValues(getActivity().getApplicationContext(), R.xml.flashcard_settings, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        minutesPicker = rootView.findViewById(R.id.number_picker_minutes);
        wpmPicker = rootView.findViewById(R.id.wpm_number_picker);
        resetLetterWeights = rootView.findViewById(R.id.reset_weights);
        sessionViewModel = ViewModelProviders.of(this).get(FlashcardTrainingMainScreenViewModel.class);
        sessionViewModel.getLatestSession(sessionType).observe(this, (sessionWithEvents) -> {
            int playLetterWPM = preferences.getInt(getResources().getString(R.string.setting_flashcard_letter_wpm), 25);
            long prevDurationRequestedMillis = -1L;
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

        return rootView;
    }

    private void handleStartButtonClick(View view) {
        int toneFrequency = preferences.getInt(getResources().getString(R.string.setting_flashcard_audio_tone), 440);
        Intent sendIntent = new Intent(view.getContext(), sessionActivityClass);
        Bundle bundle = new Bundle();
        bundle.putInt(FlashcardKeyboardSessionActivity.WPM_REQUESTED, wpmPicker.getProgress());
        bundle.putInt(FlashcardKeyboardSessionActivity.DURATION_REQUESTED_MINUTES, minutesPicker.getProgress());
        bundle.putBoolean(FlashcardKeyboardSessionActivity.REQUEST_WEIGHTS_RESET, resetLetterWeights.isChecked());
        bundle.putInt(FlashcardKeyboardSessionActivity.TONE_FREQUENCY_HZ, toneFrequency);
        sendIntent.putExtras(bundle);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivityForResult(sendIntent, KEYBOARD_REQUEST_CODE);
        resetLetterWeights.setChecked(false);
    }

    private FlashcardStartScreenActivity getSocraticActivity() {
        return ((FlashcardStartScreenActivity)getActivity());
    }

    private void launchSettings(View view) {
        Intent intent = new Intent(view.getContext(), getSocraticActivity().getSettingsActivity());
        startActivity(intent);
    }
}
