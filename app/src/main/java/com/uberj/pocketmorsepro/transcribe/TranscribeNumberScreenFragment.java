package com.uberj.pocketmorsepro.transcribe;

import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.uberj.pocketmorsepro.ProgressGradient;
import com.uberj.pocketmorsepro.R;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeSessionType;
import com.uberj.pocketmorsepro.transcribe.storage.TranscribeTrainingSession;

import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

public class TranscribeNumberScreenFragment extends Fragment {
    private TranscribeTrainingMainScreenViewModel sessionViewModel;
    private TranscribeSessionType sessionType;

    public static TranscribeNumberScreenFragment newInstance(TranscribeSessionType sessionType) {
        TranscribeNumberScreenFragment fragment = new TranscribeNumberScreenFragment();
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
