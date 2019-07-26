package com.konkawise.dtv;

import android.os.Looper;

import com.sw.dvblib.msg.cb.TimeMsgCB;

import java.util.LinkedList;
import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.DateTime_t;
import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;

public class RealTimeManager {
    private SystemTimeMsg mSystemTimeMsg = new SystemTimeMsg();
    private List<OnReceiveTimeListener> mSystemTimeRegisters = new LinkedList<>();

    private static class RealTimeManagerHolder {
        private static RealTimeManager INSTANCE = new RealTimeManager();
    }

    public static RealTimeManager getInstance() {
        return RealTimeManagerHolder.INSTANCE;
    }

    public void start() {
        SWDVBManager.getInstance().regMsgHandler(Constants.TIME_CALLBACK_MSG_ID, Looper.getMainLooper(), mSystemTimeMsg);
    }

    public void stop() {
        SWDVBManager.getInstance().unRegMsgHandler(Constants.TIME_CALLBACK_MSG_ID, mSystemTimeMsg);
    }

    public void register(OnReceiveTimeListener listener) {
        mSystemTimeRegisters.add(listener);
    }

    public void unregister(OnReceiveTimeListener listener) {
        mSystemTimeRegisters.remove(listener);
    }

    private class SystemTimeMsg extends TimeMsgCB {
        @Override
        public int Timer_ITIS_BROADCTIME(int utcdate, int utctime, int param) {
            SysTime_t sysTime = transSysTime(utcdate, utctime);
            if (sysTime != null) {
                String time = sysTime.Year + "-" +
                        (sysTime.Month < 10 ? "0" + sysTime.Month : sysTime.Month) + "-" +
                        (sysTime.Day < 10 ? "0" + sysTime.Day : sysTime.Day) + " " +
                        (sysTime.Hour < 10 ? "0" + sysTime.Hour : sysTime.Hour) + ":" +
                        (sysTime.Minute < 10 ? "0" + sysTime.Minute : sysTime.Minute) + ":" +
                        (sysTime.Second < 10 ? "0" + sysTime.Second : sysTime.Second);
                for (OnReceiveTimeListener listener : mSystemTimeRegisters) {
                    if (listener != null) {
                        listener.onReceiveTimeCallback(time);
                    }
                }
            }

            return super.Timer_ITIS_BROADCTIME(utcdate, utctime, param);
        }

        private SysTime_t transSysTime(int utcdate, int utctime) {
            SysTime_t stime = new SysTime_t();
            int temp = utctime / 3600;
            stime.Hour = temp;
            temp = utctime % 3600 / 60;
            stime.Minute = temp;
            temp = utctime % 3600 % 60;
            stime.Second = temp;
            DateTime_t dtime = transDate(utcdate);
            if (dtime != null) {
                stime.Year = dtime.Year;
                stime.Month = dtime.Month;
                stime.Day = dtime.Day;
                stime.Weekday = dtime.Weekday;
                return stime;
            }
            return null;
        }

        private DateTime_t transDate(int mjd) {
            DateTime_t dtime = new DateTime_t();
            int y = (int) (((double) ((float) mjd) - 15078.2D) / 365.25D);
            int m = (int) (((double) ((float) mjd) - 14956.1D - (double) ((int) ((double) ((float) y) * 365.25D))) / 30.6001D);
            int d = mjd - 14956 - (int) ((double) ((float) y) * 365.25D) - (int) ((double) ((float) m) * 30.6001D);
            int k = m != 14 && m != 15 ? 0 : 1;
            y = y + k + 1900;
            m = m - 1 - k * 12;
            int wd = (mjd + 2) % 7 + 1;
            if (0 != mjd) {
                dtime.Year = y;
                dtime.Month = m;
                dtime.Day = d;
                dtime.Weekday = wd;
            } else {
                dtime.Year = 0;
                dtime.Month = 0;
                dtime.Day = 0;
                dtime.Weekday = 0;
            }

            return dtime;
        }
    }

    public interface OnReceiveTimeListener {
        void onReceiveTimeCallback(String time);
    }
}
