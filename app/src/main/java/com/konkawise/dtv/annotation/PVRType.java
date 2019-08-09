package com.konkawise.dtv.annotation;

import android.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.PVR_TYPE_TIMESHIFT, Constants.PVR_TYPE_RECORD
})
public @interface PVRType {
}
