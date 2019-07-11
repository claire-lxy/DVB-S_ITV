package com.konkawise.dtv.annotation;

import android.support.annotation.IntDef;

import com.konkawise.dtv.Constants;

@IntDef(flag = true, value = {
        Constants.USB_TYPE_ATTACH, Constants.USB_TYPE_DETACH
})
public @interface UsbObserveType {
}
