package com.konkawise.dtv;

import com.sw.dvblib.SWTimer;

import vendor.konka.hardware.dtvmanager.V1_0.EpgEvent_t;
import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;
import vendor.konka.hardware.dtvmanager.V1_0.UtcTime_t;

public class SWTimerManager {

    private static class SWTimerManagerHolder {
        private static final SWTimerManager INSTANCE = new SWTimerManager();
    }

    private SWTimerManager() {
        SWTimer.CreateInstance();
    }

    public static SWTimerManager getInstance() {
        return SWTimerManagerHolder.INSTANCE;
    }

    /**
     * 获取当前时间年月日时分秒数据
     */
    public SysTime_t getSysTime() {
        return SWTimer.CreateInstance().getSysTime();
    }

    /**
     * 根据EpgEvent获取开始时间
     */
    public SysTime_t getStartTime(EpgEvent_t epgEvent) {
        return SWTimer.CreateInstance().transUtctimeToSystime(getUtcTime(epgEvent, 0));
    }

    /**
     * 根据EpgEvent获取结束时间
     */
    public SysTime_t getEndTime(EpgEvent_t epgEvent) {
        return SWTimer.CreateInstance().transUtctimeToSystime(getUtcTime(epgEvent, epgEvent.DurSeconds));
    }

    private UtcTime_t getUtcTime(EpgEvent_t epgEvent, int seconds) {
        UtcTime_t utcTime = new UtcTime_t();
        utcTime.utcdate = epgEvent.utcStartData;
        utcTime.utctime = epgEvent.utcStartTime + seconds;
        return utcTime;
    }

    /**
     * UTC转换成年月日时分秒
     */
    public SysTime_t transUtctimeToSystime(UtcTime_t utcTime) {
        return SWTimer.CreateInstance().transUtctimeToSystime(utcTime);
    }
}
