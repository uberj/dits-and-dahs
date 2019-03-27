package com.uberj.pocketmorsepro.socratic;

import com.tomergoldst.tooltips.ToolTip;
import com.tomergoldst.tooltips.ToolTipsManager;
import com.uberj.pocketmorsepro.R;
import com.uberj.pocketmorsepro.socratic.storage.SocraticSessionType;
import com.uberj.pocketmorsepro.socratic.storage.SocraticTrainingSessionWithEvents;

import androidx.annotation.NonNull;

import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.Locale;

public class SocraticNumbersScreenFragment extends Fragment implements View.OnTouchListener {
    private SocraticTrainingMainScreenViewModel sessionViewModel;
    private SocraticSessionType sessionType;
    private ToolTipsManager mToolTipsManager;

    public static SocraticNumbersScreenFragment newInstance(SocraticSessionType sessionType) {
        SocraticNumbersScreenFragment fragment = new SocraticNumbersScreenFragment();
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

        ConstraintLayout rootView = (ConstraintLayout) inflater.inflate(R.layout.socratic_training_numbers_screen_fragment, container, false);
        rootView.setOnTouchListener(this);
        mToolTipsManager = new ToolTipsManager();


        TableLayout acronymContainer = rootView.findViewById(R.id.acronym_container);
        acronymContainer.setOnTouchListener(this);
        ScrollView detailsScrollContainer = rootView.findViewById(R.id.details_container_scroll);
        detailsScrollContainer.setOnTouchListener(this);
        TableLayout detailsContainer = rootView.findViewById(R.id.details_container);
        detailsContainer.setOnTouchListener(this);
        sessionViewModel = ViewModelProviders.of(this).get(SocraticTrainingMainScreenViewModel.class);
        sessionViewModel.getLatestSession(sessionType).observe(this, (mostRecentSession) -> {
            double wpmAverage = -1;
            double accuracy = -1;
            long prevDurationMillis = -1;
            if (!mostRecentSession.isEmpty()) {
                SocraticTrainingSessionWithEvents s = mostRecentSession.get(0);
                SocraticUtil.Analysis analysis = SocraticUtil.analyseSession(s);
                wpmAverage = analysis.wpmAverage;
                accuracy = analysis.overAllAccuracy;
                prevDurationMillis = s.session.durationWorkedMillis;
                detailsContainer.removeAllViews();
                acronymContainer.removeAllViews();
                buildDetailsTable(rootView, detailsContainer, acronymContainer, analysis);
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

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mToolTipsManager.dismissAll();
        return false;
    }

    private enum Infos {
        NUM_PLAYS("# Plays", "Number of times this symbol was played"),
        PBCG("APBCG", "AVERAGE times the letter was PLAYED BEFORE a CORRECT GUESS was made"),
        IBCG("IBCG", "Number of INCORRECT guesses were made BEFORE a CORRECT GUESS was entered"),
        ATCG("ATCG", "AVERAGE TIME it took before CORRECT guess was made"),
        TOP5("TOP5", "TOP FIVE INCORRECT guesses when this letter was played");

        public final String info;
        public final String description;

        Infos(String description, String info) {
            this.description = description;
            this.info = info;
        }
    }

    private void buildDetailsTable(ViewGroup rootView, TableLayout dataContainer, TableLayout acronymContainer, SocraticUtil.Analysis analysis) {
        addHeader(rootView, acronymContainer);
        for (SocraticUtil.SymbolAnalysis sa : analysis.symbolAnalysis) {
            TableRow dataRow = new TableRow(getContext());
            dataRow.setGravity(Gravity.CENTER);

            TextView symbol = new TextView(getContext());
            symbol.setText(sa.symbol);
            setupData(symbol);
            dataRow.addView(symbol);

            TextView numPlays = new TextView(getContext());
            numPlays.setText(sa.numberPlays);
            setupData(numPlays);
            dataRow.addView(numPlays);

            TextView aPBCG = new TextView(getContext());
            aPBCG.setText(String.format(Locale.ENGLISH, "%02f", sa.averagePlaysBeforeCorrectGuess));
            setupData(aPBCG);
            dataRow.addView(aPBCG);

            TextView iBCG = new TextView(getContext());
            aPBCG.setText(String.format(Locale.ENGLISH, "%s", sa.incorrectGuessesBeforeCorrectGuess));
            setupData(iBCG);
            dataRow.addView(iBCG);

            TextView aTC = new TextView(getContext());
            aPBCG.setText(String.format(Locale.ENGLISH, "%02f", sa.averageTimeBeforeCorrectGuessSeconds));
            setupData(aTC);
            dataRow.addView(aTC);

            TextView top5 = new TextView(getContext());
            // TOP5 -- Top five incorrect guesses when this letter was played
            top5.setText(String.valueOf(sa.topFiveIncorrectGuesses));
            setupData(top5);
            dataRow.addView(top5);
            dataContainer.addView(dataRow);
        }
    }

    private void addHeader(ViewGroup rootView, TableLayout headerContainer) {
        TableRow headerRow = new TableRow(getContext());
        headerRow.setGravity(Gravity.CENTER);

        TextView stringNameTitle = new TextView(getContext());
        stringNameTitle.setText("Symbol");
        headerRow.addView(stringNameTitle);

        TextView numPlays = new TextView(getContext());
        setupHeader(rootView, numPlays, Infos.NUM_PLAYS, ToolTip.POSITION_BELOW);
        headerRow.addView(numPlays);

        TextView pBCG = new TextView(getContext());
        setupHeader(rootView, pBCG, Infos.PBCG, ToolTip.POSITION_BELOW);
        headerRow.addView(pBCG);

        TextView iBCG = new TextView(getContext());
        setupHeader(rootView, iBCG, Infos.IBCG, ToolTip.POSITION_LEFT_TO);
        headerRow.addView(iBCG);

        TextView aTC = new TextView(getContext());
        setupHeader(rootView, aTC, Infos.ATCG, ToolTip.POSITION_LEFT_TO);
        headerRow.addView(aTC);

        TextView top5 = new TextView(getContext());
        // TOP5 -- Top five incorrect guesses when this letter was played
        setupHeader(rootView, top5, Infos.TOP5, ToolTip.POSITION_LEFT_TO);
        headerRow.addView(top5);

        headerContainer.addView(headerRow);
    }

    private void setupData(TextView tv) {
        tv.setPadding(0, 8, 24, 8);
    }

    private void setupHeader(ViewGroup rootView, TextView tv, Infos header, int tooltipPosition) {
        tv.setText(header.description);
        tv.setTextColor(getResources().getColor(R.color.design_default_color_primary_variant, getContext().getTheme()));
        tv.setOnClickListener(v -> {
            mToolTipsManager.dismissAll();
            ToolTip.Builder builder = new ToolTip.Builder(getContext(), tv, rootView, header.info, tooltipPosition);
            builder.setBackgroundColor(R.color.disabledTextViewGrey);
            builder.setAlign(ToolTip.ALIGN_LEFT);
            mToolTipsManager.show(builder.build());
        });
    }
}
