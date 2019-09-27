package com.konkawise.dtv.annotation;

import android.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.DVB_SELECT_TYPE_INSTALLATION, Constants.DVB_SELECT_TYPE_SEARCH
})
public @interface DVBSelectType {
}
