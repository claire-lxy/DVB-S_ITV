package com.konkawise.dtv.service;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWDVBManager;
import com.konkawise.dtv.base.BaseService;
import com.konkawise.dtv.dialog.CommTipsSystemDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.sw.dvblib.msg.MsgEvent;

public class RefreshChannelService extends BaseService {
    private static final String TAG = "RefreshChannelService";
    private CommTipsSystemDialog mDialog;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (mDialog == null) {
            registerRefreshChannel();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "stop refresh channel service");
        unregisterRefreshChannel();
        mDialog = null;
        super.onDestroy();
    }

    private void registerRefreshChannel() {
        MsgEvent msgEvent = SWDVBManager.getInstance().registerMsgEvent(Constants.REFRESH_CHANNEL_CALLBACK_MSG_ID);
//        msgEvent.registerCallbackListener(new CallbackListenerAdapter() {
//            @Override
//            public void PSearch_PROG_DBREFLESH() {
//                if (mDialog != null && mDialog.isShowing()) return;
//                showRefreshChannelDialog();
//            }
//        });
    }

    private void unregisterRefreshChannel() {
        SWDVBManager.getInstance().unregisterMsgEvent(Constants.REFRESH_CHANNEL_CALLBACK_MSG_ID);
    }

    private void showRefreshChannelDialog() {
        mDialog = new CommTipsSystemDialog(RefreshChannelService.this)
                .title(getString(R.string.dialog_title_tips))
                .content("NIT refresh programs")
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {

                    }
                });
        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        mDialog.show();
    }
}
