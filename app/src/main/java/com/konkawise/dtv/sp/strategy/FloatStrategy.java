package com.konkawise.dtv.sp.strategy;

import android.content.SharedPreferences;

public class FloatStrategy implements PreferenceStrategy {

    @Override
    public void put(SharedPreferences prefs, String key, Object value) {
        prefs.edit().putFloat(key, (Float) value).apply();
    }

    @Override
    public Object get(SharedPreferences prefs, String key) {
        return prefs.getFloat(key, 0);
    }
}
