package com.konkawise.dtv.ui;

import android.content.Intent;
import android.os.Message;
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
import com.konkawise.dtv.SWBookingManager;
import com.konkawise.dtv.SWEpgManager;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.SWTimerManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.UIApiManager;
import com.konkawise.dtv.adapter.ChannleInfoAdapter;
import com.konkawise.dtv.adapter.EPGListAdapter;
import com.konkawise.dtv.annotation.BookConflictType;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.bean.EpgBookParameterModel;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.PasswordDialog;
import com.konkawise.dtv.egphandle.EpgHandleCallback;
import com.konkawise.dtv.egphandle.EpgHandleResult;
import com.konkawise.dtv.egphandle.EpgHandler;
import com.konkawise.dtv.egphandle.MenuLockEpgHandler;
import com.konkawise.dtv.egphandle.ParentLockEpgHandler;
import com.konkawise.dtv.egphandle.PayEpgHandler;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.view.TVListView;
import com.konkawise.dtv.weaktool.RealTimeHelper;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.konkawise.dtv.weaktool.WeakTimerTask;
import com.sw.dvblib.SWBooking;

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
            epgHandle();
        } else {
            epgItemSelect();
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
                break;
            }
        }

        if (hasFocus) {
            updateEpgChannel(v.getId());
        }
    }

    private EPGListAdapter mProgAdapter;
    private ChannleInfoAdapter mEpgChannelAdapter;
    private EpgMsgHandler mEpgMsgHandler;
    private Timer mUpdateChannelTimer;
    /**
     * 如果开启了MenuLock但能进入Epg，说明已经输入过密码，不再需要再输入密码，mPasswordEntered=true
     * 如果没有开启MenuLock，但开启了ParentLock，遇到添加了ParentLock的Epg则需要输入一次密码
     * 如果MenuLock和ParentLock都没有开启，默认为已输入密码mPasswordEntered=true
     */
    private boolean mPasswordEntered = true;
    /**
     * 创建dialog前ListView的item会回调onItemSelect()调用egpHandle()
     * 添加标志位防止setOnKeyListener时同时回调监听到遥控器上下按键
     */
    private boolean mPasswordDialogShowing;
    private PasswordDialog mPasswordDialog;
    private EpgHandler mEpgHandler;

    private Button[] mDates;
    private int mCurrProgSelectPosition;
    private int mCurrEpgSelectPosition;
    private int mCurrentFocusDate;
    private boolean mDateButtonFocus;

    private boolean mBookEpg;

    private LoadEpgChannelRunnable mLoadEpgChannelRunnable;

    private RealTimeHelper mRealTimeHelper;

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
                    int ProgNo = context.mProgAdapter.getItem(context.mCurrProgSelectPosition).ProgNo;
                    SWPDBaseManager.getInstance().setCurrProgNo(ProgNo);
                    UIApiManager.getInstance().startPlayProgNo(ProgNo, 0);
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
                    context.showLoadingEpg();
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

        initEpgHandler();
        initCurrentDate();
        initEpgChannelDate();
        initEpgList();
        initEpgChannelList();
        initSurfaceView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 刷新booking展示
        updateEpgChannel(mCurrentFocusDate);
        startUpdateRealTime();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mRealTimeHelper.stop();
    }

    @Override
    protected void onDestroy() {
        // 将booking刷入flash
        if (mBookEpg) SWBookingManager.getInstance().updateDBase(0);

        dismissPasswordDialog();
        removeLoadEpgRunnable();
        if (mUpdateChannelTimer != null) {
            mUpdateChannelTimer.cancel();
            mUpdateChannelTimer.purge();
            mUpdateChannelTimer = null;
        }
        super.onDestroy();
    }

    private void initEpgHandler() {
        EpgHandler channelLockEpgHandler = new ParentLockEpgHandler(null);
        EpgHandler menuLockEpgHandler = new MenuLockEpgHandler(channelLockEpgHandler);
        mEpgHandler = new PayEpgHandler(menuLockEpgHandler);

        mPasswordEntered = SWFtaManager.getInstance().isOpenParentLock();
        // 如果开启了MenuLock，遇到需要ParentLock的Epg就不再需要再输入密码了
        mPasswordEntered = SWFtaManager.getInstance().isOpenMenuLock();
    }

    private void initCurrentDate() {
        SysTime_t sysTime = SWTimerManager.getInstance().getSysTime();
        if (sysTime != null) {
            String date = sysTime.Year + "-" + sysTime.Month + "-" + sysTime.Day;
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
        mProgAdapter = new EPGListAdapter(this, new ArrayList<>());
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
        mEpgChannelAdapter = new ChannleInfoAdapter(this, new ArrayList<>());
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

    private void showEpgBookingDialog() {
        if (mCurrProgSelectPosition >= mProgAdapter.getCount() && mCurrEpgSelectPosition >= mEpgChannelAdapter.getCount())
            return;

        PDPMInfo_t progInfo = mProgAdapter.getItem(mCurrProgSelectPosition);
        EpgEvent_t eventInfo = mEpgChannelAdapter.getItem(mCurrEpgSelectPosition);
        final HSubforProg_t bookInfo = SWBookingManager.getInstance().progIsSubFored(progInfo.Sat, progInfo.TsID, progInfo.ServID, eventInfo.uiEventId);
        boolean isBooked = bookInfo != null;

        List<String> epgBookCheckContent = Arrays.asList(isBooked ? mEpgBookedArray : mEpgBookingArray);
        new CommCheckItemDialog()
                .title(getString(R.string.epg_booking_dialog_title))
                .content(epgBookCheckContent)
                .position(getBookPosition(epgBookCheckContent, bookInfo))
                .setOnDismissListener(new CommCheckItemDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(CommCheckItemDialog dialog, int position, String checkContent) {
                        HSubforProg_t newBookInfo = null;
                        if (bookInfo == null) {
                            SysTime_t startTimeInfo = SWTimerManager.getInstance().getStartTime(eventInfo);
                            SysTime_t endTimeInfo = SWTimerManager.getInstance().getEndTime(eventInfo);
                            int bookSchType = getBookCheckSchType(checkContent);
                            EpgBookParameterModel parameterModel = new EpgBookParameterModel();
                            parameterModel.progInfo = progInfo;
                            parameterModel.eventInfo = eventInfo;
                            parameterModel.startTimeInfo = startTimeInfo;
                            parameterModel.endTimeInfo = endTimeInfo;
                            parameterModel.schtype = bookSchType;
                            parameterModel.schway = SWBooking.BookWay.EPG.ordinal();
                            newBookInfo = SWBookingManager.getInstance().newBookProg(parameterModel);
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

                        mBookEpg = true;
                    }
                }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
    }

    private void bookHandle(@BookConflictType int conflictType, int bookSchType, HSubforProg_t bookInfo, HSubforProg_t conflictBookInfo) {
        if (bookSchType == SWBooking.BookSchType.NONE.ordinal()) { // 取消book
            SWBookingManager.getInstance().deleteProg(bookInfo);
        } else if (bookSchType == SWBooking.BookSchType.PLAY.ordinal()
                || bookSchType == SWBooking.BookSchType.RECORD.ordinal()) { // 添加book
            if (conflictType == Constants.BOOK_CONFLICT_REPLACE) {
                BookingModel conflictBookModel = new BookingModel();
                conflictBookModel.bookInfo = conflictBookInfo;
                conflictBookModel.progInfo = SWPDBaseManager.getInstance().getProgInfoByServiceId(conflictBookInfo.servid, conflictBookInfo.tsid, conflictBookInfo.sat);

                BookingModel newBookModel = new BookingModel();
                newBookModel.bookInfo = bookInfo;
                newBookModel.progInfo = SWPDBaseManager.getInstance().getProgInfoByServiceId(bookInfo.servid, bookInfo.tsid, bookInfo.sat);
                showReplaceBookDialog(conflictBookModel, newBookModel);
            } else {
                SWBookingManager.getInstance().addProg(conflictType, conflictBookInfo, bookInfo);
            }
        }
        // 刷新列表，等待接口提供EpgEvent_t字段判断显示或隐藏图标
        // EpgEvent_t eventInfo = mEpgChannelAdapter.getItem(mCurrEpgSelectPosition);
        // eventInfo.xxx = play/record/none;
        // mAdapter.updateData(mCurrEpgSelectPosition, eventInfo);
    }

    private void showReplaceBookDialog(BookingModel conflictBookModel, BookingModel newBookModel) {
        new CommTipsDialog()
                .title(getString(R.string.dialog_book_conflict_title))
                .content(MessageFormat.format(getString(R.string.dialog_book_conflict_content),
                        conflictBookModel.getBookDate(this, BookingModel.BOOK_TIME_SEPARATOR_EMPTY), conflictBookModel.progInfo.Name, conflictBookModel.getBookType(this)))
                .lineSpacing(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 35, getResources().getDisplayMetrics()))
                .setOnPositiveListener(getString(R.string.dialog_book_conflict_positive), new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        SWBookingManager.getInstance().replaceProg(conflictBookModel.bookInfo, newBookModel.bookInfo);
                        // 刷新列表，等待接口提供EpgEvent_t字段判断显示或隐藏图标
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
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
                            epgItemSelect();
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
        if (mPasswordDialog != null && mPasswordDialog.getDialog().isShowing()) {
            mPasswordDialog.dismiss();
            mPasswordDialog = null;
        }
    }

    private void nextSelectEpgItem() {
        mCurrProgSelectPosition++;
        if (mCurrProgSelectPosition >= mProgAdapter.getCount()) {
            mCurrProgSelectPosition = mProgAdapter.getCount() - 1;
        }
        epgListFocus();
        notifyProgChange();
    }

    private void lastSelectEpgItem() {
        mCurrProgSelectPosition--;
        if (mCurrProgSelectPosition < 0) {
            mCurrProgSelectPosition = 0;
        }
        epgListFocus();
        notifyProgChange();
    }

    private void epgListFocus() {
        mLvProgList.requestFocus();
        mLvProgList.setSelection(mCurrProgSelectPosition);
        mProgAdapter.setSelectPosition(mCurrProgSelectPosition);
    }

    private void epgItemSelect() {
        mCurrentFocusDate = 0;
        updateDateFocus();

        notifyProgChange();
        notifyPlayProg();
    }

    private void epgHandle() {
        PDPMInfo_t pdpmInfo_t = mProgAdapter.getItem(mCurrProgSelectPosition);
        mEpgHandler.epgHandle(pdpmInfo_t, new EpgHandleCallback() {
            @Override
            public void handleResult(EpgHandleResult epgHandleResult) {
                if (epgHandleResult.isEpgHandled()) {
                    // 如果节目是可播放的，当前对话框在显示，取消显示
                    dismissPasswordDialog();

                    epgItemSelect();
                } else {
                    if (epgHandleResult.getEpgHandleType() == EpgHandleResult.PARENT_LOCK_EPG) {
                        if (mPasswordDialog == null) {
                            UIApiManager.getInstance().stopPlay(0);
                            showEnterPasswordDialog();
                        }
                    }
                }
            }
        });
    }

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

    private void updateEpgChannel(int id) {
        clearDateFocus();

        reloadEpgChannel(id);
    }

    private void removeLoadEpgRunnable() {
        if (mLoadEpgChannelRunnable != null) {
            ThreadPoolManager.getInstance().remove(mLoadEpgChannelRunnable);
        }
    }

    private void reloadEpgChannel(int id) {
        removeLoadEpgRunnable();

        if (mLoadEpgChannelRunnable != null) {
            mLoadEpgChannelRunnable.updateId(id);
            ThreadPoolManager.getInstance().execute(mLoadEpgChannelRunnable);
        }
    }

    private static class LoadEpgChannelRunnable extends WeakRunnable<EpgActivity> {
        int focusBtnId;

        LoadEpgChannelRunnable(EpgActivity view, int focusBtnId) {
            super(view);
            this.focusBtnId = focusBtnId;
        }

        void updateId(int id) {
            focusBtnId = id;
        }

        @Override
        protected void loadBackground() {
            EpgActivity context = mWeakReference.get();
            List<EpgEvent_t> epgBookingList = UIApiManager.getInstance().getCurrProgSchInfo(context.getFocusDateIndex(focusBtnId));
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (epgBookingList != null) {
                        context.notifyHideEpgLoading(new HandlerMsgModel(EpgMsgHandler.MSG_HIDE_EPG_LOADING)); // 有数据，epg加载隐藏
                        context.mEpgChannelAdapter.updateData(epgBookingList);
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
    private void notifyPlayProg() {
        HandlerMsgManager.getInstance().removeMessage(mEpgMsgHandler, EpgMsgHandler.MSG_PLAY_SELECT_PROG);
        HandlerMsgManager.getInstance().sendMessage(mEpgMsgHandler,
                new HandlerMsgModel(EpgMsgHandler.MSG_PLAY_SELECT_PROG, EPG_CHANGE_PLAY_PROG_DELAY));
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
            startActivity(new Intent(this, BookListActivity.class));
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
