package com.konkawise.dtv.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.DTVCommonManager;
import com.konkawise.dtv.DTVPlayerManager;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.DTVSettingManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.RealTimeManager;
import com.konkawise.dtv.DTVEpgManager;
import com.konkawise.dtv.base.BaseDialog;
import com.konkawise.dtv.bean.DateModel;
import com.konkawise.dtv.rx.RxTransformer;
import com.konkawise.dtv.weaktool.CheckSignalHelper;
import com.konkawise.dtv.weaktool.WeakToolInterface;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import vendor.konka.hardware.dtvmanager.V1_0.HEPG_Struct_Event;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgInfo;

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
    private CheckSignalHelper mCheckSignalHelper;
    private Disposable mUpdateInformationDisposable;

    public PfBarScanDialog(Context context) {
        super(context);
        mContext = context;
        initBg();
        delayDismiss();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_pfbar_layout;
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

    private void delayDismiss() {
        Disposable disposable = Observable.timer(DTVSettingManager.getInstance().dismissTimeout(), TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(aLong -> dismiss());
        Log.i(TAG, "delay dismiss = " + disposable);
    }

    private void startCheckSignal() {
        stopCheckSignal();
        mCheckSignalHelper = new CheckSignalHelper();
        mCheckSignalHelper.setOnCheckSignalListener((strength, quality) -> {
            String strengthPercent = strength + "%";
            mTvProgressStrength.setText(strengthPercent);
            mPbStrength.setProgress(strength);

            String qualityPercent = quality + "%";
            mTvProgressQuality.setText(qualityPercent);
            mPbQuality.setProgress(quality);
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

        mUpdateInformationDisposable = Observable.interval(0, 1L, TimeUnit.SECONDS)
                .map(aLong -> {
                    HProg_Struct_ProgInfo currProgInfo = DTVProgramManager.getInstance().getCurrProgInfo();
                    if (currProgInfo != null) {
                        PfInfoModel pfInfoModel = new PfInfoModel();
                        pfInfoModel.currPfInfo = DTVEpgManager.getInstance().getPFEventOfServID(currProgInfo.Sat, currProgInfo.TsID, currProgInfo.ServID, 0);
                        pfInfoModel.nextPfInfo = DTVEpgManager.getInstance().getPFEventOfServID(currProgInfo.Sat, currProgInfo.TsID, currProgInfo.ServID, 1);
                        return pfInfoModel;
                    }
                    return null;
                })
                .compose(RxTransformer.threadTransformer())
                .subscribe(pfInfoModel -> {
                    if (pfInfoModel != null) {
                        updateProgInfo(pfInfoModel.currPfInfo);
                        updateProgInformation(pfInfoModel.currPfInfo, pfInfoModel.nextPfInfo);
                    }
                });
    }

    private void stopUpdateInformation() {
        if (mUpdateInformationDisposable != null) {
            mUpdateInformationDisposable.dispose();
            mUpdateInformationDisposable = null;
        }
    }

    @Override
    public void dismiss() {
        stopCheckSignal();
        stopUpdateInformation();
        RealTimeManager.getInstance().unregister(this);
        super.dismiss();
    }

    private void updateProgInfo(HEPG_Struct_Event currPfInfo) {
        HProg_Struct_ProgInfo currProgInfo = DTVProgramManager.getInstance().getCurrProgInfo();
        if (currProgInfo != null) {
            mTvProgNum.setText(String.valueOf(currProgInfo.PShowNo));
            mTvProgName.setText(currProgInfo.Name);
            mTvSubtitleNum.setText(String.valueOf(DTVPlayerManager.getInstance().getSubtitleNum(currProgInfo.ServID)));
            mTvTeletxtNum.setText(String.valueOf(DTVPlayerManager.getInstance().getTeletextNum(currProgInfo.ServID)));
            mTvRateNum.setText(String.valueOf(currPfInfo != null ? currPfInfo.Rating : 0));
            mTvSoundNum.setText(String.valueOf(currProgInfo.audioDB.audioName.size()));

            mIvProgFav.setVisibility(currProgInfo.FavFlag >= 1 ? View.VISIBLE : View.INVISIBLE);
            mIvProgLock.setVisibility(currProgInfo.LockFlag == 1 ? View.VISIBLE : View.INVISIBLE);
            mIvProgPay.setVisibility(currProgInfo.CasFlag == 1 ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void updateProgInformation(HEPG_Struct_Event currPfInfo, HEPG_Struct_Event nextPfInfo) {
        mTvInformation1.setText(getInformation(currPfInfo));
        mTvInformation2.setText(getInformation(nextPfInfo));
    }

    private String getInformation(HEPG_Struct_Event info) {
        if (info != null && info.utcStartData > 0 && info.utcStartTime > 0) {
            return new DateModel(DTVCommonManager.getInstance().getStartTime(info),
                    DTVCommonManager.getInstance().getEndTime(info)).getFormatHourAndMinute() + " " + info.memEventName;
        }
        return mContext.getString(R.string.no_information);
    }

    public void updatePfInformation() {
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

    private static class PfInfoModel {
        HEPG_Struct_Event currPfInfo;
        HEPG_Struct_Event nextPfInfo;
    }
}
