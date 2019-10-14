package com.konkawise.dtv.ui;

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
import com.konkawise.dtv.HandlerMsgManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.RealTimeManager;
import com.konkawise.dtv.SWBookingManager;
import com.konkawise.dtv.SWDVBManager;
import com.konkawise.dtv.SWEpgManager;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.SWTimerManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.UIApiManager;
import com.konkawise.dtv.adapter.EpgChannelListAdapter;
import com.konkawise.dtv.adapter.EpgProgListAdapter;
import com.konkawise.dtv.annotation.BookConflictType;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.bean.EpgBookParameterModel;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.PasswordDialog;
import com.konkawise.dtv.event.BookUpdateEvent;
import com.konkawise.dtv.utils.TimeUtils;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.view.TVListView;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.konkawise.dtv.weaktool.WeakTimerTask;
import com.sw.dvblib.SWBooking;
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
import vendor.konka.hardware.dtvmanager.V1_0.EpgEvent_t;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Enum_Type;
import vendor.konka.hardware.dtvmanager.V1_0.HSetting_Enum_Property;
import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;

public class EpgActivity extends BaseActivity implements RealTimeManager.OnReceiveTimeListener {
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
        SWPDBaseManager.getInstance().setCurrProgNo(progNo);
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
        SWPDBaseManager.getInstance().setCurrProgNo(mProgAdapter.getItem(position).ProgNo);
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

    private EpgProgListAdapter mProgAdapter;
    private EpgChannelListAdapter mEpgChannelAdapter;
    private EpgMsgHandler mEpgMsgHandler;
    private Timer mUpdateChannelTimer;

    private boolean mPasswordEntered = false;
    /**
     * 创建dialog前ListView的item会回调onItemSelect()调用egpHandle()
     * 添加标志位防止setOnKeyListener时同时回调监听到遥控器上下按键
     */
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
                    PDPMInfo_t currProgInfo = context.mProgAdapter.getItem(context.mCurrProgSelectPosition);
                    SWPDBaseManager.getInstance().setCurrProgNo(currProgInfo.ProgNo);
                    UIApiManager.getInstance().startPlayProgNo(currProgInfo.ProgNo, conditon);
                    context.notifyEpgChange(currProgInfo);
                    break;
                case MSG_EPG_CHANGE_LOAD:
                    if (msg.obj instanceof PDPMInfo_t) {
                        context.showLoadingEpg();
                        PDPMInfo_t progInfo = (PDPMInfo_t) msg.obj;
                        SWEpgManager.getInstance().sentDataReq(progInfo.Sat, progInfo.TsID, progInfo.ServID); // 通知底层立即搜索该频道的EPG
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
            context.notifyEpgChange(SWPDBaseManager.getInstance().getCurrProgInfo());
            context.notifyUpdateDate();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_epg;
    }

    @Override
    protected void setup() {
        SWPDBaseManager.getInstance().setCurrProgType(HProg_Enum_Type.TVPROG, 0);
        mEpgMsgHandler = new EpgMsgHandler(this);
        EventBus.getDefault().register(this);

        initUpdateEpgChannelTimer();
        initCurrentDate();
        initEpgChannelDate();
        initProgList();
        initEpgChannelList();
        initSurfaceView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        showSurface();
        RealTimeManager.getInstance().register(this);
        registerMsgEvent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        RealTimeManager.getInstance().unregister(this);
        unregisterMsgEvent();
        UIApiManager.getInstance().stopPlay(SWFtaManager.getInstance().getCommE2PInfo(HSetting_Enum_Property.PD_SwitchMode)); // 跳转或销毁界面要停止播放
    }

    @Override
    protected void onStop() {
        super.onStop();
        hideSurface();
    }

    @Override
    protected void onDestroy() {
        dismissPasswordDialog();
        cancelUpdateEpgChannelTimer();
        EventBus.getDefault().unregister(this);
        super.onDestroy();
    }

    private void registerMsgEvent() {
        MsgEvent msgLockEvent = SWDVBManager.getInstance().registerMsgEvent(Constants.LOCK_CALLBACK_MSG_ID);
        MsgEvent msgEpgEvent = SWDVBManager.getInstance().registerMsgEvent(Constants.EPG_CALLBACK_MSG_ID);
        msgLockEvent.registerCallbackListener(new CallbackListenerAdapter() {
            @Override
            public int ProgPlay_SWAV_ISLOCKED(int type, int progno, int progindex, int home) {
                showEnterPasswordDialog();
                return super.ProgPlay_SWAV_ISLOCKED(type, progno, progindex, home);
            }
        });
        msgEpgEvent.registerCallbackListener(new CallbackListenerAdapter() {
            @Override
            public int Epg_SchInfoReady(int sat, int tsid, int servid) {
                if (mProgAdapter.isPositionValid(mLvProgList)) {
                    PDPMInfo_t progInfo = mProgAdapter.getItem(mCurrProgSelectPosition);
                    if (progInfo != null && progInfo.Sat == sat && progInfo.TsID == tsid && progInfo.ServID == servid) {
                        updateEpgChannel();
                    }
                }
                return super.Epg_SchInfoReady(sat, tsid, servid);
            }
        });
    }

    private void unregisterMsgEvent() {
        SWDVBManager.getInstance().unregisterMsgEvent(Constants.LOCK_CALLBACK_MSG_ID);
        SWDVBManager.getInstance().unregisterMsgEvent(Constants.EPG_CALLBACK_MSG_ID);
    }

    private void initUpdateEpgChannelTimer() {
        mUpdateChannelTimer = new Timer();
        mUpdateChannelTimer.schedule(new UpdateEpgChannelTimerTask(this), 0, RELOAD_CHANNEL_PERIOD);
    }

    private void cancelUpdateEpgChannelTimer() {
        if (mUpdateChannelTimer != null) {
            mUpdateChannelTimer.cancel();
            mUpdateChannelTimer.purge();
            mUpdateChannelTimer = null;
        }
    }

    private void initCurrentDate() {
        SysTime_t sysTime = SWTimerManager.getInstance().getLocalTime();
        if (sysTime != null) {
            String date = sysTime.Year + "-" + sysTime.Month + "-" + sysTime.Day;
            mTvCurrentDate.setText(date);
        }
    }

    private void updateCurrentDate() {
        SysTime_t sysTime = SWTimerManager.getInstance().getLocalTime();
        if (sysTime != null) {
            int maxDayOfMonth = TimeUtils.getDayOfMonthByYearAndMonth(sysTime.Year, sysTime.Month);
            int year = sysTime.Year;
            int month = sysTime.Month;
            int day = sysTime.Day + mCurrentFocusDateIndex;
            if (day > maxDayOfMonth) {
                day = 1;
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
        SysTime_t sysTime = SWTimerManager.getInstance().getLocalTime();
        mDates = new Button[mDateIds.length];
        for (int i = 0; i < mDateIds.length; i++) {
            mDates[i] = findViewById(mDateIds[i]);
            if (sysTime != null && sysTime.Weekday > 0) {
                mDates[i].setText(mDateTexts[(i + sysTime.Weekday - 1) % mDateTexts.length]);
                mDates[i].setOnKeyListener(new View.OnKeyListener() {
                    @Override
                    public boolean onKey(View v, int keyCode, KeyEvent event) {
                        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN && mDateButtonFocus) {
                            mLvEpgChannel.requestFocus();
                            return true;
                        }
                        return false;
                    }
                });
            }
        }
        updateDateFocus();
    }

    private void initProgList() {
        mProgAdapter = new EpgProgListAdapter(this, new ArrayList<>());
        mLvProgList.setAdapter(mProgAdapter);
        mLvProgList.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
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
            }
        });
        ThreadPoolManager.getInstance().execute(new WeakRunnable<EpgActivity>(this) {
            @Override
            protected void loadBackground() {
                int[] currentSelectPosition = new int[1];
                List<PDPMInfo_t> progList = SWPDBaseManager.getInstance().getWholeGroupProgList(currentSelectPosition);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mCurrProgSelectPosition = currentSelectPosition[0];
                        mProgAdapter.updateData(progList);
                        mLvProgList.setSelection(mCurrProgSelectPosition);
                    }
                });
            }
        });
    }

    private void initEpgChannelList() {
        mLoadEpgChannelRunnable = new LoadEpgChannelRunnable(this);

        mEpgChannelAdapter = new EpgChannelListAdapter(this, new ArrayList<>());
        mLvEpgChannel.setAdapter(mEpgChannelAdapter);

        mLvEpgChannel.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent event) {
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
            }
        });
    }

    private void initSurfaceView() {
        mSurfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                Log.i(TAG, "epg surface create");
                UIApiManager.getInstance().setSurface(holder.getSurface());
                UIApiManager.getInstance().setWindowSize(0, 0,
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
     * Epg book对话框
     */
    private void showEpgBookingDialog() {
        if (!mProgAdapter.isPositionValid(mLvProgList) || !mEpgChannelAdapter.isPositionValid(mLvEpgChannel))
            return;

        PDPMInfo_t progInfo = mProgAdapter.getItem(mCurrProgSelectPosition);
        EpgEvent_t eventInfo = mEpgChannelAdapter.getItem(mCurrEpgSelectPosition);

        if (SWTimerManager.getInstance().isProgramPlaying(eventInfo)) {
            ToastUtils.showToast(R.string.toast_program_playing);
            return;
        }

        final HSubforProg_t bookInfo = SWBookingManager.getInstance().progIsSubFored(progInfo.Sat, progInfo.TsID, progInfo.ServID, eventInfo.uiEventId);
        boolean isBooked = bookInfo != null && (bookInfo.schtype == SWBooking.BookSchType.PLAY.ordinal() || bookInfo.schtype == SWBooking.BookSchType.RECORD.ordinal());

        List<String> epgBookCheckContent = Arrays.asList(isBooked ? mEpgBookedArray : mEpgBookingArray);
        new CommCheckItemDialog()
                .title(getString(R.string.epg_booking_dialog_title))
                .content(epgBookCheckContent)
                .position(getBookPosition(epgBookCheckContent, bookInfo))
                .setOnDismissListener(new CommCheckItemDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(CommCheckItemDialog dialog, int position, String checkContent) {
                        HSubforProg_t newBookInfo;
                        int bookSchType = getBookCheckSchType(checkContent);
                        if (!isBooked) {
                            SysTime_t startTimeInfo = SWTimerManager.getInstance().getStartTime(eventInfo);
                            SysTime_t endTimeInfo = SWTimerManager.getInstance().getEndTime(eventInfo);
                            EpgBookParameterModel parameterModel = new EpgBookParameterModel();
                            parameterModel.progInfo = progInfo;
                            parameterModel.eventInfo = eventInfo;
                            parameterModel.startTimeInfo = startTimeInfo;
                            parameterModel.endTimeInfo = endTimeInfo;
                            parameterModel.schtype = bookSchType;
                            parameterModel.schway = SWBooking.BookWay.EPG.ordinal();
                            newBookInfo = SWBookingManager.getInstance().newBookProg(parameterModel);
                        } else {
                            newBookInfo = bookInfo;
                            newBookInfo.schtype = bookSchType;
                        }
                        Log.i(TAG, "new book info = " + newBookInfo);

                        HSubforProg_t conflictBookInfo = SWBookingManager.getInstance().conflictCheck(newBookInfo, 0);
                        int conflictType = SWBookingManager.getInstance().getConflictType(conflictBookInfo);
                        switch (conflictType) {
                            case Constants.BOOK_CONFLICT_NONE: // 当前参数的book没有冲突，正常添加删除
                            case Constants.BOOK_CONFLICT_ADD: // 当前参数的book有冲突，如果是添加需要先删除后再添加
                            case Constants.BOOK_CONFLICT_REPLACE: // 当前参数的book有冲突，需要询问替换
                                bookHandle(conflictType, getBookCheckSchType(checkContent), newBookInfo, conflictBookInfo);
                                break;
                            case Constants.BOOK_CONFLICT_LIMIT:
                                ToastUtils.showToast(R.string.toast_book_limit);
                                break;
                        }

                        SWBookingManager.getInstance().updateDBase(0);
                    }
                }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
    }

    private void bookHandle(@BookConflictType int conflictType, int bookSchType, HSubforProg_t bookInfo, HSubforProg_t conflictBookInfo) {
        if (bookSchType == SWBooking.BookSchType.NONE.ordinal()) { // 取消book
            SWBookingManager.getInstance().deleteProg(bookInfo);
            updateItemBookTag(bookSchType);
        } else if (bookSchType == SWBooking.BookSchType.PLAY.ordinal()
                || bookSchType == SWBooking.BookSchType.RECORD.ordinal()) { // 添加book
            if (conflictType == Constants.BOOK_CONFLICT_REPLACE) {
                BookingModel conflictBookModel = new BookingModel();
                conflictBookModel.bookInfo = conflictBookInfo;
                conflictBookModel.progInfo = SWPDBaseManager.getInstance().getProgInfoByServiceId(conflictBookInfo.servid, conflictBookInfo.tsid, conflictBookInfo.sat);

                BookingModel newBookModel = new BookingModel();
                newBookModel.bookInfo = bookInfo;
                newBookModel.progInfo = SWPDBaseManager.getInstance().getProgInfoByServiceId(bookInfo.servid, bookInfo.tsid, bookInfo.sat);
                showReplaceBookDialog(bookSchType, conflictBookModel, newBookModel);
            } else {
                SWBookingManager.getInstance().addProg(conflictType, conflictBookInfo, bookInfo);
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
                .setOnPositiveListener(getString(R.string.dialog_book_conflict_positive), new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        SWBookingManager.getInstance().replaceProg(conflictBookModel.bookInfo, newBookModel.bookInfo);
                        updateItemBookTag(bookSchType);
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    /**
     * 更新Epg book标志图标
     */
    private void updateItemBookTag(int bookSchType) {
        if (mProgAdapter.getCount() > 0 && mEpgChannelAdapter.getCount() > 0) {
            EpgEvent_t item = mEpgChannelAdapter.getItem(mCurrEpgSelectPosition);
            if (item != null) {
                if (bookSchType == SWBooking.BookSchType.NONE.ordinal()) {
                    item.schtype = (byte) SWBooking.BookSchType.NONE.ordinal();
                } else if (bookSchType == SWBooking.BookSchType.PLAY.ordinal()) {
                    item.schtype = (byte) SWBooking.BookSchType.PLAY.ordinal();
                } else if (bookSchType == SWBooking.BookSchType.RECORD.ordinal()) {
                    item.schtype = (byte) SWBooking.BookSchType.RECORD.ordinal();
                }
                mEpgChannelAdapter.updateData(mCurrEpgSelectPosition, item);
            }
        }
    }

    private int getBookCheckSchType(String checkContent) {
        if (TextUtils.equals(checkContent, getString(R.string.book_non))) {
            return SWBooking.BookSchType.NONE.ordinal();
        } else if (TextUtils.equals(checkContent, getString(R.string.book_play))) {
            return SWBooking.BookSchType.PLAY.ordinal();
        } else if (TextUtils.equals(checkContent, getString(R.string.book_record))) {
            return SWBooking.BookSchType.RECORD.ordinal();
        }
        return -1;
    }

    /**
     * Epg book对话框根据book内容获取选中索引
     */
    private int getBookPosition(List<String> epgBookCheckContent, HSubforProg_t bookInfo) {
        if (bookInfo == null) {
            return epgBookCheckContent.indexOf(getString(R.string.book_play));
        }
        if (bookInfo.schtype == SWBooking.BookSchType.PLAY.ordinal()) {
            return epgBookCheckContent.indexOf(getString(R.string.book_play));
        } else if (bookInfo.schtype == SWBooking.BookSchType.RECORD.ordinal()) {
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
                .setOnKeyListener(new PasswordDialog.OnKeyListener() {
                    @Override
                    public boolean onKeyListener(PasswordDialog dialog, int keyCode, KeyEvent event) {
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
                    }
                }).setOnPasswordInputListener(new PasswordDialog.OnPasswordInputListener() {
                    @Override
                    public void onPasswordInput(String inputPassword, String currentPassword, boolean isValid) {
                        if (isValid) {
                            mPasswordEntered = true;
                            mPasswordDialog = null;
                            epgItemSelect(0);
                        } else {
                            ToastUtils.showToast(R.string.toast_invalid_password);
                        }
                    }
                });
        mPasswordDialog.show(getSupportFragmentManager(), PasswordDialog.TAG);

        // 加个延时处理标志位
        mEpgMsgHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mPasswordDialogShowing = true;
            }
        }, 1000);
    }

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

            final List<EpgEvent_t> epgChannelList = UIApiManager.getInstance().getCurrProgSchInfo(context.mCurrentFocusDateIndex);
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
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
    private void notifyEpgChange(PDPMInfo_t progInfo) {
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
