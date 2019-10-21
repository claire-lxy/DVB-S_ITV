package com.konkawise.dtv.ui;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.DTVCommonManager;
import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVPlayerManager;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.DTVSettingManager;
import com.konkawise.dtv.HandlerMsgManager;
import com.konkawise.dtv.PreferenceManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.RealTimeManager;
import com.konkawise.dtv.DTVBookingManager;
import com.konkawise.dtv.DTVPVRManager;
import com.konkawise.dtv.DTVDVBManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.UsbManager;
import com.konkawise.dtv.adapter.TvListAdapter;
import com.konkawise.dtv.annotation.DVBSelectType;
import com.konkawise.dtv.annotation.PVRType;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.bean.UsbInfo;
import com.konkawise.dtv.dialog.AudioDialog;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.FindChannelDialog;
import com.konkawise.dtv.dialog.InitPasswordDialog;
import com.konkawise.dtv.dialog.InputPvrMinuteDialog;
import com.konkawise.dtv.dialog.OnCommCallback;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.PasswordDialog;
import com.konkawise.dtv.dialog.PfBarScanDialog;
import com.konkawise.dtv.dialog.PfDetailDialog;
import com.konkawise.dtv.dialog.SearchChannelDialog;
import com.konkawise.dtv.dialog.SubtitleDialog;
import com.konkawise.dtv.dialog.TeletextDialog;
import com.konkawise.dtv.event.ProgramUpdateEvent;
import com.konkawise.dtv.event.RecordStateChangeEvent;
import com.konkawise.dtv.event.ReloadSatEvent;
import com.konkawise.dtv.service.BookService;
import com.konkawise.dtv.service.PowerService;
import com.konkawise.dtv.utils.TimeUtils;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.CheckSignalHelper;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.konkawise.dtv.weaktool.WeakTimerTask;
import com.sw.dvblib.DTVManager;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.listener.CallbackListenerAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_TP;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Enum_Group;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Enum_Type;
import vendor.konka.hardware.dtvmanager.V1_0.HSetting_Enum_Property;
import vendor.konka.hardware.dtvmanager.V1_0.HPlayer_Struct_Subtitle;
import vendor.konka.hardware.dtvmanager.V1_0.HPlayer_Struct_Teletext;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_SatInfo;

public class Topmost extends BaseActivity {
    private static final String TAG = "Topmost";
    private static final long RECORDING_DELAY = 1000;
    private static final long RECORDING_PERIOD = 1000;
    private static final long PLAY_PROG_DELAY = 1000;
    private static final long JUMP_PROG_DELAY = 2500;
    private static final long RECORD_TIME_HIDE_DELAY = 10 * 1000;

    private static final int KEYCODE_TV_SUBTITLE = 293;

    @BindView(R.id.sv_topmost)
    SurfaceView mSurfaceView;

    @BindView(R.id.iv_radio_bg)
    ImageView mIvRadioBackground;

    @BindView(R.id.tv_show_num)
    TextView mTvProgNum;

    @BindView(R.id.ll_recording_layout)
    ViewGroup mRecordingLayout;

    @BindView(R.id.tv_recording_time)
    TextView mTvRecordingTime;

    @BindView(R.id.tv_satellite_name)
    TextView mTvSatelliteName;

    @BindView(R.id.lv_prog_list)
    ListView mProgListView;

    @BindView(R.id.pb_loading_channel)
    ProgressBar mPbLoadingChannel;

    @BindView(R.id.ll_prog_list_menu)
    LinearLayout mProgListMenu;

    @BindView(R.id.ll_menu)
    ViewGroup mMenu;

    @BindView(R.id.item_installation)
    ViewGroup mItemInstallation;

    @BindView(R.id.iv_installation_back)
    ImageView mIvInstallationBack;

    @BindView(R.id.tv_installation)
    TextView mTvInstallation;

    @BindView(R.id.item_manual_installation)
    TextView mItemManualInstallation;

    @BindView(R.id.item_blind_scan)
    TextView mItemBlindScan;

    @BindView(R.id.item_epg)
    TextView mItemEpg;

    @BindView(R.id.item_channel_manage)
    ViewGroup mItemChannelManage;

    @BindView(R.id.iv_channel_manage_back)
    ImageView mIvChannelManageBack;

    @BindView(R.id.tv_channel_manage)
    TextView mTvChannelManage;

    @BindView(R.id.item_channel_edit)
    TextView mItemChannelEdit;

    @BindView(R.id.item_channel_favorite)
    TextView mItemChannelFavorite;

    @BindView(R.id.item_clear_channel)
    TextView mItemClearChannel;

    @BindView(R.id.item_restore_user_data)
    TextView mItemRestoreUserData;

    @BindView(R.id.item_backup_user_data)
    TextView mItemBackupUserData;

    @BindView(R.id.item_dtv_setting)
    TextView mItemDtvSetting;

    @BindView(R.id.item_data_reset)
    TextView mItemDataReset;

    @OnItemSelected(R.id.lv_prog_list)
    void onItemSelect(int position) {
        mCurrSelectProgPosition = position;
    }

    @OnItemClick(R.id.lv_prog_list)
    void onItemClick(int position) {
        toggleProgList();
        if (mProgListAdapter.getItem(position).ProgNo == DTVProgramManager.getInstance().getCurrProgNo())
            return;

        DTVProgramManager.getInstance().setCurrProgType(DTVProgramManager.getInstance().getCurrProgType(), 0);
        mProgListAdapter.setSelectPosition(position);
        playProg(mProgListAdapter.getItem(position).ProgNo);
    }

    @OnFocusChange(R.id.lv_prog_list)
    void onFocusChange(boolean hasFocus) {
        // 存在频道列表遥控选中到底部继续按下丢失焦点问题，重新获取焦点
        if (mProgListShow && !hasFocus) {
            mProgListView.requestFocus();
        }
    }

    @OnClick(R.id.item_installation)
    void installation() {
        if (isShowInstallation()) {
            showS2OrT2Dialog(Constants.DVBSelectType.INSTALLATION, true);
        } else {
            toggleInstallationItem();
        }
    }

    @OnClick(R.id.item_manual_installation)
    void manualInstallation() {
        startActivity(new Intent(this, SatelliteActivity.class));
    }

    @OnClick(R.id.item_blind_scan)
    void blindScan() {
        startActivity(new Intent(this, BlindActivity.class));
    }

    @OnClick(R.id.item_epg)
    void epg() {
        gotoEpg();
    }

    private void gotoEpg() {
        if (mProgListAdapter.getCount() <= 0) {
            if (mMenuShow) {
                showRemindSearchDialog();
            }
        } else {
            startActivity(new Intent(this, EpgActivity.class));
        }
    }

    @OnClick(R.id.item_channel_manage)
    void channelManage() {
        if (DTVProgramManager.getInstance().getProgNumOfGroup(HProg_Enum_Group.TOTAL_GROUP, 0) <= 0 && isShowChannelManageItem()) {
            if (mMenuShow) {
                showRemindSearchDialog();
            }
        } else {
            toggleChannelManageItem();
        }
    }

    @OnClick(R.id.item_channel_edit)
    void channelEdit() {
        if (DTVSettingManager.getInstance().isOpenMenuLock() && !mPasswordEntered) {
            showPasswordDialog(new PasswordDialog.OnPasswordInputListener() {
                @Override
                public void onPasswordInput(String inputPassword, String currentPassword, boolean isValid) {
                    if (isValid) {
                        mPasswordEntered = true;
                        startActivity(new Intent(Topmost.this, ChannelEditActivity.class));
                    }
                }
            });
        } else {
            startActivity(new Intent(this, ChannelEditActivity.class));
        }
    }

    @OnClick(R.id.item_channel_favorite)
    void channelFavorite() {
        if (DTVSettingManager.getInstance().isOpenMenuLock() && !mPasswordEntered) {
            showPasswordDialog(new PasswordDialog.OnPasswordInputListener() {
                @Override
                public void onPasswordInput(String inputPassword, String currentPassword, boolean isValid) {
                    if (isValid) {
                        mPasswordEntered = true;
                        startActivity(new Intent(Topmost.this, FavoriteActivity.class));
                    }
                }
            });
        } else {
            startActivity(new Intent(this, FavoriteActivity.class));
        }
    }

    @OnClick(R.id.item_clear_channel)
    void clearChannel() {
        if (DTVSettingManager.getInstance().isOpenMenuLock() && !mPasswordEntered) {
            showPasswordDialog(new PasswordDialog.OnPasswordInputListener() {
                @Override
                public void onPasswordInput(String inputPassword, String currentPassword, boolean isValid) {
                    if (isValid) {
                        mPasswordEntered = true;
                        showClearChannelDialog();
                    }
                }
            });
        } else {
            showClearChannelDialog();
        }
    }

    @OnClick(R.id.item_restore_user_data)
    void restoreUserData() {

    }

    @OnClick(R.id.item_backup_user_data)
    void backupUserData() {

    }

    @OnClick(R.id.item_dtv_setting)
    void dtvSetting() {
        startActivity(new Intent(this, DTVSettingActivity.class));
    }

    @OnClick(R.id.item_data_reset)
    void factoryReset() {
        if (DTVSettingManager.getInstance().isOpenMenuLock() && !mPasswordEntered) {
            showPasswordDialog(new PasswordDialog.OnPasswordInputListener() {
                @Override
                public void onPasswordInput(String inputPassword, String currentPassword, boolean isValid) {
                    if (isValid) {
                        mPasswordEntered = true;
                        showDataResetDialog();
                    }
                }
            });
        } else {
            showDataResetDialog();
        }
    }

    private int mCurrProgGroup;
    private int mCurrProgGroupParams;

    private int mNewProgNum;
    private boolean mLongPressed;
    private long mLongPressDelayTime;

    private Timer mRecordingTimer;
    private RecordingTimerTask mRecordingTimerTask;

    private ProgHandler mProgHandler;
    private PlayHandler mPlayHandler;
    private JumpProgHandler mJumpProgHandler;
    private StringBuilder mJumpProgNumBuilder = new StringBuilder();

    private PfBarScanDialog mPfBarScanDialog;
    private PfDetailDialog mPfDetailDialog;
    private PasswordDialog mPasswordDialog;
    private SearchChannelDialog mSearchChannelDialog;
    private InitPasswordDialog mSettingPasswordDialog;

    private boolean mProgListShow;
    private boolean mMenuShow;
    private boolean mPasswordEntered;

    private TvListAdapter mProgListAdapter;
    private int mCurrSelectProgPosition;
    private int mCurrSatPosition;
    private List<HProg_Struct_SatInfo> mSatList;
    // key:satIndex，fav分组的key从satIndex+DTVProgramManager.RANGE_SAT_INDEX开始，获取喜爱分组列表时要-DTVProgramManager.RANGE_SAT_INDEX
    private SparseArray<List<HProg_Struct_ProgInfo>> mProgListMap = new SparseArray<>();
    private LoadProgRunnable mLoadProgRunnable;
    private LoadSatRunnable mLoadSatRunnable;

    private WaitingStartRecordRunnable mWaitingStartRecordRunnable;

    private boolean mUsbAttach;

    private CheckSignalHelper mCheckSignalHelper;
    private boolean mSignalOk = true;

    private static class PlayHandler extends WeakHandler<Topmost> {
        static final int MSG_PLAY_PROG = 0;

        PlayHandler(Topmost view) {
            super(view);
        }

        @Override
        protected void handleMsg(Message msg) {
            Topmost context = mWeakReference.get();
            if (msg.what == MSG_PLAY_PROG) {
                context.dismissPasswordDialog();
                DTVProgramManager.getInstance().setCurrProgNo(msg.arg1);
                DTVPlayerManager.getInstance().startPlayProgNo(msg.arg1, 1);
            }
        }
    }

    private static class ProgHandler extends WeakHandler<Topmost> {
        static final int MSG_HIDE_PROG_NUM = 0;
        static final int MSG_SHOW_PROG_NUM = 1;
        static final int MSG_HIDE_PF_BAR = 2;
        static final int MSG_HIDE_RECORD_TIME = 3;

        ProgHandler(Topmost view) {
            super(view);
        }

        @Override
        protected void handleMsg(Message msg) {
            Topmost context = mWeakReference.get();
            switch (msg.what) {
                case MSG_HIDE_PROG_NUM:
                    context.mJumpProgNumBuilder.delete(0, context.mJumpProgNumBuilder.length());
                    context.mTvProgNum.setVisibility(View.INVISIBLE);
                    break;
                case MSG_SHOW_PROG_NUM:
                    context.showProgNum(context.getCurrentProgShowNo());
                    context.showPfInfo();
                    break;
                case MSG_HIDE_PF_BAR:
                    context.dismissPfBarScanDialog();
                    break;
                case MSG_HIDE_RECORD_TIME:
                    context.mRecordingLayout.setVisibility(View.GONE);
                    break;
            }
        }
    }

    private static class JumpProgHandler extends WeakHandler<Topmost> {
        static final int MSG_JUMP_PROG = 0;

        JumpProgHandler(Topmost view) {
            super(view);
        }

        @Override
        protected void handleMsg(Message msg) {
            Topmost context = mWeakReference.get();
            if (msg.what == MSG_JUMP_PROG) {
                int progNum = msg.arg1;
                if (context.isJumpProgNumValid() && progNum != DTVProgramManager.getInstance().getCurrProgNo()) {
                    context.mCurrSelectProgPosition = context.getPositionByProgNum(progNum);
                    context.playProg(progNum);
                } else {
                    if (!context.isJumpProgNumValid())
                        context.showProgNumInvalid(true);
                    else
                        context.showProgNumInvalid(false);
                }
                context.mJumpProgNumBuilder.delete(0, context.mJumpProgNumBuilder.length());
            }

        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.topmost;
    }

    @Override
    protected void setup() {
        DTVManager.getInstance(); // 必须先初始化库，否则使用库会出现空指针异常
        mCurrProgGroup = DTVProgramManager.getInstance().getCurrGroup();
        mCurrProgGroupParams = DTVProgramManager.getInstance().getCurrGroupParam();

        bootService();
        registerListener();
        initHandler();
        initSatList();
        initProgList();
        initSurfaceView();
        initCheckSignal();
        showRadioBackground();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RealTimeManager.getInstance().start();
        registerMsgEvent();
        showSurface();
        updatePfBarInfo();
        restoreMenuItem(); // 恢复menu初始item显示
        mCheckSignalHelper.startCheckSignal();

        checkLaunchSettingPassword();
        if (!DTVSettingManager.getInstance().isPasswordEmpty() && !DTVProgramManager.getInstance().isProgCanPlay()) {
            showSearchChannelDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        hideSurface();
        DTVPlayerManager.getInstance().stopPlay(DTVSettingManager.getInstance().getDTVProperty(HSetting_Enum_Property.PD_SwitchMode));
        unregisterMsgEvent();
        stopRecord();
        mCheckSignalHelper.stopCheckSignal();
        if (isFinishing()) {
            RealTimeManager.getInstance().stop();
            DTVDVBManager.getInstance().releaseResource();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mProgListShow) {
            toggleProgList();
        }
        if (mMenuShow) {
            toggleMenu(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        dismissAllDialog();
        unregisterListener();
    }

    private void registerMsgEvent() {
        MsgEvent msgEvent = DTVDVBManager.getInstance().registerMsgEvent(Constants.MsgCallbackId.LOCK);
        msgEvent.registerCallbackListener(new CallbackListenerAdapter() {
            @Override
            public void PLAYER_isLocked(int type, int progNo, int progIndex, int home) {
                showPasswordDialog();
            }

            @Override
            public void PLAYER_onUSBCompleted() {
                Log.i(TAG, "usb attach");
                mUsbAttach = true;
                ToastUtils.showToast(R.string.toast_storage_inserted);
            }

            @Override
            public void PLAYER_onUSBRemoved() {
                Log.i(TAG, "usb detach");
                mUsbAttach = false;
                ToastUtils.showToast(R.string.toast_storage_out);
                stopRecord();
            }
        });
    }

    private void unregisterMsgEvent() {
        DTVDVBManager.getInstance().unregisterMsgEvent(Constants.MsgCallbackId.LOCK);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleBook();
//        setIntent(new Intent()); // 处理完后要重置为空，防止其他界面返回或跳转到Topmost使用上一个book跳转的intent
    }

    private boolean handleBook() {
        int bookType = getIntent().getIntExtra(Constants.IntentKey.INTENT_BOOK_TYPE, -1);
        int recordSeconds = getIntent().getIntExtra(Constants.IntentKey.INTENT_BOOK_SECONDS, -1);
        int serviceid = getIntent().getIntExtra(Constants.IntentKey.INTENT_BOOK_SERVICEID, -1);
        int tsid = getIntent().getIntExtra(Constants.IntentKey.INTENT_BOOK_TSID, -1);
        int sat = getIntent().getIntExtra(Constants.IntentKey.INTENT_BOOK_SAT, -1);
        Log.i(TAG, "bookType = " + bookType + ", recordSeconds = " + recordSeconds + ", serviceid = " + serviceid + ", tsid = " + tsid + ", sat = " + sat);
        if (bookType == BookService.ACTION_BOOKING_PLAY) {
            if (serviceid != -1 && tsid != -1 && sat != -1) {
                DTVPlayerManager.getInstance().forcePlayProgByServiceId(serviceid, tsid, sat);
                return true;
            }
        } else if (bookType == BookService.ACTION_BOOKING_RECORD) {
            if (isUsbNotExit()) {
                ToastUtils.showToast(R.string.toast_no_storage_device);
                return false;
            }

            if (serviceid != -1 && tsid != -1 && sat != -1 && recordSeconds != -1) {
                DTVPlayerManager.getInstance().forcePlayProgByServiceId(serviceid, tsid, sat);
                startRecord(new OnCommCallback() {
                    @Override
                    public void callback(Object object) {
                        int recordFlag = (int) object;
                        if (recordFlag == 0) {
                            startRecordingTimer(recordSeconds);
                            DTVBookingManager.getInstance().setRecording(true);
                            sendHideRecordTimeMsg(new HandlerMsgModel(ProgHandler.MSG_HIDE_RECORD_TIME, RECORD_TIME_HIDE_DELAY));
                            ToastUtils.showToast(R.string.toast_start_record);
                        } else {
                            ToastUtils.showToast(R.string.toast_start_record_failed);
                        }
                    }
                });
                return true;
            }
        }
        return false;
    }

    private void startRecord(OnCommCallback callback) {
        String uuid = DTVSettingManager.getInstance().getDiskUUID();
        Set<UsbInfo> usbInfos = UsbManager.getInstance().queryUsbInfos(this);
        if (usbInfos == null || usbInfos.isEmpty()) callback.callback(UsbManager.USB_NOT_FOUND);

        if (!TextUtils.isEmpty(uuid)) {
            UsbInfo usbInfo = UsbManager.getInstance().isContainUsb(usbInfos, uuid);
            if (usbInfo != null) {
                executeRecord(usbInfo, callback);
            } else {
                checkRemainUsbRecord(usbInfos, uuid, callback);
            }
        } else {
            checkRemainUsbRecord(usbInfos, "", callback);
        }
    }

    private void checkRemainUsbRecord(Collection<UsbInfo> usbInfos, String uuid, OnCommCallback callback) {
        List<UsbInfo> remainUsbInfos = UsbManager.getInstance().getRemainUsbInfos(usbInfos, uuid);
        if (remainUsbInfos != null && remainUsbInfos.size() > 0) {
            if (remainUsbInfos.size() == 1) {
                UsbInfo usbInfo = remainUsbInfos.get(0);
                if (usbInfo != null && !TextUtils.isEmpty(usbInfo.uuid)) {
                    executeRecord(remainUsbInfos.get(0), callback);
                }
            } else {
                showSelectUsbDialog(remainUsbInfos, callback);
            }
        } else {
            callback.callback(UsbManager.USB_NOT_FOUND);
        }
    }

    private void executeRecord(UsbInfo usbInfo, OnCommCallback callback) {
        if (mWaitingStartRecordRunnable == null) {
            mWaitingStartRecordRunnable = new WaitingStartRecordRunnable(this, callback);
        } else {
            ThreadPoolManager.getInstance().remove(mWaitingStartRecordRunnable);
        }
        mWaitingStartRecordRunnable.usbInfo = usbInfo;
        if (usbInfo != null) DTVSettingManager.getInstance().setDiskUUID(usbInfo.uuid);
        ThreadPoolManager.getInstance().execute(mWaitingStartRecordRunnable);
    }

    private void stopRecord() {
        if (isRecording()) {
            DTVPVRManager.getInstance().stopRecord();

            cancelRecordingTimer();
            setRecordFlagStop();
            mRecordingLayout.setVisibility(View.GONE);
            mTvRecordingTime.setText("00:00:00");
            ToastUtils.showToast(R.string.toast_stop_record);

            showPfInfo();
        }
    }

    private void recordProg(int recordSeconds) {
        startRecord(new OnCommCallback() {
            @Override
            public void callback(Object object) {
                int recordFlag = (int) object;
                if (recordFlag == 0) {
                    DTVPVRManager.getInstance().setRecording(true);
                    startRecordingTimer(recordSeconds);
                    sendHideRecordTimeMsg(new HandlerMsgModel(ProgHandler.MSG_HIDE_RECORD_TIME, RECORD_TIME_HIDE_DELAY));
                    ToastUtils.showToast(R.string.toast_start_record);
                } else {
                    ToastUtils.showToast(R.string.toast_start_record_failed);
                }
            }
        });
    }

    private static class WaitingStartRecordRunnable extends WeakRunnable<Topmost> {
        private static final long MAX_TRY_RECORD_TIME = 5000;
        private OnCommCallback callback;
        private UsbInfo usbInfo;

        WaitingStartRecordRunnable(Topmost view, OnCommCallback callback) {
            super(view);
            this.callback = callback;
        }

        @Override
        protected void loadBackground() {
            int recordDelay = 0; // 首次设置delay为0
            int result;
            long tryStartRecordTime = 0;
            while (true) {
                if (usbInfo == null || TextUtils.isEmpty(usbInfo.path)) {
                    result = UsbManager.USB_NOT_FOUND;
                    break;
                }

                result = DTVPVRManager.getInstance().startRecord(recordDelay, usbInfo.path);

                if (result != -4) break;
                if (tryStartRecordTime >= MAX_TRY_RECORD_TIME) break;
                recordDelay = 1; // 启动录制失败设置为1

                try {
                    Thread.sleep(100);
                    tryStartRecordTime += 100;
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            final int startRecordResult = result;
            mWeakReference.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.callback(startRecordResult);
                    }
                }
            });
        }
    }

    private void startRecordingTimer(int recordSeconds) {
        cancelRecordingTimer();
        mRecordingTimer = new Timer();
        mRecordingTimerTask = new RecordingTimerTask(this, recordSeconds);
        mRecordingTimer.schedule(mRecordingTimerTask, RECORDING_DELAY, RECORDING_PERIOD);
    }

    private void cancelRecordingTimer() {
        if (mRecordingTimer != null) {
            mRecordingTimer.cancel();
            mRecordingTimer.purge();
            mRecordingTimerTask.release();
            mRecordingTimer = null;
            mRecordingTimerTask = null;
        }
    }

    private void setRecordFlagStop() {
        DTVBookingManager.getInstance().setRecording(false);
        DTVPVRManager.getInstance().setRecording(false);
    }

    private static class RecordingTimerTask extends WeakTimerTask<Topmost> {
        private int countDownSeconds;
        private int recordSeconds;

        RecordingTimerTask(Topmost view, int countDownSeconds) {
            super(view);
            this.countDownSeconds = countDownSeconds;
        }

        @Override
        protected void runTimer() {
            Topmost context = mWeakReference.get();

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (--countDownSeconds <= 0) {
                        context.stopRecord();
                    } else {
                        context.mTvRecordingTime.setText(TimeUtils.getDecimalFormatTime(++recordSeconds));
                    }
                }
            });
        }
    }

    private void bootService() {
        BookService.bootService(new Intent(this, BookService.class));
        PowerService.bootService(new Intent(this, PowerService.class));
    }

    private void registerListener() {
        EventBus.getDefault().register(this);
    }

    private void unregisterListener() {
        EventBus.getDefault().unregister(this);
    }

    private void initHandler() {
        mProgHandler = new ProgHandler(this);
        mPlayHandler = new PlayHandler(this);
        mJumpProgHandler = new JumpProgHandler(this);
    }

    /**
     * 显示台号
     */
    private void showProgNum(int progNum) {
        showProgNum(progNum, true);
    }

    /**
     * 显示台号
     *
     * @param sendMsgHide 是否需要发送msg在规定时间隐藏台号
     */
    private void showProgNum(int progNum, boolean sendMsgHide) {
        removeHideProgNumMsg();
        Log.i(TAG, "showNo:" + progNum);
        mTvProgNum.setText(String.valueOf(progNum));
        mTvProgNum.setVisibility(View.VISIBLE);
        if (sendMsgHide)
            sendProgMsg(new HandlerMsgModel(ProgHandler.MSG_HIDE_PROG_NUM, DTVSettingManager.getInstance().dismissTimeout()));
    }

    /**
     * 隐藏台号
     */
    private void hideProgNum() {
        sendProgMsg(new HandlerMsgModel(ProgHandler.MSG_HIDE_PROG_NUM));
    }

    private void initSatList() {
        mLoadSatRunnable = new LoadSatRunnable(this);
        ThreadPoolManager.getInstance().execute(mLoadSatRunnable);
    }

    /**
     * 频道列表
     */
    private void initProgList() {
        mLoadProgRunnable = new LoadProgRunnable(this);
        mProgListAdapter = new TvListAdapter(this, new ArrayList<>());
        mProgListView.setAdapter(mProgListAdapter);
        mProgListView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW && event.getAction() == KeyEvent.ACTION_UP) {
                    showFindProgChannelDialog();
                    return true;
                }
                return false;
            }
        });

        updateProgList();
    }

    /**
     * 更新频道列表
     */
    private void updateProgList() {
        if (mLoadProgRunnable != null) {
            mPbLoadingChannel.setVisibility(View.VISIBLE);
            ThreadPoolManager.getInstance().remove(mLoadProgRunnable);
            ThreadPoolManager.getInstance().execute(mLoadProgRunnable);
        }
    }

    private void updateProgListSelection(int progNum) {
        if (mProgListAdapter.getCount() > 0) {
            int scrollToPosition = getPositionByProgNum(progNum);
            mProgListView.setSelection(scrollToPosition);
            mProgListAdapter.setSelectPosition(scrollToPosition);
        }
    }

    private void updateProgListSelectionByPosotion(int scrollToPosition) {
        if (mProgListAdapter.getCount() > 0) {
            mProgListView.setSelection(scrollToPosition);
            mProgListAdapter.setSelectPosition(scrollToPosition);
        }
    }

    private static class LoadProgRunnable extends WeakRunnable<Topmost> {

        LoadProgRunnable(Topmost view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            Topmost context = mWeakReference.get();
            // 等待卫星列表获取完再获取频道列表
            while (context.mSatList == null) {
                Log.i(TAG, "waiting load satellite");
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            HProg_Struct_ProgInfo currProgInfo = DTVProgramManager.getInstance().getCurrProgInfo();
            if (currProgInfo == null) return;

            final int preProgIndex = DTVProgramManager.getInstance().getCurrProgInfo().ProgIndex;
            List<HProg_Struct_ProgInfo> progList = context.getProgList();
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    context.mPbLoadingChannel.setVisibility(View.GONE);
                    if (progList != null && !progList.isEmpty()) {
                        // 可能切换了频道类型，需要和当前最新的频道做对比
                        HProg_Struct_ProgInfo oldProgInfo = context.mProgListAdapter.getItem(context.mCurrSelectProgPosition);

                        context.mProgListAdapter.updateData(progList);

                        context.mCurrSelectProgPosition = context.getPositionByIndex(preProgIndex);
                        HProg_Struct_ProgInfo progInfo = context.mProgListAdapter.getItem(context.mCurrSelectProgPosition);
                        context.updateProgListSelectionByPosotion(context.mCurrSelectProgPosition);
                        DTVProgramManager.getInstance().setCurrProgNo(progInfo.ProgNo);

                        // 更新完成频道列表，如果播放的不是当前频道或者切换了频道类型则切换播放
                        if (progInfo.ProgIndex != preProgIndex ||
                                (oldProgInfo != null && oldProgInfo.ProgType != progInfo.ProgType)) {
                            context.playProg(progInfo.ProgNo);
                        }

                    } else {
                        context.mProgListAdapter.updateData(new ArrayList<>());
                    }
                }
            });

//            context.preloadProgList();
        }
    }

    private int getPositionByProgNum(int progNum) {
        if (mProgListAdapter.getCount() > 0) {
            for (int i = 0; i < mProgListAdapter.getData().size(); i++) {
                if (mProgListAdapter.getData().get(i).ProgNo == progNum) {
                    return i;
                }
            }
        }
        return 0;
    }

    private int getPositionByIndex(int progIndex) {
        if (mProgListAdapter.getCount() > 0) {
            for (int i = 0; i < mProgListAdapter.getData().size(); i++) {
                if (mProgListAdapter.getData().get(i).ProgIndex == progIndex) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * 获取卫星列表，如果有喜爱频道，还包含喜爱分组频道列表
     */
    private List<HProg_Struct_SatInfo> getSatList() {
        if (mSatList == null) {
            updateSatList();
        }
        return mSatList;
    }

    /**
     * 更新卫星列表
     */
    private void updateSatList() {
        if (mLoadSatRunnable != null) {
            ThreadPoolManager.getInstance().remove(mLoadSatRunnable);
            ThreadPoolManager.getInstance().execute(mLoadSatRunnable);
        }
    }

    private static class LoadSatRunnable extends WeakRunnable<Topmost> {

        LoadSatRunnable(Topmost view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            Topmost context = mWeakReference.get();

            List<HProg_Struct_SatInfo> allSatList = DTVProgramManager.getInstance().getAllSatListContainFav(context);
            if (allSatList != null && !allSatList.isEmpty()) {
                context.mSatList = new ArrayList<>(allSatList);
                context.mCurrSatPosition = 0;
                if (context.mCurrProgGroup == HProg_Enum_Group.WHOLE_GROUP) {
                    context.mCurrSatPosition = 0;
                } else {
                    int i = 0;
                    for (; i < allSatList.size(); i++) {
                        if (context.mCurrProgGroup == HProg_Enum_Group.SAT_GROUP && context.mCurrProgGroupParams == allSatList.get(i).SatIndex) {
                            context.mCurrSatPosition = i;
                            break;
                        } else if (context.mCurrProgGroup == HProg_Enum_Group.FAV_GROUP && context.mCurrProgGroupParams == (allSatList.get(i).SatIndex - DTVProgramManager.RANGE_SAT_INDEX)) {
                            context.mCurrSatPosition = i;
                            break;
                        }
                    }
                }
                Log.i(TAG, "mCurrSatPosition:" + context.mCurrSatPosition);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.mTvSatelliteName.setText(context.mSatList.get(context.mCurrSatPosition).sat_name);
                    }
                });
            } else {
                context.mSatList = new ArrayList<>();
            }
        }
    }

    private void initSurfaceView() {
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(TAG, "topmost surface create");
                DTVPlayerManager.getInstance().setSurface(holder.getSurface());
                DTVPlayerManager.getInstance().setWindowSize(0, 0,
                        getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
                if (!handleBook()) {
                    Log.i(TAG, "non handle book");
                    if (DTVProgramManager.getInstance().isProgCanPlay() && !DTVSettingManager.getInstance().isPasswordEmpty()) {
                        DTVProgramManager.getInstance().setCurrGroup(mCurrProgGroup, mCurrProgGroupParams);
                        if (DTVProgramManager.getInstance().getCurrProgNo() >= 0)
                            playProg(DTVProgramManager.getInstance().getCurrProgNo(), true);
                    }
                } else {
                    Log.i(TAG, "intent reset empty");
                    setIntent(new Intent()); // 处理完后要重置为空，防止其他界面返回或跳转到Topmost使用上一个book跳转的intent
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(TAG, "topmost surface destroy");
            }
        });
    }

    private void showSurface() {
        if (mSurfaceView.getVisibility() != View.VISIBLE) {
            mSurfaceView.setVisibility(View.VISIBLE);
        }
    }

    private void hideSurface() {
        if (mSurfaceView.getVisibility() != View.GONE) {
            mSurfaceView.setVisibility(View.GONE);
        }
    }

    private void initCheckSignal() {
        mCheckSignalHelper = new CheckSignalHelper(this);
        mCheckSignalHelper.setSignalRandom(false);
        mCheckSignalHelper.setOnCheckSignalListener((strength, quality) -> {
            if (strength <= 0 && quality <= 0) {
                if (mProgListAdapter != null && mProgListAdapter.getCount() > 0 && mSignalOk) {
                    mSignalOk = false;
                    mSurfaceView.setVisibility(View.GONE);
                }
            } else {
                if (!mSignalOk) {
                    mSignalOk = true;
                    mSurfaceView.setVisibility(View.VISIBLE);
                }
            }
        });
    }

    /**
     * 获取频道列表，如果有喜爱列表还包含喜爱分组的频道列表
     */
    private List<HProg_Struct_ProgInfo> getProgList() {
        List<HProg_Struct_SatInfo> satList = getSatList();
        if (satList != null && !satList.isEmpty()) {
            int satIndex = satList.get(mCurrSatPosition).SatIndex;
            List<HProg_Struct_ProgInfo> progInfoList = mProgListMap.get(satIndex);

            if (satIndex == -1) {
                mCurrProgGroup = HProg_Enum_Group.WHOLE_GROUP;
                mCurrProgGroupParams = 1;

            } else if (satIndex >= DTVProgramManager.RANGE_SAT_INDEX) {
                mCurrProgGroup = HProg_Enum_Group.FAV_GROUP;
                mCurrProgGroupParams = satIndex - DTVProgramManager.RANGE_SAT_INDEX;
            } else {
                mCurrProgGroup = HProg_Enum_Group.SAT_GROUP;
                mCurrProgGroupParams = satIndex;
            }
            DTVProgramManager.getInstance().setCurrGroup(mCurrProgGroup, mCurrProgGroupParams);
            if (progInfoList != null && !progInfoList.isEmpty()) {
                return progInfoList;
            }
            int[] index = new int[1];
            progInfoList = DTVProgramManager.getInstance().getCurrGroupProgInfoList(index);
            mProgListMap.put(satIndex, progInfoList);
            return progInfoList;
        }

        return null;
    }

    /**
     * 获取对应分组下的当前频道
     */
    private HProg_Struct_ProgInfo getCurrProgInfo() {
        List<HProg_Struct_SatInfo> satList = getSatList();
        if (satList != null && !satList.isEmpty()) {
            List<HProg_Struct_ProgInfo> progInfoList = mProgListMap.get(satList.get(mCurrSatPosition).SatIndex);
            if (progInfoList != null && !progInfoList.isEmpty() && mCurrSelectProgPosition < progInfoList.size()) {
                return progInfoList.get(mCurrSelectProgPosition);
            }
        }
        return null;
    }

    private HProg_Struct_ProgInfo getProgInfoByShowNum(int showNum) {
        List<HProg_Struct_SatInfo> satList = getSatList();
        if (satList != null && !satList.isEmpty()) {
            List<HProg_Struct_ProgInfo> progInfoList = mProgListMap.get(satList.get(mCurrSatPosition).SatIndex);
            if (progInfoList != null && !progInfoList.isEmpty()) {
                for (HProg_Struct_ProgInfo progInfo : progInfoList) {
                    if (progInfo.PShowNo == showNum) return progInfo;
                }
            }
        }
        return null;
    }

    /**
     * 获取在当前频道分组下当前频道的频道号
     * DTVProgramManager.getInstance().getCurrProgNo()是不区分频道分组的，在Topmost界面一般用来判断切换的频道号是否和当前相同，要和该方法有所区分
     */
    private int getCurrentProgNo() {
        HProg_Struct_ProgInfo currProgInfo = getCurrProgInfo();
        if (currProgInfo == null) {
            currProgInfo = new HProg_Struct_ProgInfo();
            currProgInfo.ProgNo = 0;
        }
        return currProgInfo.ProgNo;
    }

    /**
     * 获取在当前频道分组下当前频道的显示频道号
     */
    private int getCurrentProgShowNo() {
        HProg_Struct_ProgInfo currProgInfo = getCurrProgInfo();
        if (currProgInfo == null) {
            currProgInfo = new HProg_Struct_ProgInfo();
            currProgInfo.PShowNo = 1;
        }
        return currProgInfo.PShowNo;
    }

    /**
     * 获取在当前频道分组下第一个频道的频道号
     */
    private int getFirstProgNo() {
        if (mProgListAdapter.getCount() > 0) {
            return mProgListAdapter.getItem(0).ProgNo;
        }
        return 0;
    }

    /**
     * 获取在当前频道分组下最后一个频道的频道号
     */
    private int getLastProgNo() {
        if (mProgListAdapter.getCount() > 0) {
            return mProgListAdapter.getItem(mProgListAdapter.getCount() - 1).ProgNo;
        }
        return 0;
    }

    /**
     * 移除通知播放节目msg
     */
    private void removePlayProgMsg() {
        HandlerMsgManager.getInstance().removeMessage(mPlayHandler, PlayHandler.MSG_PLAY_PROG);
    }

    /**
     * 移除隐藏节目台号msg
     */
    private void removeHideProgNumMsg() {
        HandlerMsgManager.getInstance().removeMessage(mProgHandler, ProgHandler.MSG_HIDE_PROG_NUM);
    }

    /**
     * 移除跳台msg
     */
    private void removeJumpProgMsg() {
        HandlerMsgManager.getInstance().removeMessage(mJumpProgHandler, JumpProgHandler.MSG_JUMP_PROG);
    }

    /**
     * 移除隐藏pfbar msg
     */
    private void removeHidePfBarMsg() {
        HandlerMsgManager.getInstance().removeMessage(mProgHandler, ProgHandler.MSG_HIDE_PF_BAR);
    }

    /**
     * 移除隐藏当前录制时长显示msg
     */
    private void removeHideRecordTimeMsg() {
        HandlerMsgManager.getInstance().removeMessage(mProgHandler, ProgHandler.MSG_HIDE_RECORD_TIME);
    }

    /**
     * 台号、声道、pfbar msg通知显示隐藏
     */
    private void sendProgMsg(HandlerMsgModel progMsg) {
        HandlerMsgManager.getInstance().sendMessage(mProgHandler, progMsg);
    }

    /**
     * 切换播放节目msg通知
     */
    private void sendPlayProgMsg(HandlerMsgModel progMsg) {
        HandlerMsgManager.getInstance().sendMessage(mPlayHandler, progMsg);
    }

    /**
     * 直接跳转切台功能
     */
    private void sendJumpProgMsg(HandlerMsgModel progMsg) {
        HandlerMsgManager.getInstance().sendMessage(mJumpProgHandler, progMsg);
    }

    /**
     * 隐藏当前录制时长显示
     */
    private void sendHideRecordTimeMsg(HandlerMsgModel msg) {
        if (isRecording()) {
            removeHideRecordTimeMsg();
            mRecordingLayout.setVisibility(View.VISIBLE);
            HandlerMsgManager.getInstance().sendMessage(mProgHandler, msg);
        }
    }

    /**
     * 发送msg通知播放当前台号，同时显示相关节目信息，如台号、PF信息等
     */
    private void playProg() {
        playProg(getCurrentProgNo(), false);
    }

    /**
     * 发送msg通知播放设定台号
     */
    private void playProg(int progNum) {
        playProg(progNum, false);
    }

    private void playProg(int progNum, boolean immediately) {
        DTVPlayerManager.getInstance().stopPlay(DTVSettingManager.getInstance().getDTVProperty(HSetting_Enum_Property.PD_SwitchMode)); // 切台之前暂停当前频道播放
        DTVProgramManager.getInstance().setCurrProgNo(progNum);
        removePlayProgMsg();
        showPfInfo();
        showProgNum(DTVProgramManager.getInstance().getCurrProgInfo().PShowNo);
        showRadioBackground();
        sendPlayProgMsg(new HandlerMsgModel(PlayHandler.MSG_PLAY_PROG, progNum, immediately ? 0 : PLAY_PROG_DELAY));
    }

    /**
     * 加台播放
     */
    private void nextProg() {
        mCurrSelectProgPosition++;
        if (mCurrSelectProgPosition >= mProgListAdapter.getCount())
            mCurrSelectProgPosition = 0;
        playProg(mProgListAdapter.getItem(mCurrSelectProgPosition).ProgNo);
    }

    /**
     * 减台播放
     */
    private void lastProg() {
        mCurrSelectProgPosition--;
        if (mCurrSelectProgPosition < 0)
            mCurrSelectProgPosition = mProgListAdapter.getCount() - 1;
        playProg(mProgListAdapter.getItem(mCurrSelectProgPosition).ProgNo);
    }

    /**
     * 发送msg通知播放跳转台号
     */
    private void jumpPlayProg() {
        if (isJumpProgNumValid()) {
            removeJumpProgMsg();
            HProg_Struct_ProgInfo progInfo = getProgInfoByShowNum(Integer.valueOf(mJumpProgNumBuilder.toString()));
            if (progInfo != null) {
                sendJumpProgMsg(new HandlerMsgModel(JumpProgHandler.MSG_JUMP_PROG, progInfo.ProgNo));
            }
        } else {
            showProgNumInvalid(true);
        }
    }

    /**
     * 记录遥控器输入的切台号，延迟跳台
     */
    private void recordJumpPlayProgNum(int progNum) {
        removeJumpProgMsg();
        if (mJumpProgNumBuilder.toString().length() >= 4) {
            mJumpProgNumBuilder.delete(0, mJumpProgNumBuilder.length());
        }
        mJumpProgNumBuilder.append(progNum);
        int jumpProgShowNum = Integer.valueOf(mJumpProgNumBuilder.toString());
        showProgNum(jumpProgShowNum, true);
        if (jumpProgShowNum > 0) {
            HProg_Struct_ProgInfo progInfo = getProgInfoByShowNum(jumpProgShowNum);
            if (progInfo != null) {
                sendJumpProgMsg(new HandlerMsgModel(JumpProgHandler.MSG_JUMP_PROG, progInfo.ProgNo, JUMP_PROG_DELAY));
            }
        }
    }

    private boolean isJumpProgNumValid() {
        if (mJumpProgNumBuilder.length() > 0) {
            int jumpProgShowNum = Integer.valueOf(mJumpProgNumBuilder.toString());
            if (jumpProgShowNum > 0) {
                HProg_Struct_ProgInfo progInfo = getProgInfoByShowNum(jumpProgShowNum);
                if (progInfo != null) {
                    return progInfo.ProgNo <= getLastProgNo();
                }
            }
        }
        return false;
    }

    private void showProgNumInvalid(boolean invalid) {
        hideProgNum();
        dismissPfBarScanDialog();
        mJumpProgNumBuilder.delete(0, mJumpProgNumBuilder.length());
        if (invalid)
            ToastUtils.showToast(R.string.toast_program_number_invalid);
    }

    private void showRadioBackground() {
        mIvRadioBackground.setVisibility(DTVProgramManager.getInstance().getCurrProgType() == HProg_Enum_Type.GBPROG ? View.VISIBLE : View.INVISIBLE);
    }

    private void checkLaunchSettingPassword() {
        if (DTVSettingManager.getInstance().isPasswordEmpty()) {
            showSettingPasswordDialog();
        }
    }

    private void showPfBarScanDialog() {
        if (mPfBarScanDialog == null) {
            mPfBarScanDialog = new PfBarScanDialog(this);
        }
        updatePfBarInfo();
        if (!isPfBarShowing()) {
            mPfBarScanDialog.show();
        }
    }

    private void showPfDetailDialog() {
        if (isPfDetailShowing()) {
            dismissPfDetailDialog();
            return;
        }

        HProg_Struct_ProgInfo currProgInfo = getCurrProgInfo();
        if (currProgInfo != null) {
            HProg_Struct_SatInfo satInfo = DTVProgramManager.getInstance().getSatInfo(currProgInfo.Sat);
            int[] pids = DTVProgramManager.getInstance().getServicePID(currProgInfo.ProgNo);
            HProg_Struct_TP channelInfo = DTVProgramManager.getInstance().getTPInfoBySat(currProgInfo.Sat, currProgInfo.ProgIndex);
            mPfDetailDialog = new PfDetailDialog()
                    .information("")
                    .satelliteName(satInfo != null ? satInfo.sat_name : "")
                    .channelName(currProgInfo.Name)
                    .vpid(pids != null && pids.length == 3 ? String.valueOf(pids[0]) : "0")
                    .apid(pids != null && pids.length == 3 ? String.valueOf(pids[1]) : "0")
                    .ppid(pids != null && pids.length == 3 ? String.valueOf(pids[2]) : "0")
                    .freq(String.valueOf(currProgInfo.Freq))
                    .symbol(String.valueOf(currProgInfo.Symbol))
                    .pol(Utils.getVorH(this, channelInfo != null ? channelInfo.Qam : 0));
            mPfDetailDialog.show(getSupportFragmentManager(), PfDetailDialog.TAG);
        }
    }

    private void updatePfBarInfo() {
        if (mPfBarScanDialog != null) {
            mPfBarScanDialog.updatePfInformation();
        }
    }

    private void showSearchChannelDialog() {
        if (mSearchChannelDialog == null) {
            mSearchChannelDialog = new SearchChannelDialog()
                    .setOnSearchChannelListener(new SearchChannelDialog.OnSearchListener() {
                        @Override
                        public void onBackSearch() {
                            mItemInstallation.requestFocus();
                            toggleMenu(true);
                        }

                        @Override
                        public void onStartSearch() {
                            showS2OrT2Dialog(Constants.DVBSelectType.SEARCH, false);
                        }

                        @Override
                        public void onExitSearch() {
                            finish();
                        }
                    });
        }

        mSearchChannelDialog.show(getSupportFragmentManager(), SearchChannelDialog.TAG);
    }

    private void showRemindSearchDialog() {
        new CommRemindDialog()
                .content(getString(R.string.no_program))
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        mItemInstallation.requestFocus();
                        showS2OrT2Dialog(Constants.DVBSelectType.INSTALLATION, true);
                    }
                }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
    }

    private void showPasswordDialog() {
        if (mPasswordDialog != null && mPasswordDialog.getDialog() != null && mPasswordDialog.isVisible())
            return;

        getContentResolver().update(Uri.parse("content://dvbchannellock/dvb_info/0"), null, null, null);
        showPasswordDialog(new PasswordDialog.OnPasswordInputListener() {
            @Override
            public void onPasswordInput(String inputPassword, String currentPassword, boolean isValid) {
                if (isValid) {
                    Uri uri = Uri.parse("content://dvbchannellock/dvb_info/1");
                    getContentResolver().update(uri, null, null, null);
                    DTVPlayerManager.getInstance().startPlayProgNo(DTVProgramManager.getInstance().getCurrProgNo(), 0);
                }
            }
        }, new PasswordDialog.OnControlArrowKeyListener() {
            @Override
            public void onControlArrowKey(int playProgType) {
                switch (playProgType) {
                    case PasswordDialog.CONTROL_ARROW_LAST_PROG:
                        lastProg();
                        break;
                    case PasswordDialog.CONTROL_ARROW_NEXT_PROG:
                        nextProg();
                        break;
                }
            }
        });
    }

    private void showPasswordDialog(PasswordDialog.OnPasswordInputListener passwordInputListener) {
        showPasswordDialog(passwordInputListener, null);
    }

    private void showPasswordDialog(PasswordDialog.OnPasswordInputListener passwordInputListener,
                                    PasswordDialog.OnControlArrowKeyListener controlArrowKeyListener) {
        mPasswordDialog = new PasswordDialog();
        // 每次创建对话框都需要重置一次listener，防止一直持有上一个listener导致回调错误
        mPasswordDialog.setOnPasswordInputListener(passwordInputListener);
        mPasswordDialog.setOnControlArrowKeyListener(controlArrowKeyListener);
        if (!mPasswordDialog.isAdded() && !mPasswordDialog.isVisible() && !mPasswordDialog.isRemoving()) {
            mPasswordDialog.show(getSupportFragmentManager(), PasswordDialog.TAG);
        }
    }

    private void showS2OrT2Dialog(@DVBSelectType int selectType, boolean agreeBack) {
        List<String> content = new ArrayList<>();
        content.add(getString(R.string.installation_s2));
        content.add(getString(R.string.installation_t2));

        mProgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new CommCheckItemDialog()
                        .title(getString(R.string.dialog_title_tips))
                        .content(content)
                        .agreeBack(agreeBack)
                        .position(0)
                        .setOnDismissListener(new CommCheckItemDialog.OnDismissListener() {
                            @Override
                            public void onDismiss(CommCheckItemDialog dialog, int position, String checkContent) {
                                if (selectType == Constants.DVBSelectType.INSTALLATION) {
                                    if (position == 0) {
                                        toggleInstallationItem();
                                    } else {
                                        startActivity(new Intent(Topmost.this, InstallationT2Activity.class));
                                    }
                                } else if (selectType == Constants.DVBSelectType.SEARCH) {
                                    if (position == 0) {
                                        startActivity(new Intent(Topmost.this, SatelliteActivity.class));
                                    } else {
                                        startActivity(new Intent(Topmost.this, InstallationT2Activity.class));
                                    }
                                }
                            }
                        }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
            }
        }, 100);
    }

    private void showClearChannelDialog() {
        new CommTipsDialog().title(getString(R.string.clear_channel)).content(getString(R.string.are_you_sure_clear_prog_data))
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        DTVPlayerManager.getInstance().programReset();
                        mCurrSatPosition = 0;
                        mProgListAdapter.clearData(); // 同步清空频道列表
                        mIvRadioBackground.setVisibility(View.GONE); // 隐藏音频背景
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private void showDataResetDialog() {
        new CommTipsDialog().title(getString(R.string.data_reset)).content(getString(R.string.factory_reset_content))
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        PreferenceManager.getInstance().clear();
                        DTVCommonManager.getInstance().factoryReset();
                        mProgListAdapter.clearData(); // 同步清空频道列表
                        toggleMenu(true);
                        mIvRadioBackground.setVisibility(View.GONE); // 隐藏音频背景
                        showSettingPasswordDialog();
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private void showFindProgChannelDialog() {
        if (mProgListAdapter.getCount() <= 0) return;

        new FindChannelDialog()
                .channels(mProgListAdapter.getData())
                .setOnFindChannelCallback(new FindChannelDialog.OnFindChannelCallback() {
                    @Override
                    public void onFindChannels(HProg_Struct_ProgInfo findChannel) {
                        if (findChannel == null) return;

                        if (findChannel.ProgNo != getCurrentProgNo()) playProg(findChannel.ProgNo);
                        toggleProgList();
                        updateProgListSelection(findChannel.ProgNo);
                    }
                }).show(getSupportFragmentManager(), FindChannelDialog.TAG);
    }

    private void showExitDialog() {
        new CommTipsDialog()
                .title(getString(R.string.dialog_title_tips))
                .content(getString(R.string.exit_app_content))
                .negativeFocus(true)
                .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        dismissSettingPasswordDialog();
                        hideSurface();
                        finish();
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private void showSubtitleDialog() {
        HProg_Struct_ProgInfo progInfo = getCurrProgInfo();
        if (!DTVProgramManager.getInstance().isProgCanPlay() || progInfo == null) return;

        int currSubtitle = 0;
        int serviceid = progInfo.ServID;
        int num = DTVPlayerManager.getInstance().getSubtitleNum(serviceid);
        final int[] pids = new int[num];
        List<HashMap<String, Object>> subtitles = new ArrayList<>();
        HashMap<String, Object> off = new HashMap<>();
        off.put(Constants.SUBTITLE_NAME, "OFF");
        subtitles.add(off);
        for (int index = 0; index < num; index++) {
            HPlayer_Struct_Subtitle subtitle = DTVPlayerManager.getInstance().getSubtitleInfo(serviceid, index);
            if (subtitle.used != 0) {
                pids[index] = subtitle.Pid;
                HashMap<String, Object> map = new HashMap<>();
                map.put(Constants.SUBTITLE_NAME, subtitle.Name);
                map.put(Constants.SUBTITLE_ORG_TYPE, subtitle.OrgType == 0);
                map.put(Constants.SUBTITLE_TYPE, (subtitle.Type >= 0x20 && subtitle.Type <= 0x24) || subtitle.Type == 0x05);
                subtitles.add(map);
                if (DTVPlayerManager.getInstance().getCurSubtitleInfo(serviceid).Name.equals(subtitle.Name))
                    currSubtitle = index;
            }
        }

        new SubtitleDialog()
                .title(getString(R.string.subtitle))
                .content(subtitles)
                .position(currSubtitle)
                .setOnDismissListener(new SubtitleDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(SubtitleDialog dialog, int position, String checkContent) {
                        if (position > 0)
                            DTVPlayerManager.getInstance().openSubtitle(pids[position - 1]);
                    }
                }).show(getSupportFragmentManager(), SubtitleDialog.TAG);
    }

    private void showTeletextDialog() {
        HProg_Struct_ProgInfo progInfo = getCurrProgInfo();
        if (!DTVProgramManager.getInstance().isProgCanPlay() || progInfo == null) return;

        int currTeleText = 0;
        int serviceid = progInfo.ServID;
        int num = DTVPlayerManager.getInstance().getTeletextNum(serviceid);
        final int[] pids = new int[num];
        String[] teletextNames = new String[num + 1];
        teletextNames[0] = "OFF";
        for (int index = 0; index < num; index++) {
            HPlayer_Struct_Teletext teletext = DTVPlayerManager.getInstance().getTeletextInfo(serviceid, index);
            if (teletext.used != 0) {
                teletextNames[index + 1] = teletext.Name;
                pids[index] = teletext.Pid;
            }
        }

        new TeletextDialog()
                .title(getString(R.string.teletext))
                .content(Arrays.asList(teletextNames))
                .position(currTeleText)
                .setOnDismissListener(new CommCheckItemDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(CommCheckItemDialog dialog, int position, String checkContent) {
                        if (position > 0)
                            DTVPlayerManager.getInstance().openTeletext(pids[position - 1]);
                    }
                }).show(getSupportFragmentManager(), "teletext");
    }

    private void showAudioDialog() {
        if (!DTVProgramManager.getInstance().isProgCanPlay()) return;

        new AudioDialog().title(getString(R.string.audio)).where(AudioDialog.WHERE_TOPMOST).show(getSupportFragmentManager(), AudioDialog.TAG);
    }

    private void showSettingPasswordDialog() {
        mSettingPasswordDialog = new InitPasswordDialog().setOnSavePasswordListener(new InitPasswordDialog.OnSavePasswordListener() {
            @Override
            public void onSavePassword(String password) {
                DTVSettingManager.getInstance().setPasswd(HSetting_Enum_Property.Password, password);
                DTVSettingManager.getInstance().setDTVProperty(HSetting_Enum_Property.FirstOpen, 0);

                dismissSettingPasswordDialog();

                if (!DTVProgramManager.getInstance().isProgCanPlay()) {
                    showSearchChannelDialog();
                } else {
                    playProg();
                }
            }
        }).setOnKeyListener(new InitPasswordDialog.OnKeyListener() {
            @Override
            public boolean onKeyListener(InitPasswordDialog dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
                    showExitDialog();
                    return true;
                }
                return false;
            }
        });
        mSettingPasswordDialog.show(getSupportFragmentManager(), InitPasswordDialog.TAG);
    }

    private void showInputPvrMinuteDialog(@PVRType int pvrType) {
        new InputPvrMinuteDialog()
                .setOnInputPVRContentCallback(new OnCommCallback() {
                    @Override
                    public void callback(Object object) {
                        int minutes = (int) object;
                        switch (pvrType) {
                            case Constants.PVRType.RECORD:
                                recordProg(minutes * 60);
                                break;
                            case Constants.PVRType.TIMESHIFT:
                                Intent intent = new Intent();
                                intent.setClass(Topmost.this, RecordPlayer.class);
                                intent.putExtra(Constants.IntentKey.INTENT_TIMESHIFT_RECORD_FROM, RecordPlayer.FROM_TOPMOST);
                                intent.putExtra(Constants.IntentKey.INTENT_TIMESHIFT_TIME, minutes);
                                startActivity(intent);
                                break;
                        }
                    }
                }).show(getSupportFragmentManager(), InputPvrMinuteDialog.TAG);
    }

    private void showQuitRecordDialog(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            new CommTipsDialog()
                    .content(getString(R.string.dialog_quit_record_content))
                    .negativeFocus(true)
                    .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                        @Override
                        public void onPositiveListener() {
                            stopRecord();
                        }
                    }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
        }
    }

    private void showSelectUsbDialog(List<UsbInfo> usbInfos, OnCommCallback callback) {
        List<String> usbInfoNames = new ArrayList<>();
        for (UsbInfo usbInfo : usbInfos) usbInfoNames.add(usbInfo.fsLabel);

        // 添加一个延时，防止无法弹出问题
        mProgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                new CommCheckItemDialog()
                        .title(getString(R.string.dialog_usb_select_title))
                        .content(usbInfoNames)
                        .position(0)
                        .setOnDismissListener(new CommCheckItemDialog.OnDismissListener() {
                            @Override
                            public void onDismiss(CommCheckItemDialog dialog, int position, String checkContent) {
                                UsbInfo usbInfo = usbInfos.get(position);
                                if (usbInfo != null && !TextUtils.isEmpty(usbInfo.uuid)) {
                                    executeRecord(usbInfos.get(position), callback);
                                }
                            }
                        }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
            }
        }, 200);
    }

    private boolean isPfBarShowing() {
        return mPfBarScanDialog != null && mPfBarScanDialog.isShowing();
    }

    private boolean isPfDetailShowing() {
        return mPfDetailDialog != null && mPfDetailDialog.getDialog() != null;
    }

    private void dismissAllDialog() {
        dismissPfBarScanDialog();

        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (!fragments.isEmpty()) {
            for (Fragment fragment : fragments) {
                if (fragment instanceof BaseDialogFragment) {
                    ((BaseDialogFragment) fragment).dismiss();
                }
            }
        }
    }

    private void dismissPfBarScanDialog() {
        if (isPfBarShowing()) {
            mPfBarScanDialog.dismiss();
            mPfBarScanDialog = null;
        }
    }

    private void dismissPfDetailDialog() {
        if (isPfDetailShowing()) {
            mPfDetailDialog.dismiss();
            mPfDetailDialog = null;
        }
    }

    private void dismissPasswordDialog() {
        if (mPasswordDialog != null && mPasswordDialog.getDialog() != null && mPasswordDialog.isVisible()) {
            mPasswordDialog.dismiss();
            mPasswordDialog = null;
        }
    }

    private void dismissSettingPasswordDialog() {
        if (mSettingPasswordDialog != null && mSettingPasswordDialog.getDialog() != null && mSettingPasswordDialog.isVisible()) {
            mSettingPasswordDialog.dismiss();
            mSettingPasswordDialog = null;
        }
    }

    private void toggleProgList() {
        toggleProgList(true);
    }

    private void toggleProgList(boolean isQuitShowPf) {
        startTranslateAnimation(mProgListShow ? -mProgListMenu.getLeft() : 0f,
                mProgListShow ? 0f : -mProgListMenu.getLeft(), mProgListMenu, null);
        mProgListShow = !mProgListShow;
        if (!mProgListShow && isQuitShowPf) {
            mProgHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    showPfInfo();
                }
            }, 300);
        }
    }

    private void toggleMenu() {
        toggleMenu(false);
    }

    private void toggleMenu(boolean isStop) {
        if (mMenuShow) mPasswordEntered = false;
        startTranslateAnimation(mMenuShow ? -mMenu.getLeft() : 0f,
                mMenuShow ? 0f : -mMenu.getLeft(), mMenu, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mMenuShow) restoreMenuItem(); // menu隐藏动画执行结束，恢复menu的item显示
                        mMenuShow = !mMenuShow;

                        if (!mMenuShow) {
                            if (mProgListAdapter.getCount() > 0) {
                                showPfInfo();
                            } else {
                                if (!isStop) showSearchChannelDialog();
                            }
                        }
                    }
                });
    }

    private void startTranslateAnimation(float start, float end, Object target, AnimatorListenerAdapter listener) {
        @SuppressLint("ObjectAnimatorBinding") ObjectAnimator animator = ObjectAnimator.ofFloat(target, "translationX", start, end);
        if (listener != null) animator.addListener(listener);
        animator.setDuration(300);
        animator.start();
    }

    public void showPfInfo() {
        if (!mProgListShow) {

            showPfBarScanDialog();

            // 当前处于录制，显示录制时长
            sendHideRecordTimeMsg(new HandlerMsgModel(ProgHandler.MSG_HIDE_RECORD_TIME, RECORD_TIME_HIDE_DELAY));

            if (!mLongPressed) {
                removeHidePfBarMsg();
                sendProgMsg(new HandlerMsgModel(ProgHandler.MSG_HIDE_PF_BAR, DTVSettingManager.getInstance().dismissTimeout()));
            }
        }
    }

    private void toggleInstallationItem() {
        boolean isShowInstallation = isShowInstallation();
        mIvInstallationBack.setVisibility(isShowInstallation ? View.VISIBLE : View.GONE);
        mTvInstallation.setText(getString(isShowInstallation ? R.string.back : R.string.Installation));
        mItemManualInstallation.setVisibility(isShowInstallation ? View.VISIBLE : View.GONE);
        mItemBlindScan.setVisibility(isShowInstallation ? View.VISIBLE : View.GONE);
        mItemEpg.setVisibility(isShowInstallation ? View.GONE : View.VISIBLE);
        mItemChannelManage.setVisibility(isShowInstallation ? View.GONE : View.VISIBLE);
        mItemDtvSetting.setVisibility(isShowInstallation ? View.GONE : View.VISIBLE);
        mItemDataReset.setVisibility(isShowInstallation ? View.GONE : View.VISIBLE);
    }

    private void toggleChannelManageItem() {
        boolean isShowChannelManage = isShowChannelManageItem();
        mItemInstallation.setVisibility(isShowChannelManage ? View.GONE : View.VISIBLE);
        mItemEpg.setVisibility(isShowChannelManage ? View.GONE : View.VISIBLE);
        mIvChannelManageBack.setVisibility(isShowChannelManage ? View.VISIBLE : View.GONE);
        mTvChannelManage.setText(getString(isShowChannelManage ? R.string.back : R.string.Channel_management));
        mItemChannelEdit.setVisibility(isShowChannelManage ? View.VISIBLE : View.GONE);
        mItemChannelFavorite.setVisibility(isShowChannelManage ? View.VISIBLE : View.GONE);
        mItemClearChannel.setVisibility(isShowChannelManage ? View.VISIBLE : View.GONE);
        mItemRestoreUserData.setVisibility(isShowChannelManage && DTVProgramManager.getInstance().isProgCanPlay() ? View.VISIBLE : View.GONE);
        mItemBackupUserData.setVisibility(isShowChannelManage ? View.VISIBLE : View.GONE);
        mItemDtvSetting.setVisibility(isShowChannelManage ? View.GONE : View.VISIBLE);
        mItemDataReset.setVisibility(isShowChannelManage ? View.GONE : View.VISIBLE);
    }

    private boolean isShowInstallation() {
        return mIvInstallationBack.getVisibility() == View.GONE;
    }

    private boolean isShowChannelManageItem() {
        return mIvChannelManageBack.getVisibility() == View.GONE;
    }

    private void restoreMenuItem() {
        mItemInstallation.setVisibility(View.VISIBLE);
        mIvInstallationBack.setVisibility(View.GONE);
        mTvInstallation.setText(getString(R.string.Installation));
        mItemManualInstallation.setVisibility(View.GONE);
        mItemBlindScan.setVisibility(View.GONE);
        mItemEpg.setVisibility(View.VISIBLE);
        mItemChannelManage.setVisibility(View.VISIBLE);
        mIvChannelManageBack.setVisibility(View.GONE);
        mTvChannelManage.setText(getString(R.string.Channel_management));
        mItemChannelEdit.setVisibility(View.GONE);
        mItemChannelFavorite.setVisibility(View.GONE);
        mItemClearChannel.setVisibility(View.GONE);
        mItemRestoreUserData.setVisibility(View.GONE);
        mItemBackupUserData.setVisibility(View.GONE);
        mItemDtvSetting.setVisibility(View.VISIBLE);
        mItemDataReset.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // EPG
        if (keyCode == KeyEvent.KEYCODE_F1) {
            gotoEpg();
            return true;
        }

        // TEXT
        if (keyCode == KeyEvent.KEYCODE_TV_TELETEXT) {
            showTeletextDialog();
            return true;
        }

        // SUB
        // keycode=293 can't be caught, so use F3.
        if (keyCode == KeyEvent.KEYCODE_F3) {
            showSubtitleDialog();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK) {
            showAudioDialog();
            return true;
        }

        // PAUSE
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            if (isUsbNotExit()) {
                ToastUtils.showToast(R.string.toast_no_storage_device);
                return true;
            }

//            showInputPvrMinuteDialog(Constants.TIMESHIFT);
            Intent intent = new Intent();
            intent.setClass(Topmost.this, RecordPlayer.class);
            intent.putExtra(Constants.IntentKey.INTENT_TIMESHIFT_RECORD_FROM, RecordPlayer.FROM_TOPMOST);
            intent.putExtra(Constants.IntentKey.INTENT_TIMESHIFT_PROGNUM, mTvProgNum.getText().toString());
            startActivity(intent);
            return true;
        }

        // STOP
        if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            stopRecord();
            return true;
        }

        // SHIFT
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            if (isUsbNotExit()) {
                ToastUtils.showToast(R.string.toast_no_storage_device);
                return true;
            }

//            showInputPvrMinuteDialog(Constants.TIMESHIFT);
            Intent intent = new Intent();
            intent.setClass(Topmost.this, RecordPlayer.class);
            intent.putExtra(Constants.IntentKey.INTENT_TIMESHIFT_RECORD_FROM, RecordPlayer.FROM_TOPMOST);
            intent.putExtra(Constants.IntentKey.INTENT_TIMESHIFT_PROGNUM, mTvProgNum.getText().toString());
            startActivity(intent);
            return true;
        }

        // INFO
        if (keyCode == KeyEvent.KEYCODE_INFO) {
            if (isPfBarShowing()) {
                showPfDetailDialog();
            } else {
                showPfInfo();
            }
            return true;
        }

        // REWIND
        if (keyCode == KeyEvent.KEYCODE_MEDIA_REWIND) {
            return true;
        }

        // FORWARD
        if (keyCode == KeyEvent.KEYCODE_FORWARD) {
            return true;
        }

        // RECORD
        if (keyCode == KeyEvent.KEYCODE_MEDIA_RECORD) {
            if (isUsbNotExit()) {
                ToastUtils.showToast(R.string.toast_no_storage_device);
                return true;
            }

            if (DTVBookingManager.getInstance().isRecording()) {
                showQuitRecordDialog(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_UNKNOWN));
            } else {
                if (mProgListAdapter.getCount() > 0) {
                    int recordMinutes = DTVSettingManager.getInstance().getDTVProperty(HSetting_Enum_Property.RecordMaxMin);
                    if (recordMinutes == 0) {
                        showInputPvrMinuteDialog(Constants.PVRType.RECORD);
                    } else {
                        recordProg(recordMinutes >= Integer.MAX_VALUE ? Integer.MAX_VALUE : recordMinutes * 60);
                    }
                }
            }
            return true;
        }

        // TV/RADIO
        if (keyCode == KeyEvent.KEYCODE_TV_RADIO_SERVICE) {
            stopRecord();
            int currProgType = DTVProgramManager.getInstance().getCurrProgType();
            int group = currProgType == HProg_Enum_Type.GBPROG ? HProg_Enum_Type.TVPROG : HProg_Enum_Type.GBPROG;
            int num = DTVProgramManager.getInstance().getProgNumOfType(group, 0);
            if (num > 0) {
                DTVProgramManager.getInstance().setCurrProgType(currProgType == HProg_Enum_Type.GBPROG ?
                        HProg_Enum_Type.TVPROG: HProg_Enum_Type.GBPROG, 0);
                onProgramUpdate(new ProgramUpdateEvent(true));
            } else {
                ToastUtils.showToast(group == HProg_Enum_Type.GBPROG ? R.string.toast_no_radio : R.string.toast_no_tv);
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            dismissPfBarScanDialog();
            if (!mMenuShow) {
                if (mProgListShow) toggleProgList(false); // 隐藏正在显示的频道列表
                mItemInstallation.requestFocus();
                toggleMenu();
            }
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_0) {
            recordJumpPlayProgNum(0);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_1) {
            recordJumpPlayProgNum(1);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_2) {
            recordJumpPlayProgNum(2);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_3) {
            recordJumpPlayProgNum(3);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_4) {
            recordJumpPlayProgNum(4);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_5) {
            recordJumpPlayProgNum(5);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_6) {
            recordJumpPlayProgNum(6);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_7) {
            recordJumpPlayProgNum(7);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_8) {
            recordJumpPlayProgNum(8);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_9) {
            recordJumpPlayProgNum(9);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            if (mMenuShow) {
                toggleMenu();
                return true;
            }
            if (mProgListShow) {
                toggleProgList();
                return true;
            }
            if (isPfBarShowing()) {
                mTvProgNum.setVisibility(View.INVISIBLE);
                dismissPfBarScanDialog();
                return true;
            } else {
                showExitDialog();
                return super.onKeyDown(keyCode, event);
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 没有节目，不处理节目切换
        if (mProgListAdapter.getCount() <= 0 && !mMenuShow) {
            if (unInterceptEventWhenProgEmpty(event)) {
                return super.dispatchKeyEvent(event);
            }
            return true;
        }

        // 如果处于booking录制状态，拦截提示退出
        if (interceptEventWhenRecord(event)) {
            if (isPfBarShowing()) {
                dismissPfBarScanDialog();
            } else {
                sendHideRecordTimeMsg(new HandlerMsgModel(ProgHandler.MSG_HIDE_RECORD_TIME, RECORD_TIME_HIDE_DELAY));
                showQuitRecordDialog(event);
            }
            return true;
        }

        // 频道列表上下左右切换
        if (mProgListShow && mProgListAdapter.getCount() > 0) {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN
                    && event.getAction() == KeyEvent.ACTION_DOWN
                    && ++mCurrSelectProgPosition >= mProgListAdapter.getCount()) {
                mProgListView.setSelection(0);
                return true;
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP
                    && event.getAction() == KeyEvent.ACTION_DOWN
                    && --mCurrSelectProgPosition <= -1) {
                mProgListView.setSelection(mProgListAdapter.getCount() - 1);
                return true;
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                List<HProg_Struct_SatInfo> satList = getSatList();
                if (satList == null || satList.isEmpty()) return super.dispatchKeyEvent(event);

                if (--mCurrSatPosition < 0) mCurrSatPosition = satList.size() - 1;
                mTvSatelliteName.setText(satList.get(mCurrSatPosition).sat_name);
                updateProgList();
                return true;
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                List<HProg_Struct_SatInfo> satList = getSatList();
                if (satList == null || satList.isEmpty()) return super.dispatchKeyEvent(event);

                if (++mCurrSatPosition >= satList.size()) mCurrSatPosition = 0;
                mTvSatelliteName.setText(satList.get(mCurrSatPosition).sat_name);
                updateProgList();
                return true;
            }

            return super.dispatchKeyEvent(event);
        }

        // menu显示
        if (mMenuShow && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (mItemDataReset.isFocused()) {
                    mItemInstallation.requestFocus();
                    return true;
                }
                if (mItemBlindScan.isFocused()) {
                    mItemInstallation.requestFocus();
                    return true;
                }
                if (mItemBackupUserData.isFocused()) {
                    mItemChannelManage.requestFocus();
                    return true;
                }
            }
            return super.dispatchKeyEvent(event);
        }

        // menu显示
        if (mMenuShow && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (mItemInstallation.isFocused() && mIvInstallationBack.getVisibility() == View.GONE) {
                    mItemDataReset.requestFocus();
                    return true;
                }
                if (mItemInstallation.isFocused() && mIvInstallationBack.getVisibility() == View.VISIBLE) {
                    mItemBlindScan.requestFocus();
                    return true;
                }
                if (mItemChannelManage.isFocused() && mIvChannelManageBack.getVisibility() == View.VISIBLE) {
                    mItemBackupUserData.requestFocus();
                    return true;
                }
            }
            return super.dispatchKeyEvent(event);
        }

        if (mMenuShow && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            return true;
        }

        if (mMenuShow && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            return true;
        }

        // menu显示，焦点在menu上不处理
        if (mMenuShow && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)) {
            return super.dispatchKeyEvent(event);
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (isJumpProgNumValid()) {
                jumpPlayProg();
            } else {
                if (mJumpProgNumBuilder.length() > 0) {
                    showProgNumInvalid(true);
                } else {
                    dismissPfBarScanDialog();
                    // 频道列表有数据时才弹出
                    if (!mProgListShow && mProgListAdapter.getCount() > 0) {
                        mProgListView.requestFocus();
                        updateProgListSelectionByPosotion(mCurrSelectProgPosition);
                        toggleProgList();
                    } else {
                        return super.dispatchKeyEvent(event);
                    }
                }
            }
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN || event.getKeyCode() == KeyEvent.KEYCODE_CHANNEL_DOWN) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    if (event.getRepeatCount() == 0) {
                        mLongPressed = false;
                        event.startTracking();
                        mNewProgNum = getCurrentProgNo() - 1;
                        mLongPressDelayTime = System.currentTimeMillis();
                    } else {
                        mLongPressed = true;
                        if (System.currentTimeMillis() - mLongPressDelayTime > 200) {
                            removePlayProgMsg();
                            mLongPressDelayTime = System.currentTimeMillis();
                            if (--mNewProgNum < getFirstProgNo()) mNewProgNum = getLastProgNo();
                            int position = getPositionByProgNum(mNewProgNum);
                            if (position >= 0) {
                                showProgNum(mProgListAdapter.getItem(position).PShowNo, !mLongPressed);
                            }
                        }
                    }
                    break;

                case KeyEvent.ACTION_UP:
                    if (mProgListShow) {
                        return super.dispatchKeyEvent(event);
                    } else {
                        if (mNewProgNum != DTVProgramManager.getInstance().getCurrProgNo()) {
                            if (mLongPressed) {
                                mLongPressed = false;
                                int position = getPositionByProgNum(mNewProgNum);
                                if (position >= 0) {
                                    playProg(mProgListAdapter.getItem(position).ProgNo);
                                }
                            } else {
                                lastProg();
                            }
                        }
                        mLongPressDelayTime = 0;
                        mNewProgNum = 0;
                    }
                    break;
            }

            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_CHANNEL_UP) {
            switch (event.getAction()) {
                case KeyEvent.ACTION_DOWN:
                    if (event.getRepeatCount() == 0) {
                        mLongPressed = false;
                        event.startTracking();
                        mNewProgNum = getCurrentProgNo() + 1;
                        mLongPressDelayTime = System.currentTimeMillis();
                    } else {
                        mLongPressed = true;
                        if (System.currentTimeMillis() - mLongPressDelayTime > 200) {
                            removePlayProgMsg();
                            mLongPressDelayTime = System.currentTimeMillis();
                            if (++mNewProgNum > getLastProgNo()) mNewProgNum = getFirstProgNo();
                            int position = getPositionByProgNum(mNewProgNum);
                            if (position >= 0) {
                                showProgNum(mProgListAdapter.getItem(position).PShowNo, !mLongPressed);
                            }
                        }
                    }
                    break;

                case KeyEvent.ACTION_UP:
                    if (mProgListShow) {
                        return super.dispatchKeyEvent(event);
                    } else {
                        if (mNewProgNum != DTVProgramManager.getInstance().getCurrProgNo()) {
                            if (mLongPressed) {
                                mLongPressed = false;
                                int position = getPositionByProgNum(mNewProgNum);
                                if (position >= 0) {
                                    playProg(mProgListAdapter.getItem(position).ProgNo);
                                }
                            } else {
                                nextProg();
                            }
                        }
                    }

                    mNewProgNum = 0;
                    mLongPressDelayTime = 0;
                    break;
            }
            return true;
        }

        return super.dispatchKeyEvent(event);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean interceptEventWhenRecord(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return isRecording() &&
                keyCode != KeyEvent.KEYCODE_TV_TELETEXT &&
                keyCode != KEYCODE_TV_SUBTITLE &&
                keyCode != KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK &&
                keyCode != KeyEvent.KEYCODE_INFO &&
                keyCode != KeyEvent.KEYCODE_MEDIA_REWIND &&
                keyCode != KeyEvent.KEYCODE_FORWARD &&
                keyCode != KeyEvent.KEYCODE_PROG_RED &&
                keyCode != KeyEvent.KEYCODE_PROG_GREEN &&
                keyCode != KeyEvent.KEYCODE_PROG_YELLOW &&
                keyCode != KeyEvent.KEYCODE_PROG_BLUE &&
                keyCode != KeyEvent.KEYCODE_DPAD_LEFT &&
                keyCode != KeyEvent.KEYCODE_DPAD_RIGHT &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_UNKNOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_FORWARD_DEL;
    }

    private boolean unInterceptEventWhenProgEmpty(KeyEvent event) {
        int keyCode = event.getKeyCode();
        return keyCode == KeyEvent.KEYCODE_BACK ||
                keyCode == KeyEvent.KEYCODE_MENU ||
                keyCode == KeyEvent.KEYCODE_UNKNOWN ||
                keyCode == KeyEvent.KEYCODE_VOLUME_UP ||
                keyCode == KeyEvent.KEYCODE_VOLUME_DOWN;
    }

    @Override
    public boolean onHomeHandleCallback() {
        dismissAllDialog();
        stopRecord();
        // 按下home时，iTV需要提前释放surface，SurfaceView隐藏时会主动回调onSurfaceDestroy更快销毁surface，让launcher能及时拿到surface显示出画面
        hideSurface();
        return super.onHomeHandleCallback();
    }

    private boolean isRecording() {
        return DTVBookingManager.getInstance().isRecording() || DTVPVRManager.getInstance().isRecording();
    }

    private boolean isUsbNotExit() {
        return !UsbManager.getInstance().isUsbExist(this) && !mUsbAttach;
    }

    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP || event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            mLongPressed = true;
            return true;
        }
        return super.onKeyLongPress(keyCode, event);
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onProgramUpdate(ProgramUpdateEvent event) {
        Log.i(TAG, "onProgramUpdate");
        if (event.tvSize != 0 || event.radioSize != 0 || event.isProgramEdit) {
            reloadSat();
            reloadProg();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReloadSat(ReloadSatEvent event) {
        Log.i(TAG, "onReloadSat");
        reloadSat();
    }

    private void reloadSat() {
        mSatList = null; // 置空重新加载
        updateSatList();
    }

    private void reloadProg() {
        mProgListMap.clear(); // 更新频道列表前清空缓存
        updateProgList();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordStateChange(RecordStateChangeEvent event) {
        if (!event.isRecording) {
            stopRecord();
        }
    }
}
