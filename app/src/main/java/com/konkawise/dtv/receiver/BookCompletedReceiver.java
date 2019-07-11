package com.konkawise.dtv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class BookCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
//        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
//            Intent bookListenerServiceIntent = new Intent(context, BookListenerService.class);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//                context.startForegroundService(bookListenerServiceIntent);
//            } else {
//                context.startService(bookListenerServiceIntent);
//            }
//        }
    }
}
