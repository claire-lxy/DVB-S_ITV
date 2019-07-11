package com.konkawise.dtv.annotation;

import android.support.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.LOCALE_TYPE_SYSTEM,
        Constants.LOCALE_TYPE_ITALIAN,
        Constants.LOCALE_TYPE_ENGLISH,
        Constants.LOCALE_TYPE_CHINESE
})
public @interface LocaleType {
}
