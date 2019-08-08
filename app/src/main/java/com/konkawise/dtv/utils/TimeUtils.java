package com.konkawise.dtv.utils;

import android.content.Context;
import android.text.TextUtils;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWTimerManager;
import com.konkawise.dtv.bean.DateModel;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;

public class TimeUtils {
    public static final int YEAR = 0;
    public static final int MONTH = 1;
    public static final int DAY = 2;
    public static final int HOUR = 3;
    public static final int MINUTE = 4;

    private static final DecimalFormat sDecimalFormat = new DecimalFormat("00");

    /**
     * 获取当前年份
     */
    public static int getYear() {
        return getYear("");
    }

    public static int getYear(String year) {
        if (TextUtils.isEmpty(year)) {
            SysTime_t sysTime_t = SWTimerManager.getInstance().getLocalTime();
            if (sysTime_t != null) {
                return sysTime_t.Year;
            }
        }
        return Integer.valueOf(year);
    }

    /**
     * 获取当前月份
     */
    public static int getMonth() {
        return getMonth("");
    }

    public static int getMonth(String month) {
        if (TextUtils.isEmpty(month)) {
            SysTime_t sysTime_t = SWTimerManager.getInstance().getLocalTime();
            if (sysTime_t != null) {
                return sysTime_t.Month;
            }
        }
        return Integer.valueOf(month);
    }

    /**
     * 获取当前日期
     */
    public static int getDay() {
        return getDay("");
    }

    public static int getDay(String day) {
        if (TextUtils.isEmpty(day)) {
            SysTime_t sysTime_t = SWTimerManager.getInstance().getLocalTime();
            if (sysTime_t != null) {
                return sysTime_t.Day;
            }
        }
        return Integer.valueOf(day);
    }

    /**
     * 获取当前小时
     */
    public static int getHour() {
        return getHour("");
    }

    public static int getHour(String hour) {
        if (TextUtils.isEmpty(hour)) {
            SysTime_t sysTime_t = SWTimerManager.getInstance().getLocalTime();
            if (sysTime_t != null) {
                return sysTime_t.Hour;
            }
        }
        return Integer.valueOf(hour);
    }

    /**
     * 获取当前小时
     */
    public static int getMinute() {
        return getMinute("");
    }

    public static int getMinute(String minute) {
        if (TextUtils.isEmpty(minute)) {
            SysTime_t sysTime_t = SWTimerManager.getInstance().getLocalTime();
            if (sysTime_t != null) {
                return sysTime_t.Minute;
            }
        }
        return Integer.valueOf(minute);
    }

    /**
     * 根据年月日获取星期几
     */
    public static int getDayOfWeek(int year, int month, int day) {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.YEAR, year);
        instance.set(Calendar.MONTH, month - 1);
        instance.set(Calendar.DAY_OF_MONTH, day);
        return instance.get(Calendar.DAY_OF_WEEK);
    }

    /**
     * 传递星期几获取当月相同星期的具体日期
     *
     * @param targetDayOfWeek 星期，targetDayOfWeek=Calendar.SUNDAY~SATURDAY
     */
    public static List<Integer> getDayOfMonthsByDayOfWeek(int year, int month, int targetDayOfWeek) {
        int maxDayOfMonth = getDayOfMonthByYearAndMonth(year, month);
        List<Integer> dayOfWeeks = new ArrayList<>();
        for (int i = 1; i <= maxDayOfMonth; i++) {
            int dayOfWeek = getDayOfWeek(year, month, i);
            if (dayOfWeek == targetDayOfWeek) {
                dayOfWeeks.add(i);
            }
        }
        return dayOfWeeks;
    }

    public static int getWeekByStr(Context context, String week) {
        if (TextUtils.equals(week, context.getString(R.string.sunday))) {
            return Calendar.SUNDAY;
        } else if (TextUtils.equals(week, context.getString(R.string.monday))) {
            return Calendar.MONDAY;
        } else if (TextUtils.equals(week, context.getString(R.string.tuesday))) {
            return Calendar.TUESDAY;
        } else if (TextUtils.equals(week, context.getString(R.string.wednesday))) {
            return Calendar.WEDNESDAY;
        } else if (TextUtils.equals(week, context.getString(R.string.thursday))) {
            return Calendar.THURSDAY;
        } else if (TextUtils.equals(week, context.getString(R.string.friday))) {
            return Calendar.FRIDAY;
        } else if (TextUtils.equals(week, context.getString(R.string.saturday))) {
            return Calendar.SATURDAY;
        } else {
            return -1;
        }
    }

    /**
     * 获取当年某月的最大天数
     */
    public static int getDayOfMonthByYearAndMonth(int year, int month) {
        Calendar instance = Calendar.getInstance();
        instance.set(Calendar.YEAR, year);
        instance.set(Calendar.MONTH, month - 1);
        return instance.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    public static int getTotalSeconds(SysTime_t startTime, SysTime_t endTime) {
        if (endTime.Hour < startTime.Hour) endTime.Day += 1; // 计算时间不需要理会超过当前月份天数问题，超过一天直接+1计算
        DateModel dateModel = new DateModel(startTime, endTime);
        return dateModel.getBetweenSeconds();
    }

    /**
     * 根据提供的年月日时分和待计算日期之间的秒数，获取具体日期
     */
    public static int[] getEndDate(int year, int month, int day, int hour, int minute, int seconds) {
        long startTime = new Date(year, month, day, hour, minute).getTime();
        long endTime = startTime + seconds * 1000;

        int[] endDateArr = new int[5];
        Date endDate = new Date(endTime);
        endDateArr[YEAR] = endDate.getYear();
        endDateArr[MONTH] = endDate.getMonth();
        endDateArr[DAY] = endDate.getDate();
        endDateArr[HOUR] = endDate.getHours();
        endDateArr[MINUTE] = endDate.getMinutes();
        int dayOfMonth = getDayOfMonthByYearAndMonth(endDateArr[YEAR], endDateArr[MONTH]);
        // 计算的天数超过了当月的总天数，月份+1
        if (endDateArr[DAY] > dayOfMonth) {
            endDateArr[MONTH]++;
            endDateArr[DAY] = 1;
            // 计算的月份超过了总月份，年份+1
            if (endDateArr[MONTH] > 12) {
                endDateArr[YEAR]++;
                endDateArr[MONTH] = 1;
            }
        }
        return endDateArr;
    }

    public static boolean isMonthValid(int month) {
        return month >= 0 && month <= 12;
    }

    public static boolean isHourValid(int hour) {
        return hour >= 0 && hour <= 23;
    }

    public static boolean isMinuteValid(int minute) {
        return minute >= 0 && minute <= 59;
    }

    public static boolean isBookSecondsValid(int startHour, int startMinute, int endHour, int endMinute) {
        int hour = endHour - startHour;
        if (hour == 0) {
            return endMinute - startMinute >= 1; // 至少一分钟
        }
        return hour >= 0;
    }

    /**
     * 根据秒数获取格式化时分秒
     *
     * @return 00:00:00
     */
    public static String getDecimalFormatTime(int seconds) {
        if (seconds <= 0) {
            return "00:00:00";
        }

        String hour = sDecimalFormat.format(seconds / 3600);
        String minute = sDecimalFormat.format(seconds % 3600 / 60);
        String second = sDecimalFormat.format(seconds % 60);
        return hour + ":" + minute + ":" + second;
    }
}
