package com.konkawise.dtv.sp.strategy;

import com.konkawise.dtv.sp.StrategyKeyConstant;

import java.util.HashMap;
import java.util.Map;

public class StrategyFactory {
    private static final Map<String, PreferenceStrategy> sStrategyMap = new HashMap<>();

    static {
        sStrategyMap.put(StrategyKeyConstant.KEY_PREFIX_BOOLEAN, new BooleanStrategy());
        sStrategyMap.put(StrategyKeyConstant.KEY_PREFIX_FLOAT, new FloatStrategy());
        sStrategyMap.put(StrategyKeyConstant.KEY_PREFIX_INT, new IntStrategy());
        sStrategyMap.put(StrategyKeyConstant.KEY_PREFIX_LONG, new LongStrategy());
        sStrategyMap.put(StrategyKeyConstant.KEY_PREFIX_STRING, new StringStrategy());
        sStrategyMap.put(StrategyKeyConstant.KEY_PREFIX_STRING_SET, new StringSetStrategy());
    }

    public static PreferenceStrategy getStrategy(String strategy) {
        PreferenceStrategy preferenceStrategy = sStrategyMap.get(strategy);
        if (preferenceStrategy == null) {
            throw new IllegalArgumentException("preference strategy not found");
        }
        return preferenceStrategy;
    }
}
