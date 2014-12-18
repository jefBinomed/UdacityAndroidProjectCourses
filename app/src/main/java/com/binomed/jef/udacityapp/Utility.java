package com.binomed.jef.udacityapp;

import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;

/**
 * Created by jef on 18/12/14.
 */
public class Utility {
    public static String getPreferredTheme(FragmentActivity activity) {
        return PreferenceManager.getDefaultSharedPreferences(activity).getString(activity.getString(R.string.pref_theme_key), activity.getString(R.string.pref_theme_default));
    }
}
