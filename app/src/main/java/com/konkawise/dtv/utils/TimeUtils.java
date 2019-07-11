package com.konkawise.dtv.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.TextUtils;

import com.konkawise.dtv.R;
import com.konkawise.dtv.bean.DateModel;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;

public class TimeUtils {
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat sFormat = new SimpleDateFormat("yyyy-MM-dd");
    public static final int YEAR = 0;
    public static final int MONTH = 1;
    public static final int DAY = 2;
    public static final int HOUR = 3;
    public static final int MINUTE = 4;

    public static int getYear() {
        return getYear("");
    }

    public static int getYear(String year) {
        if (TextUtils.isEmpty(year)) {
            return new Date().getYear() + 1900;
        }
        return Integer.valueOf(year);
    }

    public static int getMonth() {
        return getMonth("");
    }

    public static int getMonth(String month) {
        if (TextUtils.isEmpty(month)) {
            return new Date().getMonth() + 1;
        }
        return Integer.valueOf(month);
    }

    public static int getDay() {
        return getDay("");
    }

    public static int getDay(String day) {
        if (TextUtils.isEmpty(day)) {
            return new Date().getDate();
        }
        return Integer.valueOf(day);
    }

    public static int getHour() {
        return getHour("");
    }

    public static int getHour(String hour) {
        if (TextUtils.isEmpty(hour)) {
            return new Date().getHours();
        }
        return Integer.valueOf(hour);
    }

    public static int getMinute() {
        return getMinute("");
    }

    public static int getMinute(String minute) {
        if (TextUtils.isEmpty(minute)) {
            return new Date().getMinutes();
        }
        return Integer.valueOf(minute);
    }

    public static int getDayOfWeek(int year, int month, int day) {
        return getDayOfWeek(year + "-" + month + "-" + day);
    }

    public static int getDayOfWeek(String date) {
        try {
            Calendar.getInstance().setTime(sFormat.parse(date));
            return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
        } catch (ParseException e) {
            e.printStackTrace();
            return -1;
        }
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
     * 获取当年某月的天数
     */
    public static int getDayOfMonthByYearAndMonth(int year, int month) {
        Calendar.getInstance().set(Calendar.YEAR, year);
        Calendar.getInstance().set(Calendar.MONTH, month - 1);
        return Calendar.getInstance().getActualMaximum(Calendar.DAY_OF_MONTH);
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
            return endMinute - startMinute >= 5;
        }
        return hour >= 0;
    }
}
