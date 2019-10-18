package com.konkawise.dtv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.konkawise.dtv.DTVDVBManager;
import com.konkawise.dtv.event.BookRegisterListenerEvent;

import org.greenrobot.eventbus.EventBus;

public class HomeReceiver extends BroadcastReceiver {
    private static final String TAG = "HomeReceiver";
    private OnReceiveHomeHandleListener mOnReceiveHomeHandleListener;

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_CLOSE_SYSTEM_DIALOGS.equals(intent.getAction())) {
            Log.i(TAG, "receive home");
            EventBus.getDefault().post(new BookRegisterListenerEvent(true));
            if (mOnReceiveHomeHandleListener != null) {
                boolean handleCallback = mOnReceiveHomeHandleListener.onHomeHandleCallback();
                if (handleCallback) {
                    DTVDVBManager.getInstance().releaseResource();
                }
            } else {
                DTVDVBManager.getInstance().releaseResource();
            }
        }
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
