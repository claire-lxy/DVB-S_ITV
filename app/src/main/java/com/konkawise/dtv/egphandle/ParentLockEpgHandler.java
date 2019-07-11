package com.konkawise.dtv.egphandle;

import android.support.annotation.NonNull;
import android.util.Log;

import com.konkawise.dtv.SWFtaManager;

import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

/**
 * Epg频道加锁处理类
 */
public class ParentLockEpgHandler extends EpgHandler {
    // 已加锁
    private static final int LOCK = 1;

    private final boolean mParentLockOpened;

    public ParentLockEpgHandler(EpgHandler epgHandler) {
        super(epgHandler);
        mParentLockOpened = SWFtaManager.getInstance().isOpenParentLock();
    }

    @Override
    public void epgHandle(@NonNull PDPMInfo_t pdpmInfo_t, EpgHandleCallback callback) {
        Log.i("EpgFragment", "isOpenParentLock==" + mParentLockOpened);
        if (mParentLockOpened) {
            if (pdpmInfo_t.LockFlag == LOCK && callback != null) {
                callback.handleResult(new EpgHandleResult(EpgHandleResult.PARENT_LOCK_EPG, false));
            } else {
                if (callback != null) {
                    callback.handleResult(new EpgHandleResult(EpgHandleResult.PARENT_LOCK_EPG, true));
                }
            }
        } else {
            if (callback != null) {
                callback.handleResult(new EpgHandleResult(EpgHandleResult.PARENT_LOCK_EPG, true));
            }
        }
    }
}
