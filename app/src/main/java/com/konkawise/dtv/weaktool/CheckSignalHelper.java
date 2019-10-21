package com.konkawise.dtv.weaktool;

import android.os.Handler;
import android.os.Looper;

import com.konkawise.dtv.DTVSearchManager;

import java.util.Timer;

public class CheckSignalHelper {
    private static final long CHECK_SIGNAL_PERIOD = 1000;
    private WeakToolInterface mContext;
    private Timer mCheckSignalTimer;
    private Handler mHandler;
    private OnCheckSignalListener mOnCheckSignalListener;
    private boolean mSignalRandom = true;

    public CheckSignalHelper(WeakToolInterface context) {
        this.mContext = context;
        this.mHandler = new Handler(Looper.getMainLooper());
    }

    public void startCheckSignal() {
        stopCheckSignal();
        mCheckSignalTimer = new Timer();
        mCheckSignalTimer.schedule(new CheckSignalTimerTask(mContext), 0, CHECK_SIGNAL_PERIOD);
    }

    public void stopCheckSignal() {
        if (mCheckSignalTimer != null) {
            mCheckSignalTimer.cancel();
            mCheckSignalTimer.purge();
            mCheckSignalTimer = null;
        }
    }

    private class CheckSignalTimerTask extends WeakTimerTask<WeakToolInterface> {

        CheckSignalTimerTask(WeakToolInterface view) {
            super(view);
        }

        @Override
        protected void runTimer() {
            int strength = getSignalStrength();
            int quality = getSignalQuality();
            // 实现进度条信号上下浮动效果
            int randomStrength;
            int randomQuality;
            if (mSignalRandom) {
                randomStrength = (int) (getRandomMin(strength) + Math.random() * (getRandomMax(strength) - getRandomMin(strength) + 1));
                randomQuality = (int) (getRandomMax(quality) + Math.random() * (getRandomMax(quality) - getRandomMin(quality) + 1));
            } else {
                randomStrength = strength;
                randomQuality = quality;
            }

            mHandler.post(() -> {
                if (mOnCheckSignalListener != null) {
                    mOnCheckSignalListener.signal(getValidValue(randomStrength), getValidValue(randomQuality));
                }
            });
        }

        private int getRandomMin(int value) {
            return value - 2 < 0 ? 0 : value - 2;
        }

        private int getRandomMax(int value) {
            return value + 2 > 100 ? 100 : value + 2;
        }

        private int getSignalStrength() {
            return getValidValue(DTVSearchManager.getInstance().getSignalStatus(DTVSearchManager.SIGNAL_STRENGTH));
        }

        private int getSignalQuality() {
            return getValidValue(DTVSearchManager.getInstance().getSignalStatus(DTVSearchManager.SIGNAL_QUALITY));
        }

        private int getValidValue(int value) {
            if (value > 100) {
                return 100;
            } else if (value < 0) {
                return 0;
            } else {
                return value;
            }
        }
    }

    public void setOnCheckSignalListener(OnCheckSignalListener listener) {
        this.mOnCheckSignalListener = listener;
    }

    public void setSignalRandom(boolean isSignalRandom) {
        this.mSignalRandom = isSignalRandom;
    }

    public interface OnCheckSignalListener {
        void signal(int strength, int quality);
    }
}
