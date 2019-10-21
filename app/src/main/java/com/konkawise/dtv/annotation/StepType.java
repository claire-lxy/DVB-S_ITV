package com.konkawise.dtv.annotation;

import android.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.StepType.NONE,
        Constants.StepType.PLUS,
        Constants.StepType.MINUS
})
public @interface StepType {
}
