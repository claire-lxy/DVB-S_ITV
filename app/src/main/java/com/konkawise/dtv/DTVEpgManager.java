package com.konkawise.dtv;

import com.sw.dvblib.DTVEPG;

import java.util.ArrayList;

import vendor.konka.hardware.dtvmanager.V1_0.HEPG_Struct_Event;

public class DTVEpgManager {

    private static class DTVEpgManagerHolder {
        private static final DTVEpgManager INSTANCE = new DTVEpgManager();
    }

    private DTVEpgManager() {
        DTVEPG.getInstance();
    }

    public static DTVEpgManager getInstance() {
        return DTVEpgManagerHolder.INSTANCE;
    }

    public HEPG_Struct_Event getNextEitOfService(int index) {
        return DTVEPG.getInstance().getNextEventOfService(index);
    }

    /**
     * 通知刷新EpgEvent
     */
    public void sentDataReq(int sat, int tsid, int serviceid) {
        DTVEPG.getInstance().sentDataReq(sat, tsid, serviceid);
    }

    /**
     * 获取EpgEvent信息
     *
     * @param index index=0为当前频道的EpgEvent，index=1为下一个频道的EpgEvent
     */
    public HEPG_Struct_Event getPFEventOfServID(int sat, int tsid, int serviceid, int index) {
        return DTVEPG.getInstance().getPFEventOfServID(sat, tsid, serviceid, index);
    }

    /**
     * 根据日期索引获取当天正在播放或未播放的EPG频道
     *
     * @param dayIndex 0~6
     */
    public ArrayList<HEPG_Struct_Event> getCurrProgSchInfo(int dayIndex) {
        return DTVEPG.getInstance().getEpgEventListByDayIndex(dayIndex);
    }
}
