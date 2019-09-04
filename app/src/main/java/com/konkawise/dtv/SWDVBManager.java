package com.konkawise.dtv;

import com.sw.dvblib.SWDVB;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.emitter.MsgEventEmitter;

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

    public void registerDTVListener(SWDVB.DTVListener listener) {
        SWDVB.GetInstance().registerMsgCallbackListener(listener);
    }

    public void unregisterDTVListener(SWDVB.DTVListener listener) {
        SWDVB.GetInstance().unregisterMsgCallbackListener(listener);
    }

    public MsgEvent registerMsgEvent(int callbackId) {
        return SWDVB.GetInstance().registerMsgEvent(callbackId);
    }

    public void unregisterMsgEvent(int callbackId) {
        SWDVB.GetInstance().unregisterMsgEvent(callbackId);
    }

    public void removeMsgEvent(int callbackId, MsgEvent msgEvent) {
        SWDVB.GetInstance().removeMsgEvent(callbackId, msgEvent);
    }

    public int addEmitter(int emitterKey, MsgEventEmitter emitter) {
        return SWDVB.GetInstance().addEmitter(emitterKey, emitter);
    }

    public MsgEventEmitter removeEmitter(int emitterKey) {
        return SWDVB.GetInstance().removeEmitter(emitterKey);
    }

    public void releaseResource() {
        unregisterMsgEvent(Constants.SCAN_CALLBACK_MSG_ID);
        unregisterMsgEvent(Constants.LOCK_CALLBACK_MSG_ID);
        unregisterMsgEvent(Constants.PVR_CALLBACK_MSG_ID);
        unregisterMsgEvent(Constants.EPG_CALLBACK_MSG_ID);
        RealTimeManager.getInstance().stop();
        SWDVB.Destory();
    }
}
