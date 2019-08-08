package com.konkawise.dtv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.PropertiesManager;
import com.konkawise.dtv.SWDVBManager;

public class PowerReceiver extends BroadcastReceiver {
    private static final String TAG = "PowerReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_SCREEN_OFF.equals(intent.getAction())) {
            Log.i(TAG, "receive screen off");
            // 待机情况下设置系统属性，book触发时亮起屏幕倒计时弹框
            PropertiesManager.getInstance().setProperty(Constants.STANDBY_SCREEN_ON_PROPERTY, "true");
            SWDVBManager.getInstance().releaseResource();
        }
    }
}
