package com.konkawise.dtv.annotation;

import android.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.STEP_TYPE_NONE_STEP,
        Constants.STEP_TYPE_PLUS_STEP,
        Constants.STEP_TYPE_MINUS_STEP
})
public @interface StepType {
}
