package com.konkawise.dtv.weaktool;

import android.os.Handler;
import android.os.Looper;

import com.konkawise.dtv.SWTimerManager;

import java.util.Timer;

import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;

public class RealTimeHelper {
    private static final long UPDATE_REAL_TIME_PERIOD = 1000;
    private WeakToolInterface mContext;
    private Timer mRealTimeTimer;
    private Handler mHandler;
    private OnRealTimerListener mOnRealTimeListener;

    public RealTimeHelper(WeakToolInterface context) {
        this.mContext = context;
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void start() {
        stop();
        mRealTimeTimer = new Timer();
        mRealTimeTimer.schedule(new RealTimeTimerTask(mContext), 0, UPDATE_REAL_TIME_PERIOD);
    }

    public void stop() {
        if (mRealTimeTimer != null) {
            mRealTimeTimer.cancel();
            mRealTimeTimer.purge();
            mRealTimeTimer = null;
        }
    }

    private class RealTimeTimerTask extends WeakTimerTask<WeakToolInterface> {

        RealTimeTimerTask(WeakToolInterface view) {
            super(view);
        }

        @Override
        protected void runTimer() {
            SysTime_t sysTime = SWTimerManager.getInstance().getSysTime();

            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (mOnRealTimeListener != null && sysTime != null) {
                        String realTime = sysTime.Year + "-" +
                                (sysTime.Month < 10 ? "0" + sysTime.Month : sysTime.Month) + "-" +
                                (sysTime.Day < 10 ? "0" + sysTime.Day : sysTime.Day) + " " +
                                (sysTime.Hour < 10 ? "0" + sysTime.Hour : sysTime.Hour) + ":" +
                                (sysTime.Minute < 10 ? "0" + sysTime.Minute : sysTime.Minute) + ":" +
                                (sysTime.Second < 10 ? "0" + sysTime.Second : sysTime.Second);
                        mOnRealTimeListener.onRealTimeCallback(realTime);
                    }
                }
            });
        }
    }

    public void setOnRealTimeListener(OnRealTimerListener listener) {
        this.mOnRealTimeListener = listener;
    }

    public interface OnRealTimerListener {
        void onRealTimeCallback(String realTime);
    }
}
