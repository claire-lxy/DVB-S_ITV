package com.konkawise.dtv.sp.strategy;

import android.content.SharedPreferences;

public class BooleanStrategy implements PreferenceStrategy {

    @Override
    public void put(SharedPreferences prefs, String key, Object value) {
        prefs.edit().putBoolean(key, (Boolean) value).apply();
    }

    @Override
    public Object get(SharedPreferences prefs, String key) {
        return prefs.getBoolean(key, false);
    }
}
