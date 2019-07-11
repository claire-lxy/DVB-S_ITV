package com.konkawise.dtv.sp.strategy;

import android.content.SharedPreferences;

public class IntStrategy implements PreferenceStrategy {

    @Override
    public void put(SharedPreferences prefs, String key, Object value) {
        prefs.edit().putInt(key, (Integer) value).apply();
    }

    @Override
    public Object get(SharedPreferences prefs, String key) {
        return prefs.getInt(key, 0);
    }
}
