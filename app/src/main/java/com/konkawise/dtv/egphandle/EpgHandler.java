package com.konkawise.dtv.egphandle;

import android.support.annotation.NonNull;

import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;


/**
 * 职责链模式实现对Epg节目特殊处理，有其他对Epg的特殊处理都可以新建EpgHandler
 */
public abstract class EpgHandler {
    protected EpgHandler mNextEpgHandler;

    public EpgHandler(EpgHandler epgHandler) {
        this.mNextEpgHandler = epgHandler;
    }

    public abstract void epgHandle(@NonNull PDPMInfo_t pdpmInfo_t, EpgHandleCallback callback);
}
