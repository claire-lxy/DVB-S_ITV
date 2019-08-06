package com.konkawise.dtv;

import com.konkawise.dtv.bean.DateModel;
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

    public boolean isProgramPlaying(EpgEvent_t epgEvent_t) {
        if (epgEvent_t == null) return false;

        SysTime_t currTime = getLocalTime();
        SysTime_t startTime = getStartTime(epgEvent_t);
        SysTime_t endTime = getEndTime(epgEvent_t);
        if (currTime.Day == startTime.Day) {
            return new DateModel(startTime, currTime).isBetween(new DateModel(currTime, endTime));
        }
        return false;
    }

    /**
     * 获取当前时间年月日时分秒数据
     */
    public SysTime_t getLocalTime() {
        return SWTimer.CreateInstance().getLocalTime();
    }

    /**
     * 根据EpgEvent获取开始时间
     */
    public SysTime_t getStartTime(EpgEvent_t epgEvent) {
        return mjdToLocal(getUtcTime(epgEvent, 0));
    }

    /**
     * 根据EpgEvent获取结束时间
     */
    public SysTime_t getEndTime(EpgEvent_t epgEvent) {
        return mjdToLocal(getUtcTime(epgEvent, epgEvent.DurSeconds));
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
    public SysTime_t mjdToLocal(UtcTime_t utcTime) {
        return SWTimer.CreateInstance().mjdToLocal(utcTime);
    }
}
