package com.konkawise.dtv.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.util.Log;
import android.view.WindowManager;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.HandlerMsgManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWBookingManager;
import com.konkawise.dtv.SWDJAPVRManager;
import com.konkawise.dtv.SWDVBManager;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.WeakToolManager;
import com.konkawise.dtv.base.BaseService;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.dialog.BookReadyDialog;
import com.konkawise.dtv.dialog.OnCommNegativeListener;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.QuitRecordingDialog;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakToolInterface;
import com.sw.dvblib.SWBooking;
import com.sw.dvblib.msg.cb.TimeMsgCB;

import java.text.MessageFormat;

import vendor.konka.hardware.dtvmanager.V1_0.HForplayprog_t;
import vendor.konka.hardware.dtvmanager.V1_0.HPDPPlayInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;

public class BookService extends BaseService implements WeakToolInterface {
    private static final String TAG = "BookService";
    public static final int ACTION_BOOKING_PLAY = 1 << 1;
    public static final int ACTION_BOOKING_RECORD = 1 << 2;

    private BookReadyDialog mBookStandbyDialog;
    private QuitRecordingDialog mQuitRecordingDialog;
    private BookCountDownHandler mBookCountDownHandler;
    private TimeMsgCB mTimeMsgCB = new BookMsgCB();

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
        SWDVBManager.getInstance().unRegMsgHandler(Constants.BOOK_CALLBACK_MSG_ID, mTimeMsgCB);
        super.onDestroy();
    }

    private void registerBookMsg() {
        SWDVBManager.getInstance().regMsgHandler(Constants.BOOK_CALLBACK_MSG_ID, getMainLooper(), mTimeMsgCB);
    }

    private class BookMsgCB extends TimeMsgCB {
        // 预订节目播放倒计时
        @Override
        public int Timer_ITIS_SUBFORTIME(int id) {
            Log.i(TAG, "预订节目播放倒计时");
            showBookReadyDialog();
            return 0;
        }

        // 预录节目倒计时
        @Override
        public int Timer_ITIS_SUBFORRECTIME(int id) {
            Log.i(TAG, "预录节目倒计时");
            showBookReadyDialog();
            return 0;
        }

        // 开始预订节目播放
        @Override
        public int Timer_ITIS_TIMETOPLAY(int type, int id, int sat, int tsid, int servid, int evtid) {
            Log.i(TAG, "开始预订节目播放");
            bookReady();
            return 0;
        }

        // 开始预录节目
        @Override
        public int Timer_ITIS_TIMETOREC(int type, int id, int sat, int tsid, int servid, int evtid) {
            Log.i(TAG, "开始预录节目");
            bookReady();
            return 0;
        }
    }

    private void showBookReadyDialog() {
        HSubforProg_t bookInfo = SWBookingManager.getInstance().getReadyProgInfo();
        if (bookInfo == null) return;
        PDPInfo_t progInfo = SWPDBaseManager.getInstance().getProgInfoByServiceId(bookInfo.servid, bookInfo.tsid, bookInfo.sat);
        if (progInfo == null) return;

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

        mBookStandbyDialog = new BookReadyDialog(this)
                .content(MessageFormat.format(getString(R.string.dialog_book_ready_content), content, String.valueOf(60)))
                .channelName(channelName)
                .mode(mode)
                .setOnPositiveListener(positive, new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        dismissBookReadyDialog();
                        removeHandlerMsg();
                        if (SWDJAPVRManager.getInstance().isRecording()) {
                            showQuitRecordingDialog();
                        } else {
                            SWBookingManager.getInstance().cancelSubForPlay(4, SWBookingManager.getInstance().getCancelBookProg(bookInfo)); // 取消定时器防止到点回调消息过来
                            bookReady(bookInfo);
                        }
                    }
                })
                .setOnNegativeListener(getString(R.string.dialog_book_cancel), new OnCommNegativeListener() {
                    @Override
                    public void onNegativeListener() {
                        dismissBookReadyDialog();
                        removeHandlerMsg();
                        SWBookingManager.getInstance().cancelSubForPlay(4, SWBookingManager.getInstance().getCancelBookProg(bookInfo));
                    }
                });
        if (mBookStandbyDialog.getWindow() != null) {
            mBookStandbyDialog.getWindow().setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        mBookStandbyDialog.show();
        mBookCountDownHandler = new BookCountDownHandler(this, bookInfo);
        HandlerMsgManager.getInstance().sendMessage(mBookCountDownHandler, new HandlerMsgModel(BookCountDownHandler.MSG_COUNT_DOWN_SECONDS));
    }

    private void showQuitRecordingDialog() {
        mQuitRecordingDialog = new QuitRecordingDialog(this);
        mQuitRecordingDialog.content(getString(R.string.dialog_quit_record_content))
                .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        dismissQuitRecordingDialog();
                        SWDJAPVRManager.getInstance().stopRecord();

                        HSubforProg_t bookProg = SWBookingManager.getInstance().getReadyProgInfo();
                        if (bookProg != null) {
                            bookReady(bookProg);
                        }
                    }
                })
                .setOnNegativeListener("", new OnCommNegativeListener() {
                    @Override
                    public void onNegativeListener() {
                        dismissQuitRecordingDialog();
                    }
                }).show();
    }

    private void updateBookReadyContent(HSubforProg_t bookInfo, int countDownSecond) {
        if (mBookStandbyDialog != null && mBookStandbyDialog.isShowing() && bookInfo != null) {
            String content;
            if (bookInfo.schtype == SWBooking.BookSchType.RECORD.ordinal()) {
                content = getString(R.string.dialog_book_record_content);
            } else if (bookInfo.schtype == SWBooking.BookSchType.PLAY.ordinal()) {
                content = getString(R.string.dialog_book_play_content);
            } else {
                content = getString(R.string.dialog_book_standby_content);
            }
            mBookStandbyDialog.updateContent(MessageFormat.format(getString(R.string.dialog_book_ready_content), content, String.valueOf(countDownSecond)));
        }
    }

    private void dismissBookReadyDialog() {
        if (mBookStandbyDialog != null && mBookStandbyDialog.isShowing()) {
            mBookStandbyDialog.dismiss();
            mBookStandbyDialog = null;
        }
    }

    private void dismissQuitRecordingDialog() {
        if (mQuitRecordingDialog != null && mQuitRecordingDialog.isShowing()) {
            mQuitRecordingDialog.dismiss();
            mQuitRecordingDialog = null;
        }
    }

    private static class BookCountDownHandler extends WeakHandler<BookService> {
        static final int MSG_COUNT_DOWN_SECONDS = 0;
        static final long COUNT_DOWN_SECOND_DELAY = 1000;
        private HSubforProg_t bookInfo;
        private int countDownSeconds;

        BookCountDownHandler(BookService view, HSubforProg_t bookInfo) {
            super(view);
            this.bookInfo = bookInfo;
            this.countDownSeconds = 60;
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
     * 立即跳转执行预录或播放
     */
    private void bookReady(HSubforProg_t bookInfo) {
        if (bookInfo != null) {
            startBook(bookInfo.schtype, bookInfo.lasttime);
        }
    }

    /**
     * 接收消息到点执行预录或播放
     */
    private void bookReady() {
        HForplayprog_t readyBookInfo = SWBookingManager.getInstance().getCurrSubForPlay();
        if (readyBookInfo != null) {
            startBook(readyBookInfo.schtype, readyBookInfo.lasttime);
        }
    }

    private void startBook(int schtype, int lasttime) {
        removeHandlerMsg();
        dismissBookReadyDialog();

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

            HPDPPlayInfo_t playInfo = SWFtaManager.getInstance().getCurrPlayInfo(0);
            if (playInfo != null) {
                intent.putExtra(Constants.IntentKey.INTENT_BOOK_PROG_TYPE, playInfo.Progtype);
                intent.putExtra(Constants.IntentKey.INTENT_BOOK_PROG_NUM, playInfo.Progno);
                startActivity(intent);
            }
        }
    }
}
