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

import java.text.DecimalFormat;
import java.util.Locale;
import java.util.function.Function;

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


        TextView detailsExplanation = rootView.findViewById(R.id.details_explaination);
        TableLayout detailsContainer = rootView.findViewById(R.id.details_container);
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
                initDetailsTable(rootView, detailsContainer, detailsExplanation, analysis);
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
        NUM_PLAYS("# Plays", "Number of times this symbol was played", "# of plays"),
        ACCURACY("ACCURACY", "How accurate you were at guessing each character", "Seconds"),
        PBCG("APBCG", "AVERAGE number of times a letter was PLAYED BEFORE a CORRECT GUESS was made", "# of plays"),
        IBCG("IBCG", "Number of INCORRECT guesses were made BEFORE a CORRECT GUESS was entered", "# of guesses"),
        ATCG("ATCG", "AVERAGE TIME before CORRECT guess was entered", "Seconds"),
        TOP5("TOP5", "TOP FIVE INCORRECT guesses when this letter was played", "Guesses");

        public final String info;
        public final String description;
        public final String units;

        Infos(String description, String info, String units) {
            this.description = description;
            this.info = info;
            this.units = units;
        }
    }

    private void initDetailsTable(ViewGroup rootView, TableLayout dataContainer, TextView acronymContainer, SocraticUtil.Analysis analysis) {
        DecimalFormat format = new DecimalFormat("#.##");
        View overallAccuracyBackground = rootView.findViewById(R.id.overall_accuracy_background);
        View overallAccuracyBackgroundShow = rootView.findViewById(R.id.show_accuracy_detail);
        View overallAPBCGBackground = rootView.findViewById(R.id.overall_apbcg_background);
        View overallAPBCGBackgroundShow = rootView.findViewById(R.id.show_apbcg_detail);
        View overallATBCGBackground = rootView.findViewById(R.id.overall_atbcg_background);
        View overallATBCGBackgroundShow = rootView.findViewById(R.id.show_atbcg_detail);
        overallAccuracyBackground.setOnClickListener(v -> {
            acronymContainer.setText(Infos.ACCURACY.info);

            overallAccuracyBackground.setBackground(getResources().getDrawable(R.drawable.rounded_corners_for_numbers_detail_view, getContext().getTheme()));
            overallAccuracyBackgroundShow.setVisibility(View.INVISIBLE);

            overallAPBCGBackground.setBackgroundColor(getResources().getColor(R.color.defaultBackground, getContext().getTheme()));
            overallAPBCGBackgroundShow.setVisibility(View.VISIBLE);

            overallATBCGBackground.setBackgroundColor(getResources().getColor(R.color.defaultBackground, getContext().getTheme()));
            overallATBCGBackgroundShow.setVisibility(View.VISIBLE);

            setupDataView(
                    Infos.ACCURACY,
                    dataContainer,
                    analysis,
                    (SocraticUtil.SymbolAnalysis sa) -> format.format(sa.accuracy));
        });
        overallAPBCGBackground.setOnClickListener(v -> {
            acronymContainer.setText(Infos.PBCG.info);
            overallAccuracyBackground.setBackgroundColor(getResources().getColor(R.color.defaultBackground, getContext().getTheme()));
            overallAccuracyBackgroundShow.setVisibility(View.VISIBLE);

            overallAPBCGBackground.setBackground(getResources().getDrawable(R.drawable.rounded_corners_for_numbers_detail_view, getContext().getTheme()));
            overallAPBCGBackgroundShow.setVisibility(View.INVISIBLE);

            overallATBCGBackground.setBackgroundColor(getResources().getColor(R.color.defaultBackground, getContext().getTheme()));
            overallATBCGBackgroundShow.setVisibility(View.VISIBLE);

            setupDataView(
                    Infos.PBCG,
                    dataContainer,
                    analysis,
                    (SocraticUtil.SymbolAnalysis sa) -> format.format(sa.averagePlaysBeforeCorrectGuess));
        });
        overallATBCGBackground.setOnClickListener(v -> {
            acronymContainer.setText(Infos.ATCG.info);
            overallAccuracyBackground.setBackgroundColor(getResources().getColor(R.color.defaultBackground, getContext().getTheme()));
            overallAccuracyBackgroundShow.setVisibility(View.VISIBLE);

            overallAPBCGBackground.setBackgroundColor(getResources().getColor(R.color.defaultBackground, getContext().getTheme()));
            overallAPBCGBackgroundShow.setVisibility(View.VISIBLE);

            overallATBCGBackground.setBackground(getResources().getDrawable(R.drawable.rounded_corners_for_numbers_detail_view, getContext().getTheme()));
            overallATBCGBackgroundShow.setVisibility(View.INVISIBLE);

            setupDataView(
                    Infos.ATCG,
                    dataContainer,
                    analysis,
                    (SocraticUtil.SymbolAnalysis sa) -> format.format(sa.averageTimeBeforeCorrectGuessSeconds));
        });


        // Init
        overallAccuracyBackground.callOnClick();
    }

    private void setupDataView(Infos info, TableLayout dataContainer, SocraticUtil.Analysis analysis, Function<SocraticUtil.SymbolAnalysis, String> dataf) {
        dataContainer.removeAllViews();
        TableRow headerRow = new TableRow(getContext());

        TextView symbol = new TextView(getContext());
        symbol.setText("Symbol");
        headerRow.addView(symbol);

        TextView units = new TextView(getContext());
        symbol.setText(info.units);

        headerRow.addView(units);

        for (SocraticUtil.SymbolAnalysis sa : analysis.symbolAnalysis) {
            TableRow dataRow = new TableRow(getContext());
            dataRow.setGravity(Gravity.CENTER);

            TextView symbolName = new TextView(getContext());
            symbolName.setText(sa.symbol);
            setupData(symbolName);
            dataRow.addView(symbolName);

            TextView accuracy = new TextView(getContext());
            accuracy.setText(dataf.apply(sa));
            setupData(accuracy);
            dataRow.addView(accuracy);
            dataContainer.addView(dataRow);
        }
    }

    private void setupData(TextView tv) {
        tv.setPadding(0, 8, 48, 8);
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
