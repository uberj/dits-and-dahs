package com.uberj.ditsanddahs.transcribe;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.crashlytics.android.Crashlytics;
import com.uberj.ditsanddahs.ProgressGradient;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.training.DialogFragmentProvider;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeSessionType;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeTrainingSession;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import it.sephiroth.android.library.numberpicker.NumberPicker;

public class TranscribeStartScreenFragment extends Fragment {
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


    public static TranscribeStartScreenFragment newInstance(TranscribeSessionType sessionType, Class<? extends FragmentActivity> sessionActivityClass) {
        TranscribeStartScreenFragment fragment = new TranscribeStartScreenFragment();
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

    public void showIncludedLetterPicker(View v) {
        // setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
        builder.setTitle("Choose which letters to play");

        List<String> possibleStrings = getTranscribeActivity().getPossibleStrings();
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
                sessionViewModel.selectedStrings.setValue(booleanMapToSelectedStrings(getTranscribeActivity().getPossibleStrings(), selectedStringsBooleanMap));
                sessionViewModel.selectedStringsBooleanMap.setValue(selectedStringsBooleanMap);
            });
        } else {
            builder.setNeutralButton("Select All", (dialog, which) -> {
                for (int i = 0; i < selectedStringsBooleanMap.length; i++) {
                    selectedStringsBooleanMap[i] = true;
                }
                sessionViewModel.selectedStrings.setValue(booleanMapToSelectedStrings(getTranscribeActivity().getPossibleStrings(), selectedStringsBooleanMap));
                sessionViewModel.selectedStringsBooleanMap.setValue(selectedStringsBooleanMap);
            });
        }

        builder.setPositiveButton("OK", (dialog, which) -> {
            // user clicked OK
            ArrayList<String> strings = booleanMapToSelectedStrings(getTranscribeActivity().getPossibleStrings(), selectedStringsBooleanMap);
            if (strings.isEmpty()) {
                strings = new ArrayList<>(getTranscribeActivity().initialSelectedStrings());
            }
            sessionViewModel.selectedStrings.setValue(strings);
            sessionViewModel.selectedStringsBooleanMap.setValue(selectedStringsBooleanMap);
        });

        AlertDialog dialog = builder.create();
        dialog.show();
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
        View rootView = inflater.inflate(R.layout.transcribe_training_start_screen_fragment, container, false);
        ImageView helpWPM = rootView.findViewById(R.id.wpmhelp);
        helpWPM.setOnClickListener((l) -> {
            DialogFragmentProvider provider = (DialogFragmentProvider) getActivity();
            DialogFragment dialog = provider.getHelpDialog();
            FragmentManager supportFragmentManager = provider.getHelpDialogFragmentManager();
            dialog.show(supportFragmentManager, dialog.getTag());
        });

        TextView changeLetters = rootView.findViewById(R.id.change_included_letters);
        changeLetters.setOnClickListener(this::showIncludedLetterPicker);


        PreferenceManager.setDefaultValues(getActivity().getApplicationContext(), R.xml.transcribe_settings, false);
        preferences = PreferenceManager.getDefaultSharedPreferences(getActivity().getApplicationContext());
        minutesPicker = rootView.findViewById(R.id.number_picker_minutes);
        letterWpmNumberPicker = rootView.findViewById(R.id.letter_wpm_number_picker);
        effectiveWpmNumberPicker = rootView.findViewById(R.id.effective_wpm_number_picker);
        selectedStringsContainer = rootView.findViewById(R.id.selected_strings);
        suggestAddLettersHelpText = rootView.findViewById(R.id.suggest_add_letters_help_text);
        additionalSettingsLink = rootView.findViewById(R.id.additional_settings);
        sessionViewModel = ViewModelProviders.of(this).get(TranscribeTrainingMainScreenViewModel.class);
        registerOnChangeListeners();

        sessionViewModel.selectedStrings.observe(this, (updatedSelectedStrings) -> {
            if (updatedSelectedStrings == null) {
                return;
            }
            sessionViewModel.selectedStringsBooleanMap.setValue(selectedStringsToBooleanMap(updatedSelectedStrings));
            selectedStringsContainer.setText(Joiner.on(", ").join(updatedSelectedStrings));
        });

        additionalSettingsLink.setOnClickListener(this::launchSettings);

        sessionViewModel.getLatestSession(sessionType).observe(this, (session) -> {
            this.setupNumbersBasedOnPreviousSession(session);
            this.setupPreviousSettings(session);
        });

        Button startButton = rootView.findViewById(R.id.start_button);
        startButton.setOnClickListener(this::handleStartButtonClick);

        return rootView;
    }

    private void launchSettings(View view) {
        Intent intent = new Intent(view.getContext(), getTranscribeActivity().getSettingsActivity());
        startActivity(intent);
    }

    private void registerOnChangeListeners() {
        minutesPicker.setNumberPickerChangeListener(new NumberPicker.OnNumberPickerChangeListener() {
            @Override
            public void onProgressChanged(@NotNull NumberPicker numberPicker, int minutes, boolean b) {
                SharedPreferences.Editor edit = preferences.edit();
                edit.putInt(getResources().getString(R.string.setting_transcribe_duration_minutes), minutes);
                edit.apply();
            }

            @Override
            public void onStartTrackingTouch(@NotNull NumberPicker numberPicker) {

            }

            @Override
            public void onStopTrackingTouch(@NotNull NumberPicker numberPicker) {

            }
        });
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
        boolean targetIssueStrings = preferences.getBoolean(getResources().getString(R.string.setting_transcribe_target_issue_letters), false);
        int audioToneFrequency = preferences.getInt(getResources().getString(R.string.setting_transcribe_audio_tone), 440);
        int startDelaySeconds = preferences.getInt(getResources().getString(R.string.setting_transcribe_start_delay_seconds), 3);
        int endDelaySeconds = preferences.getInt(getResources().getString(R.string.setting_transcribe_end_delay_seconds), 3);
        if (endDelaySeconds == Integer.valueOf(getResources().getString(R.string.setting_transcribe_end_delay_seconds_max_value))) {
            endDelaySeconds = -1;
        }
        int fadeInOutPercentage = preferences.getInt(getResources().getString(R.string.setting_fade_in_out_percentage), 30);

        bundle.putInt(TranscribeKeyboardSessionActivity.LETTER_WPM_REQUESTED, letterWpmNumberPicker.getProgress());
        bundle.putInt(TranscribeKeyboardSessionActivity.EFFECTIVE_WPM_REQUESTED, effectiveWpmNumberPicker.getProgress());
        bundle.putInt(TranscribeKeyboardSessionActivity.DURATION_REQUESTED_MINUTES, minutesPicker.getProgress());
        bundle.putBoolean(TranscribeKeyboardSessionActivity.TARGET_ISSUE_STRINGS, targetIssueStrings);
        bundle.putInt(TranscribeKeyboardSessionActivity.AUDIO_TONE_FREQUENCY, audioToneFrequency);
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

    private void setupNumbersBasedOnPreviousSession(List<TranscribeTrainingSession> possibleSession) {
        if (possibleSession == null || possibleSession.isEmpty()) {
            suggestAddLettersHelpText.setVisibility(View.GONE);
            return;
        }

        TranscribeTrainingSession session = possibleSession.get(0);
        TranscribeUtil.TranscribeSessionAnalysis analysis = TranscribeUtil.analyzeSession(getContext(), session);
        int roundedAccuracy = (int) (100 * analysis.overallAccuracyRate);
        boolean suggestAdd = roundedAccuracy > SUGGEST_ADDING_MORE_LETTERS_ACCURACY_CUTOFF;
        boolean suggestRemove = roundedAccuracy < SUGGEST_REMOVING_LETTERS_ACCURACY_CUTOFF;

        SpannableStringBuilder ssb = null;
        boolean showSuggestion = false;
        if (suggestRemove && session.stringsRequested.size() != getTranscribeActivity().initialSelectedStrings().size()) {
            ssb = buildBaseSuggestion(roundedAccuracy);
            if (r.nextBoolean()) {
                ssb.append("Learning this language is very hard. Consider mastering a smaller subset of letters first, before introducing more characters.");
            } else {
                ssb.append("Learning this language is very hard. Consider lowering the effective speed to give yourself more time between words and letters.");
            }
            showSuggestion = true;
        } else if (suggestAdd && session.stringsRequested.size() != getTranscribeActivity().getPossibleStrings().size()) {
            ssb = buildBaseSuggestion(roundedAccuracy);
            ssb.append("Very good! Consider adding some new letters.");
            showSuggestion = true;
        }

        if (showSuggestion) {
            suggestAddLettersHelpText.setText(ssb);
            suggestAddLettersHelpText.setVisibility(View.VISIBLE);
        } else {
            suggestAddLettersHelpText.setVisibility(View.GONE);
        }
    }

    private void setupPreviousSettings(List<TranscribeTrainingSession> mostRecentSession) {
        int letterWpm = preferences.getInt(getResources().getString(R.string.setting_transcribe_letter_wpm), -1);
        int effectiveWpm = preferences.getInt(getResources().getString(R.string.setting_transcribe_effective_wpm), -1);
        int prevDurationRequestedMinutes = preferences.getInt(getResources().getString(R.string.setting_transcribe_duration_minutes), -1);
        List<String> prevSelectedLetters = null;
        if (!mostRecentSession.isEmpty()) {
            TranscribeTrainingSession session = mostRecentSession.get(0);
            prevSelectedLetters = session.stringsRequested;
        }

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

        if (prevDurationRequestedMinutes > 0) {
            minutesPicker.setProgress((int) prevDurationRequestedMinutes);
        } else {
            minutesPicker.setProgress(1);
        }

        List<String> selectedStrings;
        if (prevSelectedLetters == null) {
            selectedStrings = getTranscribeActivity().initialSelectedStrings();
        } else {
            selectedStrings = prevSelectedLetters;
        }

        sessionViewModel.selectedStrings.setValue(Lists.newArrayList(selectedStrings));
    }

    private SpannableStringBuilder buildBaseSuggestion(int roundedAccuracy) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append("You copied with ");
        int startOffset = ssb.length();
        ssb.append(String.valueOf(roundedAccuracy))
                .append("%");
        ForegroundColorSpan errorSpanColor = new ForegroundColorSpan(ProgressGradient.forWeight(Math.min(100, roundedAccuracy)));
        ssb.setSpan(errorSpanColor, startOffset, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        ssb.append(" accuracy last round. ");
        return ssb;
    }

    private boolean[] selectedStringsToBooleanMap(List<String> selectedLetters) {
        List<String> possibleStrings = getTranscribeActivity().getPossibleStrings();
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


}
