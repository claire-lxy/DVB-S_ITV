package com.konkawise.dtv.dialog;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Message;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.RealTimeManager;
import com.konkawise.dtv.SWEpgManager;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.SWTimerManager;
import com.konkawise.dtv.UIApiManager;
import com.konkawise.dtv.WeakToolManager;
import com.konkawise.dtv.base.BaseDialog;
import com.konkawise.dtv.bean.DateModel;
import com.konkawise.dtv.ui.Topmost;
import com.konkawise.dtv.weaktool.CheckSignalHelper;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakTimerTask;
import com.konkawise.dtv.weaktool.WeakToolInterface;
import com.sw.dvblib.SWDVB;

import java.util.Timer;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.EpgEvent_t;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

public class PfBarScanDialog extends BaseDialog implements WeakToolInterface, RealTimeManager.OnReceiveTimeListener {
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
    private int mCurrSelectProgPosition;
    private CheckSignalHelper mCheckSignalHelper;
    private Timer mUpdateInformationTimer;

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
        RealTimeManager.getInstance().register(this);
    }

    private void startUpdateInformation() {
        stopUpdateInformation();
        mUpdateInformationTimer = new Timer();
        mUpdateInformationTimer.schedule(new UpdateInformationTimerTask(this), 0, 1000);
    }

    private void stopUpdateInformation() {
        if (mUpdateInformationTimer != null) {
            mUpdateInformationTimer.cancel();
            mUpdateInformationTimer.purge();
            mUpdateInformationTimer = null;
        }
    }

    private static class UpdateInformationTimerTask extends WeakTimerTask<PfBarScanDialog> {

        UpdateInformationTimerTask(PfBarScanDialog view) {
            super(view);
        }

        @Override
        protected void runTimer() {
            EpgEvent_t currPfInfo = UIApiManager.getInstance().getCurrProgPFInfo();
            EpgEvent_t nextPfInfo = null;
            EpgEvent_t pfInfo = null;
            if (currPfInfo != null) {
                nextPfInfo = SWEpgManager.getInstance().getNextEitOfService(currPfInfo.sNextveit_index);
                if (nextPfInfo != null) {
                    pfInfo = SWEpgManager.getInstance().getNextEitOfService(nextPfInfo.sNextveit_index);
                }
            }

            final EpgEvent_t nextPf = nextPfInfo;
            final EpgEvent_t nnextPf = pfInfo;
            PfBarScanDialog dialog = mWeakReference.get();
            dialog.sHandler.post(new Runnable() {
                @Override
                public void run() {
                    dialog.updateProgInfo(currPfInfo);
                    dialog.updateProgInformation(nextPf, nnextPf);
                }
            });
        }
    }

    @Override
    public void dismiss() {
        stopCheckSignal();
        stopUpdateInformation();
        RealTimeManager.getInstance().unregister(this);
        WeakToolManager.getInstance().removeWeakTool(this);
        super.dismiss();
    }

    private void updateProgInfo(EpgEvent_t currPfInfo) {
        PDPMInfo_t currProgInfo = SWPDBaseManager.getInstance().getCurrProgInfo();
        if (currProgInfo != null) {
            mTvProgNum.setText(Topmost.LCNON ? String.valueOf(currProgInfo.PShowNo) : String.valueOf(mCurrSelectProgPosition + 1));
            mTvProgName.setText(currProgInfo.Name);
            mTvSubtitleNum.setText(String.valueOf(SWFtaManager.getInstance().getSubtitleNum(currProgInfo.ServID)));
            mTvTeletxtNum.setText(String.valueOf(SWFtaManager.getInstance().getTeletextNum(currProgInfo.ServID)));
            mTvRateNum.setText(String.valueOf(currPfInfo != null ? currPfInfo.Rating : 0));
            mTvSoundNum.setText(String.valueOf(currProgInfo.audioDB.audioName.size()));

            mIvProgFav.setVisibility(currProgInfo.FavFlag == 1 ? View.VISIBLE : View.INVISIBLE);
            mIvProgLock.setVisibility(currProgInfo.LockFlag == 1 ? View.VISIBLE : View.INVISIBLE);
            mIvProgPay.setVisibility(currProgInfo.CasFlag == 1 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void updateProgInformation(EpgEvent_t nextPfInfo, EpgEvent_t pfInfo) {
        if (nextPfInfo != null) mTvInformation1.setText(getInformation(nextPfInfo));
        if (pfInfo != null) mTvInformation2.setText(getInformation(pfInfo));
    }

    private String getInformation(EpgEvent_t info) {
        if (info == null) {
            return mContext.getString(R.string.no_information);
        }

        return new DateModel(SWTimerManager.getInstance().getStartTime(info),
                SWTimerManager.getInstance().getEndTime(info)).getFormatHourAndMinute() + " " + info.memEventName;
    }

    public void updatePfInformation(int mCurrSelectProgPosition) {
        this.mCurrSelectProgPosition = mCurrSelectProgPosition;
        startCheckSignal();
        startRealTime();
        startUpdateInformation();
    }

    @Override
    public void onReceiveTimeCallback(String time) {
        if (!TextUtils.isEmpty(time)) {
            mTvTime.setText(time);
        }
    }
}
