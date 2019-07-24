package com.uberj.ditsanddahs.flashcard;

import com.crashlytics.android.Crashlytics;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardSessionType;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardTrainingSessionWithEvents;
import com.uberj.ditsanddahs.training.DialogFragmentProvider;

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

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.uberj.ditsanddahs.flashcard.FlashcardStartScreenActivity.KEYBOARD_REQUEST_CODE;


public class FlashcardStartScreenFragment extends Fragment implements AdapterView.OnItemSelectedListener  {
    private NumberPicker effectivePicker;
    private NumberPicker durationPicker;
    private NumberPicker wpmPicker;
    private FlashcardTrainingMainScreenViewModel sessionViewModel;
    private FlashcardSessionType sessionType;
    private SharedPreferences preferences;
    private TextView selectedStringsContainer;
    private TextView sessionLengthTitle;
    private String sessionActivityClassName;
    private Spinner spinner;


    public static FlashcardStartScreenFragment newInstance(Class<? extends FragmentActivity> sessionActivityClass) {
        FlashcardStartScreenFragment fragment = new FlashcardStartScreenFragment();
        fragment.setSessionActivityClass(sessionActivityClass);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int pos, long id) {
        // An item was selected. You can retrieve the selected item using
        // parent.getItemAtPosition(pos)
        String cardType = FlashcardUtil.getCardType(getResources(), pos);
        if (cardType.equals(getResources().getString(R.string.fcc_call_signs_flashcard_type))) {
            selectFCCCallSigns(parent.getRootView());
        } else {
            selectCommonWords(parent.getRootView());
        }
    }

    private void selectCommonWords(View rootView) {
        rootView.findViewById(R.id.include_title).setVisibility(View.VISIBLE);
        rootView.findViewById(R.id.change_included_letters).setVisibility(View.VISIBLE);
        TextView selectedStrings = rootView.findViewById(R.id.selected_strings);
        selectedStrings.setText(Joiner.on(", ").join(Objects.requireNonNull(sessionViewModel.selectedStrings.getValue())));
        sessionType = FlashcardSessionType.RANDOM_WORDS;
    }

    private void selectFCCCallSigns(View rootView) {
        rootView.findViewById(R.id.include_title).setVisibility(View.GONE);
        rootView.findViewById(R.id.change_included_letters).setVisibility(View.GONE);
        TextView selectedStrings = rootView.findViewById(R.id.selected_strings);
        selectedStrings.setText(getResources().getString(R.string.random_calls_selected_description));
        sessionType = FlashcardSessionType.RANDOM_FCC_CALLSIGNS;
    }

    public void onNothingSelected(AdapterView<?> parent) {
        // Another interface callback
        System.out.println("here");
    }

    private void setSessionActivityClass(Class<? extends FragmentActivity> sessionActivityClass) {
        this.sessionActivityClassName = sessionActivityClass.getName();
    }

    public void setSessionType(FlashcardSessionType sessionType) {
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
        super.onResume();
        setupDurationLogic();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            sessionType = FlashcardSessionType.valueOf(savedInstanceState.getString("sessionType"));
            sessionActivityClassName = savedInstanceState.getString("sessionActivityClassName");
        }

        View rootView = inflater.inflate(R.layout.flashcard_training_start_screen_fragment, container, false);
        ImageView helpWPM = rootView.findViewById(R.id.wpmhelp);
        helpWPM.setOnClickListener((l) -> {
            DialogFragmentProvider provider = (DialogFragmentProvider) getActivity();
            DialogFragment dialog = provider.getHelpDialog();
            FragmentManager supportFragmentManager = provider.getHelpDialogFragmentManager();
            dialog.show(supportFragmentManager, dialog.getTag());
        });

        spinner = rootView.findViewById(R.id.card_type_selector);
        // Create an ArrayAdapter using the string array and a default spinner layout
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.flashcard_type, android.R.layout.simple_spinner_item);
        // Specify the layout to use when the list of choices appears
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        // Apply the adapter to the spinner
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(this);

        TextView changeLetters = rootView.findViewById(R.id.change_included_letters);
        changeLetters.setOnClickListener(this::showIncludedLetterPicker);
        PreferenceManager.setDefaultValues(getActivity().getApplicationContext(), R.xml.flashcard_settings, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        effectivePicker = rootView.findViewById(R.id.effective_wpm_number_picker);
        durationPicker = rootView.findViewById(R.id.number_picker_duration);
        wpmPicker = rootView.findViewById(R.id.letter_wpm_number_picker);
        sessionLengthTitle = rootView.findViewById(R.id.session_length);
        selectedStringsContainer = rootView.findViewById(R.id.selected_strings);
        sessionViewModel = ViewModelProviders.of(this).get(FlashcardTrainingMainScreenViewModel.class);
        sessionViewModel.getLatestSession().observe(this, (sessionWithEvents) -> {
            int playLetterWPM = preferences.getInt(getResources().getString(R.string.setting_flashcard_letter_wpm), 25);
            int effectiveLetterWPM = preferences.getInt(getResources().getString(R.string.setting_flashcard_effective_wpm), 25);
            setupDurationLogic();
            FlashcardSessionType prevSessionType;

            wpmPicker.setProgress(playLetterWPM);
            effectivePicker.setProgress(effectiveLetterWPM);
            List<String> prevSelectedLetters;
            if (!sessionWithEvents.isEmpty()) {
                FlashcardTrainingSessionWithEvents session = sessionWithEvents.get(0);
                prevSelectedLetters = session.session.cards;
                prevSessionType = FlashcardSessionType.valueOf(session.session.sessionType);
            } else {
                prevSelectedLetters = getFlashcardActivity().initialSelectedStrings();
                prevSessionType = FlashcardSessionType.RANDOM_WORDS;
            }

            sessionViewModel.selectedStrings.setValue(Lists.newArrayList(prevSelectedLetters));

            if (prevSessionType.equals(FlashcardSessionType.RANDOM_FCC_CALLSIGNS)) {
                selectFCCCallSigns(rootView);
            } else {
                selectCommonWords(rootView);
            }
            spinner.setSelection(FlashcardUtil.getCardTypePos(getResources(), prevSessionType));

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

        registerNumberPickerListeners();

        return rootView;
    }

    private void registerNumberPickerListeners() {
        // Letter WPM is the upper bound of the effective WPM
        wpmPicker.setNumberPickerChangeListener(new NumberPicker.OnNumberPickerChangeListener() {
            @Override
            public void onProgressChanged(@NotNull NumberPicker numberPicker, int letterWpm, boolean b) {
                int effectiveWpm = effectivePicker.getProgress();
                if (effectiveWpm > letterWpm) {
                    effectivePicker.setProgress(letterWpm);
                }
                SharedPreferences.Editor edit = preferences.edit();
                edit.putInt(getResources().getString(R.string.setting_flashcard_letter_wpm), letterWpm);
                edit.apply();
            }

            @Override
            public void onStartTrackingTouch(@NotNull NumberPicker numberPicker) {

            }

            @Override
            public void onStopTrackingTouch(@NotNull NumberPicker numberPicker) {

            }
        });

        effectivePicker.setNumberPickerChangeListener(new NumberPicker.OnNumberPickerChangeListener() {
            @Override
            public void onProgressChanged(@NotNull NumberPicker numberPicker, int effectiveWpm, boolean b) {
                int letterWpm = wpmPicker.getProgress();
                if (effectiveWpm > letterWpm) {
                    wpmPicker.setProgress(effectiveWpm);
                }
                SharedPreferences.Editor edit = preferences.edit();
                edit.putInt(getResources().getString(R.string.setting_flashcard_effective_wpm), effectiveWpm);
                edit.apply();
            }

            @Override
            public void onStartTrackingTouch(@NotNull NumberPicker numberPicker) {

            }

            @Override
            public void onStopTrackingTouch(@NotNull NumberPicker numberPicker) {

            }
        });

        durationPicker.setNumberPickerChangeListener(new NumberPicker.OnNumberPickerChangeListener() {
            @Override
            public void onProgressChanged(@NotNull NumberPicker durationPicker, int effectiveWpm, boolean b) {
                int durationPickerProgress = durationPicker.getProgress();
                setDurationUnits(durationPickerProgress);
            }

            @Override
            public void onStartTrackingTouch(@NotNull NumberPicker numberPicker) {

            }

            @Override
            public void onStopTrackingTouch(@NotNull NumberPicker numberPicker) {

            }
        });
    }

    private void setDurationUnits(int progress) {
        String numCardsDurationUnit = getResources().getString(R.string.flashcard_num_cards_option);
        String durationUnit = preferences.getString(getResources().getString(R.string.setting_flashcard_duration_unit), numCardsDurationUnit);
        SharedPreferences.Editor edit = preferences.edit();
        if (durationUnit.equals(numCardsDurationUnit)) {
            edit.putInt(getResources().getString(R.string.setting_flashcard_duration_num_cards), progress);
        } else {
            edit.putInt(getResources().getString(R.string.setting_flashcard_duration_time_minutes), progress);
        }
        edit.apply();
    }

    private void setupDurationLogic() {
        String numCardsDurationUnit = getResources().getString(R.string.flashcard_num_cards_option);
        String durationUnit = preferences.getString(getResources().getString(R.string.setting_flashcard_duration_unit), numCardsDurationUnit);
        if (durationUnit.equals(numCardsDurationUnit)) {
            int progress = preferences.getInt(getResources().getString(R.string.setting_flashcard_duration_num_cards), 20);
            durationPicker.setProgress(progress);
            sessionLengthTitle.setText("# of words played:");
        } else {
            int progress = preferences.getInt(getResources().getString(R.string.setting_flashcard_duration_time_minutes), 5);
            durationPicker.setProgress(progress);
            sessionLengthTitle.setText("Duration (minutes):");
        }
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

        boolean allTrue = true;
        for (boolean b : selectedStringsBooleanMap) {
            if (!b) {
                allTrue = false;
            }
        }

        if (allTrue) {
            builder.setNeutralButton("Reset/De-select", (dialog, which) -> {
                for (int i = 0; i < selectedStringsBooleanMap.length; i++) {
                    if (i == 0 || i == 1) {
                        selectedStringsBooleanMap[i] = true;
                    } else  {
                        selectedStringsBooleanMap[i] = false;
                    }
                }
                sessionViewModel.selectedStrings.setValue(booleanMapToSelectedStrings(getFlashcardActivity().getPossibleStrings(), selectedStringsBooleanMap));
                sessionViewModel.selectedStringsBooleanMap.setValue(selectedStringsBooleanMap);
            });
        } else {
            builder.setNeutralButton("Select All", (dialog, which) -> {
                for (int i = 0; i < selectedStringsBooleanMap.length; i++) {
                    selectedStringsBooleanMap[i] = true;
                }
                sessionViewModel.selectedStrings.setValue(booleanMapToSelectedStrings(getFlashcardActivity().getPossibleStrings(), selectedStringsBooleanMap));
                sessionViewModel.selectedStringsBooleanMap.setValue(selectedStringsBooleanMap);
            });
        }

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
        String numCardsDurationUnit = getResources().getString(R.string.flashcard_num_cards_option);
        String durationUnit = preferences.getString(getResources().getString(R.string.setting_flashcard_duration_unit), numCardsDurationUnit);
        Class<? extends FragmentActivity> sessionActivityClass = null;
        try {
            sessionActivityClass = (Class<? extends FragmentActivity>) Class.forName(sessionActivityClassName);
        } catch (ClassNotFoundException e) {
            Crashlytics.log(String.format("Couldn't find sessionActivityClassName with '%s'", sessionActivityClassName));
        }
        Intent sendIntent = new Intent(view.getContext(), sessionActivityClass);
        Bundle bundle = new Bundle();
        bundle.putInt(FlashcardKeyboardSessionActivity.WPM_REQUESTED, wpmPicker.getProgress());
        bundle.putInt(FlashcardKeyboardSessionActivity.DURATION_UNITS_REQUESTED, durationPicker.getProgress());
        bundle.putString(FlashcardKeyboardSessionActivity.DURATION_UNIT, durationUnit);
        bundle.putInt(FlashcardKeyboardSessionActivity.TONE_FREQUENCY_HZ, toneFrequency);
        bundle.putString(FlashcardKeyboardSessionActivity.SESSION_TYPE, sessionType.name());
        bundle.putStringArrayList(
                FlashcardKeyboardSessionActivity.MESSAGES_REQUESTED,
                sessionViewModel.selectedStrings.getValue()
        );
        sendIntent.putExtras(bundle);
        sendIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
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
