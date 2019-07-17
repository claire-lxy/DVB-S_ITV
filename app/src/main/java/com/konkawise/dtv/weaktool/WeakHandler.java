package com.konkawise.dtv.weaktool;

import android.os.Handler;
import android.os.Message;

import com.konkawise.dtv.WeakToolManager;

import java.lang.ref.WeakReference;

public abstract class WeakHandler<T extends WeakToolInterface> extends Handler implements WeakTool {
    protected WeakReference<T> mWeakReference;

    public WeakHandler(T view) {
        mWeakReference = new WeakReference<>(view);
        WeakToolManager.getInstance().addWeakTool(view, this);
    }

    @Override
    public void handleMessage(Message msg) {
        if (mWeakReference != null && mWeakReference.get() != null) {
            handleMsg(msg);
        }
    }

    @Override
    public void release() {
        removeCallbacksAndMessages(null);
    }

    protected abstract void handleMsg(Message msg);
}
