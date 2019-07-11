package com.konkawise.dtv.weaktool;

import com.konkawise.dtv.WeakToolManager;

import java.lang.ref.WeakReference;
import java.util.TimerTask;

public abstract class WeakTimerTask<T extends WeakToolInterface> extends TimerTask implements WeakTool{
    protected WeakReference<T> mWeakReference;

    public WeakTimerTask(T view) {
        mWeakReference = new WeakReference<>(view);
        WeakToolManager.getInstance().addWeakTool(view, this);
    }

    @Override
    public void run() {
        if (mWeakReference != null && mWeakReference.get() != null) {
            runTimer();
        }
    }

    @Override
    public void release() {
        if (mWeakReference != null) {
            mWeakReference.clear();
        }
        cancel();
    }

    protected abstract void runTimer();
}
