package com.konkawise.dtv.annotation;

import android.support.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.LocaleType.SYSTEM,
        Constants.LocaleType.ITALIAN,
        Constants.LocaleType.ENGLISH,
        Constants.LocaleType.CHINESE
})
public @interface LocaleType {
}
