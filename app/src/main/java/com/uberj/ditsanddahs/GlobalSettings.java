package com.uberj.ditsanddahs;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import com.google.common.collect.Sets;

import java.util.Set;

public class GlobalSettings {
    private final int fadeInOutPercentage;
    private final boolean collapseProSigns;
    private final int symbolsBetweenLetters;
    private final int symbolsBetweenWords;
    private final Set<String> enabledProsigns;

    public GlobalSettings(int fadeInOutPercentage, boolean collapseProSigns, int symbolsBetweenLetters, int symbolsBetweenWords, Set<String> enabledProsigns) {
        this.fadeInOutPercentage = fadeInOutPercentage;
        this.collapseProSigns = collapseProSigns;
        this.symbolsBetweenLetters = symbolsBetweenLetters;
        this.symbolsBetweenWords = symbolsBetweenWords;
        this.enabledProsigns = enabledProsigns;
    }

    public static GlobalSettings fromContext(Context applicationContext) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(applicationContext);
        return new GlobalSettings(
                preferences.getInt(applicationContext.getResources().getString(R.string.setting_fade_in_out_percentage), 30),
                preferences.getBoolean(applicationContext.getResources().getString(R.string.global_setting_collapse_prosigns), false),
                preferences.getInt(applicationContext.getResources().getString(R.string.global_setting_symbols_between_letters), 3),
                preferences.getInt(applicationContext.getResources().getString(R.string.global_setting_symbols_between_words), 7),
                preferences.getStringSet(applicationContext.getResources().getString(R.string.global_setting_enabled_prosigns), Sets.newConcurrentHashSet())
        );
    }

    public int getFadeInOutPercentage() {
        return fadeInOutPercentage;
    }

    public boolean shouldCollapseProSigns() {
        return collapseProSigns;
    }

    public int getSymbolsBetweenLetters() {
        return symbolsBetweenLetters;
    }

    public int getSymbolsBetweenWords() {
        return symbolsBetweenWords;
    }

    public Set<String> getEnabledProsigns() {
        return enabledProsigns;
    }
}
