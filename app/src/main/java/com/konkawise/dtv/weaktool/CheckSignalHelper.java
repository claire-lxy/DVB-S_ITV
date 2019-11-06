package com.konkawise.dtv.weaktool;

import com.konkawise.dtv.DTVSearchManager;
import com.konkawise.dtv.rx.RxTransformer;

import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.disposables.Disposable;

public class CheckSignalHelper {
    private OnCheckSignalListener mOnCheckSignalListener;
    private boolean mSignalRandom = true;
    private Disposable mDisposable;

    public void startCheckSignal() {
        stopCheckSignal();

        int[] signal = new int[2];
        mDisposable = Observable.interval(0, 1L, TimeUnit.SECONDS)
                .map(aLong -> {
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
                    signal[0] = randomStrength;
                    signal[1] = randomQuality;
                    return signal;
                })
                .compose(RxTransformer.threadTransformer())
                .subscribe(ints -> {
                    if (mOnCheckSignalListener != null) {
                        mOnCheckSignalListener.signal(signal[0], signal[1]);
                    }
                });
    }

    public void stopCheckSignal() {
        if (mDisposable != null) {
            mDisposable.dispose();
            mDisposable = null;
        }
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
