package com.konkawise.dtv.weaktool;

import android.os.AsyncTask;

import com.konkawise.dtv.WeakToolManager;

import java.lang.ref.WeakReference;

public abstract class WeakAsyncTask<T extends WeakToolInterface, Param, Result> extends AsyncTask<Param, Void, Result> implements WeakTool {
    protected WeakReference<T> mWeakReference;

    public WeakAsyncTask(T view) {
        mWeakReference = new WeakReference<>(view);
        WeakToolManager.getInstance().addWeakTool(view, this);
    }

    @Override
    protected void onPreExecute() {
        if (mWeakReference != null && mWeakReference.get() != null) {
            preExecute();
        }
    }

    @Override
    protected Result doInBackground(Param... params) {
        if (mWeakReference != null && mWeakReference.get() != null) {
            return backgroundExecute(params);
        }
        return null;
    }

    @Override
    protected void onPostExecute(Result result) {
        if (mWeakReference != null && mWeakReference.get() != null) {
            postExecute(result);
        }
    }

    protected void preExecute() {

    }

    protected void postExecute(Result result) {

    }

    @Override
    public void release() {
        cancel(true);
    }

    protected abstract Result backgroundExecute(Param... param);
}
