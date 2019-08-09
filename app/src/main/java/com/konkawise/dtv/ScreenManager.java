package com.konkawise.dtv;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.PowerManager;
import android.text.TextUtils;

import static android.content.Context.POWER_SERVICE;

public class ScreenManager {

    private static class ScreenManagerHolder {
        private static ScreenManager INSTANCE = new ScreenManager();
    }

    public static ScreenManager getInstance() {
        return ScreenManagerHolder.INSTANCE;
    }

    public void wakeupScreen(Context context) {
        PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);
        boolean isScreenOn;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT_WATCH) {
            isScreenOn = powerManager.isInteractive();
        } else {
            isScreenOn = powerManager.isScreenOn();
        }
        if (!isScreenOn && TextUtils.equals(PropertiesManager.getInstance().getProperty(Constants.STANDBY_PROPERTY), Constants.STANDBY_SMART_SUSPEND)) {
            @SuppressLint("InvalidWakeLockTag") PowerManager.WakeLock wakeLock = powerManager.newWakeLock(
                    PowerManager.ACQUIRE_CAUSES_WAKEUP | PowerManager.SCREEN_BRIGHT_WAKE_LOCK, "wakeupScreen");
            if (wakeLock != null) {
                wakeLock.acquire(1000);
                wakeLock.release();
            }
        }
    }
}
