package com.konkawise.dtv.annotation;

import android.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.TP_TYPE_ADD, Constants.TP_TYPE_EDIT
})
public @interface TpType {
}
