package com.konkawise.dtv.sp.strategy;

import android.content.SharedPreferences;

public class LongStrategy implements PreferenceStrategy {

    @Override
    public void put(SharedPreferences prefs, String key, Object value) {
        prefs.edit().putLong(key, (Long) value).apply();
    }

    @Override
    public Object get(SharedPreferences prefs, String key) {
        return prefs.getLong(key, 0);
    }
}
