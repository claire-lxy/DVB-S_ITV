package com.konkawise.dtv.annotation;

import android.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.DVBSelectType.INSTALLATION, Constants.DVBSelectType.SEARCH
})
public @interface DVBSelectType {
}
