package com.konkawise.dtv.annotation;

import android.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.PVRType.TIMESHIFT, Constants.PVRType.RECORD
})
public @interface PVRType {
}
