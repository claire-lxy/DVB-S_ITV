package com.konkawise.dtv.ui;

import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
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
import com.konkawise.dtv.SWDJAPVRManager;
import com.konkawise.dtv.SWDVBManager;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.UIApiManager;
import com.konkawise.dtv.UsbManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.bean.RecordInfo;
import com.konkawise.dtv.bean.UsbInfo;
import com.konkawise.dtv.dialog.AudioDialog;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.EditTimeDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.SubtitleDialog;
import com.konkawise.dtv.dialog.TeletextDialog;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.listener.CallbackListenerAdapter;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.HPVR_Progress_t;
import vendor.konka.hardware.dtvmanager.V1_0.HSubtitle_t;
import vendor.konka.hardware.dtvmanager.V1_0.HTeletext_t;

public class RecordPlayer extends BaseActivity implements UsbManager.OnUsbReceiveListener {
    private static final String TAG = "RecordPlayer";

    private static final String HANDLER_THREAD_NAME = "pvr_thread";

    private static final boolean LOOPER = false;

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

    @BindView(R.id.ly_bottom)
    LinearLayout lyBottom;

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

    private RecordInfo recordInfo;
    private int currRecordPosition;
    private List<RecordInfo> recordList = new ArrayList<>();

    private HandlerThread pvrThread;
    private PVRHandler pvrHandler;

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
                HPVR_Progress_t hpvrProgressT = SWDJAPVRManager.getInstance().getPlayProgress();
                int volid = hpvrProgressT.valid;
                int progress = 0;
                int secondProgress = 0;
                if (volid == 0) {
                    context.totalDuration = 0;
                } else {
                    if (context.from == FROM_RECORD_LIST) {
                        context.totalDuration = hpvrProgressT.endMs;
                        progress = hpvrProgressT.currentMs - hpvrProgressT.startMs;
                    } else {
                        progress = hpvrProgressT.currentMs - hpvrProgressT.startMs;
                        secondProgress = hpvrProgressT.endMs;
                        context.totalDuration = hpvrProgressT.endMs;
                        if (context.seekNum >= 2 && context.gotoShiftEnd(progress, secondProgress, context.seekNum)) {
                            context.resumeFromSeek();
                        }
                    }
                }
                context.initUIContent("", context.totalDuration);
                context.refreshUI(progress, secondProgress);
                context.sendUpgradePrgressMsg(new HandlerMsgModel(MSG_UPGRADE_PROGRESS, 1000L));
            } else if (msg.what == MSG_DISMISS_CONTROL_UI) {
                context.dismissControlUI();
            }
        }
    }

    private static class PVRHandler extends WeakHandler<RecordPlayer> {
        static final int MSG_BEGIN_TIMESHIFT = 0;
        static final int MSG_STOP_TIMESHIFT = 1;
        static final int MSG_PLAY_RESUME = 2;
        static final int MSG_START_PLAY = 3;
        static final int MSG_STOP_PLAY = 4;
        static final int MSG_PLAY_SEEK = 5;
        static final int MSG_PLAY_SPEED = 6;
        static final int MSG_PLAY_PAUSE = 7;

        static final String KEY_PATH = "path";
        static final String KEY_FNAME = "fname";
        static final String KEY_LOOP = "loop";

        public PVRHandler(RecordPlayer view, Looper looper) {
            super(view, looper);
        }

        @Override
        protected void handleMsg(Message msg) {
            switch (msg.what) {
                case MSG_BEGIN_TIMESHIFT:
                    SWDJAPVRManager.getInstance().beginTimeshift();
                    break;

                case MSG_STOP_TIMESHIFT:
                    SWDJAPVRManager.getInstance().stopTimeshift();
                    break;

                case MSG_PLAY_RESUME:
                    SWDJAPVRManager.getInstance().playResume();
                    break;

                case MSG_START_PLAY:
                    Bundle bundle = msg.getData();
                    String path = bundle.getString(KEY_PATH);
                    String fName = bundle.getString(KEY_FNAME);
                    SWDJAPVRManager.getInstance().startPlay(path, fName, bundle.getInt(KEY_LOOP));
                    SWDJAPVRManager.getInstance().injectSubTTXAudio(path, fName);
                    break;

                case MSG_STOP_PLAY:
                    SWDJAPVRManager.getInstance().stopPlay();
                    break;

                case MSG_PLAY_SEEK:
                    SWDJAPVRManager.getInstance().playSeek(msg.arg1);
                    break;

                case MSG_PLAY_SPEED:
                    SWDJAPVRManager.getInstance().setPlaySpeed(msg.arg1);
                    break;

                case MSG_PLAY_PAUSE:
                    SWDJAPVRManager.getInstance().playPause();
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
        registerMsgEvent();
//        play();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stop();
        unregisterMsgEvent();
        if (playHandler != null) {
            removeUpgradeProgressMsg();
            removeDissControlUIMsg();
        }
    }

    private void registerMsgEvent() {
        MsgEvent msgEvent = SWDVBManager.getInstance().registerMsgEvent(Constants.PVR_CALLBACK_MSG_ID);
        msgEvent.registerCallbackListener(new CallbackListenerAdapter() {
            @Override
            public int PVRPlay_MODULE(int p0, int p1, int p2, int p3, int p4) {
                Log.i(TAG, "PVRPlay_MODULE---p0:" + p0 + " p1:" + p1 + " p2:" + p2 + " p3:" + p3 + " p4:" + p4);
                if (from == FROM_TOPMOST) {
                    if (p3 == 2) {
                        resumeFromSeek();
                    }
                } else {
                    if (p3 == 1) {
                        if (LOOPER) {
                            playNextRecord();
                        } else {
                            finish();
                        }
                    } else if (p3 == 2) {
                        resumeFromSeek();
                    }

                }
                return super.PVRPlay_MODULE(p0, p1, p2, p3, p4);
            }

            @Override
            public int PVRPlay_PlaybackFailed(int p0, int p1, int p2, int p3, int p4) {
                Log.i(TAG, "PVRPlay_PlaybackFailed---p0:" + p0 + " p1:" + p1 + " p2:" + p2 + " p3:" + p3 + " p4:" + p4);
                if (from == FROM_RECORD_LIST && LOOPER) {
                    playNextRecord();
                } else {
                    finish();
                }
                return super.PVRPlay_PlaybackFailed(p0, p1, p2, p3, p4);
            }
        });
    }

    private void unregisterMsgEvent() {
        SWDVBManager.getInstance().unregisterMsgEvent(Constants.PVR_CALLBACK_MSG_ID);
    }

    private void init() {
        playHandler = new PlayHandler(this);
        pvrThread = new HandlerThread(HANDLER_THREAD_NAME);
        pvrThread.start();
        pvrHandler = new PVRHandler(this, pvrThread.getLooper());

        from = getIntent().getIntExtra(Constants.IntentKey.INTENT_TIMESHIFT_RECORD_FROM, 0);

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
        lyBottom.setVisibility(View.VISIBLE);
        if (dismiss)
            sendDissControlUIMsg(new HandlerMsgModel(PlayHandler.MSG_DISMISS_CONTROL_UI, 4000L));
    }

    private void dismissControlUI() {
        lyBottom.setVisibility(View.INVISIBLE);
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

    private void initUIContent(String progNum, int endMs) {
        Log.i(TAG, "progNum:" + progNum + "tatal duration:" + endMs);
        if (!TextUtils.isEmpty(progNum)) {
            tvProgNum.setText(progNum);
        }

        sbProgress.setMax(endMs / 1000);
        tvTotalTime.setText(formatDuration(endMs));
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
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                if (currType == TYPE_PLAY) {
                    pause();
                    new EditTimeDialog()
                            .setCurrTime(SWDJAPVRManager.getInstance().getPlayProgress().currentMs > 0 ? SWDJAPVRManager.getInstance().getPlayProgress().currentMs : 0)
                            .setTimeLimit(SWDJAPVRManager.getInstance().getPlayProgress().endMs > 0 ? SWDJAPVRManager.getInstance().getPlayProgress().endMs : 0)
                            .setTimeListener(new EditTimeDialog.OnTimeListener() {
                                @Override
                                public void time(int hour, int minute, int second) {
                                    Log.i(TAG, "---jump---time:" + hour + ":" + minute + ":" + second);
                                    int currTime = (hour * 60 * 60 + minute * 60 + second) * 1000;
                                    jump(currTime);
                                }
                            })
                            .show(getSupportFragmentManager(), EditTimeDialog.TAG);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_MEDIA_STOP:
            case KeyEvent.KEYCODE_BACK:
                if (lyBottom.getVisibility() != View.VISIBLE) {
                    new CommTipsDialog()
                            .title(getString(R.string.dialog_exit_pvr_tips))
                            .content(getString(from == FROM_TOPMOST ? R.string.dialog_exit_timeshift_content : R.string.dialog_exit_playback_content))
                            .negativeFocus(true)
                            .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                                @Override
                                public void onPositiveListener() {

                                    finish();
                                }
                            }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
                    return true;
                }
                break;

            case KeyEvent.KEYCODE_MEDIA_AUDIO_TRACK:
                showAudioDialog();
                return true;

            case KeyEvent.KEYCODE_F3:
                showSubtitleDialog();
                return true;

            case KeyEvent.KEYCODE_TV_TELETEXT:
                showTeletextDialog();
                return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_SHIFT_RIGHT:
                if (currType == TYPE_PAUSE) {
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
                if (lyBottom.getVisibility() == View.VISIBLE) {
                    dismissControlUI();
                }
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

    private void showAudioDialog() {
        new AudioDialog().title(getString(R.string.audio)).where(AudioDialog.WHERE_RECORDPLAYER).show(getSupportFragmentManager(), AudioDialog.TAG);
    }

    private void showSubtitleDialog() {

        int currSubtitle = 0;
        int serviceid;
        if (from == FROM_TOPMOST) {
            serviceid = SWPDBaseManager.getInstance().getCurrProgInfo().ServID;
        } else {
            serviceid = recordInfo.getHpvrRecFileT().ServId;
        }
        Log.i("testljm", "serviceid2:" + serviceid);
        int num = SWFtaManager.getInstance().getSubtitleNum(serviceid);
        final int[] pids = new int[num];
        List<HashMap<String, Object>> subtitles = new ArrayList<>();
        HashMap<String, Object> off = new HashMap<>();
        off.put(Constants.SUBTITLE_NAME, "OFF");
        subtitles.add(off);
        for (int index = 0; index < num; index++) {
            HSubtitle_t subtitle = SWFtaManager.getInstance().getSubtitleInfo(serviceid, index);
            if (subtitle.used != 0) {
                pids[index] = subtitle.Pid;
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

        int currTeleText = 0;
        int serviceid;
        if (from == FROM_TOPMOST) {
            serviceid = SWPDBaseManager.getInstance().getCurrProgInfo().ServID;
        } else {
            serviceid = recordInfo.getHpvrRecFileT().ServId;
        }
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

    private void play() {
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
        sendUpgradePrgressMsg(new HandlerMsgModel(PlayHandler.MSG_UPGRADE_PROGRESS));
        recordInfo = RecordListActivity.mAdapter.getItem(getIntent().getIntExtra(Constants.IntentKey.INTENT_RECORD_POSITION, 0));
        initUIContent(recordInfo.getFile().getName(), 0);
        Message msg = pvrHandler.obtainMessage();
        msg.what = PVRHandler.MSG_START_PLAY;
        Bundle bundle = new Bundle();
        bundle.putString(PVRHandler.KEY_PATH, recordInfo.getFile().getParent() + "/");
        bundle.putString(PVRHandler.KEY_FNAME, recordInfo.getFile().getName());
        bundle.putInt(PVRHandler.KEY_LOOP, 0);
        msg.setData(bundle);
        pvrHandler.sendMessage(msg);
    }

    private void playNextRecord() {
        pvrHandler.sendEmptyMessage(PVRHandler.MSG_STOP_PLAY);
        if (recordList == null || recordList.size() == 0) {
            recordList = RecordListActivity.mAdapter.getData();
            for (int i = 0; i < recordList.size(); i++) {
                if (recordList.contains(recordInfo)) {
                    currRecordPosition = i;
                    break;
                }
            }
        }
        currRecordPosition++;
        if (currRecordPosition >= recordList.size())
            currRecordPosition = 0;

        recordInfo = recordList.get(currRecordPosition);
        Log.i(TAG, "playNextRecord:" + recordInfo.getFile().getName());
        switchPlayTypeUI(TYPE_PLAY, -1);
        showControlUI(true);
        initUIContent(recordInfo.getFile().getName(), 0);
        Message msg = pvrHandler.obtainMessage();
        msg.what = PVRHandler.MSG_START_PLAY;
        Bundle bundle = new Bundle();
        bundle.putString(PVRHandler.KEY_PATH, recordInfo.getFile().getParent() + "/");
        bundle.putString(PVRHandler.KEY_FNAME, recordInfo.getFile().getName());
        bundle.putInt(PVRHandler.KEY_LOOP, 0);
        msg.setData(bundle);
        pvrHandler.sendMessage(msg);
    }

    private void playTimeShift() {
//        totalDuration = getIntent().getIntExtra(Constants.IntentKey.INTENT_TIMESHIFT_TIME, 0) * 60 * 1000;
//        if (totalDuration == 0) {
//            totalDuration = SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_TimeshiftMaxMin.ordinal()) * 60 * 1000;
//        }
        switchPlayTypeUI(TYPE_PAUSE, -1);
        showControlUI(false);
        initUIContent(getIntent().getStringExtra(Constants.IntentKey.INTENT_TIMESHIFT_PROGNUM) + "  " + SWPDBaseManager.getInstance().getCurrProgInfo().Name, 0);
        sendUpgradePrgressMsg(new HandlerMsgModel(PlayHandler.MSG_UPGRADE_PROGRESS));
        pvrHandler.sendEmptyMessage(PVRHandler.MSG_BEGIN_TIMESHIFT);
    }

    private boolean gotoShiftEnd(int currMS, int endMS, int seekNum) {
        return Math.abs(currMS / 1000 - endMS / 1000) <= seekNum;
    }

    private void resume() {
        Log.i(TAG, "resume");
//        if (playHandler.hasMessages(PlayHandler.MSG_UPGRADE_PROGRESS))
//            playHandler.removeMessages(PlayHandler.MSG_UPGRADE_PROGRESS);
        switchPlayTypeUI(TYPE_PLAY, -1);
        showControlUI(true);
//        playHandler.sendEmptyMessage(PlayHandler.MSG_UPGRADE_PROGRESS);  //导致暂停图标不显示，后续需排查原因
        pvrHandler.sendEmptyMessage(PVRHandler.MSG_PLAY_RESUME);
    }

    private void jump(int currMS) {
        Log.i(TAG, "jump:" + currMS);
        switchPlayTypeUI(TYPE_PLAY, -1);
        showControlUI(true);
        Message msg = pvrHandler.obtainMessage();
        msg.what = PVRHandler.MSG_PLAY_SEEK;
        msg.arg1 = currMS;
        pvrHandler.sendMessage(msg);
        pvrHandler.sendEmptyMessage(PVRHandler.MSG_PLAY_RESUME);
    }

    private void pause() {
        Log.i(TAG, "pause");
        switchPlayTypeUI(TYPE_PAUSE, -1);
        showControlUI(false);
        pvrHandler.sendEmptyMessage(PVRHandler.MSG_PLAY_PAUSE);
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
        Message msg = pvrHandler.obtainMessage(PVRHandler.MSG_PLAY_SPEED);
        if (type == SEEK_TYPE_NO) {
            switchPlayTypeUI(TYPE_PLAY, -1);
            showControlUI(true);
            msg.arg1 = getSelectPosition(new int[]{1, 2, 4, 8, 16, 32, 64, 128}, seekNum) + 8;
            pvrHandler.sendMessage(msg);

        } else if (type == SEEK_TYPE_LEFT) {
            switchPlayTypeUI(TYPE_SEEK_BACK, seekNum);
            showControlUI(false);
            msg.arg1 = getSelectPosition(new int[]{1, 2, 4, 8, 16, 32, 64, 128}, seekNum);
            pvrHandler.sendMessage(msg);
        } else {
            switchPlayTypeUI(TYPE_SEEK_FORWARD, seekNum);
            showControlUI(false);
            msg.arg1 = getSelectPosition(new int[]{1, 2, 4, 8, 16, 32, 64, 128}, seekNum) + 8;
            pvrHandler.sendMessage(msg);
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
            pvrHandler.sendEmptyMessage(PVRHandler.MSG_STOP_TIMESHIFT);
        else
            pvrHandler.sendEmptyMessage(PVRHandler.MSG_STOP_PLAY);
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
