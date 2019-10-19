package com.konkawise.dtv;

import com.sw.dvblib.DTVManager;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.emitter.MsgEventEmitter;

public class DTVDVBManager {

    private static class DTVDVBManagerHolder {
        private static final DTVDVBManager INSTANCE = new DTVDVBManager();
    }

    private DTVDVBManager() {
        DTVManager.getInstance();
    }

    public static DTVDVBManager getInstance() {
        return DTVDVBManagerHolder.INSTANCE;
    }

    public void registerDTVListener(DTVManager.DTVListener listener) {
        DTVManager.getInstance().registerMsgCallbackListener(listener);
    }

    public void unregisterDTVListener(DTVManager.DTVListener listener) {
        DTVManager.getInstance().unregisterMsgCallbackListener(listener);
    }

    public MsgEvent registerMsgEvent(int callbackId) {
        return DTVManager.getInstance().registerMsgEvent(callbackId);
    }

    public void unregisterMsgEvent(int callbackId) {
        DTVManager.getInstance().unregisterMsgEvent(callbackId);
    }

    public void removeMsgEvent(int callbackId, MsgEvent msgEvent) {
        DTVManager.getInstance().removeMsgEvent(callbackId, msgEvent);
    }

    public int addEmitter(int emitterKey, MsgEventEmitter emitter) {
        return DTVManager.getInstance().addEmitter(emitterKey, emitter);
    }

    public MsgEventEmitter removeEmitter(int emitterKey) {
        return DTVManager.getInstance().removeEmitter(emitterKey);
    }

    public void releaseResource() {
        unregisterMsgEvent(Constants.SCAN_CALLBACK_MSG_ID);
        unregisterMsgEvent(Constants.LOCK_CALLBACK_MSG_ID);
        unregisterMsgEvent(Constants.PVR_CALLBACK_MSG_ID);
        unregisterMsgEvent(Constants.EPG_CALLBACK_MSG_ID);
        RealTimeManager.getInstance().stop();
        DTVManager.getInstance().destroy();
    }
}
