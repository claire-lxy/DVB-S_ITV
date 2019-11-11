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
import com.konkawise.dtv.DTVCommonManager;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.HandlerMsgManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.DTVBookingManager;
import com.konkawise.dtv.DTVPVRManager;
import com.konkawise.dtv.DTVDVBManager;
import com.konkawise.dtv.ScreenManager;
import com.konkawise.dtv.WeakToolManager;
import com.konkawise.dtv.base.BaseService;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.dialog.BookReadyDialog;
import com.konkawise.dtv.dialog.QuitRecordingDialog;
import com.konkawise.dtv.event.BookUpdateEvent;
import com.konkawise.dtv.event.RecordStateChangeEvent;
import com.konkawise.dtv.rx.RxBus;
import com.konkawise.dtv.utils.TimeUtils;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakToolInterface;
import com.sw.dvblib.DTVCommon;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.listener.CallbackListenerAdapter;

import java.text.MessageFormat;

import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Enum_Task;
import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Struct_PlayeTimer;
import vendor.konka.hardware.dtvmanager.V1_0.HBooking_Struct_Timer;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgBasicInfo;

public class BookService extends BaseService implements WeakToolInterface {
    private static final String TAG = "BookService";
    public static final int ACTION_BOOKING_PLAY = 1 << 1;
    public static final int ACTION_BOOKING_RECORD = 1 << 2;

    private BookReadyDialog mBookReadyDialog;
    private QuitRecordingDialog mQuitRecordingDialog;
    private BookCountDownHandler mBookCountDownHandler;
    private MsgEvent mMsgEvent;

    @Override
    public void onCreate() {
        super.onCreate();
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
        WeakToolManager.getInstance().removeWeakTool(this);
        if (mMsgEvent != null) {
            DTVDVBManager.getInstance().unregisterMsgEvent(Constants.MsgCallbackId.BOOK);
            mMsgEvent = null;
        }
        DTVDVBManager.getInstance().releaseResource();
        super.onDestroy();
    }

    private void registerBookMsg() {
        if (mMsgEvent == null) {
            Log.i(TAG, "register book msg");
            mMsgEvent = DTVDVBManager.getInstance().registerMsgEvent(Constants.MsgCallbackId.BOOK);
            mMsgEvent.registerCallbackListener(new CallbackListenerAdapter() {
                // 预订节目播放倒计时
                @Override
                public void TIME_onPlaySoon(int id) {
                    Log.i(TAG, "book play countdown");
                    showBookReadyDialog();
                }

                // 预录节目倒计时
                @Override
                public void TIME_onRecSoon(int id) {
                    Log.i(TAG, "book record countdown");
                    showBookReadyDialog();
                }

                // 开始预订节目播放
                @Override
                public void TIME_onTimeToPlay(int type, int id, int sat, int tsid, int servid, int evtid) {
                    Log.i(TAG, "book play start");
                    bookReady();
                }

                // 开始预录节目
                @Override
                public void TIME_onTimeToRec(int type, int id, int sat, int tsid, int servid, int evtid) {
                    Log.i(TAG, "book record start");
                    bookReady();
                }
            });
        }
    }

    private void showBookReadyDialog() {
        HBooking_Struct_Timer bookInfo = DTVBookingManager.getInstance().getReadyTimerInfo();
        if (bookInfo == null) return;
        HProg_Struct_ProgBasicInfo progInfo = DTVProgramManager.getInstance().getProgInfoByServiceId(bookInfo.servid, bookInfo.tsid, bookInfo.sat);
        if (progInfo == null) return;

        // 待机情况下唤醒设备显示弹框
        ScreenManager.getInstance().wakeupScreen(this);
        startListenPower();

        BookingModel bookingModel = new BookingModel(bookInfo, progInfo);
        String channelName = bookingModel.getBookChannelName();
        String mode = bookingModel.getBookMode(this);
        String content;
        String positive;
        if (bookInfo.schtype == HBooking_Enum_Task.RECORD) {
            content = getString(R.string.dialog_book_record_content);
            positive = getString(R.string.dialog_book_record);
        } else if (bookInfo.schtype == HBooking_Enum_Task.PLAY) {
            content = getString(R.string.dialog_book_play_content);
            positive = getString(R.string.dialog_book_play);
        } else {
            content = getString(R.string.dialog_book_standby_content);
            positive = getString(R.string.dialog_book_standby);
        }

        DTVCommon.TimeModel currTime = DTVCommonManager.getInstance().getLocalTime();
        DTVCommon.TimeModel bookTime = DTVCommonManager.getInstance().getTime(bookInfo.year, bookInfo.month, bookInfo.day, bookInfo.hour, bookInfo.minute, bookInfo.second);
        int countDownSeconds = TimeUtils.getTotalSeconds(currTime, bookTime);
        mBookReadyDialog = new BookReadyDialog(this)
                .content(MessageFormat.format(getString(R.string.dialog_book_ready_content), content, String.valueOf(countDownSeconds)))
                .channelName(channelName)
                .mode(mode)
                .setOnPositiveListener(positive, () -> {
                    dismissBookReadyDialog();
                    removeHandlerMsg();
                    cancelBook(bookInfo);
                    if (DTVPVRManager.getInstance().isRecording()) {
                        showQuitRecordingDialog(bookInfo);
                    } else {
                        bookReady(bookInfo);
                    }
                })
                .setOnNegativeListener(getString(R.string.dialog_book_cancel), () -> {
                    dismissBookReadyDialog();
                    removeHandlerMsg();
                    cancelBook(bookInfo);
                });
        if (mBookReadyDialog.getWindow() != null) {
            mBookReadyDialog.getWindow().setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        mBookReadyDialog.setCancelable(false);
        mBookReadyDialog.show();
        startCountDown(bookInfo, countDownSeconds);
    }

    private void startCountDown(HBooking_Struct_Timer bookInfo, int countDownSeconds) {
        removeHandlerMsg();
        mBookCountDownHandler = new BookCountDownHandler(this, bookInfo, countDownSeconds);
        HandlerMsgManager.getInstance().sendMessage(mBookCountDownHandler, new HandlerMsgModel(BookCountDownHandler.MSG_COUNT_DOWN_SECONDS));
    }

    private void showQuitRecordingDialog(HBooking_Struct_Timer bookInfo) {
        mQuitRecordingDialog = new QuitRecordingDialog(this);
        mQuitRecordingDialog.content(getString(R.string.dialog_quit_record_content))
                .setOnPositiveListener(getString(R.string.ok), () -> {
                    dismissQuitRecordingDialog();
                    RxBus.getInstance().post(new RecordStateChangeEvent(false)); // 通知Topmost停止录制
                    if (bookInfo != null) {
                        bookReady(bookInfo);
                    }
                })
                .setOnNegativeListener("", () -> {
                    dismissQuitRecordingDialog();
                    cancelBook(bookInfo);
                });
        if (mQuitRecordingDialog.getWindow() != null) {
            mQuitRecordingDialog.getWindow().setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        mQuitRecordingDialog.setCancelable(false);
        mQuitRecordingDialog.show();
    }

    private void updateBookReadyContent(HBooking_Struct_Timer bookInfo, int countDownSecond) {
        if (mBookReadyDialog != null && mBookReadyDialog.isShowing() && bookInfo != null) {
            String content;
            if (bookInfo.schtype == HBooking_Enum_Task.RECORD) {
                content = getString(R.string.dialog_book_record_content);
            } else if (bookInfo.schtype == HBooking_Enum_Task.PLAY) {
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

    private void notifyBookUpdate(HBooking_Struct_Timer bookInfo) {
        RxBus.getInstance().post(new BookUpdateEvent(bookInfo));
    }

    private static class BookCountDownHandler extends WeakHandler<BookService> {
        static final int MSG_COUNT_DOWN_SECONDS = 0;
        static final long COUNT_DOWN_SECOND_DELAY = 1000;
        HBooking_Struct_Timer bookInfo;
        int countDownSeconds;

        BookCountDownHandler(BookService view, HBooking_Struct_Timer bookInfo, int countDownSeconds) {
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
    private void bookReady(HBooking_Struct_Timer bookInfo) {
        if (bookInfo != null) {
            startBook(bookInfo.schtype, bookInfo.lasttime, bookInfo.sat, bookInfo.tsid, bookInfo.servid);
            notifyBookUpdate(bookInfo);
        }
    }

    /**
     * 接收消息到点执行预录或播放
     */
    private void bookReady() {
        HBooking_Struct_PlayeTimer readyBookInfo = DTVBookingManager.getInstance().getCurrPlayTimerInfo();
        if (readyBookInfo != null) {
            startBook(readyBookInfo.schtype, readyBookInfo.lasttime, readyBookInfo.sat, readyBookInfo.tsid, readyBookInfo.servid);
        }
    }

    /**
     * 取消book
     */
    private void cancelBook(HBooking_Struct_Timer bookInfo) {
        stopListenPower();

        if (bookInfo != null) {
            DTVBookingManager.getInstance().cancelPlayTimer(4, DTVBookingManager.getInstance().getCancelBookTimer(bookInfo));
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
        if (schtype == HBooking_Enum_Task.NONE) {
            ScreenManager.getInstance().standby(this);
        } else {
            Intent intent = new Intent();
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setComponent(new ComponentName(getPackageName(), getPackageName() + ".ui.Topmost"));
            if (schtype == HBooking_Enum_Task.PLAY) {
                intent.putExtra(Constants.IntentKey.INTENT_BOOK_TYPE, ACTION_BOOKING_PLAY);
            } else if (schtype == HBooking_Enum_Task.RECORD) {
                intent.putExtra(Constants.IntentKey.INTENT_BOOK_TYPE, ACTION_BOOKING_RECORD);
                intent.putExtra(Constants.IntentKey.INTENT_BOOK_SECONDS, lasttime);
            }

            intent.putExtra(Constants.IntentKey.INTENT_BOOK_SERVICEID, servid);
            intent.putExtra(Constants.IntentKey.INTENT_BOOK_TSID, tsid);
            intent.putExtra(Constants.IntentKey.INTENT_BOOK_SAT, sat);
            startActivity(intent);
        }
    }
}
