package com.uberj.ditsanddahs.simplesocratic;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.uberj.ditsanddahs.R;

class SocraticSettings {
    public final int toneFrequency;
    public final boolean easyMode;
    public final int fadeInOutPercentage;
    public final int durationMinutesRequested;
    public final int wpmRequested;
    public final boolean enableAutoLetterIntroduce;
    public final int missedLetterPointsRemoved;
    public final int correctLetterPointsAdded;
    public final int includeNewLetterCutoffScore;

    SocraticSettings(int toneFrequency, boolean easyMode, int fadeInOutPercentage, int durationMinutesRequested, int wpmRequested, boolean enableAutoLetterIntroduce, int missedLetterPointsRemoved, int correctLetterPointsAdded, int includeNewLetterCutoffScore) {
        this.toneFrequency = toneFrequency;
        this.easyMode = easyMode;
        this.fadeInOutPercentage = fadeInOutPercentage;
        this.durationMinutesRequested = durationMinutesRequested;
        this.wpmRequested = wpmRequested;
        this.enableAutoLetterIntroduce = enableAutoLetterIntroduce;
        this.missedLetterPointsRemoved = missedLetterPointsRemoved;
        this.correctLetterPointsAdded = correctLetterPointsAdded;
        this.includeNewLetterCutoffScore = includeNewLetterCutoffScore;
    }

    public static SocraticSettings fromContext(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return new SocraticSettings(
                preferences.getInt(context.getResources().getString(R.string.setting_socratic_audio_tone), 440),
                preferences.getBoolean(context.getResources().getString(R.string.setting_socratic_easy_mode), true),
                preferences.getInt(context.getResources().getString(R.string.setting_fade_in_out_percentage), 30),
                preferences.getInt(context.getResources().getString(R.string.setting_socratic_duration_minutes_requested), 1),
                preferences.getInt(context.getResources().getString(R.string.setting_socratic_wpm_requested), 25),
                preferences.getBoolean(context.getResources().getString(R.string.setting_socratic_enable_auto_introduce), false),
                preferences.getInt(context.getResources().getString(R.string.setting_socratic_missed_letter_points_removed), 5),
                preferences.getInt(context.getResources().getString(R.string.setting_socratic_correct_letter_points_added), 5),
                preferences.getInt(context.getResources().getString(R.string.setting_socratic_include_new_letter_cutoff_score), 50)
        );
    }
}
