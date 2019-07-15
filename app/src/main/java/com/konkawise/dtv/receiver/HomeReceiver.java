package com.konkawise.dtv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.konkawise.dtv.SWDVBManager;
import com.sw.dvblib.SWDVB;

public class HomeReceiver extends BroadcastReceiver {
    private static final String TAG = "HomeReceiver";
    private OnReceiveHomeHandleListener mOnReceiveHomeHandleListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "receive home");
        if (mOnReceiveHomeHandleListener != null) {
            boolean handleCallback = mOnReceiveHomeHandleListener.onHomeHandleCallback();
            if (handleCallback) {
                releaseResource();
            }
        } else {
            releaseResource();
        }
    }

    private void releaseResource() {
        SWDVBManager.getInstance().regMsgHandler(null, null); // 主要处理取消booking监听
        SWDVB.Destory();
    }

    public void registerReceiveHomeHandlerListener(OnReceiveHomeHandleListener listener) {
        this.mOnReceiveHomeHandleListener = listener;
    }

    public void unregisterReceiveHomeHandleListener() {
        mOnReceiveHomeHandleListener = null;
    }

    public interface OnReceiveHomeHandleListener {
        boolean onHomeHandleCallback();
    }
}
