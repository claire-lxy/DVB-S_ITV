package com.konkawise.dtv.annotation;

import android.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.TpType.ADD, Constants.TpType.EDIT
})
public @interface TpType {
}
