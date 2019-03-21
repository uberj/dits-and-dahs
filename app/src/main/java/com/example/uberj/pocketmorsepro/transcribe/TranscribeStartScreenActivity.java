package com.example.uberj.pocketmorsepro.transcribe;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.uberj.pocketmorsepro.ProgressGradient;
import com.example.uberj.pocketmorsepro.R;
import com.example.uberj.pocketmorsepro.training.DialogFragmentProvider;
import com.example.uberj.pocketmorsepro.transcribe.storage.TranscribeSessionType;
import com.example.uberj.pocketmorsepro.transcribe.storage.TranscribeTrainingSession;
import com.google.android.material.tabs.TabLayout;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.preference.PreferenceManager;
import androidx.viewpager.widget.ViewPager;
import it.sephiroth.android.library.numberpicker.NumberPicker;
import timber.log.Timber;

public abstract class TranscribeStartScreenActivity extends AppCompatActivity implements DialogFragmentProvider {
    private static final int KEYBOARD_REQUEST_CODE = 0;

    /**
     * The {@link androidx.viewpager.widget.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * androidx.fragment.app.FragmentStatePagerAdapter.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private TranscribeSessionType sessionType = getSessionType();

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.base_session_training_start_screen_container);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), getSessionActivityClass());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        // Remove this after debug
        Timber.plant(new Timber.DebugTree());
    }

    protected abstract TranscribeSessionType getSessionType();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (KEYBOARD_REQUEST_CODE == 0) {
            mViewPager.setCurrentItem(1);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public FragmentManager getHelpDialogFragmentManager() {
        return getSupportFragmentManager();
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.base_start_screen_placeholder, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private final Class<? extends FragmentActivity> sessionActivityClass;

        public SectionsPagerAdapter(FragmentManager fm, Class<? extends FragmentActivity> sessionActivityClass) {
            super(fm);
            this.sessionActivityClass = sessionActivityClass;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return StartScreenFragment.newInstance(sessionType, sessionActivityClass);
            } else {
                return NumbersScreenFragment.newInstance(sessionType);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }

    public abstract Class<? extends Activity> getSettingsActivity();

    public static class StartScreenFragment extends Fragment  {
        private static final Random r = new Random();
        private static final double SUGGEST_ADDING_MORE_LETTERS_ACCURACY_CUTOFF = 89;
        private static final double SUGGEST_REMOVING_LETTERS_ACCURACY_CUTOFF = 45;
        private NumberPicker minutesPicker;
        private NumberPicker letterWpmNumberPicker;
        private NumberPicker effectiveWpmNumberPicker;
        private TranscribeTrainingMainScreenViewModel sessionViewModel;
        private Class<? extends FragmentActivity> sessionActivityClass;
        private TranscribeSessionType sessionType;
        private TextView selectedStringsContainer;
        private TextView suggestAddLettersHelpText;
        private TextView additionalSettingsLink;
        private SharedPreferences preferences;


        public static StartScreenFragment newInstance(TranscribeSessionType sessionType, Class<? extends FragmentActivity> sessionActivityClass) {
            StartScreenFragment fragment = new StartScreenFragment();
            fragment.setSessionActivityClass(sessionActivityClass);
            fragment.setSessionType(sessionType);
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        private void setSessionActivityClass(Class<? extends FragmentActivity> sessionActivityClass) {
            this.sessionActivityClass = sessionActivityClass;
        }

        public void setSessionType(TranscribeSessionType sessionType) {
            this.sessionType = sessionType;
        }

        @Override
        public void onSaveInstanceState(@NonNull Bundle outState) {
            outState.putString("sessionType", sessionType.name());
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

            builder.setPositiveButton("OK", (dialog, which) -> {
                // user clicked OK
                sessionViewModel.selectedStrings.setValue(booleanMapToSelectedStrings(getTranscribeActivity().getPossibleStrings(), selectedStringsBooleanMap));
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
            Intent sendIntent = new Intent(view.getContext(), sessionActivityClass);
            Bundle bundle = new Bundle();
            bundle.putInt(TranscribeKeyboardSessionActivity.FARNSWORTH_SPACES, 3);
            bundle.putInt(TranscribeKeyboardSessionActivity.LETTER_WPM_REQUESTED, letterWpmNumberPicker.getProgress());
            bundle.putInt(TranscribeKeyboardSessionActivity.EFFECTIVE_WPM_REQUESTED, effectiveWpmNumberPicker.getProgress());
            bundle.putInt(TranscribeKeyboardSessionActivity.DURATION_REQUESTED_MINUTES, minutesPicker.getProgress());
            boolean targetIssueStrings = preferences.getBoolean(getResources().getString(R.string.setting_transcribe_target_issue_letters), false);
            int audioToneFrequency = preferences.getInt(getResources().getString(R.string.setting_transcribe_audio_tone), 700);
            int startDelaySeconds = preferences.getInt(getResources().getString(R.string.setting_transcribe_start_delay_seconds), 3);
            int endDelaySeconds = preferences.getInt(getResources().getString(R.string.setting_transcribe_end_delay_seconds), 3);
            bundle.putBoolean(TranscribeKeyboardSessionActivity.TARGET_ISSUE_STRINGS, targetIssueStrings);
            bundle.putInt(TranscribeKeyboardSessionActivity.AUDIO_TONE_FREQUENCY, audioToneFrequency);
            bundle.putInt(TranscribeKeyboardSessionActivity.SESSION_START_DELAY_SECONDS, startDelaySeconds);
            bundle.putInt(TranscribeKeyboardSessionActivity.SESSION_END_DELAY_SECONDS, endDelaySeconds);
            bundle.putStringArrayList(
                    TranscribeKeyboardSessionActivity.STRINGS_REQUESTED,
                    sessionViewModel.selectedStrings.getValue()
            );
            sendIntent.putExtras(bundle);
            sendIntent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivityForResult(sendIntent, KEYBOARD_REQUEST_CODE);
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

    protected abstract List<String> initialSelectedStrings();

    protected abstract List<String> getPossibleStrings();

    public abstract Class<? extends FragmentActivity> getSessionActivityClass();

    public static class NumbersScreenFragment extends Fragment  {
        private TranscribeTrainingMainScreenViewModel sessionViewModel;
        private TranscribeSessionType sessionType;

        public static NumbersScreenFragment newInstance(TranscribeSessionType sessionType) {
            NumbersScreenFragment fragment = new NumbersScreenFragment();
            fragment.setSessionType(sessionType);
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        public void setSessionType(TranscribeSessionType sessionType) {
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
                sessionType = TranscribeSessionType.valueOf(savedInstanceState.getString("sessionType"));
            }

            View rootView = inflater.inflate(R.layout.transcribe_training_numbers_screen_fragment, container, false);
            sessionViewModel = ViewModelProviders.of(this).get(TranscribeTrainingMainScreenViewModel.class);
            sessionViewModel.getLatestSession(sessionType).observe(this, (possibleSession) -> {
                double overallAccuracyRate = -1;
                long prevDurationMillis = -1;
                TableLayout errorListContainer = rootView.findViewById(R.id.error_breakdown_list_container);
                if (!possibleSession.isEmpty()) {
                    TranscribeTrainingSession session = possibleSession.get(0);
                    prevDurationMillis = session.durationRequestedMillis;
                    TranscribeUtil.TranscribeSessionAnalysis analysis = TranscribeUtil.analyzeSession(getContext(), session);
                    TextView transcribeDiff = rootView.findViewById(R.id.transcribe_diff);
                    transcribeDiff.setText(analysis.messageSpan, TextView.BufferType.EDITABLE);
                    overallAccuracyRate = analysis.overallAccuracyRate;
                    errorListContainer.removeAllViews();
                    buildErrorTable(errorListContainer, analysis);
                } else {
                    TextView naTextView = new TextView(getContext());
                    naTextView.setText("N/A");
                    errorListContainer.addView(naTextView);
                }

                long prevDurationMinutes = (prevDurationMillis / 1000) / 60;
                long prevDurationSeconds = (prevDurationMillis / 1000) % 60;

                ((TextView) rootView.findViewById(R.id.prev_session_duration_time)).setText(
                        prevDurationMinutes >= 0 && prevDurationSeconds >= 0 ?
                                String.format(Locale.ENGLISH, "%02d:%02d", prevDurationMinutes, prevDurationSeconds) :
                                "N/A"
                );


                if (overallAccuracyRate >= 0) {
                    SpannableStringBuilder accuracySsb = new SpannableStringBuilder();
                    int roundedAccuracy = (int) (100 * overallAccuracyRate);
                    accuracySsb.append(String.valueOf(roundedAccuracy))
                            .append("%");
                    ForegroundColorSpan errorSpanColor = new ForegroundColorSpan(ProgressGradient.forWeight(Math.min(100, roundedAccuracy)));
                    accuracySsb.setSpan(errorSpanColor, 0, accuracySsb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                    ((TextView) rootView.findViewById(R.id.prev_session_accuracy)).setText(accuracySsb);
                } else {
                    ((TextView) rootView.findViewById(R.id.prev_session_accuracy)).setText("N/A");
                }

            });

            return rootView;
        }

        private void buildErrorTable(TableLayout errorListContainer, TranscribeUtil.TranscribeSessionAnalysis analysis) {
            TableRow headerRow = new TableRow(getContext());
            TextView stringNameTitle = new TextView(getContext());
            stringNameTitle.setText("Symbol");
            stringNameTitle.setPadding(0, 8, 24, 8);
            headerRow.addView(stringNameTitle);

            TextView errorTextTitle = new TextView(getContext());
            errorTextTitle.setText("Accuracy");
            errorTextTitle.setPadding(0, 8, 24, 8);
            headerRow.addView(errorTextTitle);

            TextView countDetails = new TextView(getContext());
            countDetails.setText("Hits/Plays");
            headerRow.addView(countDetails);

            errorListContainer.addView(headerRow);
            List<Map.Entry<String, Pair<Integer, Integer>>> worstFirstHitCases = analysis.hitMap.entrySet().stream().sorted((h1, h2) -> {
                Pair<Integer, Integer> h1Counts = h1.getValue();
                Integer h1HitCount = h1Counts.getLeft();
                Integer h1PlayCount = h1Counts.getRight();
                double h1Accuracy = (h1HitCount.doubleValue() / h1PlayCount.doubleValue()) * 100;

                Pair<Integer, Integer> h2Counts = h2.getValue();
                Integer h2HitCount = h2Counts.getLeft();
                Integer h2PlayCount = h2Counts.getRight();
                double h2Accuracy = (h2HitCount.doubleValue() / h2PlayCount.doubleValue()) * 100;

                if (h1Accuracy == h2Accuracy) {
                    return 0;
                } else {
                    return h1Accuracy < h2Accuracy ? -1 : 1;
                }
            }).collect(Collectors.toList());

            for (Map.Entry<String, Pair<Integer, Integer>> hitCase : worstFirstHitCases) {
                TableRow tableRow = new TableRow(getContext());
                TextView stringName = new TextView(getContext());
                String string = hitCase.getKey();
                if (string.equals(" ")) {
                    stringName.setText("' '");
                } else {
                    stringName.setText(string);
                }
                tableRow.addView(stringName);

                Pair<Integer, Integer> counts = hitCase.getValue();
                Integer hitCount = counts.getLeft();
                Integer playCount = counts.getRight();
                double accuracy = (hitCount.doubleValue() / playCount.doubleValue()) * 100;
                int roundedAccuracy = (int) accuracy;

                TextView errorText = new TextView(getContext());
                SpannableStringBuilder ssb = new SpannableStringBuilder();
                ssb.append(String.valueOf(roundedAccuracy))
                   .append("%");
                ForegroundColorSpan errorSpanColor = new ForegroundColorSpan(ProgressGradient.forWeight(Math.min(100, roundedAccuracy)));
                ssb.setSpan(errorSpanColor, 0, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                errorText.setText(ssb);
                tableRow.addView(errorText);

                TextView missPlays = new TextView(getContext());
                missPlays.setText(String.format(Locale.ENGLISH, "(%d/%d)", hitCount, playCount));
                tableRow.addView(missPlays);



                errorListContainer.addView(tableRow);
            }
        }
    }
}
