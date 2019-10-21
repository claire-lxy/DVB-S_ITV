package com.konkawise.dtv.annotation;

import android.support.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.UsbType.ATTACH, Constants.UsbType.DETACH
})
public @interface UsbObserveType {
}
