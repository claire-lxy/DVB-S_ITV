package com.konkawise.dtv.bean;

import android.content.Context;

import com.konkawise.dtv.R;
import com.konkawise.dtv.utils.TimeUtils;
import com.sw.dvblib.SWBooking;

import java.text.MessageFormat;

import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;

public class BookingModel {
    public static final String BOOK_TIME_SEPARATOR_EMPTY = "";
    public static final String BOOK_TIME_SEPARATOR_NEWLINE = "\n";
    private static final String[] DAY_OF_WEEK_ARRAY = {
            "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"
    };
    public HSubforProg_t bookInfo;
    public PDPInfo_t progInfo;

    public BookingModel() {

    }

    public BookingModel(HSubforProg_t bookInfo, PDPInfo_t progInfo) {
        this.bookInfo = bookInfo;
        this.progInfo = progInfo;
    }

    public BookingModel cloneBookingModel() {
        BookingModel newBookingModel = new BookingModel();
        newBookingModel.bookInfo = this.bookInfo;
        newBookingModel.progInfo = this.progInfo;
        return newBookingModel;
    }

    public String getBookProgName() {
        if (bookInfo == null) return "";

        return bookInfo.schtype == SWBooking.BookSchType.NONE.ordinal() ? "----" : progInfo.Name;
    }

    public String getBookDate(Context context, String separator) {
        if (bookInfo == null) return "";

        String startYear;
        String endYear;
        String startMonth;
        String endMonth;
        String startDay;
        String endDay;
        String startHour;
        String startMinute;
        String endHour;
        String endMinute;
        String dayOfWeek;
        String dayOfMonth;
        if (bookInfo.schtype == SWBooking.BookSchType.RECORD.ordinal()) {
            int[] endDateArr = TimeUtils.getEndDate(bookInfo.year, bookInfo.month, bookInfo.day, bookInfo.hour, bookInfo.minute, bookInfo.lasttime);
            if (endDateArr == null) return "";

            if (bookInfo.repeatway == SWBooking.BookRepeatWay.ONCE.ordinal()) {
                // 2019-6-27 12:00-2019-6-27 14:00
                startYear = String.valueOf(bookInfo.year);
                startMonth = String.valueOf(bookInfo.month);
                startDay = String.valueOf(bookInfo.day);
                startHour = String.valueOf(bookInfo.hour);
                startMinute = String.valueOf(bookInfo.minute);
                endYear = String.valueOf(endDateArr[TimeUtils.YEAR]);
                endMonth = String.valueOf(endDateArr[TimeUtils.MONTH]);
                endDay = String.valueOf(endDateArr[TimeUtils.DAY]);
                endHour = String.valueOf(endDateArr[TimeUtils.HOUR]);
                endMinute = String.valueOf(endDateArr[TimeUtils.MINUTE]);
                return MessageFormat.format(context.getString(R.string.book_time_once_format),
                        startYear, startMonth, startDay, startHour, startMinute,
                        separator, endYear, endMonth, endDay, endHour, endMinute);
            } else if (bookInfo.repeatway == SWBooking.BookRepeatWay.DAILY.ordinal()) {
                // 12:00-14:00
                startHour = String.valueOf(bookInfo.hour);
                startMinute = String.valueOf(bookInfo.minute);
                endHour = String.valueOf(endDateArr[TimeUtils.HOUR]);
                endMinute = String.valueOf(endDateArr[TimeUtils.MINUTE]);
                return MessageFormat.format(context.getString(R.string.book_time_daily_format), startHour, startMinute, endHour, endMinute);
            } else if (bookInfo.repeatway == SWBooking.BookRepeatWay.WEEKLY.ordinal()) {
                // Mon Thu 12:00-14:00
                dayOfWeek = DAY_OF_WEEK_ARRAY[TimeUtils.getDayOfWeek(bookInfo.year, bookInfo.month, bookInfo.day) - 1];
                startHour = String.valueOf(bookInfo.hour);
                startMinute = String.valueOf(bookInfo.minute);
                endHour = String.valueOf(endDateArr[TimeUtils.HOUR]);
                endMinute = String.valueOf(endDateArr[TimeUtils.MINUTE]);
                return MessageFormat.format(context.getString(R.string.book_time_weekly_or_monthly_format), dayOfWeek, separator, startHour, startMinute, endHour, endMinute);
            } else {
                // 1th 12:00-14:00
                dayOfMonth = MessageFormat.format(context.getString(R.string.book_date_day), String.valueOf(bookInfo.day));
                startHour = String.valueOf(bookInfo.hour);
                startMinute = String.valueOf(bookInfo.minute);
                endHour = String.valueOf(endDateArr[TimeUtils.HOUR]);
                endMinute = String.valueOf(endDateArr[TimeUtils.MINUTE]);
                return MessageFormat.format(context.getString(R.string.book_time_weekly_or_monthly_format), dayOfMonth, separator, startHour, startMinute, endHour, endMinute);
            }
        }

        if (bookInfo.schtype == SWBooking.BookSchType.PLAY.ordinal() || bookInfo.schtype == SWBooking.BookSchType.NONE.ordinal()) {
            if (bookInfo.repeatway == SWBooking.BookRepeatWay.ONCE.ordinal()) {
                // 2019-6-27 12:00
                startYear = String.valueOf(bookInfo.year);
                startMonth = String.valueOf(bookInfo.month);
                startDay = String.valueOf(bookInfo.day);
                startHour = String.valueOf(bookInfo.hour);
                startMinute = String.valueOf(bookInfo.minute);
                return MessageFormat.format(context.getString(R.string.book_time_once_format_non_endtime), startYear, startMonth, startDay, startHour, startMinute);
            } else if (bookInfo.repeatway == SWBooking.BookRepeatWay.DAILY.ordinal()) {
                // 12:00
                startHour = String.valueOf(bookInfo.hour);
                startMinute = String.valueOf(bookInfo.minute);
                return MessageFormat.format(context.getString(R.string.book_time_daily_format_non_endtime), startHour, startMinute);
            } else if (bookInfo.repeatway == SWBooking.BookRepeatWay.WEEKLY.ordinal()) {
                // Mon Thu 12:00
                dayOfWeek = DAY_OF_WEEK_ARRAY[TimeUtils.getDayOfWeek(bookInfo.year, bookInfo.month, bookInfo.day) - 1];
                startHour = String.valueOf(bookInfo.hour);
                startMinute = String.valueOf(bookInfo.minute);
                return MessageFormat.format(context.getString(R.string.book_time_weekly_or_monthly_format_non_endtime), dayOfWeek, separator, startHour, startMinute);
            } else {
                // 1th 12:00
                dayOfMonth = MessageFormat.format(context.getString(R.string.book_date_day), String.valueOf(bookInfo.day));
                startHour = String.valueOf(bookInfo.hour);
                startMinute = String.valueOf(bookInfo.minute);
                return MessageFormat.format(context.getString(R.string.book_time_weekly_or_monthly_format_non_endtime), dayOfMonth, separator, startHour, startMinute);
            }
        }

        return "";
    }

    public String getBookMode(Context context) {
        if (bookInfo == null) return "";

        if (bookInfo.repeatway == SWBooking.BookRepeatWay.ONCE.ordinal()) {
            return context.getString(R.string.book_once);
        } else if (bookInfo.repeatway == SWBooking.BookRepeatWay.DAILY.ordinal()) {
            return context.getString(R.string.book_daily);
        } else if (bookInfo.repeatway == SWBooking.BookRepeatWay.WEEKLY.ordinal()) {
            return context.getString(R.string.book_weekly);
        } else {
            return context.getString(R.string.book_monthly);
        }
    }

    public String getBookType(Context context) {
        if (bookInfo == null) return "";

        if (bookInfo.schtype == SWBooking.BookSchType.RECORD.ordinal()) {
            return context.getString(R.string.book_record);
        } else if (bookInfo.schtype == SWBooking.BookSchType.PLAY.ordinal()) {
            return context.getString(R.string.book_play);
        } else {
            return context.getString(R.string.book_standby);
        }
    }

    public String getBookChannelName() {
        return progInfo != null ? progInfo.Name : "";
    }

    @Override
    public String toString() {
        return "BookingModel{" +
                "bookInfo=" + bookInfo +
                ", progInfo=" + progInfo +
                '}';
    }
}
