package com.konkawise.dtv.service;

import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Message;
import android.view.WindowManager;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.HandlerMsgManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWBookingManager;
import com.konkawise.dtv.SWDVBManager;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.WeakToolManager;
import com.konkawise.dtv.base.BaseService;
import com.konkawise.dtv.bean.BookingModel;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.dialog.BookStandbyDialog;
import com.konkawise.dtv.dialog.OnCommNegativeListener;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakToolInterface;
import com.sw.dvblib.MsgCB;
import com.sw.dvblib.SWBooking;
import com.sw.dvblib.SWDVB;

import java.text.MessageFormat;

import vendor.konka.hardware.dtvmanager.V1_0.HForplayprog_t;
import vendor.konka.hardware.dtvmanager.V1_0.HSubforProg_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;

public class BookListenerService extends BaseService implements WeakToolInterface {
    public static final int ACTION_BOOKING_PLAY = 1 << 1;
    public static final int ACTION_BOOKING_RECORD = 1 << 2;

    private BookStandbyDialog mBookStandbyDialog;
    private BookCountDownHandler mBookCountDownHandler;

    @Override
    public void onCreate() {
        super.onCreate();
        SWDVB.GetInstance();
        registerBookMsg();
    }

    private void showBookStandbyDialog() {
        HSubforProg_t bookProg = SWBookingManager.getInstance().getReadyProgInfo();
        if (bookProg == null) return;
        PDPInfo_t progInfo = SWPDBaseManager.getInstance().getProgInfoByServiceId(bookProg.servid, bookProg.tsid, bookProg.sat);
        if (progInfo == null) return;

        BookingModel bookingModel = new BookingModel(bookProg, progInfo);
        String channelName = bookingModel.getBookChannelName();
        String mode = bookingModel.getBookMode(this);
        String content;
        String positive;
        if (bookProg.schtype == SWBooking.BookSchType.RECORD.ordinal()) {
            content = getString(R.string.dialog_book_record_content);
            positive = getString(R.string.dialog_book_record);
        } else if (bookProg.schtype == SWBooking.BookSchType.PLAY.ordinal()) {
            content = getString(R.string.dialog_book_play_content);
            positive = getString(R.string.dialog_book_play);
        } else {
            content = getString(R.string.dialog_book_standby_content);
            positive = getString(R.string.dialog_book_standby);
        }

        mBookStandbyDialog = new BookStandbyDialog(this)
                .content(MessageFormat.format(getString(R.string.dialog_book_content), content, String.valueOf(bookProg.second)))
                .channelName(channelName)
                .mode(mode)
                .setOnPositiveListener(positive, new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        dismissBookStandbyDialog();
                        removeHandlerMsg();
                    }
                })
                .setOnNegativeListener(getString(R.string.dialog_book_cancel), new OnCommNegativeListener() {
                    @Override
                    public void onNegativeListener() {
                        dismissBookStandbyDialog();
                        removeHandlerMsg();
                        SWBookingManager.getInstance().cancelSubForPlay(0, SWBookingManager.getInstance().getCancelBookProg(bookProg));
                    }
                });
        if (mBookStandbyDialog.getWindow() != null) {
            mBookStandbyDialog.getWindow().setType(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                    WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        }
        mBookStandbyDialog.show();
        mBookCountDownHandler = new BookCountDownHandler(this, bookProg);
        HandlerMsgManager.getInstance().sendMessage(mBookCountDownHandler, new HandlerMsgModel(BookCountDownHandler.MSG_COUNT_DOWN_SECONDS));
    }

    private void dismissBookStandbyDialog() {
        if (mBookStandbyDialog != null && mBookStandbyDialog.isShowing()) {
            mBookStandbyDialog.dismiss();
            mBookStandbyDialog = null;
        }
    }

    private static class BookCountDownHandler extends WeakHandler<BookListenerService> {
        static final int MSG_COUNT_DOWN_SECONDS = 0;
        private HSubforProg_t bookInfo;
        private int countDownSeconds;

        BookCountDownHandler(BookListenerService view, HSubforProg_t bookInfo) {
            super(view);
            this.bookInfo = bookInfo;
            this.countDownSeconds = bookInfo.second;
        }

        @Override
        protected void handleMsg(Message msg) {
            BookListenerService context = mWeakReference.get();

            if (msg.what == MSG_COUNT_DOWN_SECONDS) {
                --countDownSeconds;
                if (countDownSeconds <= 0) {
                    context.startBook(bookInfo.servid, bookInfo.tsid, bookInfo.sat);
                } else {
                    HandlerMsgManager.getInstance().sendMessage(context.mBookCountDownHandler, new HandlerMsgModel(MSG_COUNT_DOWN_SECONDS, 1000));
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

    private void registerBookMsg() {
        SWDVBManager.getInstance().regMsgHandler(getMainLooper(), new MsgCB() {
            // 预订节目播放倒计时
            @Override
            public int Timer_ITIS_SUBFORTIME(int id) {
                showBookStandbyDialog();
                return super.Timer_ITIS_SUBFORTIME(id);
            }

            // 预录节目倒计时
            @Override
            public int Timer_ITIS_SUBFORRECTIME(int id) {
                showBookStandbyDialog();
                return super.Timer_ITIS_SUBFORRECTIME(id);
            }

            // 开始预订节目播放
            @Override
            public int Timer_ITIS_TIMETOPLAY(int type, int id, int sat, int tsid, int servid, int evtid) {
                startBook(servid, tsid, sat);
                return super.Timer_ITIS_TIMETOPLAY(type, id, sat, tsid, servid, evtid);
            }

            // 开始预录节目
            @Override
            public int Timer_ITIS_TIMETOREC(int type, int id, int sat, int tsid, int servid, int evtid) {
                startBook(servid, tsid, sat);
                return super.Timer_ITIS_TIMETOREC(type, id, sat, tsid, servid, evtid);
            }
        });
    }

    private void startBook(int servid, int tsid, int sat) {
        removeHandlerMsg();

        PDPInfo_t recordProg = SWPDBaseManager.getInstance().getProgInfoByServiceId(servid, tsid, sat);
        HForplayprog_t readyProg = SWBookingManager.getInstance().getCurrSubForPlay();
        if (readyProg != null && recordProg != null) {
            // 进入待机
            if (readyProg.schtype == SWBooking.BookSchType.NONE.ordinal()) {

            } else {
                Intent intent = new Intent();
                intent.setComponent(new ComponentName("com.konkawise.dtv", "com.konkawise.dtv.ui.Topmost"));
                intent.putExtra(Constants.IntentKey.INTENT_BOOKING_RECORD, readyProg.schtype == SWBooking.BookSchType.RECORD.ordinal() ? ACTION_BOOKING_RECORD : ACTION_BOOKING_PLAY);
                intent.putExtra(Constants.IntentKey.INTENT_BOOKING_PROG_NUM, SWFtaManager.getInstance().getCurrPlayInfo(0).Progno);
                startActivity(intent);
            }
        }
    }

    @Override
    public void onDestroy() {
        WeakToolManager.getInstance().removeWeakTool(this);
        SWDVBManager.getInstance().regMsgHandler(null, null);
        super.onDestroy();
    }
}
