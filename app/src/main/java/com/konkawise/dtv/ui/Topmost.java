package com.konkawise.dtv.ui;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
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

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.HandlerMsgManager;
import com.konkawise.dtv.PreferenceManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.RealTimeManager;
import com.konkawise.dtv.SWBookingManager;
import com.konkawise.dtv.SWDJAPVRManager;
import com.konkawise.dtv.SWDVBManager;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.SWPSearchManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.UIApiManager;
import com.konkawise.dtv.UsbManager;
import com.konkawise.dtv.adapter.TvListAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.dialog.AudioDialog;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.FindChannelDialog;
import com.konkawise.dtv.dialog.InitPasswordDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.PasswordDialog;
import com.konkawise.dtv.dialog.PfBarScanDialog;
import com.konkawise.dtv.dialog.PfDetailDialog;
import com.konkawise.dtv.dialog.SearchChannelDialog;
import com.konkawise.dtv.dialog.SearchProgramDialog;
import com.konkawise.dtv.dialog.SetPVRTimeDialog;
import com.konkawise.dtv.dialog.SubtitleDialog;
import com.konkawise.dtv.dialog.TeletextDialog;
import com.konkawise.dtv.event.ProgramUpdateEvent;
import com.konkawise.dtv.event.RecordStateChangeEvent;
import com.konkawise.dtv.service.BookService;
import com.konkawise.dtv.service.PowerService;
import com.konkawise.dtv.utils.TimeUtils;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.konkawise.dtv.weaktool.WeakTimerTask;
import com.sw.dvblib.SWDVB;
import com.sw.dvblib.SWFta;
import com.sw.dvblib.msg.cb.AVMsgCB;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnFocusChange;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import vendor.konka.hardware.dtvmanager.V1_0.ChannelNew_t;
import vendor.konka.hardware.dtvmanager.V1_0.HSubtitle_t;
import vendor.konka.hardware.dtvmanager.V1_0.HTeletext_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

public class Topmost extends BaseActivity {
    private static final String TAG = "Topmost";
    private static final long SMALL_HINT_BOX_PERIOD = 1000;
    private static final long RECORDING_DELAY = 1000;
    private static final long RECORDING_PERIOD = 1000;
    private static final long PLAY_PROG_DELAY = 1000;
    private static final long JUMP_PROG_DELAY = 2500;
    private static final long RECORD_TIME_HIDE_DELAY = 10 * 1000;

    private static final int KEYCODE_TV_SUBTITLE = 293;

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

    @BindView(R.id.sv_topmost)
    SurfaceView mSurfaceView;

    @BindView(R.id.iv_radio_bg)
    ImageView mIvRadioBackground;

    @BindView(R.id.tv_prog_num)
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

    @BindView(R.id.item_factory_reset)
    TextView mItemFactoryReset;

    @BindView(R.id.item_dtv_setting)
    TextView mItemDtvSetting;

    @OnItemSelected(R.id.lv_prog_list)
    void onItemSelect(int position) {
        mCurrSelectProgPosition = position;
    }

    @OnItemClick(R.id.lv_prog_list)
    void onItemClick(int position) {
        toggleProgList();
        if (mProgListAdapter.getItem(position).ProgNo == getCurrentProgNum()) return;

        SWPDBaseManager.getInstance().setCurrProgType(SWPDBaseManager.getInstance().getCurrProgType(), 0);
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
            showInstallationSelectDialog();
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
            showEpgSearchDialog();
        } else {
            startActivity(new Intent(this, EpgActivity.class));
        }
    }

    @OnClick(R.id.item_channel_manage)
    void channelManage() {
        toggleChannelManageItem();
    }

    @OnClick(R.id.item_channel_edit)
    void channelEdit() {
        if (SWFtaManager.getInstance().isOpenMenuLock() && !mPasswordEntered) {
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
        if (SWFtaManager.getInstance().isOpenMenuLock() && !mPasswordEntered) {
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
        if (SWFtaManager.getInstance().isOpenMenuLock() && !mPasswordEntered) {
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

    @OnClick(R.id.item_factory_reset)
    void factoryReset() {
        if (SWFtaManager.getInstance().isOpenMenuLock() && !mPasswordEntered) {
            showPasswordDialog(new PasswordDialog.OnPasswordInputListener() {
                @Override
                public void onPasswordInput(String inputPassword, String currentPassword, boolean isValid) {
                    if (isValid) {
                        mPasswordEntered = true;
                        showFactoryResetDialog();
                    }
                }
            });
        } else {
            showFactoryResetDialog();
        }
    }

    @OnClick(R.id.item_dtv_setting)
    void dtvSetting() {
        startActivity(new Intent(this, DTVSettingActivity.class));
    }

    private int mNewProgNum;
    private boolean mLongPressed;
    private long mLongPressDelayTime;

    private Timer mSmallHintBoxTimer;
    private SmallHintBoxTimerTask mSmallHintBoxTimerTask;
    private SmallHintBox mSmallHintBox;

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
    private List<SatInfo_t> mSatList;
    // 所有卫星的频道列表缓存
    private List<PDPMInfo_t> mAllSatProgListCache = new ArrayList<>();
    // 喜爱分组频道列表缓存
    private SparseArray<List<PDPMInfo_t>> mFavoriteProgListCacheMap = new SparseArray<>();
    // 对应卫星索引的频道列表缓存
    private SparseArray<List<PDPMInfo_t>> mSatProgListCacheMap = new SparseArray<>();
    private LoadProgRunnable mLoadProgRunnable;
    private LoadSatRunnable mLoadSatRunnable;

    private WaitingStartRecordRunnable mWaitingStartRecordRunnable;

    private AVMsgCB mAvMsgCB = new PlayMsgCB();
    private boolean mUsbAttach;

    private class PlayMsgCB extends AVMsgCB {
        @Override
        public int ProgPlay_SWAV_ISLOCKED(int type, int progno, int progindex, int home) {
            showPasswordDialog();
            return super.ProgPlay_SWAV_ISLOCKED(type, progno, progindex, home);
        }

        @Override
        public int ProgPlay_SWAV_USBAttach() {
            Log.i(TAG, "usb attach");
            mUsbAttach = true;
            return super.ProgPlay_SWAV_USBAttach();
        }

        @Override
        public int ProgPlay_SWAV_USBDetach() {
            Log.i(TAG, "usb detach");
            mUsbAttach = false;
            stopRecord();
            return super.ProgPlay_SWAV_USBDetach();
        }
    }

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
                SWPDBaseManager.getInstance().setCurrProgNo(msg.arg1);
                UIApiManager.getInstance().startPlayProgNo(msg.arg1, 1);
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
                    context.mTvProgNum.setVisibility(View.INVISIBLE);
                    break;
                case MSG_SHOW_PROG_NUM:
                    context.showProgNum(context.getCurrentProgShowNum());
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
            if (msg.what == MSG_JUMP_PROG && context.isJumpProgNumValid()) {
                context.playProg(msg.arg1);
                context.mJumpProgNumBuilder.delete(0, context.mJumpProgNumBuilder.length());
            } else {
                context.showProgNumInvalid();
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.topmost;
    }

    @Override
    protected void setup() {
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }

        SWDVB.GetInstance(); // 必须先初始化库，否则使用库会出现空指针异常
        mSmallHintBox = new SmallHintBox(this);

        bootService();
        registerListener();
        initHandler();
        initSatList();
        initProgList();
        initSurfaceView();
        showRadioBackground();
        checkLaunchSettingPassword();
    }

    @Override
    protected void onResume() {
        super.onResume();
        RealTimeManager.getInstance().start();
        SWDVBManager.getInstance().regMsgHandler(Constants.LOCK_CALLBACK_MSG_ID, Looper.getMainLooper(), mAvMsgCB);
        showSurface();
        updatePfBarInfo();
//        startSmallHintBoxTimer();
        restoreMenuItem(); // 恢复menu初始item显示

        if (PreferenceManager.getInstance().getBoolean(Constants.PrefsKey.FIRST_LAUNCH)
                && SWPDBaseManager.getInstance().getCurrProgInfo() == null) {
            showSearchChannelDialog();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        UIApiManager.getInstance().stopPlay(0);
        SWDVBManager.getInstance().unRegMsgHandler(Constants.LOCK_CALLBACK_MSG_ID, mAvMsgCB);
        cancelSmallHintBoxTimer();
        stopRecord();
        if (isFinishing()) {
            SWDVBManager.getInstance().releaseResource();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideSurface();
        if (mProgListShow) {
            toggleProgList();
        }
        if (mMenuShow) {
            toggleMenu();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        cancelSmallHintBoxTimer();
        dismissPfBarScanDialog();
        dismissPasswordDialog();
        unregisterListener();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleBook();
        setIntent(new Intent()); // 处理完后要重置为空，防止其他界面返回或跳转到Topmost使用上一个book跳转的intent
    }

    private boolean handleBook() {
        int bookType = getIntent().getIntExtra(Constants.IntentKey.INTENT_BOOK_TYPE, -1);
        int recordSeconds = getIntent().getIntExtra(Constants.IntentKey.INTENT_BOOK_SECONDS, -1);
        int progType = getIntent().getIntExtra(Constants.IntentKey.INTENT_BOOK_PROG_TYPE, -1);
        int progNum = getIntent().getIntExtra(Constants.IntentKey.INTENT_BOOK_PROG_NUM, -1);
        Log.i(TAG, "bookType = " + bookType + ", recordSeconds = " + recordSeconds + ", progType = " + progType + ", progNum = " + progNum);
        if (bookType == BookService.ACTION_BOOKING_PLAY) {
            if (progType != -1 && progNum != -1) {
                SWPDBaseManager.getInstance().setCurrProgType(progType, 0);
                playProg(progNum);
                return true;
            }
        } else if (bookType == BookService.ACTION_BOOKING_RECORD) {
            if (!isUsbExit()) {
                ToastUtils.showToast(R.string.toast_no_storage_device);
                return false;
            }

            if (progType != -1 && progNum != -1 && recordSeconds != -1) {
                startRecord(new OnStartRecordCallback() {
                    @Override
                    public void startRecordCallback(int recordFlag) {
                        if (recordFlag == 0) {
                            SWPDBaseManager.getInstance().setCurrProgType(progType, 0);
                            playProg(progNum, true);
                            startRecordingTimer(recordSeconds);
                            SWBookingManager.getInstance().setRecording(true);
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

    private void startRecord(OnStartRecordCallback callback) {
        if (mWaitingStartRecordRunnable == null) {
            mWaitingStartRecordRunnable = new WaitingStartRecordRunnable(this, callback);
        } else {
            ThreadPoolManager.getInstance().remove(mWaitingStartRecordRunnable);
        }
        ThreadPoolManager.getInstance().execute(mWaitingStartRecordRunnable);
    }

    private static class WaitingStartRecordRunnable extends WeakRunnable<Topmost> {
        private OnStartRecordCallback callback;

        WaitingStartRecordRunnable(Topmost view, OnStartRecordCallback callback) {
            super(view);
            this.callback = callback;
        }

        @Override
        protected void loadBackground() {
            int recordDelay = 0;
            int result;
            while (true) {
                result = SWDJAPVRManager.getInstance().startRecord(recordDelay);
                if (result != -4) break;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                recordDelay++;
            }

            final int startRecordResult = result;
            mWeakReference.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (callback != null) {
                        callback.startRecordCallback(startRecordResult);
                    }
                }
            });
        }
    }

    private void startRecordingTimer(int recordSeconds) {
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
        SWBookingManager.getInstance().setRecording(false);
        SWDJAPVRManager.getInstance().setRecording(false);
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

    private void startSmallHintBoxTimer() {
        cancelSmallHintBoxTimer();
        mSmallHintBoxTimer = new Timer();
        mSmallHintBoxTimerTask = new SmallHintBoxTimerTask(this);
        mSmallHintBoxTimer.schedule(mSmallHintBoxTimerTask, 0, SMALL_HINT_BOX_PERIOD);
    }

    private void cancelSmallHintBoxTimer() {
        if (mSmallHintBoxTimer != null) {
            mSmallHintBoxTimer.cancel();
            mSmallHintBoxTimer.purge();
            mSmallHintBoxTimerTask.release();
            mSmallHintBoxTimer = null;
            mSmallHintBoxTimerTask = null;
        }
    }

    private class SmallHintBoxTimerTask extends WeakTimerTask<Topmost> {

        SmallHintBoxTimerTask(Topmost view) {
            super(view);
        }

        @Override
        protected void runTimer() {
            Topmost context = mWeakReference.get();

//            final boolean hasSignal = hasSignal();
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
//                    mSmallHintBox.hintBox(SmallHintBox.CHECK_TYPE_SIGNAL, hasSignal);
                }
            });
        }

        private boolean hasSignal() {
            int strength = SWPSearchManager.getInstance().getSignalStatus(SWPSearchManager.SIGNAL_STRENGTH);
            int quality = SWPSearchManager.getInstance().getSignalStatus(SWPSearchManager.SIGNAL_QUALITY);
            return strength > 0 || quality > 0;
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
        mTvProgNum.setText(String.valueOf(progNum));
        mTvProgNum.setVisibility(View.VISIBLE);
        if (sendMsgHide)
            sendProgMsg(new HandlerMsgModel(ProgHandler.MSG_HIDE_PROG_NUM, SWFtaManager.getInstance().dismissTimeout()));
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
            int scrollToPosition = getScrollToPosition(progNum);
            mProgListView.setSelection(scrollToPosition);
            mProgListAdapter.setSelectPosition(scrollToPosition);
        }
    }

    private static class LoadProgRunnable extends WeakRunnable<Topmost> {
        static final int MAX_DELAY_LOAD_PROG = 3;
        private int delayTime;

        LoadProgRunnable(Topmost view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            Topmost context = mWeakReference.get();
            // 等待卫星列表获取完再获取频道列表
            while (context.mSatList == null || context.mSatList.isEmpty()) {
                if (delayTime >= MAX_DELAY_LOAD_PROG) break;
                Log.i(TAG, "waiting load satellite");
                try {
                    delayTime++;
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            List<PDPMInfo_t> progList = context.getProgList();
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    context.mPbLoadingChannel.setVisibility(View.GONE);
                    if (progList != null && !progList.isEmpty()) {
                        context.mProgListAdapter.updateData(progList);
                        PDPMInfo_t progInfo = SWPDBaseManager.getInstance().getCurrProgInfo();
                        if (progInfo != null) {
                            context.updateProgListSelection(progInfo.ProgNo);
                        }
                    } else {
                        context.mProgListAdapter.updateData(new ArrayList<>());
                    }
                }
            });

        }
    }

    private int getScrollToPosition(int progNum) {
        if (mProgListAdapter.getCount() > 0) {
            for (int i = 0; i < mProgListAdapter.getData().size(); i++) {
                if (mProgListAdapter.getData().get(i).ProgNo == progNum) {
                    return i;
                }
            }
        }
        return 0;
    }

    /**
     * 获取卫星列表，如果有喜爱频道，还包含喜爱分组频道列表
     */
    private List<SatInfo_t> getSatList() {
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

            if (context.mSatList == null) {
                context.mSatList = new ArrayList<>();
            } else {
                context.mSatList.clear();
            }
            List<SatInfo_t> allSatList = SWPDBaseManager.getInstance().getAllSatListContainFav(context);
            if (allSatList != null && !allSatList.isEmpty()) {
                context.mSatList.addAll(allSatList);
            }
        }
    }

    private void initSurfaceView() {
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(TAG, "topmost surface create");
                UIApiManager.getInstance().setSurface(holder.getSurface());
                UIApiManager.getInstance().setWindowSize(0, 0,
                        getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
                if (!handleBook()) {
                    if (SWPDBaseManager.getInstance().getCurrProgInfo() != null && PreferenceManager.getInstance().getBoolean(Constants.PrefsKey.FIRST_LAUNCH))
                        playProg(getCurrentProgNum(), true);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(TAG, "topmost surface destroy");
                //               UIApiManager.getInstance().stopPlay(0);
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

    /**
     * 获取频道列表，如果有喜爱列表还包含喜爱分组的频道列表
     */
    private List<PDPMInfo_t> getProgList() {
        if (mCurrSatPosition == 0) {
            if (mAllSatProgListCache.isEmpty()) {
                List<PDPMInfo_t> totalProgList = SWPDBaseManager.getInstance().getWholeGroupProgList();
                if (totalProgList != null && !totalProgList.isEmpty()) {
                    mAllSatProgListCache.addAll(totalProgList);
                }
            }
            return mAllSatProgListCache;
        } else {
            List<SatInfo_t> allSatList = SWPDBaseManager.getInstance().getAllSatList(this);
            if (allSatList != null && !allSatList.isEmpty()) {
                int satIndex = getSatList().get(mCurrSatPosition).SatIndex;
                // 当前卫星索引比添加了All的卫星索引还大，说明有喜爱分组，获取喜爱分组频道列表
                if (mCurrSatPosition > allSatList.size() - 1) {
                    List<PDPMInfo_t> favoriteProgList = mFavoriteProgListCacheMap.get(satIndex); // 这里的SatIndex是存储喜爱分组的索引
                    if (favoriteProgList == null || favoriteProgList.isEmpty()) {
                        favoriteProgList = SWPDBaseManager.getInstance().getFavListByIndex(satIndex);
                        mFavoriteProgListCacheMap.put(satIndex, favoriteProgList);
                    }
                    return favoriteProgList;
                } else {
                    List<PDPMInfo_t> progList = mSatProgListCacheMap.get(satIndex);
                    if (progList == null || progList.isEmpty()) {
                        progList = SWPDBaseManager.getInstance().getCurrGroupProgListByCond(1, satIndex);
                        mSatProgListCacheMap.put(satIndex, progList);
                    }
                    return progList;
                }
            }
        }
        return null;
    }

    private void clearProgListCache() {
        mAllSatProgListCache.clear();
        mFavoriteProgListCacheMap.clear();
        mSatProgListCacheMap.clear();
    }

    /**
     * 当前播放台号，0~getTotalProgNum()-1
     */
    private int getCurrentProgNum() {
        PDPMInfo_t currProgInfo = SWPDBaseManager.getInstance().getCurrProgInfo();
        if (currProgInfo == null) {
            currProgInfo = new PDPMInfo_t();
            currProgInfo.ProgNo = 0;
        }
        return currProgInfo.ProgNo;
    }

    /**
     * 当前显示台号,1~getTotalProgNum()
     */
    private int getCurrentProgShowNum() {
        PDPMInfo_t currentProgInfo = SWPDBaseManager.getInstance().getCurrProgInfo();
        if (currentProgInfo == null) {
            currentProgInfo = new PDPMInfo_t();
            currentProgInfo.PShowNo = 1;
        }
        return currentProgInfo.PShowNo;
    }

    /**
     * 总频道数
     */
    private int getTotalProgNum() {
        return SWPDBaseManager.getInstance().getProgNumOfCurrGroup();
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
        playProg(getCurrentProgNum(), false);
    }

    /**
     * 发送msg通知播放设定台号
     */
    private void playProg(int progNum) {
        playProg(progNum, false);
    }

    private void playProg(int progNum, boolean immediately) {
        UIApiManager.getInstance().stopPlay(0); // 切台之前暂停当前频道播放
        SWPDBaseManager.getInstance().setCurrProgNo(progNum);
        removePlayProgMsg();
        showPfInfo();
        showProgNum(getCurrentProgShowNum());
        showRadioBackground();
        sendPlayProgMsg(new HandlerMsgModel(PlayHandler.MSG_PLAY_PROG, progNum, immediately ? 0 : PLAY_PROG_DELAY));
    }

    /**
     * 加台播放
     */
    private void nextProg() {
        int nextProgNum = getCurrentProgNum() + 1;
        if (nextProgNum >= getTotalProgNum()) {
            nextProgNum = 0;
        }
        playProg(nextProgNum);
    }

    /**
     * 减台播放
     */
    private void lastProg() {
        int lastProgNum = getCurrentProgNum() - 1;
        if (lastProgNum < 0) {
            lastProgNum = getTotalProgNum() - 1;
        }
        playProg(lastProgNum);
    }

    /**
     * 发送msg通知播放跳转台号
     */
    private void jumpPlayProg() {
        if (isJumpProgNumValid()) {
            removeJumpProgMsg();
            sendJumpProgMsg(new HandlerMsgModel(JumpProgHandler.MSG_JUMP_PROG, Integer.valueOf(mJumpProgNumBuilder.toString()) - 1));
        } else {
            showProgNumInvalid();
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
        showProgNum(Integer.valueOf(mJumpProgNumBuilder.toString()), false);
        sendJumpProgMsg(new HandlerMsgModel(JumpProgHandler.MSG_JUMP_PROG, Integer.valueOf(mJumpProgNumBuilder.toString()) - 1, JUMP_PROG_DELAY));
    }

    private boolean isJumpProgNumValid() {
        if (mJumpProgNumBuilder.length() > 0) {
            int jumProgNum = Integer.valueOf(mJumpProgNumBuilder.toString());
            return jumProgNum > 0 && jumProgNum <= getTotalProgNum();
        }
        return false;
    }

    private void showProgNumInvalid() {
        hideProgNum();
        dismissPfBarScanDialog();
        mJumpProgNumBuilder.delete(0, mJumpProgNumBuilder.length());
        ToastUtils.showToast(R.string.toast_program_number_invalid);
    }

    private void showRadioBackground() {
        mIvRadioBackground.setVisibility(SWPDBaseManager.getInstance().getCurrProgType() == 1 ? View.VISIBLE : View.INVISIBLE);
    }

    private void checkLaunchSettingPassword() {
        if (!PreferenceManager.getInstance().getBoolean(Constants.PrefsKey.FIRST_LAUNCH)) {
            showSettingPasswordDialog();
        }
    }

    private boolean isProgLock() {
        return SWPDBaseManager.getInstance().isProgLock() && SWFtaManager.getInstance().isOpenParentLock();
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

        PDPMInfo_t currProgInfo = SWPDBaseManager.getInstance().getCurrProgInfo();
        if (currProgInfo != null) {
            SatInfo_t satInfo = SWPDBaseManager.getInstance().getSatInfo(currProgInfo.Sat);
            int[] pids = SWPDBaseManager.getInstance().getServicePID(currProgInfo.ProgNo);
            ChannelNew_t channelInfo = SWPDBaseManager.getInstance().getChannelInfoBySat(currProgInfo.Sat, currProgInfo.ProgIndex);
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
                        public void onKeyBack() {
                            mSearchChannelDialog = null;
                            mItemInstallation.requestFocus();
                            toggleMenu();
                        }

                        @Override
                        public void onKeyCenter() {
                            mSearchChannelDialog = null;
                            startActivity(new Intent(Topmost.this, BlindActivity.class));
                        }
                    });
        }

        mSearchChannelDialog.show(getSupportFragmentManager(), SearchProgramDialog.TAG);

        // 延时处理标志位，规避点击ok键进入apk dialog响应onKeyListener
        mProgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (mSearchChannelDialog != null) {
                    mSearchChannelDialog.resetInit();
                }
            }
        }, 1000);
    }

    private void showEpgSearchDialog() {
        new CommRemindDialog()
                .content(getString(R.string.no_program))
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        mItemInstallation.requestFocus();
                        showInstallationSelectDialog();
                    }
                }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
    }

    private void showPasswordDialog() {
        if (mPasswordDialog != null && mPasswordDialog.getDialog() != null && mPasswordDialog.isVisible())
            return;
        if (!isProgLock()) return;

        getContentResolver().update(Uri.parse("content://dvbchannellock/dvb_info/0"), null, null, null);
        showPasswordDialog(null, new PasswordDialog.OnControlArrowKeyListener() {
            @Override
            public void onControlArrowKey(int playProgType) {
                switch (playProgType) {
                    case PasswordDialog.CONTROL_ARROW_LAST_PROG:
                        lastProg();
                        break;
                    case PasswordDialog.CONTROL_ARROW_NEXT_PROG:
                        nextProg();
                        break;
                    case PasswordDialog.CONTROL_ARROW_CURRENT_PROG:
                        dismissPasswordDialog();
                        Uri uri = Uri.parse("content://dvbchannellock/dvb_info/1");
                        getContentResolver().update(uri, null, null, null);
                        UIApiManager.getInstance().startPlayProgNo(SWPDBaseManager.getInstance().getCurrProgNo(), 0);
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

    private void showInstallationSelectDialog() {
        List<String> content = new ArrayList<>();
        content.add(getString(R.string.installation_s2));
        content.add(getString(R.string.installation_t2));
        new CommCheckItemDialog()
                .title(getString(R.string.dialog_title_tips))
                .content(content)
                .position(0)
                .setOnDismissListener(new CommCheckItemDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(CommCheckItemDialog dialog, int position, String checkContent) {
                        if (position == 0) {
                            toggleInstallationItem();
                        } else {
                            startActivity(new Intent(Topmost.this, InstallationT2Activity.class));
                        }
                    }
                }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
    }

    private void showClearChannelDialog() {
        new CommTipsDialog().title(getString(R.string.clear_channel)).content(getString(R.string.are_you_sure_clear_prog_data))
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        SWFtaManager.getInstance().clearChannel();
                        mCurrSatPosition = 0;
                        mProgListAdapter.clearData(); // 同步清空频道列表
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private void showFactoryResetDialog() {
        new CommTipsDialog().title(getString(R.string.factory_reset)).content(getString(R.string.factory_reset_content))
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        SWFtaManager.getInstance().factoryReset();
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private void showFindProgChannelDialog() {
        if (mProgListAdapter.getCount() <= 0) return;

        new FindChannelDialog()
                .channels(mProgListAdapter.getData())
                .setOnFindChannelCallback(new FindChannelDialog.OnFindChannelCallback() {
                    @Override
                    public void onFindChannels(PDPMInfo_t findChannel) {
                        if (findChannel == null) return;

                        if (findChannel.ProgNo != getCurrentProgNum()) playProg(findChannel.ProgNo);
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
                        hideSurface();
                        finish();
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private void showSubtitleDialog() {
        if (SWPDBaseManager.getInstance().getCurrProgInfo() == null) return;

        int currSubtitle = 0;
        int serviceid = SWPDBaseManager.getInstance().getCurrProgInfo().ServID;
        int num = SWFtaManager.getInstance().getSubtitleNum(serviceid);
        final int[] pids = new int[num];
        List<HashMap<String, Object>> subtitles = new ArrayList<>();
        HashMap<String, Object> off = new HashMap<>();
        off.put(Constants.SUBTITLE_NAME, "OFF");
        subtitles.add(off);
        for (int index = 0; index < num; index++) {
            HSubtitle_t subtitle = SWFtaManager.getInstance().getSubtitleInfo(serviceid, index);
            if (subtitle.used != 0) {
                HashMap<String, Object> map = new HashMap<>();
                map.put(Constants.SUBTITLE_NAME, subtitle.Name);
                map.put(Constants.SUBTITLE_ORG_TYPE, subtitle.OrgType == 0);
                map.put(Constants.SUBTITLE_TYPE, (subtitle.Type >= 0x20 && subtitle.Type <= 0x24) || subtitle.Type == 0x05);
                subtitles.add(map);
                if (SWFtaManager.getInstance().getCurSubtitleInfo(serviceid).Name.equals(subtitle.Name))
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
                            SWFtaManager.getInstance().openSubtitle(pids[position - 1]);
                    }
                }).show(getSupportFragmentManager(), SubtitleDialog.TAG);
    }

    private void showTeletextDialog() {
        if (SWPDBaseManager.getInstance().getCurrProgInfo() == null) return;

        int currTeleText = 0;
        int serviceid = SWPDBaseManager.getInstance().getCurrProgInfo().ServID;
        int num = SWFtaManager.getInstance().getTeletextNum(serviceid);
        final int[] pids = new int[num];
        String[] teletextNames = new String[num + 1];
        teletextNames[0] = "OFF";
        for (int index = 0; index < num; index++) {
            HTeletext_t teletext = SWFtaManager.getInstance().getTeletextInfo(serviceid, index);
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
                            SWFtaManager.getInstance().openTeletext(pids[position - 1]);
                    }
                }).show(getSupportFragmentManager(), "teletext");
    }

    private void showAudioDialog() {
        if (SWPDBaseManager.getInstance().getCurrProgInfo() == null) return;

        new AudioDialog().title(getString(R.string.audio)).show(getSupportFragmentManager(), AudioDialog.TAG);
    }

    private void showSettingPasswordDialog() {
        mSettingPasswordDialog = new InitPasswordDialog().setOnSavePasswordListener(new InitPasswordDialog.OnSavePasswordListener() {
            @Override
            public void onSavePassword(String password) {
                SWFtaManager.getInstance().setCommPWDInfo(SWFta.E_E2PP.E2P_Password.ordinal(), password);
                PreferenceManager.getInstance().putBoolean(Constants.PrefsKey.FIRST_LAUNCH, true);

                dismissSettingPasswordDialog();

                if (SWPDBaseManager.getInstance().getCurrProgInfo() == null) {
                    showSearchChannelDialog();
                } else {
                    playProg();
                }
            }
        }).setOnKeyListener(new InitPasswordDialog.OnKeyListener() {
            @Override
            public boolean onKeyListener(InitPasswordDialog dialog, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    dismissSettingPasswordDialog();
                    showExitDialog();
                    return true;
                }
                return false;
            }
        });
        mSettingPasswordDialog.show(getSupportFragmentManager(), Constants.PrefsKey.FIRST_LAUNCH);
    }

    private void showQuitRecordDialog(KeyEvent event) {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            new CommTipsDialog()
                    .content(getString(R.string.dialog_quit_record_content))
                    .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                        @Override
                        public void onPositiveListener() {
                            stopRecord();
                        }
                    }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
        }
    }

    private void stopRecord() {
        if (isRecording()) {
            SWDJAPVRManager.getInstance().stopRecord();

            cancelRecordingTimer();
            setRecordFlagStop();
            mRecordingLayout.setVisibility(View.GONE);
            mTvRecordingTime.setText(TimeUtils.getDecimalFormatTime(0));
            ToastUtils.showToast(R.string.toast_stop_record);
        }
    }

    private boolean isPfBarShowing() {
        return mPfBarScanDialog != null && mPfBarScanDialog.isShowing();
    }

    private boolean isPfDetailShowing() {
        return mPfDetailDialog != null && mPfDetailDialog.getDialog() != null;
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
        startTranslateAnimation(mProgListShow ? -mProgListMenu.getLeft() : 0f,
                mProgListShow ? 0f : -mProgListMenu.getLeft(), mProgListMenu, null);
        mProgListShow = !mProgListShow;
    }

    private void toggleMenu() {
        if (mMenuShow) {
            mPasswordEntered = false;
        }
        startTranslateAnimation(mMenuShow ? -mMenu.getLeft() : 0f,
                mMenuShow ? 0f : -mMenu.getLeft(), mMenu, new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        super.onAnimationEnd(animation);
                        if (mMenuShow) {
                            restoreMenuItem(); // menu隐藏动画执行结束，恢复menu的item显示
                        }
                        mMenuShow = !mMenuShow;
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
                sendProgMsg(new HandlerMsgModel(ProgHandler.MSG_HIDE_PF_BAR, SWFtaManager.getInstance().dismissTimeout()));
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
        mItemFactoryReset.setVisibility(isShowChannelManage ? View.VISIBLE : View.GONE);
        mItemDtvSetting.setVisibility(isShowChannelManage ? View.GONE : View.VISIBLE);
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
        mItemFactoryReset.setVisibility(View.GONE);
        mItemDtvSetting.setVisibility(View.VISIBLE);
    }

    private boolean isMenuTopFocused() {
        return mItemInstallation.isFocused() || (mItemChannelManage.isFocused() && mIvChannelManageBack.getVisibility() == View.VISIBLE);
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
            return true;
        }

        // SUB
        if (keyCode == KEYCODE_TV_SUBTITLE) {
            return true;
        }

        // PAUSE
        if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
            if (!isUsbExit()) {
                ToastUtils.showToast(R.string.toast_no_storage_device);
                return true;
            }
            new SetPVRTimeDialog()
                    .setOnPasswordInputListener(new SetPVRTimeDialog.OnTimeInputListener() {
                        @Override
                        public void onPasswordInput(int minute) {
                            Intent intent = new Intent();
                            intent.setClass(Topmost.this, RecordPlayer.class);
                            intent.putExtra("from", RecordPlayer.FROM_TOPMOST);
                            intent.putExtra("time", minute);
                            startActivity(intent);
                        }
                    })
                    .show(getSupportFragmentManager(), SetPVRTimeDialog.TAG);
            return true;
        }

        // STOP
        if (keyCode == KeyEvent.KEYCODE_MEDIA_STOP) {
            stopRecord();
            return true;
        }

        // SHIFT
        if (keyCode == KeyEvent.KEYCODE_SHIFT_RIGHT) {
            if (!isUsbExit()) {
                ToastUtils.showToast(R.string.toast_no_storage_device);
                return true;
            }
            new SetPVRTimeDialog()
                    .setOnPasswordInputListener(new SetPVRTimeDialog.OnTimeInputListener() {
                        @Override
                        public void onPasswordInput(int minute) {
                            Intent intent = new Intent();
                            intent.setClass(Topmost.this, RecordPlayer.class);
                            intent.putExtra("from", RecordPlayer.FROM_TOPMOST);
                            intent.putExtra("time", minute);
                            startActivity(intent);
                        }
                    })
                    .show(getSupportFragmentManager(), SetPVRTimeDialog.TAG);
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
            if (!isUsbExit()) {
                ToastUtils.showToast(R.string.toast_no_storage_device);
                return true;
            }

            if (SWBookingManager.getInstance().isRecording()) {
                showQuitRecordDialog(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_UNKNOWN));
            } else {
                if (mProgListAdapter.getCount() > 0) {
                    startRecord(new OnStartRecordCallback() {
                        @Override
                        public void startRecordCallback(int recordFlag) {
                            if (recordFlag == 0) {
                                SWDJAPVRManager.getInstance().setRecording(true);
                                startRecordingTimer(60); // test 60 seconds
                                sendHideRecordTimeMsg(new HandlerMsgModel(ProgHandler.MSG_HIDE_RECORD_TIME, RECORD_TIME_HIDE_DELAY));
                                ToastUtils.showToast(R.string.toast_start_record);
                            } else {
                                ToastUtils.showToast(R.string.toast_start_record_failed);
                            }
                        }
                    });
                }
            }
            return true;
        }

        // TV/RADIO
        if (keyCode == KeyEvent.KEYCODE_TV_RADIO_SERVICE) {
            stopRecord();
            SWPDBaseManager.getInstance().setCurrProgType(SWPDBaseManager.getInstance().getCurrProgType() == 1 ? 0 : 1, 0);
            updateProgList();
            playProg();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_MENU) {
            dismissPfBarScanDialog();
            if (!mMenuShow) {
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

        if (keyCode == KeyEvent.KEYCODE_TV_TELETEXT) {
            showTeletextDialog();
            return true;
        }

        // keycode=293 can't be caught, so use F3.
        if (keyCode == KeyEvent.KEYCODE_F3) {
            showSubtitleDialog();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK) {
            showAudioDialog();
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
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint("RestrictedApi")
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        // 没有节目，不处理节目切换
        if (mProgListAdapter.getCount() <= 0) return super.dispatchKeyEvent(event);
        // 如果处于booking录制状态，拦截提示退出
        if (interceptEventWhenRecord(event)) {
            showQuitRecordDialog(event);
            return true;
        }

        // 频道列表上下切换
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
                List<SatInfo_t> satList = getSatList();
                if (satList == null || satList.isEmpty()) return super.dispatchKeyEvent(event);

                if (--mCurrSatPosition < 0) mCurrSatPosition = satList.size() - 1;
                mTvSatelliteName.setText(satList.get(mCurrSatPosition).sat_name);
                updateProgList();
                return true;
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                List<SatInfo_t> satList = getSatList();
                if (satList == null || satList.isEmpty()) return super.dispatchKeyEvent(event);

                if (++mCurrSatPosition >= satList.size()) mCurrSatPosition = 0;
                mTvSatelliteName.setText(satList.get(mCurrSatPosition).sat_name);
                updateProgList();
                return true;
            }

            return super.dispatchKeyEvent(event);
        }

        // menu显示，焦点在menu上不处理
        if (mMenuShow && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            return super.dispatchKeyEvent(event);
        }

        // menu显示，焦点在menu上
        if (mMenuShow && event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            if (isMenuTopFocused()) return true; // 避免menu已经到顶持续按上键导致焦点丢失问题
            return super.dispatchKeyEvent(event);
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
                    showProgNumInvalid();
                } else {
                    dismissPfBarScanDialog();
                    // 频道列表有数据时才弹出
                    if (!mProgListShow && mProgListAdapter.getCount() > 0) {
                        mProgListView.requestFocus();
                        updateProgListSelection(getCurrentProgNum());
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
                        mNewProgNum = getCurrentProgNum() - 1;
                        mLongPressDelayTime = System.currentTimeMillis();
                    } else {
                        mLongPressed = true;
                        if (System.currentTimeMillis() - mLongPressDelayTime > 200) {
                            removePlayProgMsg();
                            mLongPressDelayTime = System.currentTimeMillis();
                            if (--mNewProgNum < 0) mNewProgNum = getTotalProgNum() - 1;
                            // mNewProgNum是逻辑切台播放台号，显示台号显示要+1
                            showProgNum(mNewProgNum + 1, !mLongPressed);
                        }
                    }
                    break;

                case KeyEvent.ACTION_UP:
                    if (mProgListShow) {
                        return super.dispatchKeyEvent(event);
                    } else {
                        if (mLongPressed) {
                            mLongPressed = false;
                            playProg(mNewProgNum);
                        } else {
                            lastProg();
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
                        mNewProgNum = getCurrentProgNum() + 1;
                        mLongPressDelayTime = System.currentTimeMillis();
                    } else {
                        mLongPressed = true;
                        if (System.currentTimeMillis() - mLongPressDelayTime > 200) {
                            removePlayProgMsg();
                            mLongPressDelayTime = System.currentTimeMillis();
                            if (++mNewProgNum >= getTotalProgNum()) mNewProgNum = 0;
                            // mNewProgNum是逻辑切台播放台号，显示台号显示要+1
                            showProgNum(mNewProgNum + 1, !mLongPressed);
                        }
                    }
                    break;

                case KeyEvent.ACTION_UP:
                    if (mLongPressed) {
                        mLongPressed = false;
                        playProg(mNewProgNum);
                    } else {
                        nextProg();
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
                keyCode != KeyEvent.KEYCODE_DPAD_CENTER &&
                keyCode != KeyEvent.KEYCODE_VOLUME_MUTE &&
                keyCode != KeyEvent.KEYCODE_UNKNOWN &&
                keyCode != KeyEvent.KEYCODE_VOLUME_UP &&
                keyCode != KeyEvent.KEYCODE_VOLUME_DOWN &&
                keyCode != KeyEvent.KEYCODE_FORWARD_DEL;
    }

    @Override
    public boolean onHomeHandleCallback() {
        stopRecord();
        // 按下home时，iTV需要提前释放surface，SurfaceView隐藏时会主动回调onSurfaceDestroy更快销毁surface，让launcher能及时拿到surface显示出画面
        hideSurface();
        return super.onHomeHandleCallback();
    }

    private boolean isRecording() {
        return SWBookingManager.getInstance().isRecording() || SWDJAPVRManager.getInstance().isRecording();
    }

    private boolean isUsbExit() {
        return UsbManager.getInstance().isUsbExist() || mUsbAttach;
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
    public void onReceiveProgramUpdate(ProgramUpdateEvent event) {
        Log.i(TAG, "receive program update, tv size = " + event.tvSize + ", radio size = " + event.radioSize);
        if (event.tvSize != 0 || event.radioSize != 0 || event.isProgramEdit) {
            updateSatList();
            clearProgListCache(); // 更新频道列表前清空缓存
            updateProgList();
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onRecordStateChange(RecordStateChangeEvent event) {
        if (!event.isRecording) {
            Log.i(TAG, "book stop current record");
            stopRecord();
        }
    }

    public interface OnStartRecordCallback {
        void startRecordCallback(int recordFlag);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
