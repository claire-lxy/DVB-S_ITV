package com.konkawise.dtv.sp.strategy;

import android.content.SharedPreferences;

public interface PreferenceStrategy {

    void put(SharedPreferences prefs, String key, Object value);

    Object get(SharedPreferences prefs, String key);
}
