package com.konkawise.dtv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.konkawise.dtv.service.BookService;

public class BookCompletedReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            BookService.bootService(new Intent(context, BookService.class));
        }
    }
}
