package com.konkawise.dtv.service;

import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.WindowManager;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVDVBManager;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.DTVSearchManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseService;
import com.konkawise.dtv.dialog.CommTipsSystemDialog;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.listener.CallbackListenerAdapter;

import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HSearch_Enum_CAType;
import vendor.konka.hardware.dtvmanager.V1_0.HSearch_Enum_ProgType;

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
        MsgEvent msgEvent = DTVDVBManager.getInstance().registerMsgEvent(Constants.MsgCallbackId.REFRESH_CHANNEL);
        msgEvent.registerCallbackListener(new CallbackListenerAdapter() {
            @Override
            public void SEARCH_onDBRefresh() {
                if (mDialog != null && mDialog.isShowing()) return;
                showRefreshChannelDialog();
            }
        });
    }

    private void unregisterRefreshChannel() {
        DTVDVBManager.getInstance().unregisterMsgEvent(Constants.MsgCallbackId.REFRESH_CHANNEL);
    }

    private void showRefreshChannelDialog() {
        HProg_Struct_ProgInfo currProgInfo = DTVProgramManager.getInstance().getCurrProgInfo();
        if (currProgInfo == null) return;

        mDialog = new CommTipsSystemDialog(RefreshChannelService.this)
                .title(getString(R.string.dialog_title_tips))
                .content(getString(R.string.dialog_refresh_nit))
                .setOnPositiveListener("", () -> DTVSearchManager.getInstance().searchByNIT(currProgInfo.Sat, currProgInfo.Freq, currProgInfo.Symbol,
                        currProgInfo.Qam, HSearch_Enum_ProgType.ALL, 0, HSearch_Enum_CAType.ALLCA));
        if (mDialog.getWindow() != null) {
            mDialog.getWindow().setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        mDialog.show();
    }
}
