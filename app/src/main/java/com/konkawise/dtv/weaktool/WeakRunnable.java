package com.konkawise.dtv.weaktool;

import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.WeakToolManager;

import java.lang.ref.WeakReference;

public abstract class WeakRunnable<T extends WeakToolInterface> implements Runnable, WeakTool {
    protected WeakReference<T> mWeakReference;

    public WeakRunnable(T view) {
        mWeakReference = new WeakReference<>(view);
        WeakToolManager.getInstance().addWeakTool(view, this);
    }

    @Override
    public void run() {
        if (mWeakReference != null && mWeakReference.get() != null) {
            loadBackground();
        }
    }

    @Override
    public void release() {
        ThreadPoolManager.getInstance().remove(this);
    }

    protected abstract void loadBackground();
}
