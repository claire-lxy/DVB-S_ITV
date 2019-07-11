package com.konkawise.dtv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.konkawise.dtv.SWDVBManager;
import com.sw.dvblib.SWDVB;

public class HomeReceiver extends BroadcastReceiver {
    private static final String TAG = "HomeReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "receiver home");
        SWDVBManager.getInstance().regMsgHandler(null, null); // 主要处理取消booking监听
        SWDVB.Destory();
    }
}
