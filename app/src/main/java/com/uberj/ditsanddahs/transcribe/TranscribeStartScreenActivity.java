package com.uberj.ditsanddahs.transcribe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.training.DialogFragmentProvider;
import com.uberj.ditsanddahs.transcribe.storage.TranscribeSessionType;
import com.google.android.material.tabs.TabLayout;

import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

public abstract class TranscribeStartScreenActivity extends AppCompatActivity implements DialogFragmentProvider {
    public static final int KEYBOARD_REQUEST_CODE = 0;

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
    }

    protected abstract TranscribeSessionType getSessionType();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
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
                return getStartScreenFragment(sessionType, sessionActivityClass);
            } else {
                return TranscribeNumberScreenFragment.newInstance(sessionType);
            }
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }

    protected abstract Fragment getStartScreenFragment(TranscribeSessionType sessionType, Class<? extends FragmentActivity> sessionActivityClass);

    public abstract Class<? extends Activity> getSettingsActivity();

    protected abstract List<String> initialSelectedStrings();

    protected abstract List<String> getPossibleStrings();

    public abstract Class<? extends FragmentActivity> getSessionActivityClass();
}
