package com.konkawise.dtv.sp.strategy;

import android.content.SharedPreferences;

import java.util.Set;

public class StringSetStrategy implements PreferenceStrategy {

    @SuppressWarnings("unchecked")
    @Override
    public void put(SharedPreferences prefs, String key, Object value) {
        prefs.edit().putStringSet(key, (Set<String>) value).apply();
    }

    @Override
    public Object get(SharedPreferences prefs, String key) {
        return prefs.getStringSet(key, null);
    }
}
