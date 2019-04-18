package com.uberj.pocketmorsepro.flashcard;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.uberj.pocketmorsepro.ProgressGradient;
import com.uberj.pocketmorsepro.R;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardSessionType;
import com.uberj.pocketmorsepro.flashcard.storage.FlashcardTrainingSessionWithEvents;

import androidx.annotation.NonNull;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;

import static com.uberj.pocketmorsepro.flashcard.FlashcardTrainingSessionViewModel.TIME_LIMITED_SESSION_TYPE;

public class FlashcardNumbersScreenFragment extends Fragment {
    private static final DecimalFormat DECIMAL_STAT_FORMATTER = new DecimalFormat("#.##");
    private static final String SYMBOL_COLUMN_NAME = "Symbol";
    private static final String BLANK_DETAIL = "-";
    private FlashcardTrainingMainScreenViewModel sessionViewModel;
    private FlashcardSessionType sessionType;
    private ScrollView detailsContainerScroll;

    public static FlashcardNumbersScreenFragment newInstance(FlashcardSessionType sessionType) {
        FlashcardNumbersScreenFragment fragment = new FlashcardNumbersScreenFragment();
        fragment.setSessionType(sessionType);
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public void setSessionType(FlashcardSessionType sessionType) {
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
            sessionType = FlashcardSessionType.valueOf(savedInstanceState.getString("sessionType"));
        }

        ConstraintLayout rootView = (ConstraintLayout) inflater.inflate(R.layout.flashcard_training_numbers_screen_fragment, container, false);

        detailsContainerScroll = rootView.findViewById(R.id.details_container_scroll);
        sessionViewModel = ViewModelProviders.of(this).get(FlashcardTrainingMainScreenViewModel.class);
        sessionViewModel.getLatestSession(sessionType).observe(this, (mostRecentSession) -> {
            double firstGuessAccuracy = -1;
            int skipCount = -1;
            int numberCardsCompleted = -1;
            long durationUnits = -1;
            if (!mostRecentSession.isEmpty()) {
                FlashcardTrainingSessionWithEvents s = mostRecentSession.get(0);
                durationUnits = FlashcardUtil.calcDurationMillis(s.events);
                numberCardsCompleted = FlashcardUtil.calcNumCardsCompleted(s.events);
            }
            long prevDurationMinutes = (durationUnits / 1000) / 60;
            long prevDurationSeconds = (durationUnits / 1000) % 60;

            ((TextView) rootView.findViewById(R.id.prev_session_duration_time)).setText(
                    prevDurationMinutes >= 0 && prevDurationSeconds >= 0 ?
                            String.format(Locale.ENGLISH, "%02d:%02d", prevDurationMinutes, prevDurationSeconds) :
                            "N/A"
            );
            ((TextView) rootView.findViewById(R.id.prev_session_first_guess_accuracy)).setText(
                    firstGuessAccuracy >= 0 ? String.format(Locale.ENGLISH, "%.2f", firstGuessAccuracy) : "N/A"
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
