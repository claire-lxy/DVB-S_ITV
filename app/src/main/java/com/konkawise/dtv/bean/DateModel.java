package com.konkawise.dtv.bean;

import android.annotation.SuppressLint;


import com.sw.dvblib.SWTimer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateModel {
    @SuppressLint("SimpleDateFormat")
    private static SimpleDateFormat sDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final SWTimer.TimeModel start;
    private final SWTimer.TimeModel end;

    public DateModel(SWTimer.TimeModel start, SWTimer.TimeModel end) {
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

    public String formatTime(SWTimer.TimeModel sysTime) {
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

    private Date getDate(SWTimer.TimeModel sysTime) {
        return new Date(sysTime.Year, sysTime.Month, sysTime.Day, sysTime.Hour, sysTime.Minute, sysTime.Second);
    }

    public String getYear(SWTimer.TimeModel sysTime) {
        return String.valueOf(sysTime.Year);
    }

    public String getMonth(SWTimer.TimeModel sysTime) {
        return sysTime.Month < 10 ? "/0" + sysTime.Month : "/" + sysTime.Month;
    }

    public String getDay(SWTimer.TimeModel sysTime) {
        return sysTime.Day < 10 ? "/0" + sysTime.Day : "/" + sysTime.Day;
    }

    public String getHour(SWTimer.TimeModel sysTime) {
        return sysTime.Hour < 10 ? " 0" + sysTime.Hour : " " + sysTime.Hour;
    }

    public String getMinute(SWTimer.TimeModel sysTime) {
        return sysTime.Minute < 10 ? ":0" + sysTime.Minute : ":" + sysTime.Minute;
    }

    public String getSecond(SWTimer.TimeModel sysTime) {
        return sysTime.Second < 10 ? ":0" + sysTime.Second : ":" + sysTime.Second;
    }

    public SWTimer.TimeModel getStart() {
        return start;
    }

    public SWTimer.TimeModel getEnd() {
        return end;
    }
}
