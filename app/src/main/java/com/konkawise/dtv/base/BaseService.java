package com.konkawise.dtv.base;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;

import com.konkawise.dtv.KonkaApplication;

public abstract class BaseService extends Service {

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(
                    new NotificationChannel(getPackageName(), getPackageName(),
                            NotificationManager.IMPORTANCE_DEFAULT));
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, getPackageName());
            startForeground(1, builder.build());
        }
    }

    @Override
    public void onDestroy() {
        stopForeground(true);
        super.onDestroy();
    }

    public static void bootService(Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            KonkaApplication.getContext().startForegroundService(intent);
        } else {
            KonkaApplication.getContext().startService(intent);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
