package com.example.uberj.test1.socratic;

import com.example.uberj.test1.R;
import com.example.uberj.test1.socratic.storage.SocraticTrainingEngineSettings;
import com.example.uberj.test1.socratic.storage.SocraticSessionType;
import com.example.uberj.test1.training.DialogFragmentProvider;
import com.google.android.material.tabs.TabLayout;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager.widget.ViewPager;
import it.sephiroth.android.library.numberpicker.NumberPicker;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

public abstract class SocraticStartScreenActivity extends AppCompatActivity implements DialogFragmentProvider {
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
    private SocraticSessionType sessionType = getSessionType();

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
    }

    protected abstract SocraticSessionType getSessionType();

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
        private NumberPicker wpmPicker;
        private CheckBox resetLetterWeights;
        private SocraticTrainingMainScreenViewModel sessionViewModel;
        private Class<? extends FragmentActivity> sessionActivityClass;
        private SocraticSessionType sessionType;


        public static StartScreenFragment newInstance(SocraticSessionType sessionType, Class<? extends FragmentActivity> sessionActivityClass) {
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

        public void setSessionType(SocraticSessionType sessionType) {
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
                sessionType = SocraticSessionType.valueOf(savedInstanceState.getString("sessionType"));
            }

            View rootView = inflater.inflate(R.layout.socratic_training_start_screen_fragment, container, false);
            ImageView helpWPM = rootView.findViewById(R.id.wpmhelp);
            helpWPM.setOnClickListener((l) -> {
                DialogFragmentProvider provider = (DialogFragmentProvider) getActivity();
                DialogFragment dialog = provider.getHelpDialog();
                FragmentManager supportFragmentManager = provider.getHelpDialogFragmentManager();
                dialog.show(supportFragmentManager, dialog.getTag());
            });


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

            Button startButton = rootView.findViewById(R.id.start_button);
            startButton.setOnClickListener(v -> {
                Intent sendIntent = new Intent(rootView.getContext(), sessionActivityClass);
                Bundle bundle = new Bundle();
                bundle.putInt(SocraticKeyboardSessionActivity.WPM_REQUESTED, wpmPicker.getProgress());
                bundle.putInt(SocraticKeyboardSessionActivity.DURATION_REQUESTED_MINUTES, minutesPicker.getProgress());
                bundle.putBoolean(SocraticKeyboardSessionActivity.REQUEST_WEIGHTS_RESET, resetLetterWeights.isChecked());
                sendIntent.putExtras(bundle);
                startActivityForResult(sendIntent, KEYBOARD_REQUEST_CODE);
                resetLetterWeights.setChecked(false);
            });

            return rootView;
        }

    }

    public abstract Class<? extends FragmentActivity> getSessionActivityClass();

    public static class NumbersScreenFragment extends Fragment  {
        private SocraticTrainingMainScreenViewModel sessionViewModel;
        private SocraticSessionType sessionType;

        public static NumbersScreenFragment newInstance(SocraticSessionType sessionType) {
            NumbersScreenFragment fragment = new NumbersScreenFragment();
            fragment.setSessionType(sessionType);
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        public void setSessionType(SocraticSessionType sessionType) {
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
                sessionType = SocraticSessionType.valueOf(savedInstanceState.getString("sessionType"));
            }

            View rootView = inflater.inflate(R.layout.socratic_training_numbers_screen_fragment, container, false);
            sessionViewModel = ViewModelProviders.of(this).get(SocraticTrainingMainScreenViewModel.class);
            sessionViewModel.getLatestSession(sessionType).observe(this, (mostRecentSession) -> {
                float wpmAverage = -1;
                double accuracy = -1;
                long prevDurationMillis = -1;
                if (!mostRecentSession.isEmpty()) {
                    wpmAverage = mostRecentSession.get(0).wpmAverage;
                    accuracy = mostRecentSession.get(0).accuracy;
                    prevDurationMillis = mostRecentSession.get(0).durationWorkedMillis;

                }

                long prevDurationMinutes = (prevDurationMillis / 1000) / 60;
                long prevDurationSeconds = (prevDurationMillis / 1000) % 60;

                ((TextView) rootView.findViewById(R.id.prev_session_duration_time)).setText(
                        prevDurationMinutes >= 0 && prevDurationSeconds >= 0 ?
                                String.format(Locale.ENGLISH, "%02d:%02d", prevDurationMinutes, prevDurationSeconds) :
                                "N/A"
                );
                ((TextView) rootView.findViewById(R.id.prev_session_wpm_average)).setText(
                        wpmAverage >= 0 ? String.format(Locale.ENGLISH, "%.2f", wpmAverage) : "N/A"
                );
                ((TextView) rootView.findViewById(R.id.prev_session_accuracy)).setText(
                        accuracy >= 0 ? (int) (100 * accuracy) + "%" : "N/A"
                );
            });

            return rootView;
        }
    }
}
