package com.konkawise.dtv.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.SWTimerManager;
import com.konkawise.dtv.UIApiManager;
import com.konkawise.dtv.WeakToolManager;
import com.konkawise.dtv.base.BaseDialog;
import com.konkawise.dtv.bean.DateModel;
import com.konkawise.dtv.weaktool.CheckSignalHelper;
import com.konkawise.dtv.weaktool.RealTimeHelper;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakToolInterface;
import com.sw.dvblib.SWDVB;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.EpgEvent_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

public class PfBarScanDialog extends BaseDialog implements WeakToolInterface {
    public static final String TAG = "PfBarScanDialog";

    @BindView(R.id.tv_pf_prog_num)
    TextView mTvProgNum;

    @BindView(R.id.tv_pf_prog_name)
    TextView mTvProgName;

    @BindView(R.id.iv_prog_fav)
    ImageView mIvProgFav;

    @BindView(R.id.iv_prog_lock)
    ImageView mIvProgLock;

    @BindView(R.id.iv_prog_pay)
    ImageView mIvProgPay;

    @BindView(R.id.tv_pf_sub_num)
    TextView mTvSubtitleNum;

    @BindView(R.id.tv_pf_txt_num)
    TextView mTvTeletxtNum;

    @BindView(R.id.tv_pf_rate_num)
    TextView mTvRateNum;

    @BindView(R.id.tv_pf_sound_num)
    TextView mTvSoundNum;

    @BindView(R.id.tv_pf_time)
    TextView mTvTime;

    @BindView(R.id.tv_pf_information_1)
    TextView mTvInformation1;

    @BindView(R.id.tv_pf_information_2)
    TextView mTvInformation2;

    @BindView(R.id.pb_quality)
    ProgressBar mPbQuality;

    @BindView(R.id.pb_strength)
    ProgressBar mPbStrength;

    @BindView(R.id.tv_progress_strength)
    TextView mTvProgressStrength;

    @BindView(R.id.tv_progress_quality)
    TextView mTvProgressQuality;

    private Context mContext;
    private AudioManager mAudioManager;
    private CheckSignalHelper mCheckSignalHelper;
    private RealTimeHelper mRealTimeHelper;

    @SuppressLint("HandlerLeak")
    private WeakHandler<PfBarScanDialog> sHandler = new WeakHandler<PfBarScanDialog>(this) {

        @Override
        protected void handleMsg(Message msg) {
            dismiss();
        }
    };

    public PfBarScanDialog(Context context) {
        super(context);
        initBg();
        SWDVB.GetInstance();
        mContext = context;
        mAudioManager = (AudioManager) getContext().getSystemService(Context.AUDIO_SERVICE);
        sHandler.sendEmptyMessageDelayed(0, SWFtaManager.getInstance().dismissTimeout());
    }

    @Override
    protected int getLayoutId() {
        return R.layout.pf_info_popwindow;
    }

    private void initBg() {
        Window window = getWindow();
        WindowManager.LayoutParams layoutParams = window.getAttributes();
        layoutParams.gravity = Gravity.BOTTOM;
        layoutParams.y = 20;
        window.setAttributes(layoutParams);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        window.setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE);
    }

    private void startCheckSignal() {
        stopCheckSignal();
        mCheckSignalHelper = new CheckSignalHelper(this);
        mCheckSignalHelper.setOnCheckSignalListener(new CheckSignalHelper.OnCheckSignalListener() {
            @Override
            public void signal(int strength, int quality) {
                String strengthPercent = strength + "%";
                mTvProgressStrength.setText(strengthPercent);
                mPbStrength.setProgress(strength);

                String qualityPercent = quality + "%";
                mTvProgressQuality.setText(qualityPercent);
                mPbQuality.setProgress(quality);
            }
        });
        mCheckSignalHelper.startCheckSignal();
    }

    private void stopCheckSignal() {
        if (mCheckSignalHelper != null) {
            mCheckSignalHelper.stopCheckSignal();
            mCheckSignalHelper = null;
        }
    }

    private void startRealTime() {
        stopRealTime();
        mRealTimeHelper = new RealTimeHelper(this);
        mRealTimeHelper.setOnRealTimeListener(new RealTimeHelper.OnRealTimerListener() {
            @Override
            public void onRealTimeCallback(String realTime) {
                mTvTime.setText(realTime);
            }
        });
        mRealTimeHelper.start();
    }

    private void stopRealTime() {
        if (mRealTimeHelper != null) {
            mRealTimeHelper.stop();
            mRealTimeHelper = null;
        }
    }

    @Override
    public void dismiss() {
        stopCheckSignal();
        stopRealTime();
        WeakToolManager.getInstance().removeWeakTool(this);
        super.dismiss();
    }

    public void updateVolume(int volume) {
//      mTvSoundNum.setText(String.valueOf(volume));
    }

    private void updateProgInfo() {
        PDPMInfo_t currProgInfo = SWPDBaseManager.getInstance().getCurrProgInfo();
        if (currProgInfo != null) {
            mTvProgNum.setText(String.valueOf(currProgInfo.PShowNo));
            mTvProgName.setText(currProgInfo.Name);
            mTvSubtitleNum.setText(String.valueOf(SWFtaManager.getInstance().getSubtitleNum(currProgInfo.ServID)));
            mTvTeletxtNum.setText(String.valueOf(SWFtaManager.getInstance().getTeletextNum(currProgInfo.ServID)));
            mTvRateNum.setText("0");
            mTvSoundNum.setText(String.valueOf(currProgInfo.audioDB.audioName.size()));

            mIvProgFav.setVisibility(currProgInfo.FavFlag == 1 ? View.VISIBLE : View.INVISIBLE);
            mIvProgLock.setVisibility(currProgInfo.LockFlag == 1 ? View.VISIBLE : View.INVISIBLE);
            mIvProgPay.setVisibility(currProgInfo.CasFlag == 1 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void updateProgInformation() {
        mTvInformation1.setText(getInformation(UIApiManager.getInstance().getCurrProgPFInfo(0)));
        mTvInformation2.setText(getInformation(UIApiManager.getInstance().getCurrProgPFInfo(1)));
    }

    private String getInformation(EpgEvent_t info) {
        if (info == null) {
            return mContext.getString(R.string.no_information);
        }

        return new DateModel(SWTimerManager.getInstance().getStartTime(info),
                SWTimerManager.getInstance().getEndTime(info)).getFormatHourAndMinute() + " " + info.memEventName;
    }

    public void updatePfInformation() {
//      mTvSoundNum.setText(String.valueOf(mAudioManager.getStreamVolume(AudioManager.STREAM_MUSIC)));
        startCheckSignal();
        startRealTime();
        updateProgInfo();
        updateProgInformation();
    }
}
