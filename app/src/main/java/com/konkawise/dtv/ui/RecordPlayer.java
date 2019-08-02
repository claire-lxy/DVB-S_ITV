package com.konkawise.dtv.ui;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Handler;
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

import com.konkawise.dtv.HandlerMsgManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.HandlerMsgModel;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.SeekTimeDialog;
import com.konkawise.dtv.weaktool.WeakHandler;

import java.io.IOException;
import java.text.MessageFormat;

import butterknife.BindView;

public class RecordPlayer extends BaseActivity {
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

    //读写权限
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE};
    //请求状态码
    private static int REQUEST_PERMISSION_CODE = 1;

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
    private MediaPlayer player;

    private int seekNum = 1;
    private int seekType = SEEK_TYPE_NO;

    private static class PlayHandler extends WeakHandler<RecordPlayer> {

        static final int MSG_UPGRADE_PROGRESS = 0;
        static final int MSG_DISMISS_CONTROL_UI = 1;
        static final int MSG_SEEK = 2;

        public PlayHandler(RecordPlayer view) {
            super(view);
        }

        @Override
        protected void handleMsg(Message msg) {
            RecordPlayer context = mWeakReference.get();
            if (msg.what == MSG_UPGRADE_PROGRESS) {
                int progress = context.player.getCurrentPosition();
                Log.i(TAG, "MSG_UPGRADE_PROGRESS:" + progress);
                context.refreshUI(progress);
                context.sendUpgradePrgressMsg(new HandlerMsgModel(MSG_UPGRADE_PROGRESS, 1000L));
            } else if (msg.what == MSG_DISMISS_CONTROL_UI) {
                context.tvProgNum.setVisibility(View.INVISIBLE);
                context.lyControl.setVisibility(View.INVISIBLE);
            } else if (msg.what == MSG_SEEK) {
                context.seek(context.seekNum, context.seekType);
            }
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_record_player;
    }

    @Override
    protected void setup() {
        //for test by mediaplayer
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS_STORAGE, REQUEST_PERMISSION_CODE);
            }
        }
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);

        init();
        play();
    }

    private void init() {
        playHandler = new PlayHandler(this);

        from = getIntent().getIntExtra("from", 0);

        player = new MediaPlayer();
        SurfaceHolder holder = svRecordPlayer.getHolder();
        holder.addCallback(new SurfaceHolder.Callback() {
            @Override
            public void surfaceCreated(SurfaceHolder holder) {
                player.setDisplay(holder);
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

    private void refreshUI(int progress) {
        tvCurrTime.setText(formatDuration(progress));
        sbProgress.setProgress(progress / 1000);
        if (from == FROM_TOPMOST)      //时移状态下才需要second progress
            sbProgress.setSecondaryProgress(progress / 500);
    }

    private void initUIContent() {
        sbProgress.setMax(player.getDuration() / 1000);
        tvTotalTime.setText(formatDuration(player.getDuration()));
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
        if (player != null && player.isPlaying()) {
            player.stop();
            player = null;
        }
        if (playHandler != null) {
            removeUpgradeProgressMsg();
            removeDissControlUIMsg();
            removeSeekMsg();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (currType == TYPE_PLAY) {
                    pause();
                    new SeekTimeDialog()
                            .setCurrTime(0, 0, 5)
                            .setTimeLimit(0, 2, 10)
                            .setTimeListener(new SeekTimeDialog.OnTimeListener() {
                                @Override
                                public void time(int hour, int minute, int second) {
                                    Log.i(TAG, "---resume---time:" + hour + ":" + minute + ":" + second);
                                    resume();
                                }
                            })
                            .show(getSupportFragmentManager(), SeekTimeDialog.TAG);
                } else if (currType == TYPE_PAUSE) {
                    resume();
                } else if (currType == TYPE_SEEK_BACK || currType == TYPE_SEEK_FORWARD) {
                    seek(seekNum, SEEK_TYPE_NO);
                }
                return true;

            case KeyEvent.KEYCODE_BACK:
                new CommTipsDialog()
                        .title(getString(R.string.dialog_title_tips))
                        .content(getString(R.string.exit_app_content))
                        .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                            @Override
                            public void onPositiveListener() {

                                finish();
                            }
                        }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
                return true;

            case KeyEvent.KEYCODE_DPAD_LEFT:
                recordSeekTypeNum(SEEK_TYPE_LEFT);
                seek(seekNum, seekType);
                return true;

            case KeyEvent.KEYCODE_DPAD_RIGHT:
                recordSeekTypeNum(SEEK_TYPE_RIGHT);
                seek(seekNum, seekType);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    private void play() {
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
    }

    private void resume() {
        Log.i(TAG, "resume");
//        if (playHandler.hasMessages(PlayHandler.MSG_UPGRADE_PROGRESS))
//            playHandler.removeMessages(PlayHandler.MSG_UPGRADE_PROGRESS);
        switchPlayTypeUI(TYPE_PLAY, -1);
        showControlUI(true);
//        playHandler.sendEmptyMessage(PlayHandler.MSG_UPGRADE_PROGRESS);  //导致暂停图标不显示，后续需排查原因
        if (player.isPlaying())
            return;
        player.start();
    }

    private void pause() {
        Log.i(TAG, "pause");
//        if (playHandler.hasMessages(PlayHandler.MSG_UPGRADE_PROGRESS))
//            playHandler.removeMessages(PlayHandler.MSG_UPGRADE_PROGRESS);
        switchPlayTypeUI(TYPE_PAUSE, -1);
        showControlUI(false);
        if (!player.isPlaying())
            return;
        player.pause();
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

    private void seek(int seekNum, int type) {
        Log.i(TAG, "seek");
        removeSeekMsg();
        int progress = 0;
        if (type == SEEK_TYPE_NO) {
            resume();
        } else if (type == SEEK_TYPE_LEFT) {
            progress = player.getCurrentPosition() - seekNum * 1000;
            if (progress < 0) {
                seekType = SEEK_TYPE_NO;
                this.seekNum = 0;
                player.seekTo(0);
                resume();
            }
            switchPlayTypeUI(TYPE_SEEK_BACK, seekNum);
            showControlUI(false);
            player.seekTo(progress);
            sendSeekMsg(new HandlerMsgModel(PlayHandler.MSG_SEEK, 1000L));
        } else {
            progress = player.getCurrentPosition() + seekNum * 1000;
            if (progress > player.getDuration()) {
                seekType = SEEK_TYPE_NO;
                this.seekNum = 0;
                player.seekTo(player.getDuration());
                stop();
            }
            switchPlayTypeUI(TYPE_SEEK_FORWARD, seekNum);
            showControlUI(false);
            player.seekTo(progress);
            sendSeekMsg(new HandlerMsgModel(PlayHandler.MSG_SEEK, 1000L));
        }
    }

    private void stop() {
        Log.i(TAG, "stop");
        switchPlayTypeUI(TYPE_STOP, -1);
        showControlUI(false);
//        if (playHandler.hasMessages(PlayHandler.MSG_UPGRADE_PROGRESS))
//            playHandler.removeMessages(PlayHandler.MSG_UPGRADE_PROGRESS);
    }

    private void sendSeekMsg(HandlerMsgModel progMsg) {
        HandlerMsgManager.getInstance().sendMessage(playHandler, progMsg);
    }

    private void sendUpgradePrgressMsg(HandlerMsgModel progMsg) {
        HandlerMsgManager.getInstance().sendMessage(playHandler, progMsg);
    }

    private void sendDissControlUIMsg(HandlerMsgModel progMsg) {
        HandlerMsgManager.getInstance().sendMessage(playHandler, progMsg);
    }

    private void removeSeekMsg() {
        HandlerMsgManager.getInstance().removeMessage(playHandler, PlayHandler.MSG_SEEK);
    }

    private void removeUpgradeProgressMsg() {
        HandlerMsgManager.getInstance().removeMessage(playHandler, PlayHandler.MSG_UPGRADE_PROGRESS);
    }

    private void removeDissControlUIMsg() {
        HandlerMsgManager.getInstance().removeMessage(playHandler, PlayHandler.MSG_DISMISS_CONTROL_UI);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
