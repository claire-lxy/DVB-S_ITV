package com.konkawise.dtv.sp;

import android.support.annotation.StringDef;

@StringDef(value = {
        StrategyKeyConstant.KEY_PREFIX_BOOLEAN,
        StrategyKeyConstant.KEY_PREFIX_FLOAT,
        StrategyKeyConstant.KEY_PREFIX_INT,
        StrategyKeyConstant.KEY_PREFIX_LONG,
        StrategyKeyConstant.KEY_PREFIX_STRING,
        StrategyKeyConstant.KEY_PREFIX_STRING_SET
})
public @interface PreferenceStrategy {
}
