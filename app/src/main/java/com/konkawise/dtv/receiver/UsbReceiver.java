package com.konkawise.dtv.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.UsbManager;

public class UsbReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        UsbManager.getInstance().usbObserveReceive(context, intent, Intent.ACTION_MEDIA_MOUNTED.equals(intent.getAction())
                ? Constants.USB_TYPE_ATTACH : Constants.USB_TYPE_DETACH);
    }
}
