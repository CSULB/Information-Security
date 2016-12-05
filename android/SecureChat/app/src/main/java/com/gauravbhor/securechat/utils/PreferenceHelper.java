package com.gauravbhor.securechat.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;

public class PreferenceHelper {

    private static SharedPreferences preferences;

    public static void init(Context context) {
        preferences = context.getSharedPreferences(null, Context.MODE_PRIVATE);
    }

    public static boolean contains(String key) {
        return preferences.contains(key);
    }

    public static void save(String key, String value) {
        preferences.edit().putString(key, value).apply();
    }

    public static void save(String key, long value) {
        preferences.edit().putLong(key, value).apply();
    }

    public static void save(String key, int value) {
        preferences.edit().putInt(key, value).apply();
    }

    public static void save(String key, boolean value) {
        preferences.edit().putBoolean(key, value).apply();
    }

    public static String getString(String key) {
        return preferences.getString(key, null);
    }

    @NonNull
    public static long getLong(String key) {
        return preferences.getLong(key, 0);
    }

    @NonNull
    public static int getInt(String key) {
        return preferences.getInt(key, 0);
    }

    @NonNull
    public static boolean getBoolean(String key) {
        return preferences.getBoolean(key, false);
    }
}
