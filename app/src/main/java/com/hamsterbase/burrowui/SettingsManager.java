package com.hamsterbase.burrowui;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class SettingsManager {
    private static final String PREFS_NAME = "Burrow UI";
    private static final String SELECTED_APPS_KEY = "SelectedApps";
    private static final String SHOW_SETTINGS_ICON_KEY = "ShowSettingsIcon";
    private static final String PREF_DATE_FORMAT = "date_format";
    private static final String DEFAULT_DATE_FORMAT = "EEE,MMM d";

    private SharedPreferences sharedPreferences;

    public SettingsManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public String getDateFormat() {
        return sharedPreferences.getString(PREF_DATE_FORMAT, DEFAULT_DATE_FORMAT);
    }

    public Set<String> getSelectedApps() {
        String savedApps = sharedPreferences.getString(SELECTED_APPS_KEY, "");
        if (!savedApps.isEmpty()) {
            return new HashSet<>(Arrays.asList(savedApps.split(",")));
        }
        return new HashSet<>();
    }

    public void setSelectedApps(Set<String> selectedApps) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SELECTED_APPS_KEY, String.join(",", selectedApps));
        editor.apply();
    }

    public boolean isShowSettingsIcon() {
        return sharedPreferences.getBoolean(SHOW_SETTINGS_ICON_KEY, true);
    }

    public void setShowSettingsIcon(boolean show) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SHOW_SETTINGS_ICON_KEY, show);
        editor.apply();
    }
}
