package com.example.uberj.test1;

import com.example.uberj.test1.LetterTraining.LetterTrainingKeyboardSessionActivity;
import com.example.uberj.test1.storage.LetterTrainingSessionDAO;
import com.example.uberj.test1.storage.TheDatabase;
import com.google.android.material.tabs.TabLayout;

import androidx.appcompat.app.AppCompatActivity;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
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
import android.widget.TextView;

import java.util.Locale;

public class Main4Activity extends AppCompatActivity {
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

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main4);

        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

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
            View rootView = inflater.inflate(R.layout.fragment_main4, container, false);
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            if (position == 0) {
                return StartScreenFragment.newInstance();
            } else {
                return NumbersScreenFragment.newInstance();
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

        public static StartScreenFragment newInstance() {
            StartScreenFragment fragment = new StartScreenFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.letter_training_start_screen_fragment, container, false);
            minutesPicker = rootView.findViewById(R.id.number_picker_minutes);
            LetterTrainingSessionDAO letterTrainingSessionDAO = TheDatabase.getDatabase(rootView.getContext()).trainingSessionDAO();
            letterTrainingSessionDAO.getLatestSession((latestSession) -> {
                long prevDurationRequestedMillis = latestSession.map((ts) -> ts.durationRequestedMillis).orElse(-1l);
                if (prevDurationRequestedMillis >= 0) {
                    long prevDurationRequestedMinutes = (prevDurationRequestedMillis / 1000) / 60;
                    minutesPicker.setProgress((int) prevDurationRequestedMinutes);
                } else {
                    minutesPicker.setProgress(1);
                }
            });

            Button startButton = rootView.findViewById(R.id.start_button);
            startButton.setOnClickListener(v -> {
                Intent sendIntent = new Intent(rootView.getContext(), LetterTrainingKeyboardSessionActivity.class);
                Bundle bundle = new Bundle();
                bundle.putInt(LetterTrainingKeyboardSessionActivity.WPM_REQUESTED, 20);
                bundle.putInt(LetterTrainingKeyboardSessionActivity.DURATION_REQUESTED_MINUTES, minutesPicker.getProgress());
                sendIntent.putExtras(bundle);
                startActivityForResult(sendIntent, KEYBOARD_REQUEST_CODE);  // NOTE: Ignore request code for now. might become important later
            });

            return rootView;
        }
    }


    public static class NumbersScreenFragment extends Fragment  {
        public static NumbersScreenFragment newInstance() {
            NumbersScreenFragment fragment = new NumbersScreenFragment();
            Bundle args = new Bundle();
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.letter_training_numbers_screen_fragment, container, false);
            LetterTrainingSessionDAO letterTrainingSessionDAO = TheDatabase.getDatabase(rootView.getContext()).trainingSessionDAO();
            letterTrainingSessionDAO.getLatestSession((latestSession) -> {
                float wpmAverage = latestSession.map(ts -> ts.wpmAverage).orElse(-1f);
                float errorRate = latestSession.map(ts -> ts.errorRate).orElse(-1f);
                long prevDurationMillis = latestSession.map((ts) -> ts.durationWorkedMillis).orElse(-1l);
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
                ((TextView) rootView.findViewById(R.id.prev_session_error_rate)).setText(
                        errorRate >= 0 ? (int) (100 * errorRate) + "%" : "N/A"
                );
            });
            return rootView;
        }
    }
}
