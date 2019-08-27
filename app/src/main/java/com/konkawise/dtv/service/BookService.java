package com.konkawise.dtv.service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.WindowManager;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.HandlerMsgManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWBookingManager;
import com.konkawise.dtv.SWDJAPVRManager;
import com.konkawise.dtv.SWDVBManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.SWTimerManager;
import com.konkawise.dtv.ScreenManager;
import com.konkawise.dtv.WeakToolManager;
import com.konkawise.dtv.base.BaseService;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.dialog.BookReadyDialog;
import com.konkawise.dtv.dialog.OnCommNegativeListener;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.QuitRecordingDialog;
import com.konkawise.dtv.event.BookRegisterListenerEvent;
import com.konkawise.dtv.event.BookUpdateEvent;
import com.konkawise.dtv.event.RecordStateChangeEvent;
import com.konkawise.dtv.utils.TimeUtils;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakToolInterface;
import com.sw.dvblib.SWBooking;
import com.sw.dvblib.SWDVB;
import com.sw.dvblib.msg.cb.TimeMsgCB;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.text.MessageFormat;

import vendor.konka.hardware.dtvmanager.V1_0.HForplayprog_t;
import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.SysTime_t;

public class BookService extends BaseService implements WeakToolInterface {
    private static final String TAG = "BookService";
    public static final int ACTION_BOOKING_PLAY = 1 << 1;
    public static final int ACTION_BOOKING_RECORD = 1 << 2;

    private BookReadyDialog mBookReadyDialog;
    private QuitRecordingDialog mQuitRecordingDialog;
    private BookCountDownHandler mBookCountDownHandler;
    private TimeMsgCB mTimeMsgCB;
    private SWDVB.DTVListener mDTVListener;

    @Override
    public void onCreate() {
        super.onCreate();
        EventBus.getDefault().register(this);
        Log.i(TAG, "book service create");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i(TAG, "book service start");
        registerBookMsg();
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.i(TAG, "book service destroy");
        EventBus.getDefault().unregister(this);
        WeakToolManager.getInstance().removeWeakTool(this);
        if (mDTVListener != null) {
            SWDVBManager.getInstance().unregisterDTVListener(mDTVListener);
            mDTVListener = null;
        }
        if (mTimeMsgCB != null) {
            SWDVBManager.getInstance().unRegMsgHandler(Constants.BOOK_CALLBACK_MSG_ID, mTimeMsgCB);
            mTimeMsgCB = null;
        }
        super.onDestroy();
    }

    private void registerBookMsg() {
        if (mTimeMsgCB == null) {
            Log.i(TAG, "register book msg");
            mTimeMsgCB = new BookMsgCB();
            mDTVListener = new SWDVB.DTVListener(SWDVB.GetInstance());
            SWDVBManager.getInstance().regMsgHandler(Constants.BOOK_CALLBACK_MSG_ID, getMainLooper(), mTimeMsgCB);
        }
    }

    private class BookMsgCB extends TimeMsgCB {
        // 预订节目播放倒计时
        @Override
        public int Timer_ITIS_SUBFORTIME(int id) {
            Log.i(TAG, "book play countdown");
            showBookReadyDialog();
            return super.Timer_ITIS_SUBFORTIME(id);
        }

        // 预录节目倒计时
        @Override
        public int Timer_ITIS_SUBFORRECTIME(int id) {
            Log.i(TAG, "book record countdown");
            showBookReadyDialog();
            return super.Timer_ITIS_SUBFORRECTIME(id);
        }

        // 开始预订节目播放
        @Override
        public int Timer_ITIS_TIMETOPLAY(int type, int id, int sat, int tsid, int servid, int evtid) {
            Log.i(TAG, "book play start");
            bookReady();
            return super.Timer_ITIS_TIMETOPLAY(type, id, sat, tsid, servid, evtid);
        }

        // 开始预录节目
        @Override
        public int Timer_ITIS_TIMETOREC(int type, int id, int sat, int tsid, int servid, int evtid) {
            Log.i(TAG, "book record start");
            bookReady();
            return super.Timer_ITIS_TIMETOREC(type, id, sat, tsid, servid, evtid);
        }
    }

    private void showBookReadyDialog() {
        HSubforProg_t bookInfo = SWBookingManager.getInstance().getReadyProgInfo();
        if (bookInfo == null) return;
        PDPInfo_t progInfo = SWPDBaseManager.getInstance().getProgInfoByServiceId(bookInfo.servid, bookInfo.tsid, bookInfo.sat);
        if (progInfo == null) return;

        // 待机情况下唤醒设备显示弹框
        ScreenManager.getInstance().wakeupScreen(this);
        startListenPower();

        BookingModel bookingModel = new BookingModel(bookInfo, progInfo);
        String channelName = bookingModel.getBookChannelName();
        String mode = bookingModel.getBookMode(this);
        String content;
        String positive;
        if (bookInfo.schtype == SWBooking.BookSchType.RECORD.ordinal()) {
            content = getString(R.string.dialog_book_record_content);
            positive = getString(R.string.dialog_book_record);
        } else if (bookInfo.schtype == SWBooking.BookSchType.PLAY.ordinal()) {
            content = getString(R.string.dialog_book_play_content);
            positive = getString(R.string.dialog_book_play);
        } else {
            content = getString(R.string.dialog_book_standby_content);
            positive = getString(R.string.dialog_book_standby);
        }

        SysTime_t currTime = SWTimerManager.getInstance().getLocalTime();
        SysTime_t bookTime = SWTimerManager.getInstance().getTime(bookInfo.year, bookInfo.month, bookInfo.day, bookInfo.hour, bookInfo.minute, bookInfo.second);
        int countDownSeconds = TimeUtils.getTotalSeconds(currTime, bookTime);
        mBookReadyDialog = new BookReadyDialog(this)
                .content(MessageFormat.format(getString(R.string.dialog_book_ready_content), content, String.valueOf(countDownSeconds)))
                .channelName(channelName)
                .mode(mode)
                .setOnPositiveListener(positive, new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        dismissBookReadyDialog();
                        removeHandlerMsg();
                        cancelBook(bookInfo);
                        if (SWDJAPVRManager.getInstance().isRecording()) {
                            showQuitRecordingDialog(bookInfo);
                        } else {
                            bookReady(bookInfo);
                        }
                    }
                })
                .setOnNegativeListener(getString(R.string.dialog_book_cancel), new OnCommNegativeListener() {
                    @Override
                    public void onNegativeListener() {
                        dismissBookReadyDialog();
                        removeHandlerMsg();
                        cancelBook(bookInfo);
                    }
                });
        if (mBookReadyDialog.getWindow() != null) {
            mBookReadyDialog.getWindow().setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        mBookReadyDialog.setCancelable(false);
        mBookReadyDialog.show();
        startCountDown(bookInfo, countDownSeconds);
    }

    private void startCountDown(HSubforProg_t bookInfo, int countDownSeconds) {
        removeHandlerMsg();
        mBookCountDownHandler = new BookCountDownHandler(this, bookInfo, countDownSeconds);
        HandlerMsgManager.getInstance().sendMessage(mBookCountDownHandler, new HandlerMsgModel(BookCountDownHandler.MSG_COUNT_DOWN_SECONDS));
    }

    private void showQuitRecordingDialog(HSubforProg_t bookInfo) {
        mQuitRecordingDialog = new QuitRecordingDialog(this);
        mQuitRecordingDialog.content(getString(R.string.dialog_quit_record_content))
                .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        dismissQuitRecordingDialog();
                        EventBus.getDefault().post(new RecordStateChangeEvent(false)); // 通知Topmost停止录制
                        if (bookInfo != null) {
                            bookReady(bookInfo);
                        }
                    }
                })
                .setOnNegativeListener("", new OnCommNegativeListener() {
                    @Override
                    public void onNegativeListener() {
                        dismissQuitRecordingDialog();
                        cancelBook(bookInfo);
                    }
                });
        if (mQuitRecordingDialog.getWindow() != null) {
            mQuitRecordingDialog.getWindow().setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        mQuitRecordingDialog.setCancelable(false);
        mQuitRecordingDialog.show();
    }

    private void updateBookReadyContent(HSubforProg_t bookInfo, int countDownSecond) {
        if (mBookReadyDialog != null && mBookReadyDialog.isShowing() && bookInfo != null) {
            String content;
            if (bookInfo.schtype == SWBooking.BookSchType.RECORD.ordinal()) {
                content = getString(R.string.dialog_book_record_content);
            } else if (bookInfo.schtype == SWBooking.BookSchType.PLAY.ordinal()) {
                content = getString(R.string.dialog_book_play_content);
            } else {
                content = getString(R.string.dialog_book_standby_content);
            }
            mBookReadyDialog.updateContent(MessageFormat.format(getString(R.string.dialog_book_ready_content), content, String.valueOf(countDownSecond)));
        }
    }

    private void dismissBookReadyDialog() {
        if (mBookReadyDialog != null && mBookReadyDialog.isShowing()) {
            mBookReadyDialog.dismiss();
            mBookReadyDialog = null;
        }
    }

    private void dismissQuitRecordingDialog() {
        if (mQuitRecordingDialog != null && mQuitRecordingDialog.isShowing()) {
            mQuitRecordingDialog.dismiss();
            mQuitRecordingDialog = null;
        }
    }

    private void notifyBookUpdate(HSubforProg_t bookInfo) {
        EventBus.getDefault().post(new BookUpdateEvent(bookInfo));
    }

    private static class BookCountDownHandler extends WeakHandler<BookService> {
        static final int MSG_COUNT_DOWN_SECONDS = 0;
        static final long COUNT_DOWN_SECOND_DELAY = 1000;
        HSubforProg_t bookInfo;
        int countDownSeconds;

        BookCountDownHandler(BookService view, HSubforProg_t bookInfo, int countDownSeconds) {
            super(view);
            this.bookInfo = bookInfo;
            this.countDownSeconds = countDownSeconds;
        }

        @Override
        protected void handleMsg(Message msg) {
            BookService context = mWeakReference.get();

            if (msg.what == MSG_COUNT_DOWN_SECONDS) {
                context.updateBookReadyContent(bookInfo, --countDownSeconds);
                if (countDownSeconds <= 0) {
                    context.bookReady();
                } else {
                    HandlerMsgManager.getInstance().sendMessage(context.mBookCountDownHandler, new HandlerMsgModel(MSG_COUNT_DOWN_SECONDS, COUNT_DOWN_SECOND_DELAY));
                }
            }
        }
    }

    private void removeHandlerMsg() {
        if (mBookCountDownHandler != null) {
            mBookCountDownHandler.release();
            mBookCountDownHandler = null;
        }
    }

    /**
     * 启动监听待机按键服务，主要解决在book到点唤醒屏幕后，在倒计时的时候直接再次待机导致book无法再启动
     */
    private void startListenPower() {
        if (isAppInBackground()) {
            PowerService.bootService(new Intent(this, PowerService.class));
        }
    }

    private void stopListenPower() {
        if (isAppInBackground()) {
            PowerService.pauseService(new Intent(this, PowerService.class));
        }
    }

    private boolean isAppInBackground() {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        ComponentName cn = am.getRunningTasks(1).get(0).topActivity;
        if (cn != null) {
            String pkgName = cn.getPackageName();
            return TextUtils.isEmpty(pkgName) || !pkgName.equals(getPackageName());
        }
        return false;
    }

    /**
     * 立即跳转执行预录或播放
     */
    private void bookReady(HSubforProg_t bookInfo) {
        if (bookInfo != null) {
            startBook(bookInfo.schtype, bookInfo.lasttime, bookInfo.sat, bookInfo.tsid, bookInfo.servid);
            notifyBookUpdate(bookInfo);
        }
    }

    /**
     * 接收消息到点执行预录或播放
     */
    private void bookReady() {
        HForplayprog_t readyBookInfo = SWBookingManager.getInstance().getCurrSubForPlay();
        if (readyBookInfo != null) {
            startBook(readyBookInfo.schtype, readyBookInfo.lasttime, readyBookInfo.sat, readyBookInfo.tsid, readyBookInfo.servid);
        }
    }

    /**
     * 取消book
     */
    private void cancelBook(HSubforProg_t bookInfo) {
        stopListenPower();

        if (bookInfo != null) {
            SWBookingManager.getInstance().cancelSubForPlay(4, SWBookingManager.getInstance().getCancelBookProg(bookInfo));
            notifyBookUpdate(bookInfo);
        }
    }

    /**
     * 启动book
     */
    private void startBook(int schtype, int lasttime, int sat, int tsid, int servid) {
        removeHandlerMsg();
        dismissBookReadyDialog();

        ScreenManager.getInstance().wakeupScreen(this);
        stopListenPower();

        // 进入待机
        if (schtype == SWBooking.BookSchType.NONE.ordinal()) {

        } else {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName(getPackageName(), getPackageName() + ".ui.Topmost"));
            if (schtype == SWBooking.BookSchType.PLAY.ordinal()) {
                intent.putExtra(Constants.IntentKey.INTENT_BOOK_TYPE, ACTION_BOOKING_PLAY);
            } else if (schtype == SWBooking.BookSchType.RECORD.ordinal()) {
                intent.putExtra(Constants.IntentKey.INTENT_BOOK_TYPE, ACTION_BOOKING_RECORD);
                intent.putExtra(Constants.IntentKey.INTENT_BOOK_SECONDS, lasttime);
            }

            intent.putExtra(Constants.IntentKey.INTENT_BOOK_SERVICEID, servid);
            intent.putExtra(Constants.IntentKey.INTENT_BOOK_TSID, tsid);
            intent.putExtra(Constants.IntentKey.INTENT_BOOK_SAT, sat);
            startActivity(intent);
        }
    }

    // 按home或退出apk时，通知注册book消息通道
    // 进入apk时，通知解注册book消息通道
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveRegisterBookListener(BookRegisterListenerEvent event) {
        if (mDTVListener != null) {
            if (event.isRegisterBookListener) {
                SWDVBManager.getInstance().registerDTVListener(mDTVListener);
            } else {
                SWDVBManager.getInstance().unregisterDTVListener(mDTVListener);
            }
        }
    }
}
