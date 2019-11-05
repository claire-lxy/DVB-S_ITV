package com.konkawise.dtv.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVCommonManager;
import com.konkawise.dtv.DTVPlayerManager;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.DTVSettingManager;
import com.konkawise.dtv.HandlerMsgManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.RealTimeManager;
import com.konkawise.dtv.DTVBookingManager;
import com.konkawise.dtv.DTVDVBManager;
import com.konkawise.dtv.DTVEpgManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.adapter.EpgChannelListAdapter;
import com.konkawise.dtv.adapter.EpgProgListAdapter;
import com.konkawise.dtv.annotation.BookConflictType;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.bean.EpgBookParameterModel;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.PasswordDialog;
import com.konkawise.dtv.event.BookUpdateEvent;
import com.konkawise.dtv.utils.TimeUtils;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.view.TVListView;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.konkawise.dtv.weaktool.WeakTimerTask;
import com.sw.dvblib.DTVCommon;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.listener.CallbackListenerAdapter;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnFocusChange;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Enum_From;
import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Enum_Task;
import vendor.konka.hardware.dtvmanager.V1_0.HEPG_Struct_Event;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Enum_Type;
import vendor.konka.hardware.dtvmanager.V1_0.HSetting_Enum_Property;
import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Struct_Timer;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgInfo;

public class EpgActivity extends BaseActivity implements LifecycleObserver, RealTimeManager.OnReceiveTimeListener {
    private static final String TAG = "EpgActivity";
    private static final long RELOAD_CHANNEL_PERIOD = 60 * 1000;
    private static final long PLAY_PROG_DELAY = 1000;
    private static final int[] mDateIds = {R.id.btn_date_0, R.id.btn_date_1, R.id.btn_date_2,
            R.id.btn_date_3, R.id.btn_date_4, R.id.btn_date_5, R.id.btn_date_6};

    @BindView(R.id.sv_epg)
    SurfaceView mSurfaceView;

    @BindView(R.id.tv_current_date)
    TextView mTvCurrentDate;

    @BindView(R.id.lv_prog_list)
    TVListView mLvProgList;

    @BindView(R.id.tv_system_time)
    TextView mTvSystemTime;

    @BindView(R.id.lv_epg_channel)
    TVListView mLvEpgChannel;

    @BindView(R.id.pb_loading_epg)
    ProgressBar mPbLoadingEpg;

    @BindArray(R.array.dateTexts)
    String[] mDateTexts;

    @BindArray(R.array.epg_booking)
    String[] mEpgBookingArray;

    @BindArray(R.array.epg_booked)
    String[] mEpgBookedArray;

    @OnItemClick(R.id.lv_prog_list)
    void clickProgItem(int position) {
        int progNo = mProgAdapter.getItem(position).ProgNo;
        DTVProgramManager.getInstance().setCurrProgNo(progNo);
        Intent intent = new Intent(this, Topmost.class);
        startActivity(intent);
        finish();
    }

    @OnItemClick(R.id.lv_epg_channel)
    void clickEpgItem() {
        showEpgBookingDialog();
    }

    @OnItemSelected(R.id.lv_prog_list)
    void selectProgItem(int position) {
        DTVProgramManager.getInstance().setCurrProgNo(mProgAdapter.getItem(position).ProgNo);
        mCurrProgSelectPosition = position;
        mProgAdapter.setSelectPosition(mCurrProgSelectPosition);
        if (!mPasswordEntered) {
            epgItemSelect(1);
        } else {
            epgItemSelect(0);
        }
    }

    @OnItemSelected(R.id.lv_epg_channel)
    void selectEpgItem(int position) {
        mCurrEpgSelectPosition = position;
    }

    @OnFocusChange(R.id.lv_prog_list)
    void epgFocusChange(boolean hasFocus) {
        mProgAdapter.setFocus(hasFocus);
    }

    @OnFocusChange({R.id.btn_date_0, R.id.btn_date_1, R.id.btn_date_2, R.id.btn_date_3, R.id.btn_date_4, R.id.btn_date_5, R.id.btn_date_6})
    void dayFocusChange(View v, boolean hasFocus) {
        mDateButtonFocus = hasFocus;
        for (int i = 0; i < mDateIds.length; i++) {
            if (mDateIds[i] == v.getId()) {
                mCurrentFocusDateIndex = i;
                updateDateFocus();
                updateCurrentDate();
                break;
            }
        }

        if (hasFocus && mProgAdapter.isPositionValid(mLvProgList)) {
            notifyEpgChange(mProgAdapter.getItem(mCurrProgSelectPosition));
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void registerEpgBookUpdate() {
        EventBus.getDefault().register(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void unregisterEpgBookUpdate() {
        EventBus.getDefault().unregister(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void registerRealTimeUpdate() {
        RealTimeManager.getInstance().register(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void unregisterRealTimeUpdate() {
        RealTimeManager.getInstance().unregister(this);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void registerReceiveLockAndEpgMsg() {
        MsgEvent msgLockEvent = DTVDVBManager.getInstance().registerMsgEvent(Constants.MsgCallbackId.LOCK);
        MsgEvent msgEpgEvent = DTVDVBManager.getInstance().registerMsgEvent(Constants.MsgCallbackId.EPG);
        msgLockEvent.registerCallbackListener(new CallbackListenerAdapter() {
            @Override
            public void PLAYER_isLocked(int type, int progNo, int progIndex, int home) {
                showEnterPasswordDialog();
            }
        });
        msgEpgEvent.registerCallbackListener(new CallbackListenerAdapter() {
            @Override
            public void EPG_onSchInfoReady(int sat, int tsid, int servid) {
                if (mProgAdapter.isPositionValid(mLvProgList)) {
                    HProg_Struct_ProgInfo progInfo = mProgAdapter.getItem(mCurrProgSelectPosition);
                    if (progInfo != null && progInfo.Sat == sat && progInfo.TsID == tsid && progInfo.ServID == servid) {
                        updateEpgChannel();
                    }
                }
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void unregisterReceiveLockAndEpgMsg() {
        DTVDVBManager.getInstance().unregisterMsgEvent(Constants.MsgCallbackId.LOCK);
        DTVDVBManager.getInstance().unregisterMsgEvent(Constants.MsgCallbackId.EPG);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void showSurface() {
        if (mSurfaceView.getVisibility() != View.VISIBLE) {
            mSurfaceView.setVisibility(View.VISIBLE);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void hideSurface() {
        if (mSurfaceView.getVisibility() != View.GONE) {
            mSurfaceView.setVisibility(View.GONE);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void saveStopPlayProperty() {
        // 跳转或销毁界面要停止播放
        DTVPlayerManager.getInstance().stopPlay(DTVSettingManager.getInstance().getDTVProperty(HSetting_Enum_Property.PD_SwitchMode));
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void initUpdateEpgChannelTimer() {
        mUpdateChannelTimer = new Timer();
        mUpdateChannelTimer.schedule(new UpdateEpgChannelTimerTask(this), 0, RELOAD_CHANNEL_PERIOD);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void cancelUpdateEpgChannelTimer() {
        if (mUpdateChannelTimer != null) {
            mUpdateChannelTimer.cancel();
            mUpdateChannelTimer.purge();
            mUpdateChannelTimer = null;
        }
    }

    private EpgProgListAdapter mProgAdapter;
    private EpgChannelListAdapter mEpgChannelAdapter;
    private EpgMsgHandler mEpgMsgHandler;
    private Timer mUpdateChannelTimer;

    private boolean mPasswordEntered = false;
    private boolean mPasswordDialogShowing;
    private PasswordDialog mPasswordDialog;

    private Button[] mDates;
    private int mCurrProgSelectPosition;
    private int mCurrEpgSelectPosition;
    private int mCurrentFocusDateIndex;
    private boolean mDateButtonFocus;

    private LoadEpgChannelRunnable mLoadEpgChannelRunnable;

    @Override
    public void onReceiveTimeCallback(String time) {
        if (!TextUtils.isEmpty(time)) {
            mTvSystemTime.setText(time);
        }
    }

    private static class EpgMsgHandler extends WeakHandler<EpgActivity> {
        static final int MSG_PLAY_SELECT_PROG = 0;
        static final int MSG_EPG_CHANGE_LOAD = 1;
        static final int MSG_UPDATE_DATE = 2;

        EpgMsgHandler(EpgActivity view) {
            super(view);
        }

        @Override
        protected void handleMsg(Message msg) {
            EpgActivity context = mWeakReference.get();
            switch (msg.what) {
                case MSG_PLAY_SELECT_PROG:
                    int conditon = msg.arg1;
                    HProg_Struct_ProgInfo currProgInfo = context.mProgAdapter.getItem(context.mCurrProgSelectPosition);
                    DTVProgramManager.getInstance().setCurrProgNo(currProgInfo.ProgNo);
                    DTVPlayerManager.getInstance().startPlayProgNo(currProgInfo.ProgNo, conditon);
                    context.notifyEpgChange(currProgInfo);
                    break;
                case MSG_EPG_CHANGE_LOAD:
                    if (msg.obj instanceof HProg_Struct_ProgInfo) {
                        context.showLoadingEpg();
                        HProg_Struct_ProgInfo progInfo = (HProg_Struct_ProgInfo) msg.obj;
                        DTVEpgManager.getInstance().sentDataReq(progInfo.Sat, progInfo.TsID, progInfo.ServID); // 通知底层立即搜索该频道的EPG
                    }
                    break;
                case MSG_UPDATE_DATE:
                    context.initCurrentDate();
                    context.initEpgChannelDate();
                    break;
            }
        }
    }

    private static class UpdateEpgChannelTimerTask extends WeakTimerTask<EpgActivity> {

        UpdateEpgChannelTimerTask(EpgActivity view) {
            super(view);
        }

        @Override
        protected void runTimer() {
            final EpgActivity context = mWeakReference.get();
            context.notifyEpgChange(DTVProgramManager.getInstance().getCurrProgInfo());
            context.notifyUpdateDate();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_epg;
    }

    @Override
    protected void setup() {
        DTVProgramManager.getInstance().setCurrProgType(HProg_Enum_Type.TVPROG, 0);
        mEpgMsgHandler = new EpgMsgHandler(this);

        initCurrentDate();
        initEpgChannelDate();
        initProgList();
        initEpgChannelList();
        initSurfaceView();
    }

    @Override
    protected LifecycleObserver provideLifecycleObserver() {
        return this;
    }

    private void initCurrentDate() {
        DTVCommon.TimeModel sysTime = DTVCommonManager.getInstance().getLocalTime();
        if (sysTime != null) {
            String date = sysTime.Year + "-" + sysTime.Month + "-" + sysTime.Day;
            mTvCurrentDate.setText(date);
        }
    }

    private void updateCurrentDate() {
        DTVCommon.TimeModel sysTime = DTVCommonManager.getInstance().getLocalTime();
        if (sysTime != null) {
            int maxDayOfMonth = TimeUtils.getDayOfMonthByYearAndMonth(sysTime.Year, sysTime.Month);
            int year = sysTime.Year;
            int month = sysTime.Month;
            int day = sysTime.Day + mCurrentFocusDateIndex;
            if (day > maxDayOfMonth) {
                day = mCurrentFocusDateIndex - (maxDayOfMonth - sysTime.Day);
                month++;
                if (month > 12) {
                    month = 1;
                    year++;
                }
            }
            String date = year + "-" + month + "-" + day;
            mTvCurrentDate.setText(date);
        }
    }

    private void initEpgChannelDate() {
        DTVCommon.TimeModel sysTime = DTVCommonManager.getInstance().getLocalTime();
        mDates = new Button[mDateIds.length];
        for (int i = 0; i < mDateIds.length; i++) {
            mDates[i] = findViewById(mDateIds[i]);
            if (sysTime != null && sysTime.Weekday > 0) {
                mDates[i].setText(mDateTexts[(i + sysTime.Weekday - 1) % mDateTexts.length]);
                mDates[i].setOnKeyListener((v, keyCode, event) -> {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN && mDateButtonFocus) {
                        mLvEpgChannel.requestFocus();
                        return true;
                    }
                    return false;
                });
            }
        }
        updateDateFocus();
    }

    private void initProgList() {
        mProgAdapter = new EpgProgListAdapter(this, new ArrayList<>());
        mLvProgList.setAdapter(mProgAdapter);
        mLvProgList.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                    if (mCurrProgSelectPosition >= mProgAdapter.getCount() - 1) {
                        mLvProgList.setSelection(0);
                        return true;
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                    if (mCurrProgSelectPosition <= 0) {
                        mLvProgList.setSelection(mProgAdapter.getCount() - 1);
                        return true;
                    }
                } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    mDates[0].requestFocus();
                    return true;
                }
            }

            return false;
        });
        ThreadPoolManager.getInstance().execute(new WeakRunnable<EpgActivity>(this) {
            @Override
            protected void loadBackground() {
                int[] currentSelectPosition = new int[1];
                List<HProg_Struct_ProgInfo> progList = DTVProgramManager.getInstance().getWholeGroupProgInfoList(currentSelectPosition);
                runOnUiThread(() -> {
                    mCurrProgSelectPosition = currentSelectPosition[0];
                    mProgAdapter.updateData(progList);
                    mLvProgList.setSelection(mCurrProgSelectPosition);
                });
            }
        });
    }

    private void initEpgChannelList() {
        mLoadEpgChannelRunnable = new LoadEpgChannelRunnable(this);

        mEpgChannelAdapter = new EpgChannelListAdapter(this, new ArrayList<>());
        mLvEpgChannel.setAdapter(mEpgChannelAdapter);

        mLvEpgChannel.setOnKeyListener((view, keycode, event) -> {
            if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                mLvEpgChannel.setNextFocusUpId(mDateIds[mCurrentFocusDateIndex]);
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                if (mCurrentFocusDateIndex == 0) {
                    mLvProgList.requestFocus();
                } else {
                    mDates[--mCurrentFocusDateIndex].requestFocus();
                }
                return true;
            } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                if (mCurrentFocusDateIndex < mDates.length - 1) {
                    mDates[++mCurrentFocusDateIndex].requestFocus();
                }
                return true;
            }
            return false;
        });
    }

    private void initSurfaceView() {
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(TAG, "epg surface create");
                DTVPlayerManager.getInstance().setSurface(holder.getSurface());
                DTVPlayerManager.getInstance().setWindowSize(0, 0,
                        getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
                if (mProgAdapter.isPositionValid(mLvProgList)) {
                    notifyPlayProg(mPasswordEntered ? 0 : 1);
                }
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {
                Log.i(TAG, "epg surface destroy");
            }
        });
    }

    /**
     * Epg book对话框
     */
    private void showEpgBookingDialog() {
        if (!mProgAdapter.isPositionValid(mLvProgList) || !mEpgChannelAdapter.isPositionValid(mLvEpgChannel))
            return;

        HProg_Struct_ProgInfo progInfo = mProgAdapter.getItem(mCurrProgSelectPosition);
        HEPG_Struct_Event eventInfo = mEpgChannelAdapter.getItem(mCurrEpgSelectPosition);

        if (DTVCommonManager.getInstance().isProgramPlaying(eventInfo)) {
            ToastUtils.showToast(R.string.toast_program_playing);
            return;
        }

        final HBooking_Struct_Timer bookInfo = DTVBookingManager.getInstance().progIsBooked(progInfo.Sat, progInfo.TsID, progInfo.ServID, eventInfo.uiEventId);
        boolean isBooked = bookInfo != null && (bookInfo.schtype == HBooking_Enum_Task.PLAY || bookInfo.schtype == HBooking_Enum_Task.RECORD);

        List<String> epgBookCheckContent = Arrays.asList(isBooked ? mEpgBookedArray : mEpgBookingArray);
        new CommCheckItemDialog()
                .title(getString(R.string.epg_booking_dialog_title))
                .content(epgBookCheckContent)
                .position(getBookPosition(epgBookCheckContent, bookInfo))
                .setOnDismissListener((dialog, position, checkContent) -> {
                    HBooking_Struct_Timer newBookInfo;
                    int bookSchType = getBookCheckSchType(checkContent);
                    if (!isBooked) {
                        DTVCommon.TimeModel startTimeInfo = DTVCommonManager.getInstance().getStartTime(eventInfo);
                        DTVCommon.TimeModel endTimeInfo = DTVCommonManager.getInstance().getEndTime(eventInfo);
                        EpgBookParameterModel parameterModel = new EpgBookParameterModel();
                        parameterModel.progInfo = progInfo;
                        parameterModel.eventInfo = eventInfo;
                        parameterModel.startTimeInfo = startTimeInfo;
                        parameterModel.endTimeInfo = endTimeInfo;
                        parameterModel.schtype = bookSchType;
                        parameterModel.schway = HBooking_Enum_From.EPG;
                        newBookInfo = DTVBookingManager.getInstance().newBookTimer(parameterModel);
                    } else {
                        newBookInfo = bookInfo;
                        newBookInfo.schtype = bookSchType;
                    }
                    Log.i(TAG, "new book info = " + newBookInfo);

                    HBooking_Struct_Timer conflictBookInfo = DTVBookingManager.getInstance().conflictCheck(newBookInfo);
                    int conflictType = DTVBookingManager.getInstance().getConflictType(conflictBookInfo);
                    switch (conflictType) {
                        case Constants.BookConflictType.NONE: // 当前参数的book没有冲突，正常添加删除
                        case Constants.BookConflictType.ADD: // 当前参数的book有冲突，如果是添加需要先删除后再添加
                        case Constants.BookConflictType.REPLACE: // 当前参数的book有冲突，需要询问替换
                            bookHandle(conflictType, getBookCheckSchType(checkContent), newBookInfo, conflictBookInfo);
                            break;
                        case Constants.BookConflictType.LIMIT:
                            ToastUtils.showToast(R.string.toast_book_limit);
                            break;
                    }

                    DTVBookingManager.getInstance().updateDBase(0);
                }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
    }

    private void bookHandle(@BookConflictType int conflictType, int bookSchType, HBooking_Struct_Timer bookInfo, HBooking_Struct_Timer conflictBookInfo) {
        if (bookSchType == HBooking_Enum_Task.NONE) { // 取消book
            DTVBookingManager.getInstance().deleteTimer(bookInfo);
            updateItemBookTag(bookSchType);
        } else if (bookSchType == HBooking_Enum_Task.PLAY
                || bookSchType == HBooking_Enum_Task.RECORD) { // 添加book
            if (conflictType == Constants.BookConflictType.REPLACE) {
                BookingModel conflictBookModel = new BookingModel();
                conflictBookModel.bookInfo = conflictBookInfo;
                conflictBookModel.progInfo = DTVProgramManager.getInstance().getProgInfoByServiceId(conflictBookInfo.servid, conflictBookInfo.tsid, conflictBookInfo.sat);

                BookingModel newBookModel = new BookingModel();
                newBookModel.bookInfo = bookInfo;
                newBookModel.progInfo = DTVProgramManager.getInstance().getProgInfoByServiceId(bookInfo.servid, bookInfo.tsid, bookInfo.sat);
                showReplaceBookDialog(bookSchType, conflictBookModel, newBookModel);
            } else {
                DTVBookingManager.getInstance().addTimer(conflictType, conflictBookInfo, bookInfo);
                updateItemBookTag(bookSchType);
            }
        }
    }

    private void showReplaceBookDialog(int bookSchType, BookingModel conflictBookModel, BookingModel newBookModel) {
        new CommTipsDialog()
                .title(getString(R.string.dialog_book_conflict_title))
                .content(MessageFormat.format(getString(R.string.dialog_book_conflict_content),
                        conflictBookModel.getBookDate(this, BookingModel.BOOK_TIME_SEPARATOR_EMPTY), conflictBookModel.progInfo.Name, conflictBookModel.getBookType(this)))
                .lineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics()))
                .setOnPositiveListener(getString(R.string.dialog_book_conflict_positive), () -> {
                    DTVBookingManager.getInstance().replaceTimer(conflictBookModel.bookInfo, newBookModel.bookInfo);
                    updateItemBookTag(bookSchType);
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    /**
     * 更新Epg book标志图标
     */
    private void updateItemBookTag(int bookSchType) {
        if (mProgAdapter.getCount() > 0 && mEpgChannelAdapter.getCount() > 0) {
            HEPG_Struct_Event item = mEpgChannelAdapter.getItem(mCurrEpgSelectPosition);
            if (item != null) {
                if (bookSchType == HBooking_Enum_Task.NONE) {
                    item.schtype = HBooking_Enum_Task.NONE;
                } else if (bookSchType == HBooking_Enum_Task.PLAY) {
                    item.schtype = HBooking_Enum_Task.PLAY;
                } else if (bookSchType == HBooking_Enum_Task.RECORD) {
                    item.schtype = HBooking_Enum_Task.RECORD;
                }
                mEpgChannelAdapter.updateData(mCurrEpgSelectPosition, item);
            }
        }
    }

    private int getBookCheckSchType(String checkContent) {
        if (TextUtils.equals(checkContent, getString(R.string.book_non))) {
            return HBooking_Enum_Task.NONE;
        } else if (TextUtils.equals(checkContent, getString(R.string.book_play))) {
            return HBooking_Enum_Task.PLAY;
        } else if (TextUtils.equals(checkContent, getString(R.string.book_record))) {
            return HBooking_Enum_Task.RECORD;
        }
        return -1;
    }

    /**
     * Epg book对话框根据book内容获取选中索引
     */
    private int getBookPosition(List<String> epgBookCheckContent, HBooking_Struct_Timer bookInfo) {
        if (bookInfo == null) {
            return epgBookCheckContent.indexOf(getString(R.string.book_play));
        }
        if (bookInfo.schtype == HBooking_Enum_Task.PLAY) {
            return epgBookCheckContent.indexOf(getString(R.string.book_play));
        } else if (bookInfo.schtype == HBooking_Enum_Task.RECORD) {
            return epgBookCheckContent.indexOf(getString(R.string.book_record));
        } else {
            return epgBookCheckContent.indexOf(getString(R.string.book_non));
        }
    }

    private void showEnterPasswordDialog() {
        // 对话框显示，监听遥控器上下按键让用户可以切换到没有ParentLock的节目时取消弹框
        if (mPasswordDialog != null && mPasswordDialog.isVisible())
            return;
        mPasswordDialog = new PasswordDialog()
                .setOnKeyListener((dialog, keyCode, event) -> {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_BACK:
                            return true;

                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            if (mPasswordDialogShowing && event.getAction() == KeyEvent.ACTION_DOWN) {
                                nextSelectEpgItem();
                                return true;
                            }
                            return false;

                        case KeyEvent.KEYCODE_DPAD_UP:
                            if (mPasswordDialogShowing && event.getAction() == KeyEvent.ACTION_DOWN) {
                                lastSelectEpgItem();
                                return true;
                            }
                            return false;
                    }

                    return false;
                }).setOnPasswordInputListener((inputPassword, currentPassword, isValid) -> {
                    if (isValid) {
                        mPasswordEntered = true;
                        mPasswordDialog = null;
                        epgItemSelect(0);
                    } else {
                        ToastUtils.showToast(R.string.toast_invalid_password);
                    }
                });
        mPasswordDialog.show(getSupportFragmentManager(), PasswordDialog.TAG);

        // 加个延时处理标志位
        mEpgMsgHandler.postDelayed(() -> mPasswordDialogShowing = true, 1000);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void dismissPasswordDialog() {
        if (mPasswordDialog != null && mPasswordDialog.isVisible()) {
            mPasswordDialog.dismiss();
            mPasswordDialog = null;
        }
    }

    private void nextSelectEpgItem() {
        mCurrProgSelectPosition++;
        if (mCurrProgSelectPosition >= mProgAdapter.getCount()) {
            mCurrProgSelectPosition = mProgAdapter.getCount() - 1;
        }
        progListFocus();
    }

    private void lastSelectEpgItem() {
        mCurrProgSelectPosition--;
        if (mCurrProgSelectPosition < 0) {
            mCurrProgSelectPosition = 0;
        }
        progListFocus();
    }

    /**
     * 频道列表设置焦点
     */
    private void progListFocus() {
        mLvProgList.requestFocus();
        mLvProgList.setSelection(mCurrProgSelectPosition);
        mProgAdapter.setSelectPosition(mCurrProgSelectPosition);
    }

    /**
     * 切换频道
     */
    private void epgItemSelect(int condition) {
        dismissPasswordDialog();
        mCurrentFocusDateIndex = 0;
        updateDateFocus();

        notifyPlayProg(condition);
    }

    /**
     * 更新日期焦点背景
     */
    private void updateDateFocus() {
        for (int i = 0; i < mDates.length; i++) {
            if (i == mCurrentFocusDateIndex) {
                mDates[i].setTextColor(getResources().getColor(R.color.epg_text_select));
                if (!mDates[i].isFocused()) {
                    mDates[i].setBackgroundColor(getResources().getColor(R.color.channel_edit_gray));
                } else {
                    mDates[i].setBackgroundResource(R.mipmap.btn_selected_bg);
                }
            } else {
                mDates[i].setTextColor(getResources().getColor(R.color.epg_btn_selector));
                mDates[i].setBackgroundResource(R.drawable.button_nomal_transtalate_shape);
            }
        }
    }

    /**
     * 根据日期id刷新Epg Channel列表
     */
    private void updateEpgChannel() {
        clearDateFocus();

        if (mLoadEpgChannelRunnable != null && mProgAdapter.isPositionValid(mLvProgList)) {
            ThreadPoolManager.getInstance().remove(mLoadEpgChannelRunnable);
            ThreadPoolManager.getInstance().execute(mLoadEpgChannelRunnable);
        }
    }

    private static class LoadEpgChannelRunnable extends WeakRunnable<EpgActivity> {

        LoadEpgChannelRunnable(EpgActivity view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            EpgActivity context = mWeakReference.get();

            final List<HEPG_Struct_Event> epgChannelList = DTVEpgManager.getInstance().getCurrProgSchInfo(context.mCurrentFocusDateIndex);
            context.runOnUiThread(() -> {
                context.hideLoadingEpg();
                if (context.mEpgChannelAdapter == null) {
                    context.mEpgChannelAdapter = new EpgChannelListAdapter(context, new ArrayList<>());
                    context.mLvEpgChannel.setAdapter(context.mEpgChannelAdapter);
                }

                if (epgChannelList != null && !epgChannelList.isEmpty()) {
                    context.mEpgChannelAdapter.updateData(epgChannelList);
                } else {
                    context.mEpgChannelAdapter.updateData(new ArrayList<>());
                }
            });
        }
    }

    private void clearDateFocus() {
        for (Button button : mDates) {
            button.setCompoundDrawables(null, null, null, null);
        }
    }

    private void showLoadingEpg() {
        if (mPbLoadingEpg.getVisibility() == View.GONE) {
            mPbLoadingEpg.setVisibility(View.VISIBLE);
        }
    }

    private void hideLoadingEpg() {
        if (mPbLoadingEpg.getVisibility() == View.VISIBLE) {
            mPbLoadingEpg.setVisibility(View.GONE);
        }
    }

    /**
     * 发送消息通知切换频道更新Epg列表
     */
    private void notifyEpgChange(HProg_Struct_ProgInfo progInfo) {
        HandlerMsgManager.getInstance().removeMessage(mEpgMsgHandler, EpgMsgHandler.MSG_EPG_CHANGE_LOAD);
        HandlerMsgManager.getInstance().sendMessage(mEpgMsgHandler, new HandlerMsgModel(EpgMsgHandler.MSG_EPG_CHANGE_LOAD, progInfo));
    }

    /**
     * 发送消息通知刷新日期显示
     */
    private void notifyUpdateDate() {
        HandlerMsgManager.getInstance().sendMessage(mEpgMsgHandler, new HandlerMsgModel(EpgMsgHandler.MSG_UPDATE_DATE, 0));
    }

    /**
     * 发送消息通知切换频道播放
     */
    private void notifyPlayProg(int condition) {
        HandlerMsgManager.getInstance().removeMessage(mEpgMsgHandler, EpgMsgHandler.MSG_PLAY_SELECT_PROG);
        HandlerMsgManager.getInstance().sendMessage(mEpgMsgHandler,
                new HandlerMsgModel(EpgMsgHandler.MSG_PLAY_SELECT_PROG, condition, PLAY_PROG_DELAY));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            startActivityForResult(new Intent(this, BookListActivity.class), Constants.RequestCode.REQUEST_CODE_EPG_BOOK);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == Constants.RequestCode.REQUEST_CODE_EPG_BOOK && data != null &&
                data.getBooleanExtra(Constants.IntentKey.INTENT_BOOK_UPDATE, false)) {
            if (mProgAdapter.isPositionValid(mLvProgList)) {
                notifyEpgChange(mProgAdapter.getItem(mCurrProgSelectPosition));
            }
        }
    }

    @Override
    public boolean onHomeHandleCallback() {
        hideSurface(); // 按下home键提前销毁surface，让launcher拿到surface资源
        return super.onHomeHandleCallback();
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBookUpdate(BookUpdateEvent event) {
        if (mProgAdapter.isPositionValid(mLvProgList)) {
            notifyEpgChange(mProgAdapter.getItem(mCurrProgSelectPosition));
        }
    }
}
