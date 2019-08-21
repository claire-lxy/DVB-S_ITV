package com.konkawise.dtv;

import com.sw.dvblib.SWEpg;

import vendor.konka.hardware.dtvmanager.V1_0.EpgEvent_t;

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

    public EpgEvent_t getNextEitOfService(int index) {
        return SWEpg.CreateInstance().getNextEitOfService(index);
    }

    /**
     * 通知刷新EpgEvent
     */
    public void sentDataReq(int sat, int tsid, int serviceid) {
        SWEpg.CreateInstance().sentDataReq(sat, tsid, serviceid);
    }

    /**
     * 获取EpgEvent信息
     *
     * @param index index=0为当前频道的EpgEvent，index=1为下一个频道的EpgEvent
     */
    public EpgEvent_t getPfEitOfServID(int sat, int tsid, int serviceid, int index) {
        return SWEpg.CreateInstance().getPfEitOfServID(sat, tsid, serviceid, index);
    }
}
