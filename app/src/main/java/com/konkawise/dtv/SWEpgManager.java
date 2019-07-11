package com.konkawise.dtv;

import com.sw.dvblib.SWEpg;

public class SWEpgManager {

    private static class SWEpgManagerHolder {
        private static final SWEpgManager INSTANCE = new SWEpgManager();
    }

    private SWEpgManager() {
        SWEpg.CreateInstance();
    }

    public static SWEpgManager getInstance() {
        return SWEpgManagerHolder.INSTANCE;
    }

    /**
     * 通知刷新EpgEvent
     */
    public void sentDataReq(int sat, int tsid, int serviceid) {
        SWEpg.CreateInstance().sentDataReq(sat, tsid, serviceid);
    }
}
