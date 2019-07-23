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

    public void regMsgHandler(Looper looper, MsgCB msgCB) {
        SWDVB.GetInstance().regMsgHandler(looper, msgCB);
    }

    public void regMsgHandler(int callbackId, Looper looper, MsgCB msgCB) {
        SWDVB.GetInstance().regMsgHandler(callbackId, looper, msgCB);
    }

    public void unRegMsgHandler(int callbackId, MsgCB msgCB) {
        SWDVB.GetInstance().unregisterMsgHandler(callbackId, msgCB);
    }
}
