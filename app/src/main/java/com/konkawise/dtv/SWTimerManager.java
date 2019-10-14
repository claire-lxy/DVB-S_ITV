package com.konkawise.dtv;

import com.konkawise.dtv.bean.DateModel;
import com.sw.dvblib.SWTimer;

import vendor.konka.hardware.dtvmanager.V1_0.HCommon_Struct_MDJ;
import vendor.konka.hardware.dtvmanager.V1_0.HEPG_Struct_Event;

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

    public boolean isProgramPlaying(HEPG_Struct_Event epgEvent_t) {
        if (epgEvent_t == null) return false;

        SWTimer.TimeModel currTime = getLocalTime();
        SWTimer.TimeModel startTime = getStartTime(epgEvent_t);
        SWTimer.TimeModel endTime = getEndTime(epgEvent_t);
        if (currTime.Day == startTime.Day) {
            return new DateModel(startTime, currTime).isBetween(new DateModel(currTime, endTime));
        }
        return false;
    }

    /**
     * 获取当前时间年月日时分秒数据
     */
    public SWTimer.TimeModel getLocalTime() {
        return SWTimer.CreateInstance().getLocalTime();
    }

    /**
     * 根据EpgEvent获取开始时间
     */
    public SWTimer.TimeModel getStartTime(HEPG_Struct_Event epgEvent) {
        return mjdToLocal(getUtcTime(epgEvent, 0));
    }

    /**
     * 根据EpgEvent获取结束时间
     */
    public SWTimer.TimeModel getEndTime(HEPG_Struct_Event epgEvent) {
        return mjdToLocal(getUtcTime(epgEvent, epgEvent.DurSeconds));
    }

    private HCommon_Struct_MDJ getUtcTime(HEPG_Struct_Event epgEvent, int seconds) {
        HCommon_Struct_MDJ utcTime = new HCommon_Struct_MDJ();
        utcTime.date = epgEvent.utcStartData;
        utcTime.time = epgEvent.utcStartTime + seconds;
        return utcTime;
    }

    /**
     * UTC转换成年月日时分秒
     */
    public SWTimer.TimeModel mjdToLocal(HCommon_Struct_MDJ utcTime) {
        return mjdToLocal(utcTime, true);
    }

    public SWTimer.TimeModel mjdToLocal(HCommon_Struct_MDJ utcTime, boolean adjustZoneTime) {
        return SWTimer.CreateInstance().mjdToLocal(utcTime, adjustZoneTime);
    }

    public SWTimer.TimeModel getTime(int year, int month, int day, int hour, int minute, int second) {
        SWTimer.TimeModel time = new SWTimer.TimeModel();
        time.Year = year;
        time.Month = month;
        time.Day = day;
        time.Hour = hour;
        time.Minute = minute;
        time.Second = second;
        return time;
    }
}
