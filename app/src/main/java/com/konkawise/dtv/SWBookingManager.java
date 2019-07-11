package com.konkawise.dtv;

import android.support.annotation.NonNull;

import com.konkawise.dtv.annotation.BookConflictType;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.bean.DateModel;
import com.konkawise.dtv.bean.EpgBookParameterModel;
import com.konkawise.dtv.utils.TimeUtils;
import com.sw.dvblib.SWBooking;

import java.util.ArrayList;
import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.HForplayprog_t;
import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;

public class SWBookingManager {

    private static class SWBookingManagerHolder {
        private static SWBookingManager INSTANCE = new SWBookingManager();
    }

    private SWBookingManager() {
        SWBooking.CreateInstance();
    }

    public static SWBookingManager getInstance() {
        return SWBookingManagerHolder.INSTANCE;
    }

    public int getProgNum() {
        return SWBooking.CreateInstance().getProgNum();
    }

    public HSubforProg_t getProgInfo(int index) {
        return SWBooking.CreateInstance().getProgInfo(index);
    }

    public void addProg(@BookConflictType int bookConflictType, HSubforProg_t deleteBookProg, HSubforProg_t bookProg) {
        if (bookConflictType == Constants.BOOK_CONFLICT_ADD && deleteBookProg != null) {
            deleteProg(deleteBookProg);
        }
        addProg(bookProg);
    }

    public void addProg(HSubforProg_t prog) {
        SWBooking.CreateInstance().addProg(prog);
    }

    public void replaceProg(HSubforProg_t oldProg, HSubforProg_t newProg) {
        SWBooking.CreateInstance().replaceProg(oldProg, newProg);
    }

    public void deleteProg(HSubforProg_t prog) {
        SWBooking.CreateInstance().deleteProg(prog);
    }

    public HSubforProg_t progIsSubFored(int sat, int tsid, int servid, int eventid) {
        return SWBooking.CreateInstance().progIsSubFored(sat, tsid, servid, eventid);
    }

    public int updateDBase(int speed) {
        return SWBooking.CreateInstance().updateDBase(speed);
    }

    public HSubforProg_t conflictCheck(HSubforProg_t prog, int telltype) {
        return SWBooking.CreateInstance().conflictCheck(prog, telltype);
    }

    public int cancelSubForPlay(int keyType, HForplayprog_t prog) {
        return SWBooking.CreateInstance().cancelSubForPlay(keyType, prog);
    }

    public HSubforProg_t getReadyProgInfo() {
        return SWBooking.CreateInstance().getReadyProgInfo();
    }

    public HForplayprog_t getCurrSubForPlay() {
        return SWBooking.CreateInstance().getCurrSubForPlay();
    }

    public HForplayprog_t getCancelBookProg(HSubforProg_t bookProg) {
        HForplayprog_t cancelBookProg = new HForplayprog_t();
        cancelBookProg.used = bookProg.used;
        cancelBookProg.type = bookProg.type;
        cancelBookProg.schtype = bookProg.schtype;
        cancelBookProg.sat = bookProg.sat;
        cancelBookProg.tsid = bookProg.tsid;
        cancelBookProg.servid = bookProg.servid;
        cancelBookProg.eventid = bookProg.eventid;
        cancelBookProg.refservid = bookProg.refservid;
        cancelBookProg.refeventid = bookProg.refeventid;
        cancelBookProg.year = bookProg.year;
        cancelBookProg.month = bookProg.month;
        cancelBookProg.day = bookProg.day;
        cancelBookProg.hour = bookProg.hour;
        cancelBookProg.minute = bookProg.minute;
        cancelBookProg.second = bookProg.second;
        cancelBookProg.weekday = bookProg.weekday;
        cancelBookProg.lasttime = bookProg.lasttime;
        cancelBookProg.markiddate = bookProg.markiddate;
        cancelBookProg.markidtime = bookProg.markidtime;
        return cancelBookProg;
    }

    public HSubforProg_t newBookProg(@NonNull EpgBookParameterModel parameterModel) {
        HSubforProg_t newBookProg = new HSubforProg_t();
        newBookProg.used = 0;
        newBookProg.type = parameterModel.type;
        newBookProg.schtype = parameterModel.schtype;
        newBookProg.schway = parameterModel.schway;
        newBookProg.repeatway = SWBooking.BookRepeatWay.ONCE.ordinal();
        newBookProg.sat = parameterModel.progInfo != null ? parameterModel.progInfo.Sat : 0;
        newBookProg.tsid = parameterModel.progInfo != null ? parameterModel.progInfo.TsID : 0;
        newBookProg.servid = parameterModel.progInfo != null ? parameterModel.progInfo.ServID : 0;
        newBookProg.eventid = parameterModel.eventInfo != null ? parameterModel.eventInfo.uiEventId : 0;
        newBookProg.year = parameterModel.startTimeInfo != null ? parameterModel.startTimeInfo.Year : TimeUtils.getYear();
        newBookProg.month = parameterModel.startTimeInfo != null ? parameterModel.startTimeInfo.Month : TimeUtils.getMonth();
        newBookProg.day = parameterModel.startTimeInfo != null ? parameterModel.startTimeInfo.Day : TimeUtils.getDay();
        newBookProg.hour = parameterModel.startTimeInfo != null ? parameterModel.startTimeInfo.Hour : TimeUtils.getHour();
        newBookProg.minute = parameterModel.startTimeInfo != null ? parameterModel.startTimeInfo.Minute : TimeUtils.getMinute();
        newBookProg.second = parameterModel.startTimeInfo != null ? parameterModel.startTimeInfo.Second : 0;
        if (parameterModel.eventInfo != null) {
            SysTime_t startTime = SWTimerManager.getInstance().getStartTime(parameterModel.eventInfo);
            SysTime_t endTime = SWTimerManager.getInstance().getEndTime(parameterModel.eventInfo);
            newBookProg.lasttime = new DateModel(startTime, endTime).getBetweenSeconds();
        }
        return newBookProg;
    }

    @BookConflictType
    public int getConflictType(HSubforProg_t conflictBookProg) {
        if (conflictBookProg == null) return -1;

        if (conflictBookProg.used == SWBooking.BookUse.NONE.ordinal()) {
            return SWBookingManager.getInstance().getProgNum() >= SWBooking.MAX_BOOKING_NUM ? Constants.BOOK_CONFLICT_LIMIT : Constants.BOOK_CONFLICT_NONE;
        } else if (conflictBookProg.used == SWBooking.BookUse.EXIT.ordinal()) {
            return Constants.BOOK_CONFLICT_ADD;
        } else if (conflictBookProg.used == SWBooking.BookUse.CONFILCT.ordinal()) {
            return Constants.BOOK_CONFLICT_REPLACE;
        }

        return -1;
    }

    public List<BookingModel> getBookingModelList() {
        List<HSubforProg_t> bookingList = getBookingList();
        List<BookingModel> bookingModels = new ArrayList<>();
        if (bookingList != null && !bookingList.isEmpty()) {
            for (HSubforProg_t bookInfo : bookingList) {
                PDPInfo_t progInfo = SWPDBaseManager.getInstance().getProgInfoByServiceId(bookInfo.servid, bookInfo.tsid, bookInfo.sat);
                if (progInfo != null) {
                    BookingModel bookingModel = new BookingModel(bookInfo, progInfo);
                    bookingModels.add(bookingModel);
                }
            }
        }
        return bookingModels;
    }

    public List<HSubforProg_t> getBookingList() {
        List<HSubforProg_t> bookList = new ArrayList<>();
        int num = getProgNum();
        for (int i = 0; i < num; i++) {
            HSubforProg_t bookProg = getProgInfo(i);
            if (bookProg != null) {
                bookList.add(bookProg);
            }
        }
        return bookList;
    }
}
