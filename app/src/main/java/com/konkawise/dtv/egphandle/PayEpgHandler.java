package com.konkawise.dtv.egphandle;

import android.support.annotation.NonNull;

import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;


/**
 * Epg节目付费处理类
 */
public class PayEpgHandler extends EpgHandler {
    // 节目需要付费
    private static final int PAID = 1;

    public PayEpgHandler(EpgHandler epgHandler) {
        super(epgHandler);
    }

    @Override
    public void epgHandle(@NonNull PDPMInfo_t pdpmInfo_t, EpgHandleCallback callback) {
        if (pdpmInfo_t.CasFlag == PAID) {
            if (callback != null) {
                // 节目需要付费，正常回调走业务流程
                callback.handleResult(new EpgHandleResult(EpgHandleResult.PAY_EPG, true));
            }
        } else {
            if (mNextEpgHandler != null) {
                mNextEpgHandler.epgHandle(pdpmInfo_t, callback);
            }
        }
    }
}
