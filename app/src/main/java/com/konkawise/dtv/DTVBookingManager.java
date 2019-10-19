package com.konkawise.dtv;

import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.konkawise.dtv.annotation.BookConflictType;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.bean.DateModel;
import com.konkawise.dtv.bean.EpgBookParameterModel;
import com.konkawise.dtv.utils.TimeUtils;
import com.sw.dvblib.DTVBooking;
import com.sw.dvblib.DTVCommon;

import java.util.ArrayList;
import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Enum_Repeat;
import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Enum_Status;
import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Struct_PlayeTimer;
import vendor.konka.hardware.dtvmanager.V1_0. HBooking_Struct_Timer;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgBasicInfo;

public class DTVBookingManager {
    public static final String DEFAULT_BOOK_CONTENT = "book";
    private boolean mRecording;

    private static class DTVBookingManagerHolder {
        private static DTVBookingManager INSTANCE = new DTVBookingManager();
    }

    private DTVBookingManager() {
        DTVBooking.getInstance();
    }

    public static DTVBookingManager getInstance() {
        return DTVBookingManagerHolder.INSTANCE;
    }

    /**
     * 获取有效定时器数量
     */
    public int getTimerNum() {
        return DTVBooking.getInstance().getTimerNum();
    }

    /**
     * 根据索引号获取定时器
     */
    public  HBooking_Struct_Timer getTimerInfo(int index) {
        return DTVBooking.getInstance().getTimerInfo(index);
    }

    /**
     * 添加一个定时器，处理冲突情况下添加定时器前移除旧定时器
     */
    public void addTimer(@BookConflictType int bookConflictType, HBooking_Struct_Timer deleteBookProg, HBooking_Struct_Timer bookProg) {
        if (bookConflictType == Constants.BOOK_CONFLICT_ADD && deleteBookProg != null) {
            deleteTimer(deleteBookProg);
        }
        addTimer(bookProg);
    }

    /**
     * 添加一个定时器
     */
    public void addTimer(HBooking_Struct_Timer prog) {
        DTVBooking.getInstance().addTimer(prog);
    }

    /**
     * 替换一个定时器
     */
    public void replaceTimer(HBooking_Struct_Timer oldProg, HBooking_Struct_Timer newProg) {
        DTVBooking.getInstance().replaceTimer(oldProg, newProg);
    }

    /**
     * 删除一个定时器
     */
    public void deleteTimer(HBooking_Struct_Timer prog) {
        DTVBooking.getInstance().deleteTimer(prog);
    }

    /**
     * 检查一个事件是否被预订
     */
    public  HBooking_Struct_Timer progIsBooked(int sat, int tsid, int servid, int eventid) {
        return DTVBooking.getInstance().progIsBooked(sat, tsid, servid, eventid);
    }

    /**
     * 更新数据库，在界面添加删除定时器后，退出界面前应该将数据刷入flash更新
     */
    public int updateDBase(int speed) {
        return DTVBooking.getInstance().updateDBase(speed);
    }

    /**
     * 检查定时器是否有冲突
     *
     * @return 返回一个与当前定时器有冲突的定时器
     */
    public  HBooking_Struct_Timer conflictCheck( HBooking_Struct_Timer prog) {
        return DTVBooking.getInstance().conflictCheck(prog);
    }

    /**
     * 取消一个即将播放或预录的操作
     */
    public int cancelPlayTimer(int keyType, HBooking_Struct_PlayeTimer prog) {
        return DTVBooking.getInstance().cancelPlayTimer(keyType, prog);
    }

    /**
     * 获取即将触发的定时器
     */
    public  HBooking_Struct_Timer getReadyTimerInfo() {
        return DTVBooking.getInstance().getReadyTimerInfo();
    }

    /**
     * 获取当前播放或录制的信息
     */
    public HBooking_Struct_PlayeTimer getCurrPlayTimerInfo() {
        return DTVBooking.getInstance().getCurrPlayTimerInfo();
    }

    public HBooking_Struct_PlayeTimer getCancelBookTimer(HBooking_Struct_Timer bookProg) {
        HBooking_Struct_PlayeTimer cancelBookProg = new HBooking_Struct_PlayeTimer();
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

    public  HBooking_Struct_Timer newBookTimer(@NonNull EpgBookParameterModel parameterModel) {
        HBooking_Struct_Timer newBookProg = new  HBooking_Struct_Timer();
        newBookProg.used = 0;
        newBookProg.type = parameterModel.type;
        newBookProg.schtype = parameterModel.schtype;
        newBookProg.schway = parameterModel.schway;
        newBookProg.repeatway = HBooking_Enum_Repeat.ONCE;
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
            DTVCommon.TimeModel startTime = DTVCommonManager.getInstance().getStartTime(parameterModel.eventInfo);
            DTVCommon.TimeModel endTime = DTVCommonManager.getInstance().getEndTime(parameterModel.eventInfo);
            newBookProg.lasttime = new DateModel(startTime, endTime).getBetweenSeconds();
            newBookProg.name = TextUtils.isEmpty(parameterModel.eventInfo.memEventName) ? DEFAULT_BOOK_CONTENT : parameterModel.eventInfo.memEventName;
            newBookProg.content = TextUtils.isEmpty(parameterModel.eventInfo.memEventDesc) ? DEFAULT_BOOK_CONTENT : parameterModel.eventInfo.memEventDesc;
        } else {
            newBookProg.name = DEFAULT_BOOK_CONTENT;
            newBookProg.content = DEFAULT_BOOK_CONTENT;
        }
        return newBookProg;
    }

    @BookConflictType
    public int getConflictType( HBooking_Struct_Timer conflictBookProg) {
        if (conflictBookProg == null) return -1;

        if (conflictBookProg.used == HBooking_Enum_Status.INVALID) {
            return DTVBookingManager.getInstance().getTimerNum() >= DTVBooking.MAX_BOOKING_NUM ? Constants.BOOK_CONFLICT_LIMIT : Constants.BOOK_CONFLICT_NONE;
        } else if (conflictBookProg.used == HBooking_Enum_Status.VALID) {
            return Constants.BOOK_CONFLICT_ADD;
        } else if (conflictBookProg.used == HBooking_Enum_Status.CONCLICT) {
            return Constants.BOOK_CONFLICT_REPLACE;
        }

        return -1;
    }

    public List<BookingModel> getBookingModelList() {
        List< HBooking_Struct_Timer> bookingList = getBookingList();
        List<BookingModel> bookingModels = new ArrayList<>();
        if (bookingList != null && !bookingList.isEmpty()) {
            for ( HBooking_Struct_Timer bookInfo : bookingList) {
                HProg_Struct_ProgBasicInfo progInfo = DTVProgramManager.getInstance().getProgInfoByServiceId(bookInfo.servid, bookInfo.tsid, bookInfo.sat);
                if (progInfo != null) {
                    BookingModel bookingModel = new BookingModel(bookInfo, progInfo);
                    bookingModels.add(bookingModel);
                }
            }
        }
        return bookingModels;
    }

    public List< HBooking_Struct_Timer> getBookingList() {
        List< HBooking_Struct_Timer> bookList = new ArrayList<>();
        int num = getTimerNum();
        for (int i = 0; i < num; i++) {
            HBooking_Struct_Timer bookProg = getTimerInfo(i);
            if (bookProg != null) {
                bookList.add(bookProg);
            }
        }
        return bookList;
    }

    public void setRecording(boolean recording) {
        this.mRecording = recording;
    }

    public boolean isRecording() {
        return mRecording;
    }
}
