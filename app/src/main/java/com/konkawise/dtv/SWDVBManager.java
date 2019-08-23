package com.konkawise.dtv;

import android.os.Looper;

import com.sw.dvblib.SWDVB;
import com.sw.dvblib.msg.cb.MsgCB;

public class SWDVBManager {

    private static class SWDVBManagerHolder {
        private static final SWDVBManager INSTANCE = new SWDVBManager();
    }

    private SWDVBManager() {
        SWDVB.GetInstance();
    }

    public static SWDVBManager getInstance() {
        return SWDVBManagerHolder.INSTANCE;
    }

    public void regMsgHandler(int callbackId, Looper looper, MsgCB msgCB) {
        SWDVB.GetInstance().regMsgHandler(callbackId, looper, msgCB);
    }

    public void unRegMsgHandler(int callbackId, MsgCB msgCB) {
        SWDVB.GetInstance().unregisterMsgHandler(callbackId, msgCB);
    }

    public void registerDTVListener(SWDVB.DTVListener listener) {
        SWDVB.GetInstance().registerMsgCallbackListener(listener);
    }

    public void unregisterDTVListener(SWDVB.DTVListener listener) {
        SWDVB.GetInstance().unregisterMsgCallbackListener(listener);
    }

    public void releaseResource() {
        SWDVB.Destory();
    }
}
