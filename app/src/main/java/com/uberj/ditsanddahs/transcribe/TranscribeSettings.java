package com.uberj.ditsanddahs.transcribe;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.uberj.ditsanddahs.R;

public class TranscribeSettings {
    public final boolean targetIssueStrings;
    public final int audioToneFrequency;
    public final int startDelaySeconds;
    public final int endDelaySeconds;
    public final int durationMinutesRequested;
    public final int letterWpmRequested;
    public final int effectiveWpmRequested;
    public final int secondAudioToneFrequency;
    public final int secondsBetweenStationTransmissions;

    private TranscribeSettings(boolean targetIssueStrings, int audioToneFrequency, int startDelaySeconds, int endDelaySeconds, int durationMinutesRequested, int letterWpmRequested, int effectiveWpmRequested, int secondAudioToneFrequency, int secondsBetweenStationTransmissions) {
        this.targetIssueStrings = targetIssueStrings;
        this.audioToneFrequency = audioToneFrequency;
        this.startDelaySeconds = startDelaySeconds;
        this.endDelaySeconds = endDelaySeconds;
        this.durationMinutesRequested = durationMinutesRequested;
        this.letterWpmRequested = letterWpmRequested;
        this.effectiveWpmRequested = effectiveWpmRequested;
        this.secondAudioToneFrequency = secondAudioToneFrequency;
        this.secondsBetweenStationTransmissions = secondsBetweenStationTransmissions;
    }

    public static TranscribeSettings fromContext(Context context) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        int endDelaySeconds = preferences.getInt(context.getResources().getString(R.string.setting_transcribe_end_delay_seconds), 3);
        if (endDelaySeconds == Integer.valueOf(context.getResources().getString(R.string.setting_transcribe_end_delay_seconds_max_value))) {
            endDelaySeconds = -1;
        }

        return new TranscribeSettings(
            preferences.getBoolean(context.getResources().getString(R.string.setting_transcribe_target_issue_letters), false),
            preferences.getInt(context.getResources().getString(R.string.setting_transcribe_audio_tone), 440),
            preferences.getInt(context.getResources().getString(R.string.setting_transcribe_start_delay_seconds), 2),
            endDelaySeconds,
            preferences.getInt(context.getResources().getString(R.string.setting_transcribe_duration_minutes), 2),
            preferences.getInt(context.getResources().getString(R.string.setting_transcribe_letter_wpm), 25),
            preferences.getInt(context.getResources().getString(R.string.setting_transcribe_effective_wpm), 5),
            preferences.getInt(context.getResources().getString(R.string.second_station_setting_transcribe_audio_tone), 470),
            preferences.getInt(context.getResources().getString(R.string.qso_simulator_seconds_between_station_transmissions), 5)
        );
    }
}
