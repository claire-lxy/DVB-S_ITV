package com.konkawise.dtv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.konkawise.dtv.DTVDVBManager;
import com.konkawise.dtv.event.RecordStateChangeEvent;

import org.greenrobot.eventbus.EventBus;

public class PowerReceiver extends BroadcastReceiver {
    private static final String TAG = "PowerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            Log.i(TAG, "receive screen off");
            EventBus.getDefault().post(new RecordStateChangeEvent(false));
            DTVDVBManager.getInstance().releaseResource();
        }
    }
}
