package com.konkawise.dtv.egphandle;

import android.support.annotation.IntDef;

public class EpgHandleResult {
    public static final int PAY_EPG = 1 << 1;
    public static final int MENU_LOCK_EPG = 1 << 2;
    public static final int PARENT_LOCK_EPG = 1 << 3;

    @IntDef(flag = true, value = {
            PAY_EPG, MENU_LOCK_EPG, PARENT_LOCK_EPG
    })
    private @interface EpgHandleType {
    }

    @EpgHandleType
    private int epgHandleType;

    // true 不需要处理 false callback回调处理
    private boolean epgHandled;

    public EpgHandleResult(int epgHandleType, boolean epgHandled) {
        this.epgHandleType = epgHandleType;
        this.epgHandled = epgHandled;
    }

    public int getEpgHandleType() {
        return epgHandleType;
    }

    public void setEpgHandleType(int epgHandleType) {
        this.epgHandleType = epgHandleType;
    }

    public boolean isEpgHandled() {
        return epgHandled;
    }

    public void setEpgHandled(boolean epgHandled) {
        this.epgHandled = epgHandled;
    }


}
