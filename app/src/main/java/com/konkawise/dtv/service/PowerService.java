package com.konkawise.dtv.service;

import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.konkawise.dtv.base.BaseService;
import com.konkawise.dtv.receiver.PowerReceiver;

public class PowerService extends BaseService {
    private static final String TAG = "PowerService";
    private PowerReceiver mPowerReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "power service create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "power service start");
        if (mPowerReceiver == null) {
            registerPowerReceiver();
        }
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "power service destroy");
        unregisterPowerReceiver();
        super.onDestroy();
    }

    private void registerPowerReceiver() {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);
        mPowerReceiver = new PowerReceiver();
        registerReceiver(mPowerReceiver, intentFilter);
    }

    private void unregisterPowerReceiver() {
        if (mPowerReceiver != null) {
            unregisterReceiver(mPowerReceiver);
        }
    }
}
