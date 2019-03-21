package com.uberj.pocketmorsepro.transcribe;

import android.os.Bundle;
import android.view.MenuItem;

import com.uberj.pocketmorsepro.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class TranscribeSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.transcribe_settings);
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.main_content, getPreferenceFragment())
                .commit();

        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                finish();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public abstract TranscribePreferenceFragment getPreferenceFragment();
}
