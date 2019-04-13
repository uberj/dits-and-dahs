package com.uberj.pocketmorsepro.flashcard;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.uberj.pocketmorsepro.R;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardSessionType;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardTrainingSessionWithEvents;
import com.uberj.pocketmorsepro.training.DialogFragmentProvider;

import androidx.annotation.NonNull;

import androidx.appcompat.app.AlertDialog;
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

import java.util.ArrayList;
import java.util.List;

import static com.uberj.pocketmorsepro.flashcard.FlashcardStartScreenActivity.KEYBOARD_REQUEST_CODE;


public class FlashcardStartScreenFragment extends Fragment {
    private NumberPicker effectivePicker;
    private NumberPicker minutesPicker;
    private NumberPicker wpmPicker;
    private FlashcardTrainingMainScreenViewModel sessionViewModel;
    private Class<? extends FragmentActivity> sessionActivityClass;
    private FlashcardSessionType sessionType;
    private SharedPreferences preferences;
    private TextView selectedStringsContainer;


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


        TextView changeLetters = rootView.findViewById(R.id.change_included_letters);
        changeLetters.setOnClickListener(this::showIncludedLetterPicker);
        PreferenceManager.setDefaultValues(getActivity().getApplicationContext(), R.xml.flashcard_settings, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        effectivePicker = rootView.findViewById(R.id.effective_wpm_number_picker);
        minutesPicker = rootView.findViewById(R.id.number_picker_minutes);
        wpmPicker = rootView.findViewById(R.id.letter_wpm_number_picker);
        selectedStringsContainer = rootView.findViewById(R.id.selected_strings);
        sessionViewModel = ViewModelProviders.of(this).get(FlashcardTrainingMainScreenViewModel.class);
        sessionViewModel.getLatestSession(sessionType).observe(this, (sessionWithEvents) -> {
            int playLetterWPM = preferences.getInt(getResources().getString(R.string.setting_flashcard_letter_wpm), 25);
            int effectiveLetterWPM = preferences.getInt(getResources().getString(R.string.setting_flashcard_effective_wpm), 25);
            int durationMinutes = preferences.getInt(getResources().getString(R.string.setting_flashcard_duration_minutes), 1);
            wpmPicker.setProgress(playLetterWPM);
            effectivePicker.setProgress(effectiveLetterWPM);
            minutesPicker.setProgress(durationMinutes);
            List<String> prevSelectedLetters;
            if (!sessionWithEvents.isEmpty()) {
                FlashcardTrainingSessionWithEvents session = sessionWithEvents.get(0);
                prevSelectedLetters = session.session.cards;
            } else {
                prevSelectedLetters = getFlashcardActivity().initialSelectedStrings();
            }

            sessionViewModel.selectedStrings.setValue(Lists.newArrayList(prevSelectedLetters));

            Button startButton = rootView.findViewById(R.id.start_button);
            startButton.setOnClickListener(this::handleStartButtonClick);
        });

        sessionViewModel.selectedStrings.observe(this, (updatedSelectedStrings) -> {
            if (updatedSelectedStrings == null) {
                return;
            }
            sessionViewModel.selectedStringsBooleanMap.setValue(selectedStringsToBooleanMap(updatedSelectedStrings));
            selectedStringsContainer.setText(Joiner.on(", ").join(updatedSelectedStrings));
        });

        TextView additionalSettingsLink = rootView.findViewById(R.id.additional_settings);
        additionalSettingsLink.setOnClickListener(this::launchSettings);

        return rootView;
    }

    private boolean[] selectedStringsToBooleanMap(List<String> selectedLetters) {
        List<String> possibleStrings = getFlashcardActivity().getPossibleStrings();
        boolean[] lettersMap = new boolean[possibleStrings.size()];
        String possible;
        for (int i = 0; i < possibleStrings.size(); i++) {
            possible = possibleStrings.get(i);
            lettersMap[i] = selectedLetters.contains(possible);
        }

        return lettersMap;
    }

    private ArrayList<String> booleanMapToSelectedStrings(List<String> possibleStrings, boolean[] selectedStringsBooleanMap) {
        ArrayList<String> selectedStrings = Lists.newArrayList();
        for (int i = 0; i < selectedStringsBooleanMap.length; i++) {
            boolean selected = selectedStringsBooleanMap[i];
            if (selected) {
                selectedStrings.add(possibleStrings.get(i));
            }
        }

        return selectedStrings;
    }

    public void showIncludedLetterPicker(View v) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Choose which letters to play");

        List<String> possibleStrings = getFlashcardActivity().getPossibleStrings();
        boolean[] selectedStringsBooleanMap = sessionViewModel.selectedStringsBooleanMap.getValue();
        builder.setMultiChoiceItems(possibleStrings.toArray(new String[]{}), selectedStringsBooleanMap, (dialog, which, isChecked) -> {
            // user checked or unchecked a box
            selectedStringsBooleanMap[which] = isChecked;
        });

        builder.setNeutralButton("Select All", (dialog, which) -> {
            for (int i = 0; i < selectedStringsBooleanMap.length; i++) {
                selectedStringsBooleanMap[i] = true;
            }
            sessionViewModel.selectedStrings.setValue(booleanMapToSelectedStrings(getFlashcardActivity().getPossibleStrings(), selectedStringsBooleanMap));
            sessionViewModel.selectedStringsBooleanMap.setValue(selectedStringsBooleanMap);
        });

        builder.setPositiveButton("OK", (dialog, which) -> {
            // user clicked OK
            sessionViewModel.selectedStrings.setValue(booleanMapToSelectedStrings(getFlashcardActivity().getPossibleStrings(), selectedStringsBooleanMap));
            sessionViewModel.selectedStringsBooleanMap.setValue(selectedStringsBooleanMap);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private FlashcardStartScreenActivity getFlashcardActivity() {
        return ((FlashcardStartScreenActivity)getActivity());
    }



    private void handleStartButtonClick(View view) {
        int toneFrequency = preferences.getInt(getResources().getString(R.string.setting_flashcard_audio_tone), 440);
        Intent sendIntent = new Intent(view.getContext(), sessionActivityClass);
        Bundle bundle = new Bundle();
        bundle.putInt(FlashcardKeyboardSessionActivity.WPM_REQUESTED, wpmPicker.getProgress());
        bundle.putInt(FlashcardKeyboardSessionActivity.DURATION_REQUESTED_MINUTES, minutesPicker.getProgress());
        bundle.putInt(FlashcardKeyboardSessionActivity.TONE_FREQUENCY_HZ, toneFrequency);
        sendIntent.putExtras(bundle);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        bundle.putStringArrayList(
                FlashcardKeyboardSessionActivity.STRINGS_REQUESTED,
                sessionViewModel.selectedStrings.getValue()
        );
        startActivityForResult(sendIntent, KEYBOARD_REQUEST_CODE);
    }

    private FlashcardStartScreenActivity getSocraticActivity() {
        return ((FlashcardStartScreenActivity)getActivity());
    }

    private void launchSettings(View view) {
        Intent intent = new Intent(view.getContext(), getSocraticActivity().getSettingsActivity());
        startActivity(intent);
    }
}
