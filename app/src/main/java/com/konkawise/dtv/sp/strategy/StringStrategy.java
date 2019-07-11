package com.konkawise.dtv.sp.strategy;

import android.content.SharedPreferences;

public class StringStrategy implements PreferenceStrategy {

    @Override
    public void put(SharedPreferences prefs, String key, Object value) {
        prefs.edit().putString(key, (String) value).apply();
    }

    @Override
    public Object get(SharedPreferences prefs, String key) {
        return prefs.getString(key, "");
    }
}
