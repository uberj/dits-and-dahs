package com.example.uberj.test1.transcribe;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.uberj.test1.BuildConfig;
import com.example.uberj.test1.R;
import com.example.uberj.test1.training.DialogFragmentProvider;
import com.example.uberj.test1.transcribe.storage.TranscribeSessionType;
import com.example.uberj.test1.transcribe.storage.TranscribeTrainingEngineSettings;
import com.google.android.material.tabs.TabLayout;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main4, menu);
        return true;
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

    public static class StartScreenFragment extends Fragment  {
        private NumberPicker minutesPicker;
        private NumberPicker letterWpmNumberPicker;
        private NumberPicker transmitWpmNumberPicker;
        private TranscribeTrainingMainScreenViewModel sessionViewModel;
        private Class<? extends FragmentActivity> sessionActivityClass;
        private TranscribeSessionType sessionType;
        private TextView selectedStringsContainer;


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
            builder.setTitle("Choose some animals");

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


            minutesPicker = rootView.findViewById(R.id.number_picker_minutes);
            letterWpmNumberPicker = rootView.findViewById(R.id.letter_wpm_number_picker);
            transmitWpmNumberPicker = rootView.findViewById(R.id.transmit_wpm_number_picker);
            selectedStringsContainer = rootView.findViewById(R.id.selected_strings);
            sessionViewModel = ViewModelProviders.of(this).get(TranscribeTrainingMainScreenViewModel.class);

            sessionViewModel.selectedStrings.observe(this, (updatedSelectedStrings) -> {
                if (updatedSelectedStrings == null) {
                    return;
                }
                sessionViewModel.selectedStringsBooleanMap.setValue(selectedStringsToBooleanMap(updatedSelectedStrings));
                selectedStringsContainer.setText(Joiner.on(", ").join(updatedSelectedStrings));
            });

            sessionViewModel.getLatestEngineSettings(sessionType).observe(this, (mostRecentSettings) -> {
                int letterWpm = -1;
                int transmitWpm = -1;
                long prevDurationRequestedMillis = -1L;
                List<String> prevSelectedLetters = null;
                if (!mostRecentSettings.isEmpty()) {
                    TranscribeTrainingEngineSettings engineSettings = mostRecentSettings.get(0);
                    letterWpm = engineSettings.letterWpmRequested;
                    transmitWpm = engineSettings.transmitWpmRequested;
                    prevDurationRequestedMillis = engineSettings.durationRequestedMillis;
                    prevSelectedLetters = engineSettings.selectedStrings;
                }

                if (letterWpm > 0) {
                    letterWpmNumberPicker.setProgress(letterWpm);
                } else {
                    letterWpmNumberPicker.setProgress(20);
                }

                if (transmitWpm > 0) {
                    transmitWpmNumberPicker.setProgress(letterWpm);
                } else {
                    transmitWpmNumberPicker.setProgress(6);
                }

                if (prevDurationRequestedMillis > 0) {
                    long prevDurationRequestedMinutes = (prevDurationRequestedMillis / 1000) / 60;
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
            });

            Button startButton = rootView.findViewById(R.id.start_button);
            startButton.setOnClickListener(v -> {
                Intent sendIntent = new Intent(rootView.getContext(), sessionActivityClass);
                Bundle bundle = new Bundle();
                bundle.putInt(TranscribeKeyboardSessionActivity.FARNSWORTH_SPACES, 3);
                bundle.putInt(TranscribeKeyboardSessionActivity.LETTER_WPM_REQUESTED, letterWpmNumberPicker.getProgress());
                bundle.putInt(TranscribeKeyboardSessionActivity.TRANSMIT_WPM_REQUESTED, transmitWpmNumberPicker.getProgress());
                bundle.putInt(TranscribeKeyboardSessionActivity.DURATION_REQUESTED_MINUTES, minutesPicker.getProgress());
                bundle.putStringArrayList(
                        TranscribeKeyboardSessionActivity.STRINGS_REQUESTED,
                        sessionViewModel.selectedStrings.getValue()
                );
                sendIntent.putExtras(bundle);
                startActivityForResult(sendIntent, KEYBOARD_REQUEST_CODE);
            });

            return rootView;
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
            sessionViewModel.getLatestSession(sessionType).observe(this, (mostRecentSession) -> {
                float errorRate = -1;
                long prevDurationMillis = -1;
                if (!mostRecentSession.isEmpty()) {
                    errorRate = mostRecentSession.get(0).errorRate;
                    prevDurationMillis = mostRecentSession.get(0).durationWorkedMillis;
                }

                long prevDurationMinutes = (prevDurationMillis / 1000) / 60;
                long prevDurationSeconds = (prevDurationMillis / 1000) % 60;

                ((TextView) rootView.findViewById(R.id.prev_session_duration_time)).setText(
                        prevDurationMinutes >= 0 && prevDurationSeconds >= 0 ?
                                String.format(Locale.ENGLISH, "%02d:%02d", prevDurationMinutes, prevDurationSeconds) :
                                "N/A"
                );
                ((TextView) rootView.findViewById(R.id.prev_session_error_rate)).setText(
                        errorRate >= 0 ? (int) (100 * errorRate) + "%" : "N/A"
                );
            });

            return rootView;
        }
    }
}
