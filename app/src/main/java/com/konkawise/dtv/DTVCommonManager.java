package com.konkawise.dtv;

import com.konkawise.dtv.bean.DateModel;
import com.sw.dvblib.DTVCommon;

import vendor.konka.hardware.dtvmanager.V1_0.HCommon_Struct_MDJ;
import vendor.konka.hardware.dtvmanager.V1_0.HEPG_Struct_Event;

public class DTVCommonManager {

    private static class DTVCommonManagerHolder {
        private static DTVCommonManager INSTANCE = new DTVCommonManager();
    }

    private DTVCommonManager() {
        DTVCommon.getInstance();
    }

    public static DTVCommonManager getInstance() {
        return DTVCommonManagerHolder.INSTANCE;
    }

    /**
     * 恢复出厂设置
     */
    public void factoryReset() {
        DTVCommon.getInstance().factoryReset(0);
    }

    public boolean isProgramPlaying(HEPG_Struct_Event epgEvent_t) {
        if (epgEvent_t == null) return false;

        DTVCommon.TimeModel currTime = getLocalTime();
        DTVCommon.TimeModel startTime = getStartTime(epgEvent_t);
        DTVCommon.TimeModel endTime = getEndTime(epgEvent_t);
        if (currTime.Day == startTime.Day) {
            return new DateModel(startTime, currTime).isBetween(new DateModel(currTime, endTime));
        }
        return false;
    }

    /**
     * 获取当前时间年月日时分秒数据
     */
    public DTVCommon.TimeModel getLocalTime() {
        return DTVCommon.getInstance().getLocalTime();
    }

    /**
     * 根据EpgEvent获取开始时间
     */
    public DTVCommon.TimeModel getStartTime(HEPG_Struct_Event epgEvent) {
        return mjdToLocal(getUtcTime(epgEvent, 0));
    }

    /**
     * 根据EpgEvent获取结束时间
     */
    public DTVCommon.TimeModel getEndTime(HEPG_Struct_Event epgEvent) {
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
    public DTVCommon.TimeModel mjdToLocal(HCommon_Struct_MDJ utcTime) {
        return mjdToLocal(utcTime, true);
    }

    public DTVCommon.TimeModel mjdToLocal(HCommon_Struct_MDJ utcTime, boolean adjustZoneTime) {
        return DTVCommon.getInstance().mjdToLocal(utcTime, adjustZoneTime);
    }

    public DTVCommon.TimeModel getTime(int year, int month, int day, int hour, int minute, int second) {
        DTVCommon.TimeModel time = new DTVCommon.TimeModel();
        time.Year = year;
        time.Month = month;
        time.Day = day;
        time.Hour = hour;
        time.Minute = minute;
        time.Second = second;
        return time;
    }
}
