package com.konkawise.dtv;

import com.sw.dvblib.DTVCommon;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.listener.CallbackListenerAdapter;

import java.util.LinkedList;
import java.util.List;

import vendor.konka.hardware.dtvmanager.V1_0.HCommon_Struct_MDJ;

public class RealTimeManager {
    private MsgEvent mMsgEvent;
    private List<OnReceiveTimeListener> mSystemTimeRegisters = new LinkedList<>();

    private static class RealTimeManagerHolder {
        private static RealTimeManager INSTANCE = new RealTimeManager();
    }

    public static RealTimeManager getInstance() {
        return RealTimeManagerHolder.INSTANCE;
    }

    public void start() {
        if (mMsgEvent == null) {
            mMsgEvent = DTVDVBManager.getInstance().registerMsgEvent(Constants.TIME_CALLBACK_MSG_ID);
            mMsgEvent.registerCallbackListener(new CallbackListenerAdapter() {
                @Override
                public void TIME_onBroadcastDateTime(int date, int time, int param) {
                    HCommon_Struct_MDJ utcTime_t = new HCommon_Struct_MDJ();
                    utcTime_t.date = date;
                    utcTime_t.time = time;
                    DTVCommon.TimeModel sysTime = DTVCommonManager.getInstance().mjdToLocal(utcTime_t);
                    if (sysTime != null) {
                        String realTime = sysTime.Year + "-" +
                                (sysTime.Month < 10 ? "0" + sysTime.Month : sysTime.Month) + "-" +
                                (sysTime.Day < 10 ? "0" + sysTime.Day : sysTime.Day) + " " +
                                (sysTime.Hour < 10 ? "0" + sysTime.Hour : sysTime.Hour) + ":" +
                                (sysTime.Minute < 10 ? "0" + sysTime.Minute : sysTime.Minute) + ":" +
                                (sysTime.Second < 10 ? "0" + sysTime.Second : sysTime.Second);
                        for (OnReceiveTimeListener listener : mSystemTimeRegisters) {
                            if (listener != null) {
                                listener.onReceiveTimeCallback(realTime);
                            }
                        }
                    }
                }
            });
        }
    }

    public void stop() {
        if (mMsgEvent != null) {
            DTVDVBManager.getInstance().unregisterMsgEvent(Constants.TIME_CALLBACK_MSG_ID);
            mMsgEvent = null;
        }
    }

    public void register(OnReceiveTimeListener listener) {
        mSystemTimeRegisters.add(listener);
    }

    public void unregister(OnReceiveTimeListener listener) {
        mSystemTimeRegisters.remove(listener);
    }

    public interface OnReceiveTimeListener {
        void onReceiveTimeCallback(String time);
    }
}
