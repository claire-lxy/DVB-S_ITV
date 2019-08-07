package com.konkawise.dtv.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.HandlerMsgManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWDVBManager;
import com.konkawise.dtv.UIApiManager;
import com.konkawise.dtv.UsbManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.bean.RecordInfo;
import com.konkawise.dtv.bean.UsbInfo;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.SeekTimeDialog;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.sw.dvblib.DJAPVR;
import com.sw.dvblib.msg.cb.AVMsgCB;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Set;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.HPVR_Progress_t;

public class RecordPlayer extends BaseActivity implements UsbManager.OnUsbReceiveListener {
    private static final String TAG = "RecordPlayer";

    static final int TYPE_PLAY = 0;
    static final int TYPE_PAUSE = 1;
    static final int TYPE_SEEK_BACK = 2;
    static final int TYPE_SEEK_FORWARD = 3;
    static final int TYPE_STOP = 4;

    public static final int FROM_TOPMOST = 0;
    public static final int FROM_RECORD_LIST = 1;

    private static final int SEEK_TYPE_LEFT = -1;
    private static final int SEEK_TYPE_NO = 0;
    private static final int SEEK_TYPE_RIGHT = 1;

    @BindView(R.id.sv_record_player)
    SurfaceView svRecordPlayer;

    @BindView(R.id.iv_player_handler)
    ImageView ivPlayerHandler;

    @BindView(R.id.tv_seek_num)
    TextView tvSeekNum;

    @BindView(R.id.tv_prog_num)
    TextView tvProgNum;

    @BindView(R.id.tv_current_time)
    TextView tvCurrTime;

    @BindView(R.id.ly_mediaplayer_control)
    LinearLayout lyControl;

    @BindView(R.id.tv_total_time)
    TextView tvTotalTime;

    @BindView(R.id.sb_progress)
    SeekBar sbProgress;

    PlayHandler playHandler;

    private int from;

    private int currType;

    private int totalDuration;
    private boolean initUIContextFlg = false;

    private int seekNum = 1;
    private int seekType = SEEK_TYPE_NO;

    RecordInfo recordInfo;

    private AVMsgCB mPvrMsgCB = new PVRMsgCB();

    private static class PlayHandler extends WeakHandler<RecordPlayer> {

        static final int MSG_UPGRADE_PROGRESS = 0;
        static final int MSG_DISMISS_CONTROL_UI = 1;

        public PlayHandler(RecordPlayer view) {
            super(view);
        }

        @Override
        protected void handleMsg(Message msg) {
            RecordPlayer context = mWeakReference.get();
            if (msg.what == MSG_UPGRADE_PROGRESS) {
                HPVR_Progress_t hpvrProgressT = DJAPVR.CreateInstance().getPlayProgress();
                int volid = hpvrProgressT.valid;
                int progress = 0;
                int secondProgress = 0;
                if (volid == 0) {
                    if (context.from == FROM_RECORD_LIST) {
                        context.totalDuration = 0;
                    }
                } else {
                    if (context.from == FROM_RECORD_LIST) {
                        context.totalDuration = hpvrProgressT.endMs;
                        progress = hpvrProgressT.currentMs;
                    } else {
                        progress = hpvrProgressT.currentMs;
                        secondProgress = hpvrProgressT.endMs;
                        if (context.seekNum >= 2 && context.gotoShiftEnd(progress, secondProgress)) {
                            context.resumeFromSeek();
                        }
                    }
                }
                context.initUIContent(context.totalDuration);
                context.refreshUI(progress, secondProgress);
                context.sendUpgradePrgressMsg(new HandlerMsgModel(MSG_UPGRADE_PROGRESS, 1000L));
            } else if (msg.what == MSG_DISMISS_CONTROL_UI) {
                context.tvProgNum.setVisibility(View.INVISIBLE);
                context.lyControl.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_record_player;
    }

    @Override
    protected void setup() {
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
        UsbManager.getInstance().registerUsbReceiveListener(this);
        init();
    }

    @Override
    protected void onResume() {
        super.onResume();
        initUIContextFlg = false;
        SWDVBManager.getInstance().regMsgHandler(Constants.PVR_CALLBACK_MSG_ID, Looper.getMainLooper(), mPvrMsgCB);
//        play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
        SWDVBManager.getInstance().unRegMsgHandler(Constants.PVR_CALLBACK_MSG_ID, mPvrMsgCB);
        if (playHandler != null) {
            removeUpgradeProgressMsg();
            removeDissControlUIMsg();
        }
    }

    private void init() {
        playHandler = new PlayHandler(this);

        from = getIntent().getIntExtra("from", 0);

        SurfaceHolder holder = svRecordPlayer.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                UIApiManager.getInstance().setSurface(holder.getSurface());
                UIApiManager.getInstance().setWindowSize(0, 0,
                        getResources().getDisplayMetrics().widthPixels, getResources().getDisplayMetrics().heightPixels);
                play();
            }

            @Override
            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

            }

            @Override
            public void surfaceDestroyed(SurfaceHolder holder) {

            }
        });
    }

    private void showControlUI(boolean dismiss) {
        removeDissControlUIMsg();
        tvProgNum.setVisibility(View.VISIBLE);
        lyControl.setVisibility(View.VISIBLE);
        if (dismiss)
            sendDissControlUIMsg(new HandlerMsgModel(PlayHandler.MSG_DISMISS_CONTROL_UI, 4000L));
    }

    private void switchPlayTypeUI(int playType, int seekNum) {
        currType = playType;
        switch (playType) {
            case TYPE_STOP:
                tvSeekNum.setVisibility(View.INVISIBLE);
                ivPlayerHandler.setVisibility(View.INVISIBLE);
                break;
            case TYPE_PLAY:
                tvSeekNum.setVisibility(View.INVISIBLE);
                if (from == FROM_RECORD_LIST) {
                    ivPlayerHandler.setVisibility(View.INVISIBLE);
                } else {
                    ivPlayerHandler.setVisibility(View.VISIBLE);
                    ivPlayerHandler.setImageResource(R.drawable.record);
                }
                break;
            case TYPE_PAUSE:
                Log.i(TAG, "switchPlayTypeUI PAUSE");
                tvSeekNum.setVisibility(View.INVISIBLE);
                ivPlayerHandler.setVisibility(View.VISIBLE);
                ivPlayerHandler.setImageResource(R.drawable.osd_pause_hl);
                break;
            case TYPE_SEEK_BACK:
                tvSeekNum.setVisibility(View.VISIBLE);
                ivPlayerHandler.setVisibility(View.VISIBLE);
                tvSeekNum.setText(MessageFormat.format("*{0}", seekNum));
                ivPlayerHandler.setImageResource(R.drawable.osd_rewind_hl);
                break;
            case TYPE_SEEK_FORWARD:
                tvSeekNum.setVisibility(View.VISIBLE);
                ivPlayerHandler.setVisibility(View.VISIBLE);
                tvSeekNum.setText(MessageFormat.format("*{0}", seekNum));
                ivPlayerHandler.setImageResource(R.drawable.osd_forward_hl);
                break;
        }
    }

    private void refreshUI(int progress, int secondProgress) {
        Log.i(TAG, "progress:" + progress + " secondProgress:" + secondProgress);
        tvCurrTime.setText(formatDuration(progress));
        sbProgress.setProgress(progress / 1000);
        if (from == FROM_TOPMOST)      //时移状态下才需要second progress
            sbProgress.setSecondaryProgress(secondProgress / 1000);
    }

    private void initUIContent(int endMs) {
        Log.i(TAG, "tatal duration:" + endMs);
        if (!initUIContextFlg) {
            sbProgress.setMax(endMs / 1000);
            tvTotalTime.setText(formatDuration(endMs));
        }
        initUIContextFlg = endMs > 0;
    }

    static String formatDuration(int milliseconds) {
        int seconds = milliseconds / 1000;
        int hourPart = seconds / (60 * 60);
        int minutePart = (seconds - hourPart * 60 * 60) / 60;
        int secondPart = (seconds - hourPart * 60 * 60) % 60;
        return (hourPart >= 10 ? hourPart : "0" + hourPart) + ":" + (minutePart >= 10 ? minutePart : "0" + minutePart) + ":" + (secondPart >= 10 ? secondPart : "0" + secondPart);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UsbManager.getInstance().unregisterUsbReceiveListener(this);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (currType == TYPE_PLAY) {
                    pause();
                    new SeekTimeDialog()
                            .setCurrTime(DJAPVR.CreateInstance().getPlayProgress().currentMs > 0 ? DJAPVR.CreateInstance().getPlayProgress().currentMs : 0)
                            .setTimeLimit(DJAPVR.CreateInstance().getPlayProgress().endMs > 0 ? DJAPVR.CreateInstance().getPlayProgress().endMs : 0)
                            .setTimeListener(new SeekTimeDialog.OnTimeListener() {
                                @Override
                                public void time(int hour, int minute, int second) {
                                    Log.i(TAG, "---jump---time:" + hour + ":" + minute + ":" + second);
                                    int currTime = (hour * 60 * 60 + minute * 60 + second) * 1000;
                                    jump(currTime);
                                }
                            })
                            .show(getSupportFragmentManager(), SeekTimeDialog.TAG);
                } else if (currType == TYPE_PAUSE) {
                    resume();
                } else if (currType == TYPE_SEEK_BACK || currType == TYPE_SEEK_FORWARD) {
                    resumeFromSeek();
                }
                return true;

            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (currType == TYPE_PLAY) {
                    pause();
                } else if (currType == TYPE_PAUSE) {
                    resume();
                } else if (currType == TYPE_SEEK_BACK || currType == TYPE_SEEK_FORWARD) {
                    resumeFromSeek();
                }
                return true;

            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_BACK:
                new CommTipsDialog()
                        .title(getString(R.string.dialog_exit_pvr_tips))
                        .content(getString(R.string.dialog_exit_timeshift_content))
                        .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                            @Override
                            public void onPositiveListener() {

                                finish();
                            }
                        }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
                return true;

            case KeyEvent.KEYCODE_MEDIA_REWIND:
            case KeyEvent.KEYCODE_DPAD_LEFT:
                recordSeekTypeNum(SEEK_TYPE_LEFT);
                seek(seekNum, seekType);
                return true;

            case KeyEvent.KEYCODE_FORWARD:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                recordSeekTypeNum(SEEK_TYPE_RIGHT);
                seek(seekNum, seekType);
                return true;

            case KeyEvent.KEYCODE_INFO:
                showControlUI(true);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void play() {
        /*
        switchPlayTypeUI(TYPE_PLAY, -1);
        String path = "/storage/sda1/HISI8.0/4.mp4";
        try {
            player.reset();
            player.setDataSource(path);
            player.prepare();
            player.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    Log.i(TAG, "PLAY");
                    showControlUI(true);
                    initUIContent();
                    sendUpgradePrgressMsg(new HandlerMsgModel(PlayHandler.MSG_UPGRADE_PROGRESS));
                    player.start();
                    if (from == FROM_TOPMOST) {
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                pause();
                            }
                        }, 800);
                    }
                }
            });

            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        */
        Log.i(TAG, "PLAY");
        if (from == FROM_TOPMOST) {
            playTimeShift();
        } else {
            playRecord();
        }
    }

    private void playRecord() {
        switchPlayTypeUI(TYPE_PLAY, -1);
        showControlUI(true);
        initUIContent(0);
        sendUpgradePrgressMsg(new HandlerMsgModel(PlayHandler.MSG_UPGRADE_PROGRESS));
        recordInfo = (RecordInfo) getIntent().getSerializableExtra("recordinfo");
        DJAPVR.CreateInstance().startPlay(recordInfo.getFile().getParent() + "/", recordInfo.getFile().getName(), 0);
    }

    private void playTimeShift() {
        totalDuration = getIntent().getIntExtra("time", 0) * 60 * 1000;
        switchPlayTypeUI(TYPE_PAUSE, -1);
        showControlUI(false);
        initUIContent(totalDuration);
        sendUpgradePrgressMsg(new HandlerMsgModel(PlayHandler.MSG_UPGRADE_PROGRESS));
        DJAPVR.CreateInstance().beginTimeshift();
    }

    private boolean gotoShiftEnd(int currMS, int endMS) {
        return currMS / 1000 == endMS / 1000;
    }

    private void resume() {
        Log.i(TAG, "resume");
//        if (playHandler.hasMessages(PlayHandler.MSG_UPGRADE_PROGRESS))
//            playHandler.removeMessages(PlayHandler.MSG_UPGRADE_PROGRESS);
        switchPlayTypeUI(TYPE_PLAY, -1);
        showControlUI(true);
//        playHandler.sendEmptyMessage(PlayHandler.MSG_UPGRADE_PROGRESS);  //导致暂停图标不显示，后续需排查原因
        DJAPVR.CreateInstance().playResume();
    }

    private void jump(int currMS) {
        Log.i(TAG, "jump:" + currMS);
        switchPlayTypeUI(TYPE_PLAY, -1);
        showControlUI(true);
        DJAPVR.CreateInstance().playSeek(currMS);
        DJAPVR.CreateInstance().playResume();
    }

    private void pause() {
        Log.i(TAG, "pause");
        switchPlayTypeUI(TYPE_PAUSE, -1);
        showControlUI(false);
        DJAPVR.CreateInstance().playPause();
    }

    private void recordSeekTypeNum(int seekType) {
        if (this.seekType == SEEK_TYPE_NO) {
            this.seekType = seekType;
            this.seekNum = 2;
            return;
        }
        if (this.seekType == seekType) {
            if (this.seekNum == 32) {
                this.seekType = SEEK_TYPE_NO;
                this.seekNum = 1;
                return;
            }
            this.seekNum = this.seekNum * 2;
        } else {
            this.seekType = SEEK_TYPE_NO;
            this.seekNum = 1;
        }
    }

    private void resumeFromSeek() {
        seekNum = 1;
        seekType = SEEK_TYPE_NO;
        seek(seekNum, seekType);
    }

    private void seek(int seekNum, int type) {
        Log.i(TAG, "seek");
        if (type == SEEK_TYPE_NO) {
            switchPlayTypeUI(TYPE_PLAY, -1);
            showControlUI(true);
            DJAPVR.CreateInstance().setPlaySpeed(getSelectPosition(new int[]{1, 2, 4, 8, 16, 32, 64, 128}, seekNum) + 8);
        } else if (type == SEEK_TYPE_LEFT) {
            switchPlayTypeUI(TYPE_SEEK_BACK, seekNum);
            showControlUI(false);
            DJAPVR.CreateInstance().setPlaySpeed(getSelectPosition(new int[]{1, 2, 4, 8, 16, 32, 64, 128}, seekNum));
        } else {
            switchPlayTypeUI(TYPE_SEEK_FORWARD, seekNum);
            showControlUI(false);
            DJAPVR.CreateInstance().setPlaySpeed(getSelectPosition(new int[]{1, 2, 4, 8, 16, 32, 64, 128}, seekNum) + 8);
        }
    }

    static int getSelectPosition(int[] datas, int value) {
        if (datas == null || datas.length <= 0) return 0;

        for (int i = 0; i < datas.length; i++) {
            if (datas[i] == value) return i;
        }
        return 0;
    }

    private void stop() {
        Log.i(TAG, "stop");
        switchPlayTypeUI(TYPE_STOP, -1);
        showControlUI(false);
        if (from == FROM_TOPMOST)
            DJAPVR.CreateInstance().stopTimeshift();
        else
            DJAPVR.CreateInstance().stopPlay();
    }

    private void sendUpgradePrgressMsg(HandlerMsgModel progMsg) {
        HandlerMsgManager.getInstance().sendMessage(playHandler, progMsg);
    }

    private void sendDissControlUIMsg(HandlerMsgModel progMsg) {
        HandlerMsgManager.getInstance().sendMessage(playHandler, progMsg);
    }

    private void removeUpgradeProgressMsg() {
        HandlerMsgManager.getInstance().removeMessage(playHandler, PlayHandler.MSG_UPGRADE_PROGRESS);
    }

    private void removeDissControlUIMsg() {
        HandlerMsgManager.getInstance().removeMessage(playHandler, PlayHandler.MSG_DISMISS_CONTROL_UI);
    }

    private class PVRMsgCB extends AVMsgCB {
        @Override
        public int PVRPlay_MODULE(int p0, int p1, int p2, int p3, int p4) {
            Log.i(TAG, "PVRPlay_MODULE---p0:" + p0 + " p1:" + p1 + " p2:" + p2 + " p3:" + p3 + " p4:" + p4);
            if (from == FROM_TOPMOST) {
                if (p3 == 2) {
                    resumeFromSeek();
                }
            } else {
                if (p3 == 1) {
                    finish();
                } else if (p3 == 2) {
                    resumeFromSeek();
                }

            }
            return super.PVRPlay_MODULE(p0, p1, p2, p3, p4);
        }
    }

    @Override
    public void onUsbReceive(int usbObserveType, Set<UsbInfo> usbInfos, UsbInfo currUsbInfo) {
        if (from == FROM_TOPMOST) {

        } else {
            if (usbObserveType == Constants.USB_TYPE_DETACH && recordInfo.getFile().getParentFile().getParent().equals(currUsbInfo.path)) {
                finish();
            }
        }
    }
}
