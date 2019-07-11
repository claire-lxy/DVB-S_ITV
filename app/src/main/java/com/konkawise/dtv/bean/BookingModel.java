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
    private static final String[] DAY_OF_MONTH_ARRAY = {
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

        if (bookInfo.schtype == SWBooking.BookSchType.RECORD.ordinal()) {
            int[] endDateArr = TimeUtils.getEndDate(bookInfo.year, bookInfo.month, bookInfo.day, bookInfo.hour, bookInfo.minute, bookInfo.lasttime);
            if (endDateArr == null) return "";

            if (bookInfo.repeatway == SWBooking.BookRepeatWay.ONCE.ordinal()) {
                // 2019-6-27 12:00-2019-6-27 14:00
                return MessageFormat.format(context.getString(R.string.book_time_once_format),
                        bookInfo.year, bookInfo.month, bookInfo.day, bookInfo.hour, bookInfo.minute,
                        separator, endDateArr[TimeUtils.YEAR], endDateArr[TimeUtils.MONTH], endDateArr[TimeUtils.DAY], endDateArr[TimeUtils.HOUR], endDateArr[TimeUtils.MINUTE]);
            } else if (bookInfo.repeatway == SWBooking.BookRepeatWay.DAILY.ordinal()) {
                // 12:00-14:00
                return MessageFormat.format(context.getString(R.string.book_time_daily_format),
                        bookInfo.hour, bookInfo.minute, endDateArr[TimeUtils.HOUR], endDateArr[TimeUtils.MINUTE]);
            } else if (bookInfo.repeatway == SWBooking.BookRepeatWay.WEEKLY.ordinal()) {
                // Mon Thu 12:00-14:00
                return MessageFormat.format(context.getString(R.string.book_time_weekly_or_monthly_format),
                        DAY_OF_MONTH_ARRAY[TimeUtils.getDayOfWeek(bookInfo.year, bookInfo.month, bookInfo.day) + 1], separator, bookInfo.hour, bookInfo.minute, endDateArr[TimeUtils.HOUR], endDateArr[TimeUtils.MINUTE]);
            } else {
                // 1th 12:00-14:00
                return MessageFormat.format(context.getString(R.string.book_time_weekly_or_monthly_format),
                        MessageFormat.format(context.getString(R.string.book_date_day), String.valueOf(bookInfo.day)),
                        separator, bookInfo.hour, bookInfo.minute, endDateArr[TimeUtils.HOUR], endDateArr[TimeUtils.MINUTE]);
            }
        }

        if (bookInfo.schtype == SWBooking.BookSchType.PLAY.ordinal() || bookInfo.schtype == SWBooking.BookSchType.NONE.ordinal()) {
            if (bookInfo.repeatway == SWBooking.BookRepeatWay.ONCE.ordinal()) {
                // 2019-6-27 12:00
                return MessageFormat.format(context.getString(R.string.book_time_once_format_non_endtime),
                        bookInfo.year, bookInfo.month, bookInfo.day, bookInfo.hour, bookInfo.minute);
            } else if (bookInfo.repeatway == SWBooking.BookRepeatWay.DAILY.ordinal()) {
                // 12:00
                return MessageFormat.format(context.getString(R.string.book_time_daily_format_non_endtime),
                        bookInfo.hour, bookInfo.minute);
            } else if (bookInfo.repeatway == SWBooking.BookRepeatWay.WEEKLY.ordinal()) {
                // Mon Thu 12:00
                return MessageFormat.format(context.getString(R.string.book_time_weekly_or_monthly_format_non_endtime),
                        DAY_OF_MONTH_ARRAY[TimeUtils.getDayOfWeek(bookInfo.year, bookInfo.month, bookInfo.day) + 1], separator, bookInfo.hour, bookInfo.minute);
            } else {
                // 1th 12:00
                return MessageFormat.format(context.getString(R.string.book_time_weekly_or_monthly_format_non_endtime),
                        MessageFormat.format(context.getString(R.string.book_date_day), String.valueOf(bookInfo.day)), separator, bookInfo.hour, bookInfo.minute);
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
