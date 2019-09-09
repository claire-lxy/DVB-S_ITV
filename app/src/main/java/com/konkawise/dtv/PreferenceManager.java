package com.konkawise.dtv;

import com.konkawise.dtv.sp.PreferenceHelper;
import com.konkawise.dtv.sp.PreferenceHelperImpl;
import com.konkawise.dtv.sp.PreferenceStrategy;
import com.konkawise.dtv.sp.StrategyKeyConstant;

import java.util.Set;

public class PreferenceManager {
    private static final String PREFERENCE_NAME = "kkdvb";
    private PreferenceHelper mPreferenceHelper;

    private static class PreferenceManagerHolder {
        private static final PreferenceManager INSTANCE = new PreferenceManager();
    }

    private PreferenceManager() {
        mPreferenceHelper = PreferenceHelperImpl.newInstance(KonkaApplication.getContext(), PREFERENCE_NAME);
    }

    public static PreferenceManager getInstance() {
        return PreferenceManagerHolder.INSTANCE;
    }

    public void putBoolean(String key, boolean value) {
        put(StrategyKeyConstant.KEY_PREFIX_BOOLEAN, key, value);
    }

    public void putFloat(String key, float value) {
        put(StrategyKeyConstant.KEY_PREFIX_FLOAT, key, value);
    }

    public void putInt(String key, int value) {
        put(StrategyKeyConstant.KEY_PREFIX_INT, key, value);
    }

    public void putLong(String key, long value) {
        put(StrategyKeyConstant.KEY_PREFIX_LONG, key, value);
    }

    public void putString(String key, String value) {
        put(StrategyKeyConstant.KEY_PREFIX_STRING, key, value);
    }

    public void putStringSet(String key, Set<String> value) {
        put(StrategyKeyConstant.KEY_PREFIX_STRING_SET, key, value);
    }

    public boolean getBoolean(String key) {
        return (boolean) get(StrategyKeyConstant.KEY_PREFIX_BOOLEAN, key);
    }

    public float getFloat(String key) {
        return (float) get(StrategyKeyConstant.KEY_PREFIX_FLOAT, key);
    }

    public int getInt(String key) {
        return (int) get(StrategyKeyConstant.KEY_PREFIX_INT, key);
    }

    public long getLong(String key) {
        return (long) get(StrategyKeyConstant.KEY_PREFIX_LONG, key);
    }

    public String getString(String key) {
        return (String) get(StrategyKeyConstant.KEY_PREFIX_STRING, key);
    }

    @SuppressWarnings("unchecked")
    public Set<String> getStringSet(String key) {
        return (Set<String>) get(StrategyKeyConstant.KEY_PREFIX_STRING_SET, key);
    }

    private void put(@PreferenceStrategy String strategy, String key, Object value) {
        mPreferenceHelper.put(strategy, key, value);
    }

    private Object get(@PreferenceStrategy String strategy, String key) {
        return mPreferenceHelper.get(strategy, key);
    }

    public void clear() {
        putInt(Constants.PrefsKey.SAVE_CHANNEL, 0);
    }
}
