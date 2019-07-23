package com.konkawise.dtv.ui;

import android.content.Intent;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.util.SparseArray;
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
import com.konkawise.dtv.SWBookingManager;
import com.konkawise.dtv.SWDVBManager;
import com.konkawise.dtv.SWEpgManager;
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
import com.konkawise.dtv.utils.TimeUtils;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.view.TVListView;
import com.konkawise.dtv.weaktool.RealTimeHelper;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.konkawise.dtv.weaktool.WeakTimerTask;
import com.sw.dvblib.SWBooking;
import com.sw.dvblib.msg.cb.AVMsgCB;

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
import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;

public class EpgActivity extends BaseActivity {
    private static final String TAG = "EpgActivity";
    private static final long RELOAD_CHANNEL_PERIOD = 60 * 1000;
    private static final long RELOAD_CHANNEL_DELAY = 5 * 1000;
    private static final long EPG_CHANGE_PLAY_PROG_DELAY = 1000;
    private static final long HIDE_EPG_LOADING_DELAY = 30 * 1000;
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
        showLoadingEpg();

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
                mCurrentFocusDate = i;
                updateDateFocus();
                updateCurrentDate();
                break;
            }
        }

        if (hasFocus) {
            updateEpgChannel(v.getId());
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
    private int mCurrentFocusDate;
    private boolean mDateButtonFocus;

    private boolean mBookEpg;

    private LoadEpgChannelRunnable mLoadEpgChannelRunnable;
    // 外层key为频道号progNo，内层key为日期id
    private SparseArray<SparseArray<List<EpgEvent_t>>> mEpgChannelCacheMap = new SparseArray<>();

    private RealTimeHelper mRealTimeHelper;

    private AVMsgCB mPlayMsgCB = new PlayMsgCB();

    private static class EpgMsgHandler extends WeakHandler<EpgActivity> {
        static final int MSG_PLAY_SELECT_PROG = 0;
        static final int MSG_PROG_CHANGE_LOAD = 1;
        static final int MSG_UPDATE_DATE = 2;
        static final int MSG_HIDE_EPG_LOADING = 3;

        EpgMsgHandler(EpgActivity view) {
            super(view);
        }

        @Override
        protected void handleMsg(Message msg) {
            EpgActivity context = mWeakReference.get();
            switch (msg.what) {
                case MSG_PLAY_SELECT_PROG:
                    int conditon = msg.arg1;
                    int ProgNo = context.mProgAdapter.getItem(context.mCurrProgSelectPosition).ProgNo;
                    SWPDBaseManager.getInstance().setCurrProgNo(ProgNo);
                    UIApiManager.getInstance().startPlayProgNo(ProgNo, conditon);
                    break;
                case MSG_PROG_CHANGE_LOAD:
                    context.updateEpgChannel(msg.arg1);
                    break;
                case MSG_UPDATE_DATE:
                    context.initCurrentDate();
                    context.initEpgChannelDate();
                    break;
                case MSG_HIDE_EPG_LOADING:
                    context.hideLoadingEpg();
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

            PDPMInfo_t pdpm = SWPDBaseManager.getInstance().getCurrProgInfo();
            if (null == pdpm) return;
            SWEpgManager.getInstance().sentDataReq(pdpm.Sat, pdpm.TsID, pdpm.ServID);

            context.notifyProgChange(new HandlerMsgModel(EpgMsgHandler.MSG_PROG_CHANGE_LOAD, context.mCurrentFocusDate, RELOAD_CHANNEL_DELAY), true);
            context.notifyUpdateDate();

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    SparseArray<List<EpgEvent_t>> epgChannelListMap = context.mEpgChannelCacheMap.get(pdpm.ProgNo);
                    if (epgChannelListMap != null) {
                        List<EpgEvent_t> epgChannelList = epgChannelListMap.get(context.mCurrentFocusDate);
                        if (epgChannelList == null || epgChannelList.isEmpty()) {
                            context.showLoadingEpg();
                        }
                    } else {
                        context.showLoadingEpg();
                    }
                }
            });
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_epg;
    }

    @Override
    protected void setup() {
        SWPDBaseManager.getInstance().setCurrProgType(0, 0);
        mEpgMsgHandler = new EpgMsgHandler(this);
        mUpdateChannelTimer = new Timer();
        mUpdateChannelTimer.schedule(new UpdateEpgChannelTimerTask(this), 0, RELOAD_CHANNEL_PERIOD);

        mLoadEpgChannelRunnable = new LoadEpgChannelRunnable(this, R.id.btn_date_0);

        initCurrentDate();
        initEpgChannelDate();
        initEpgList();
        initEpgChannelList();
        initSurfaceView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        SWDVBManager.getInstance().regMsgHandler(Constants.LOCK_CALLBACK_MSG_ID, Looper.getMainLooper(), mPlayMsgCB);
        updateEpgChannel(mCurrentFocusDate);
        startUpdateRealTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SWDVBManager.getInstance().unRegMsgHandler(Constants.LOCK_CALLBACK_MSG_ID, mPlayMsgCB);
        mRealTimeHelper.stop();
        UIApiManager.getInstance().stopPlay(0); // 跳转或销毁界面要停止播放
    }

    @Override
    protected void onDestroy() {
        if (mBookEpg) SWBookingManager.getInstance().updateDBase(0);

        dismissPasswordDialog();
        if (mUpdateChannelTimer != null) {
            mUpdateChannelTimer.cancel();
            mUpdateChannelTimer.purge();
            mUpdateChannelTimer = null;
        }
        super.onDestroy();
    }

    private void initCurrentDate() {
        SysTime_t sysTime = SWTimerManager.getInstance().getSysTime();
        if (sysTime != null) {
            String date = sysTime.Year + "-" + sysTime.Month + "-" + sysTime.Day;
            mTvCurrentDate.setText(date);
        }
    }

    private void updateCurrentDate() {
        SysTime_t sysTime = SWTimerManager.getInstance().getSysTime();
        if (sysTime != null) {
            int maxDayOfMonth = TimeUtils.getDayOfMonthByYearAndMonth(sysTime.Year, sysTime.Month);
            int year = sysTime.Year;
            int month = sysTime.Month;
            int day = sysTime.Day + mCurrentFocusDate;
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
        SysTime_t sysTime = SWTimerManager.getInstance().getSysTime();
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

    private void initEpgList() {
        mProgAdapter = new EpgProgListAdapter(this, new ArrayList<>());
        mLvProgList.setAdapter(mProgAdapter);
        mLvProgList.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (mCurrProgSelectPosition >= mProgAdapter.getCount() - 1) {
                        mLvProgList.setSelection(0);
                        return true;
                    }
                }
                if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mDates[0].requestFocus();
                    return true;
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
        mEpgChannelAdapter = new EpgChannelListAdapter(this, new ArrayList<>());
        mLvEpgChannel.setAdapter(mEpgChannelAdapter);

        mLvEpgChannel.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
                    mLvEpgChannel.setNextFocusUpId(mDateIds[mCurrentFocusDate]);
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
                    if (mCurrentFocusDate == 0) {
                        mLvProgList.requestFocus();
                    } else {
                        mDates[--mCurrentFocusDate].requestFocus();
                    }
                    return true;
                } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
                    if (mCurrentFocusDate < mDates.length - 1) {
                        mDates[++mCurrentFocusDate].requestFocus();
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
                if (mProgAdapter.getCount() > 0 && mCurrProgSelectPosition < mProgAdapter.getCount()) {
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

    private void startUpdateRealTime() {
        mRealTimeHelper = new RealTimeHelper(this);
        mRealTimeHelper.setOnRealTimeListener(new RealTimeHelper.OnRealTimerListener() {
            @Override
            public void onRealTimeCallback(String realTime) {
                if (!TextUtils.isEmpty(realTime)) {
                    mTvSystemTime.setText(realTime);
                }
            }
        });
        mRealTimeHelper.start();
    }

    /**
     * Epg book对话框
     */
    private void showEpgBookingDialog() {
        if (mCurrProgSelectPosition >= mProgAdapter.getCount() && mCurrEpgSelectPosition >= mEpgChannelAdapter.getCount())
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
                        Log.i(TAG, "conflict book info = " + conflictBookInfo);
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

                        mBookEpg = true;
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
        if (mEpgChannelAdapter.getCount() > 0) {
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

        clearEpgChannelCache();
    }

    /**
     * 清空频道当天缓存，切换日期时重新加载或1分钟后自动重新刷新加载
     */
    private void clearEpgChannelCache() {
        if (mProgAdapter.getCount() > 0 && mEpgChannelCacheMap.size() > 0) {
            SparseArray<List<EpgEvent_t>> map = mEpgChannelCacheMap.get(mProgAdapter.getItem(mCurrProgSelectPosition).ProgNo);
            if (map != null) {
                List<EpgEvent_t> epgChannelList = map.get(mCurrentFocusDate);
                if (epgChannelList != null && !epgChannelList.isEmpty()) {
                    epgChannelList.clear();
                }
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
        mCurrentFocusDate = 0;
        updateDateFocus();

        notifyProgChange();
        notifyPlayProg(condition);
    }

    /**
     * 更新日期焦点背景
     */
    private void updateDateFocus() {
        for (int i = 0; i < mDates.length; i++) {
            if (i == mCurrentFocusDate) {
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
    private void updateEpgChannel(int id) {
        clearDateFocus();

        reloadEpgChannel(id);
    }

    private void reloadEpgChannel(int id) {
        if (mLoadEpgChannelRunnable != null && mProgAdapter.getCount() > 0 && mCurrProgSelectPosition < mProgAdapter.getCount()) {
            ThreadPoolManager.getInstance().remove(mLoadEpgChannelRunnable);
            mLoadEpgChannelRunnable.progNo = mProgAdapter.getItem(mCurrProgSelectPosition).ProgNo;
            mLoadEpgChannelRunnable.focusBtnId = id;
            ThreadPoolManager.getInstance().execute(mLoadEpgChannelRunnable);
        }
    }

    private static class LoadEpgChannelRunnable extends WeakRunnable<EpgActivity> {
        int progNo;
        int focusBtnId;

        LoadEpgChannelRunnable(EpgActivity view, int focusBtnId) {
            super(view);
            this.focusBtnId = focusBtnId;
        }

        @Override
        protected void loadBackground() {
            EpgActivity context = mWeakReference.get();

            SparseArray<List<EpgEvent_t>> epgChannelMap = context.mEpgChannelCacheMap.get(progNo);
            if (epgChannelMap == null) {
                epgChannelMap = new SparseArray<>();
                List<EpgEvent_t> epgChannelList = UIApiManager.getInstance().getCurrProgSchInfo(context.getFocusDateIndex(focusBtnId));
                if (epgChannelList == null) epgChannelList = new ArrayList<>();
                epgChannelMap.put(focusBtnId, epgChannelList);
                context.mEpgChannelCacheMap.put(progNo, epgChannelMap);
            } else {
                List<EpgEvent_t> epgChannelList = epgChannelMap.get(focusBtnId);
                if (epgChannelList == null || epgChannelList.isEmpty()) {
                    epgChannelList = UIApiManager.getInstance().getCurrProgSchInfo(context.getFocusDateIndex(focusBtnId));
                    if (epgChannelList == null) epgChannelList = new ArrayList<>();
                    epgChannelMap.put(focusBtnId, epgChannelList);
                }
            }

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    List<EpgEvent_t> epgChannelList = context.mEpgChannelCacheMap.get(progNo).get(focusBtnId);
                    if (epgChannelList != null && !epgChannelList.isEmpty()) {
                        context.notifyHideEpgLoading(new HandlerMsgModel(EpgMsgHandler.MSG_HIDE_EPG_LOADING)); // 有数据，epg加载隐藏
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

    private int getFocusDateIndex(int id) {
        switch (id) {
            case R.id.btn_date_0:
                return 0;
            case R.id.btn_date_1:
                return 1;
            case R.id.btn_date_2:
                return 2;
            case R.id.btn_date_3:
                return 3;
            case R.id.btn_date_4:
                return 4;
            case R.id.btn_date_5:
                return 5;
            case R.id.btn_date_6:
                return 6;
        }
        return 0;
    }

    private void showLoadingEpg() {
        if (mPbLoadingEpg.getVisibility() == View.GONE) {
            mPbLoadingEpg.setVisibility(View.VISIBLE);

            notifyHideEpgLoading();
        }
    }

    private void hideLoadingEpg() {
        if (mPbLoadingEpg.getVisibility() == View.VISIBLE) {
            mPbLoadingEpg.setVisibility(View.GONE);
        }
    }

    /**
     * 发送消息通知切换频道更新Epg Booking列表
     */
    private void notifyProgChange() {
        notifyProgChange(new HandlerMsgModel(EpgMsgHandler.MSG_PROG_CHANGE_LOAD, R.id.btn_date_0), true);
    }

    private void notifyProgChange(HandlerMsgModel msgModel, boolean isRemoveMsg) {
        if (isRemoveMsg) {
            HandlerMsgManager.getInstance().removeMessage(mEpgMsgHandler, EpgMsgHandler.MSG_PROG_CHANGE_LOAD);
        }
        HandlerMsgManager.getInstance().sendMessage(mEpgMsgHandler, msgModel);
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
                new HandlerMsgModel(EpgMsgHandler.MSG_PLAY_SELECT_PROG, condition, EPG_CHANGE_PLAY_PROG_DELAY));
    }

    /**
     * 发送消息通知隐藏Epg Booking加载对话框
     */
    private void notifyHideEpgLoading() {
        notifyHideEpgLoading(new HandlerMsgModel(EpgMsgHandler.MSG_HIDE_EPG_LOADING, HIDE_EPG_LOADING_DELAY));
    }

    private void notifyHideEpgLoading(HandlerMsgModel msgModel) {
        HandlerMsgManager.getInstance().removeMessage(mEpgMsgHandler, EpgMsgHandler.MSG_HIDE_EPG_LOADING);
        HandlerMsgManager.getInstance().sendMessage(mEpgMsgHandler, msgModel);
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
            mEpgChannelCacheMap.clear();
            updateEpgChannel(mCurrentFocusDate);
        }
    }

    private class PlayMsgCB extends AVMsgCB {
        @Override
        public int ProgPlay_SWAV_ISLOCKED(int type, int progno, int progindex, int home) {
            Log.i(TAG, "ProgPlay_SWAV_ISLOCKED---type:" + type + " progno:" + progno + " progindex:" + progindex + " home:" + home);
            showEnterPasswordDialog();
            return super.ProgPlay_SWAV_ISLOCKED(type, progno, progindex, home);
        }

        @Override
        public int ProgPlay_SWAV_ISLOCKTIME(int type, int progno, int progindex) {
            Log.i(TAG, "ProgPlay_SWAV_ISLOCKTIME---type:" + type + " progno:" + progno + " progindex:" + progindex);
            return super.ProgPlay_SWAV_ISLOCKTIME(type, progno, progindex);
        }
    }
}
