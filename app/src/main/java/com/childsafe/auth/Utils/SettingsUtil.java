package com.childsafe.auth.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.childsafe.auth.R;

public class SettingsUtil {
    public static final String SETTINGS_PREF = "NIGHT_MODE_PREF";
    public static final String NIGHT_MODE = "NIGHT_MODE";
    public static final String LOCALE = "LOCALE";
    public static final String NOTIFICATION = "NOTIFICATION";

    public static void setDarkMode(Context context, boolean darkmode) {
        SharedPreferences mPrefs = context.getSharedPreferences(SETTINGS_PREF, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(NIGHT_MODE, darkmode);
        editor.apply();
    }

    public static boolean getDarkMode(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences(SETTINGS_PREF, context.MODE_PRIVATE);
        return mPrefs.getBoolean(NIGHT_MODE, false);
    }

    public static void setLocale(Context context, String language) {
        SharedPreferences mPrefs = context.getSharedPreferences(SETTINGS_PREF, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putString(LOCALE, language);
        editor.apply();
    }

    public static String getLocale(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences(SETTINGS_PREF, context.MODE_PRIVATE);
        return mPrefs.getString(LOCALE, "en");
    }

    public static void setNotification(Context context, boolean notification) {
        SharedPreferences mPrefs = context.getSharedPreferences(SETTINGS_PREF, context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mPrefs.edit();
        editor.putBoolean(NOTIFICATION, notification);
        Log.i("notification", ((Boolean)notification).toString());
        editor.apply();
    }

    public static boolean getNotification(Context context) {
        SharedPreferences mPrefs = context.getSharedPreferences(SETTINGS_PREF, context.MODE_PRIVATE);
        return mPrefs.getBoolean(NOTIFICATION, true);
    }

}
