package com.uberj.ditsanddahs.simplesocratic;

import com.annimon.stream.function.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.uberj.ditsanddahs.AudioManager;
import com.uberj.ditsanddahs.GlobalSettings;
import com.uberj.ditsanddahs.ProgressGradient;
import com.uberj.ditsanddahs.R;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticSessionType;
import com.uberj.ditsanddahs.simplesocratic.storage.SocraticTrainingSessionWithEvents;

import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.content.Context;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.text.DecimalFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class SocraticNumbersScreenFragment extends Fragment {
    private static final DecimalFormat DECIMAL_STAT_FORMATTER = new DecimalFormat("#.##");
    private static final String SYMBOL_COLUMN_NAME = "Symbol";
    private static final String BLANK_DETAIL = "-";
    private SocraticTrainingMainScreenViewModel sessionViewModel;
    private SocraticSessionType sessionType;
    private NestedScrollView detailsContainerScroll;

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

        ScrollView rootView = (ScrollView) inflater.inflate(R.layout.socratic_training_numbers_screen_fragment, container, false);

        TextView detailsTitle = rootView.findViewById(R.id.details_title);
        TextView detailsExplanation = rootView.findViewById(R.id.details_explaination);
        TableLayout detailsContainer = rootView.findViewById(R.id.details_container);
        detailsContainerScroll = rootView.findViewById(R.id.details_container_scroll);
        sessionViewModel = ViewModelProviders.of(this).get(SocraticTrainingMainScreenViewModel.class);
        sessionViewModel.getLatestSession(sessionType).observe(this, (mostRecentSession) -> {
            double wpmAverage = -1;
            double accuracy = -1;
            long prevDurationMillis = -1;
            double overallAPBCG = -1;
            double overallATBCG = -1;
            double overallIGBCG = -1;
            if (!mostRecentSession.isEmpty()) {
                SocraticTrainingSessionWithEvents s = mostRecentSession.get(0);
                AudioManager.MorseConfig config = buildMorseConfigFromPreviousSession(s);
                SocraticUtil.Analysis analysis = SocraticUtil.analyseSession(s, config);
                wpmAverage = analysis.wpmAverage;
                accuracy = analysis.overAllAccuracy;
                prevDurationMillis = s.session.durationWorkedMillis;
                overallAPBCG = analysis.overallAverageNumberPlaysBeforeCorrectGuess;
                overallATBCG = analysis.overallAverageSecondsBeforeCorrectGuessSeconds;
                overallIGBCG = analysis.averageNumberOfIncorrectGuessesBeforeCorrectGuess;
                detailsContainer.removeAllViews();
                initDetailsTable(rootView, detailsContainer, detailsTitle, detailsExplanation, analysis);
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
            ((TextView) rootView.findViewById(R.id.prev_session_overall_atbcg)).setText(
                    overallATBCG >= 0 ? DECIMAL_STAT_FORMATTER.format(overallATBCG) + " (s)" : "N/A"
            );
            ((TextView) rootView.findViewById(R.id.prev_session_overall_apbcg)).setText(
                    overallAPBCG >= 0 ? DECIMAL_STAT_FORMATTER.format(overallAPBCG) : "N/A"
            );

            ((TextView) rootView.findViewById(R.id.prev_session_overall_igbcg)).setText(
                    overallAPBCG >= 0 ? DECIMAL_STAT_FORMATTER.format(overallIGBCG) : "N/A"
            );

            // Init
            View overallAccuracyBackground = rootView.findViewById(R.id.overall_accuracy_background);
            overallAccuracyBackground.callOnClick();
        });


        return rootView;
    }

    private AudioManager.MorseConfig buildMorseConfigFromPreviousSession(SocraticTrainingSessionWithEvents s) {
        // TODO, actually save enough data on the session to calculate an accurate number here.
        // Going to fake it for now by using current settings rather than the actual settings used by the user
        // during the session
        Context context = getContext();
        SocraticSettings settings = SocraticSettings.fromContext(context);
        AudioManager.MorseConfig.Builder builder = AudioManager.MorseConfig.builder();
        builder.setGlobalSettings(GlobalSettings.fromContext(context));
        builder.setToneFrequencyHz(440); // This doesn't really matter downstream
        builder.setEffectiveWpm(settings.wpmRequested);
        builder.setLetterWpm(settings.wpmRequested);
        return builder.build();
    }

    private static class Column {
        public final String columnName;
        public final Function<SocraticUtil.SymbolAnalysis, CharSequence> dataExtractor;

        public Column(String columnName, Function<SocraticUtil.SymbolAnalysis, CharSequence> dataExtractor) {
            this.columnName = columnName;
            this.dataExtractor = dataExtractor;
        }
    }

    private enum Infos {
        ACCURACY("Guess Accuracy Breakdown", "Percent of time the first guess was correct"),
        APBCG("Replay Count Breakdown", "AVERAGE number of times a letter was PLAYED BEFORE a CORRECT GUESS was made"),
        IGBCG("Wrong Guess Breakdown", "Average # of INCORRECT GUESSES made BEFORE a CORRECT GUESS"),
        ATBCG("Guess Time Breakdown", "AVERAGE TIME, in seconds, BEFORE CORRECT guess was entered"),
        TOP5("Mistake Breakdown", "TOP FIVE INCORRECT guesses when this letter was played");

        public final String info;
        public final String title;

        Infos(String title, String info) {
            this.title = title;
            this.info = info;
        }
    }

    private synchronized void initDetailsTable(ViewGroup rootView, TableLayout dataContainer, TextView detailsTitle, TextView acronymContainer, SocraticUtil.Analysis analysis) {
        View overallAccuracyBackground = rootView.findViewById(R.id.overall_accuracy_background);
        View overallAccuracyBackgroundShow = rootView.findViewById(R.id.show_accuracy_detail);
        View overallAPBCGBackground = rootView.findViewById(R.id.overall_apbcg_background);
        View overallAPBCGBackgroundShow = rootView.findViewById(R.id.show_apbcg_detail);
        View overallATBCGBackground = rootView.findViewById(R.id.overall_atbcg_background);
        View overallATBCGBackgroundShow = rootView.findViewById(R.id.show_atbcg_detail);

        View overallIGBCGBackground = rootView.findViewById(R.id.overall_igbcg_background);
        View overallIGBCGBackgroundShow = rootView.findViewById(R.id.show_igbcg_detail);

        View top5Background = rootView.findViewById(R.id.top_five_mistaken_background);
        View overallTop5BackgroundShow = rootView.findViewById(R.id.show_top5_detail);

        overallAccuracyBackground.setOnClickListener(v -> {
            acronymContainer.setText(Infos.ACCURACY.info);
            detailsTitle.setText(Infos.ACCURACY.title);

            overallAccuracyBackground.setBackground(getResources().getDrawable(R.drawable.rounded_corners_for_numbers_detail_view, getContext().getTheme()));
            overallAccuracyBackgroundShow.setVisibility(View.INVISIBLE);
            overallAPBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallAPBCGBackgroundShow.setVisibility(View.VISIBLE);

            overallATBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallATBCGBackgroundShow.setVisibility(View.VISIBLE);

            overallIGBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallIGBCGBackgroundShow.setVisibility(View.VISIBLE);

            top5Background.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallTop5BackgroundShow.setVisibility(View.VISIBLE);

            detailsContainerScroll.fullScroll(ScrollView.FOCUS_UP);
            setupDataView(
                    Lists.newArrayList(
                            new Column(SYMBOL_COLUMN_NAME, (sa) -> sa.symbol),
                            new Column("Accu. %", (sa) -> sa.accuracy == null ? BLANK_DETAIL : colorized((int) (sa.accuracy * 100))),
                            new Column("Hits/Chances", (sa) -> sa.chances == 0 ? BLANK_DETAIL : String.format(Locale.ENGLISH, "%d/%d", sa.hits, sa.chances))
                    ),
                    dataContainer,
                    analysis,
                    (a1, a2) -> {
                        double v1 = a1.accuracy == null || a1.chances == 0 ? Double.MAX_VALUE : a1.accuracy;
                        double v2 = a2.accuracy == null || a2.chances == 0 ? Double.MAX_VALUE : a2.accuracy;
                        return (int) (v1 * 1000 - v2 * 1000);
                    }
            );
        });
        overallAPBCGBackground.setOnClickListener(v -> {
            acronymContainer.setText(Infos.APBCG.info);
            detailsTitle.setText(Infos.APBCG.title);
            overallAccuracyBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallAccuracyBackgroundShow.setVisibility(View.VISIBLE);

            overallAPBCGBackground.setBackground(getResources().getDrawable(R.drawable.rounded_corners_for_numbers_detail_view, getContext().getTheme()));
            overallAPBCGBackgroundShow.setVisibility(View.INVISIBLE);

            overallATBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallATBCGBackgroundShow.setVisibility(View.VISIBLE);

            overallIGBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallIGBCGBackgroundShow.setVisibility(View.VISIBLE);

            top5Background.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallTop5BackgroundShow.setVisibility(View.VISIBLE);

            detailsContainerScroll.fullScroll(ScrollView.FOCUS_UP);
            setupDataView(
                    Lists.newArrayList(
                            new Column(SYMBOL_COLUMN_NAME, sa -> sa.symbol),
                            new Column("# of plays", sa -> sa.averagePlaysBeforeCorrectGuess == null ? BLANK_DETAIL : DECIMAL_STAT_FORMATTER.format(sa.averagePlaysBeforeCorrectGuess))
                    ),
                    dataContainer,
                    analysis,
                    (a1, a2) -> (int) (orZero(a2.averagePlaysBeforeCorrectGuess) * 1000 - orZero(a1.averagePlaysBeforeCorrectGuess) * 1000)
            );
        });
        overallATBCGBackground.setOnClickListener(v -> {
            acronymContainer.setText(Infos.ATBCG.info);
            detailsTitle.setText(Infos.ATBCG.title);
            overallAccuracyBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallAccuracyBackgroundShow.setVisibility(View.VISIBLE);

            overallAPBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallAPBCGBackgroundShow.setVisibility(View.VISIBLE);

            overallATBCGBackground.setBackground(getResources().getDrawable(R.drawable.rounded_corners_for_numbers_detail_view, getContext().getTheme()));
            overallATBCGBackgroundShow.setVisibility(View.INVISIBLE);

            overallIGBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallIGBCGBackgroundShow.setVisibility(View.VISIBLE);

            top5Background.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallTop5BackgroundShow.setVisibility(View.VISIBLE);

            detailsContainerScroll.fullScroll(ScrollView.FOCUS_UP);
            setupDataView(
                    Lists.newArrayList(
                            new Column(SYMBOL_COLUMN_NAME, sa -> sa.symbol),
                            new Column("Seconds", sa -> sa.averageSecondsBeforeCorrectGuessSeconds == null ? BLANK_DETAIL : DECIMAL_STAT_FORMATTER.format(sa.averageSecondsBeforeCorrectGuessSeconds))
                    ),
                    dataContainer,
                    analysis,
                    (a1, a2) -> (int) (orZero(a2.averageSecondsBeforeCorrectGuessSeconds) * 1000 - orZero(a1.averageSecondsBeforeCorrectGuessSeconds) * 1000)
            );
        });

        overallIGBCGBackground.setOnClickListener(v -> {
            acronymContainer.setText(Infos.IGBCG.info);
            detailsTitle.setText(Infos.IGBCG.title);
            overallAccuracyBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallAccuracyBackgroundShow.setVisibility(View.VISIBLE);

            overallAPBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallAPBCGBackgroundShow.setVisibility(View.VISIBLE);

            overallATBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallATBCGBackgroundShow.setVisibility(View.VISIBLE);

            overallIGBCGBackground.setBackground(getResources().getDrawable(R.drawable.rounded_corners_for_numbers_detail_view, getContext().getTheme()));
            overallIGBCGBackgroundShow.setVisibility(View.INVISIBLE);

            top5Background.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallTop5BackgroundShow.setVisibility(View.VISIBLE);

            detailsContainerScroll.fullScroll(ScrollView.FOCUS_UP);
            setupDataView(
                    Lists.newArrayList(
                            new Column(SYMBOL_COLUMN_NAME, sa -> sa.symbol),
                            new Column("avg. wrong guesses", sa -> sa.incorrectGuessesBeforeCorrectGuess == null ? BLANK_DETAIL : DECIMAL_STAT_FORMATTER.format(sa.incorrectGuessesBeforeCorrectGuess))
                    ),
                    dataContainer,
                    analysis,
                    (a1, a2) -> orZero(a2.incorrectGuessesBeforeCorrectGuess) - orZero(a1.incorrectGuessesBeforeCorrectGuess)
            );
        });

        top5Background.setOnClickListener(v -> {
            acronymContainer.setText(Infos.TOP5.info);
            detailsTitle.setText(Infos.TOP5.title);
            overallAccuracyBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallAccuracyBackgroundShow.setVisibility(View.VISIBLE);

            overallAPBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallAPBCGBackgroundShow.setVisibility(View.VISIBLE);

            overallATBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallATBCGBackgroundShow.setVisibility(View.VISIBLE);

            overallIGBCGBackground.setBackgroundColor(ContextCompat.getColor(getContext(), R.color.defaultBackground));
            overallIGBCGBackgroundShow.setVisibility(View.VISIBLE);

            top5Background.setBackground(getResources().getDrawable(R.drawable.rounded_corners_for_numbers_detail_view, getContext().getTheme()));
            overallTop5BackgroundShow.setVisibility(View.INVISIBLE);
            Joiner on = Joiner.on(", ");

            detailsContainerScroll.fullScroll(ScrollView.FOCUS_UP);
            setupDataView(
                    Lists.newArrayList(
                            new Column(SYMBOL_COLUMN_NAME, sa -> sa.symbol),
                            new Column("Top 5", sa -> sa.topFiveIncorrectGuesses.isEmpty() ? BLANK_DETAIL : on.join(sa.topFiveIncorrectGuesses))
                    ),
                    dataContainer,
                    analysis,
                    (a1, a2) -> a2.topFiveIncorrectGuesses.size() - a1.topFiveIncorrectGuesses.size()
            );
        });

    }

    private SpannableStringBuilder underline(String s) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(s);
        ssb.setSpan(new UnderlineSpan(), 0, s.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    private int orZero(Double aDouble) {
        if (aDouble == null) {
            return 0;
        }

        return aDouble.intValue();
    }

    private synchronized void setupDataView(List<Column> columns, TableLayout dataContainer, SocraticUtil.Analysis analysis, Comparator<? super SocraticUtil.SymbolAnalysis> comparitor) {
        // Synchronized because this thing does some sorting with un-threadsafe list
        dataContainer.removeAllViews();
        TableRow headerRow = new TableRow(getContext());
        headerRow.setGravity(Gravity.START);

        for (Column column : columns) {
            TextView symbol = new TextView(getContext());
            symbol.setText(underline(column.columnName));
            setPadding(symbol);
            headerRow.addView(symbol);
        }

        dataContainer.addView(headerRow);

        Collections.sort(analysis.symbolAnalysis, comparitor);
        for (SocraticUtil.SymbolAnalysis sa : analysis.symbolAnalysis) {
            TableRow dataRow = new TableRow(getContext());
            dataRow.setGravity(Gravity.START);
            for (Column column : columns) {
                TextView columnText = new TextView(getContext());
                CharSequence columnData = column.dataExtractor.apply(sa);
                columnText.setText(columnData);
                setPadding(columnText);
                dataRow.addView(columnText);
            }
            dataContainer.addView(dataRow);
        }
    }

    private CharSequence colorized(int displayValue) {
        SpannableStringBuilder ssb = new SpannableStringBuilder();
        ssb.append(String.valueOf(displayValue))
                .append("%");
        ForegroundColorSpan errorSpanColor = new ForegroundColorSpan(ProgressGradient.forWeight(Math.min(100, displayValue)));
        ssb.setSpan(errorSpanColor, 0, ssb.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return ssb;
    }

    private void setPadding(TextView tv) {
        tv.setPadding(0, 0, 20, 2);
    }
}
