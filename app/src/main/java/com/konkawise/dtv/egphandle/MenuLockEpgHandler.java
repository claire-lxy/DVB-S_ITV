package com.konkawise.dtv.egphandle;

import android.support.annotation.NonNull;
import android.util.Log;

import com.konkawise.dtv.SWFtaManager;

import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

/**
 * Epg MenuLock处理类
 */
public class MenuLockEpgHandler extends EpgHandler {
    private final boolean mMenuLockOpened;

    public MenuLockEpgHandler(EpgHandler epgHandler) {
        super(epgHandler);
        mMenuLockOpened = SWFtaManager.getInstance().isOpenMenuLock();
    }

    @Override
    public void epgHandle(@NonNull PDPMInfo_t pdpmInfo_t, EpgHandleCallback callback) {
        Log.i("EpgFragment", "isOpenMenuLock==" + mMenuLockOpened);
        if (mMenuLockOpened) {
            if (callback != null) {
                callback.handleResult(new EpgHandleResult(EpgHandleResult.MENU_LOCK_EPG, true));
            }
        } else {
            if (mNextEpgHandler != null) {
                mNextEpgHandler.epgHandle(pdpmInfo_t, callback);
            }
        }
    }
}
