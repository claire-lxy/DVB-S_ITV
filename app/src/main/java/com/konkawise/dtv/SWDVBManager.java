package com.konkawise.dtv;

import android.os.Looper;

import com.sw.dvblib.MsgCB;
import com.sw.dvblib.SWDVB;

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
}
