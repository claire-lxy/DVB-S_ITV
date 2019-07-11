package com.konkawise.dtv.sp;

import android.content.Context;
import android.content.SharedPreferences;

import com.konkawise.dtv.sp.strategy.PreferenceStrategy;
import com.konkawise.dtv.sp.strategy.StrategyFactory;

public class PreferenceHelperImpl implements PreferenceHelper{
    private static volatile PreferenceHelper sInstance;

    private final SharedPreferences mPrefs;

    public static PreferenceHelper newInstance(Context context, String name) {
        if (sInstance == null) {
            synchronized (PreferenceHelperImpl.class) {
                if (sInstance == null) {
                    sInstance = new PreferenceHelperImpl(context, name);
                }
            }
        }
        return sInstance;
    }

    private PreferenceHelperImpl(Context context, String name) {
        mPrefs = context.getSharedPreferences(name, Context.MODE_PRIVATE);
    }

    @Override
    public void put(String strategy, String key, Object value) {
        PreferenceStrategy preferenceStrategy = StrategyFactory.getStrategy(strategy);
        preferenceStrategy.put(mPrefs, key, value);
    }

    @Override
    public Object get(String strategy, String key) {
        PreferenceStrategy preferenceStrategy = StrategyFactory.getStrategy(strategy);
        return preferenceStrategy.get(mPrefs, key);
    }
}
