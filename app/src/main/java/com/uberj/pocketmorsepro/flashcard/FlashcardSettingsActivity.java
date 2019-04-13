package com.uberj.pocketmorsepro.flashcard;

import android.os.Bundle;
import android.view.MenuItem;

import com.uberj.pocketmorsepro.R;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public abstract class FlashcardSettingsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.empty_frame_layout);
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

    public abstract FlashcardPreferenceFragment getPreferenceFragment();
}
