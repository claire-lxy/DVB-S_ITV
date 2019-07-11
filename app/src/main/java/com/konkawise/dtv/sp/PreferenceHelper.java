package com.konkawise.dtv.sp;

public interface PreferenceHelper {

    void put(String strategy, String key, Object value);

    Object get(String strategy, String key);
}
