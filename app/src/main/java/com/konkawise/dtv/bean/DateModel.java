package com.konkawise.dtv.bean;

import android.annotation.SuppressLint;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;

public class DateModel {
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final SysTime_t start;
    private final SysTime_t end;

    public DateModel(SysTime_t start, SysTime_t end) {
        this.start = start;
        this.end = end;
    }

    public boolean isBetween(DateModel dateModel) {
        try {
            return sDateFormat.parse(formatTime(start)).before(sDateFormat.parse(formatTime(end)))
                    && sDateFormat.parse(formatTime(dateModel.getStart())).before(sDateFormat.parse(formatTime(dateModel.getEnd())));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String formatTime(SysTime_t sysTime) {
        return getYear(sysTime) + getMonth(sysTime) + getDay(sysTime)
                + getHour(sysTime) + getMinute(sysTime) + getSecond(sysTime);
    }

    public String getFormatHourAndMinute() {
        return getHour(start).trim() + getMinute(start) + "-" + getHour(end).trim() + getMinute(end);
    }

    public int getBetweenSeconds() {
        long seconds = getDate(end).getTime() - getDate(start).getTime();
        if (seconds < 0) {
            return 0;
        }
        return (int) (seconds / 1000);
    }

    private Date getDate(SysTime_t sysTime) {
        return new Date(sysTime.Year, sysTime.Month, sysTime.Day, sysTime.Hour, sysTime.Minute, sysTime.Second);
    }

    public String getYear(SysTime_t sysTime) {
        return String.valueOf(sysTime.Year);
    }

    public String getMonth(SysTime_t sysTime) {
        return sysTime.Month < 10 ? "/0" + sysTime.Month : "/" + sysTime.Month;
    }

    public String getDay(SysTime_t sysTime) {
        return sysTime.Day < 10 ? "/0" + sysTime.Day : "/" + sysTime.Day;
    }

    public String getHour(SysTime_t sysTime) {
        return sysTime.Hour < 10 ? " 0" + sysTime.Hour : " " + sysTime.Hour;
    }

    public String getMinute(SysTime_t sysTime) {
        return sysTime.Minute < 10 ? ":0" + sysTime.Minute : ":" + sysTime.Minute;
    }

    public String getSecond(SysTime_t sysTime) {
        return sysTime.Second < 10 ? ":0" + sysTime.Second : ":" + sysTime.Second;
    }

    public SysTime_t getStart() {
        return start;
    }

    public SysTime_t getEnd() {
        return end;
    }
}
