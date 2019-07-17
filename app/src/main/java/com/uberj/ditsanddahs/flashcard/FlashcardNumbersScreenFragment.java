package com.uberj.ditsanddahs.flashcard;

import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardSessionType;
import com.uberj.ditsanddahs.flashcard.storage.FlashcardTrainingSessionWithEvents;

import androidx.annotation.NonNull;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ScrollView;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Locale;

public class FlashcardNumbersScreenFragment extends Fragment {
    private FlashcardTrainingMainScreenViewModel sessionViewModel;

    public static FlashcardNumbersScreenFragment newInstance() {
        FlashcardNumbersScreenFragment fragment = new FlashcardNumbersScreenFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ConstraintLayout rootView = (ConstraintLayout) inflater.inflate(R.layout.flashcard_training_numbers_screen_fragment, container, false);

        sessionViewModel = ViewModelProviders.of(this).get(FlashcardTrainingMainScreenViewModel.class);
        sessionViewModel.getLatestSession().observe(this, (mostRecentSession) -> {
            double firstGuessAccuracy = -1;
            int skipCount = -1;
            int numberCardsCompleted = -1;
            long durationUnits = -1;
            if (!mostRecentSession.isEmpty()) {
                FlashcardTrainingSessionWithEvents s = mostRecentSession.get(0);
                durationUnits = FlashcardUtil.calcDurationMillis(s.events);
                numberCardsCompleted = FlashcardUtil.calcNumCardsCompleted(s.events);
                firstGuessAccuracy = FlashcardUtil.calcFirstGuessAccuracy(s.events);
                skipCount = FlashcardUtil.calcSkipCount(s.events);
            }
            long prevDurationMinutes = (durationUnits / 1000) / 60;
            long prevDurationSeconds = (durationUnits / 1000) % 60;

            ((TextView) rootView.findViewById(R.id.prev_session_duration_time)).setText(
                    prevDurationMinutes >= 0 && prevDurationSeconds >= 0 ?
                            String.format(Locale.ENGLISH, "%02d:%02d", prevDurationMinutes, prevDurationSeconds) :
                            "N/A"
            );
            ((TextView) rootView.findViewById(R.id.prev_session_first_guess_accuracy)).setText(
                    firstGuessAccuracy >= 0 ? String.format(Locale.ENGLISH, "%s%%", (int) (firstGuessAccuracy * 100)) : "N/A"
            );
            ((TextView) rootView.findViewById(R.id.prev_session_num_cards_completed)).setText(
                    skipCount >= 0 ? String.valueOf(numberCardsCompleted) : "N/A"
            );
            ((TextView) rootView.findViewById(R.id.prev_session_skip_count)).setText(
                    skipCount >= 0 ? String.valueOf(skipCount) : "N/A"
            );
        });


        return rootView;
    }
}
