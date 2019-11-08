package com.konkawise.dtv.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.DTVSettingManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.DTVDVBManager;
import com.konkawise.dtv.DTVSearchManager;
import com.konkawise.dtv.adapter.TvAndRadioRecycleViewAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.SearchResultDialog;
import com.konkawise.dtv.event.ProgramUpdateEvent;
import com.konkawise.dtv.rx.RxBus;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.CheckSignalHelper;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.listener.CallbackListenerAdapter;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Enum_Type;
import vendor.konka.hardware.dtvmanager.V1_0.HSearch_Enum_StoreType;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgBasicInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_SatInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HSetting_Enum_Property;

public class ScanTVandRadioActivity extends BaseActivity implements LifecycleObserver {
    private final static String TAG = ScanTVandRadioActivity.class.getSimpleName();

    @BindView(R.id.rv_tv_list)
    RecyclerView mRvTv;

    @BindView(R.id.rv_radio_list)
    RecyclerView mRvRadio;

    @BindView(R.id.tv_satellite_name)
    TextView mTvSatelliteName;

    @BindView(R.id.tv_scan_tp)
    TextView mTvScanTp;

    @BindView(R.id.tv_tv_num)
    TextView mTvTvNum;

    @BindView(R.id.tv_radio_num)
    TextView mTvRadioNum;

    @BindView(R.id.tv_scan_strenth_progress)
    TextView mTvScanStrengthProgress;

    @BindView(R.id.pb_scan_strength)
    ProgressBar mPbScanStrength;

    @BindView(R.id.tv_scan_quality_progress)
    TextView mTvScanQualityProgress;

    @BindView(R.id.pb_scan_quality)
    ProgressBar mPbScanQuality;

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void startCheckSignal() {
        stopCheckSignal();

        mCheckSignalHelper = new CheckSignalHelper();
        mCheckSignalHelper.setOnCheckSignalListener((strength, quality) -> {
            String strengthPercent = strength + "%";
            mTvScanStrengthProgress.setText(strengthPercent);
            mPbScanStrength.setProgress(strength);

            String qualityPercent = quality + "%";
            mTvScanQualityProgress.setText(qualityPercent);
            mPbScanQuality.setProgress(quality);
        });
        mCheckSignalHelper.startCheckSignal();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void stopCheckSignal() {
        if (mCheckSignalHelper != null) {
            mCheckSignalHelper.stopCheckSignal();
            mCheckSignalHelper = null;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void quitBeforeStopSearch() {
        stopSearch(false);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void clearSelectSatelliteList() {
        SatelliteActivity.satList.clear();
    }

    private void stopSearch(boolean storeProgram) {
        if (mLcn == 0) {
            DTVSearchManager.getInstance().searchStop(storeProgram, HSearch_Enum_StoreType.BY_SERVID);
        } else {
            DTVSearchManager.getInstance().searchStop(storeProgram, HSearch_Enum_StoreType.BY_LOGIC_NUM);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    private void registerReceiveScanTVAndRadioMsg() {
        MsgEvent msgEvent = DTVDVBManager.getInstance().registerMsgEvent(Constants.MsgCallbackId.SCAN);
        msgEvent.registerCallbackListener(new CallbackListenerAdapter() {
            /**
             * 搜索 TS Fail  搜索节目失败
             */
            @Override
            public void SEARCH_onOneTsFailed(int allNum, int currIndex, int sat, int freq, int symbol, int qam, int plp) {
                updateScan(freq, symbol, qam, allNum, currIndex);
            }

            /**
             * 搜索 TS ok   搜索节目成功回调
             *
             * @param allNum    搜索TP个数
             * @param currIndex 当前tp
             * @param sat       卫星
             */
            @Override
            public void SEARCH_onOneTSOk(int allNum, int currIndex, int sat, int freq, int symbol, int qam, int plp) {
                ArrayList<HProg_Struct_ProgBasicInfo> pdpInfo_ts = DTVSearchManager.getInstance().getTsSearchResInfo(sat, freq, symbol, qam, plp);
                if (pdpInfo_ts == null) return;

                updateTvList(pdpInfo_ts);
                updateRadioList(pdpInfo_ts);

                mTvTvNum.setText(String.valueOf(mTvAdapter.getItemCount()));
                mTvRadioNum.setText(String.valueOf(mRadioAdapter.getItemCount()));
            }

            private void updateTvList(ArrayList<HProg_Struct_ProgBasicInfo> pdpInfoList) {
                List<HProg_Struct_ProgBasicInfo> tvList = getTvList(pdpInfoList);
                mTvAdapter.addData(tvList);
                mRvTv.scrollToPosition(mTvAdapter.getItemCount() - 1);
            }

            private void updateRadioList(ArrayList<HProg_Struct_ProgBasicInfo> pdpInfoList) {
                List<HProg_Struct_ProgBasicInfo> radioList = getRadioList(pdpInfoList);
                mRadioAdapter.addData(radioList);
                mRvRadio.scrollToPosition(mRadioAdapter.getItemCount() - 1);
            }

            private List<HProg_Struct_ProgBasicInfo> getTvList(ArrayList<HProg_Struct_ProgBasicInfo> pdpInfoList) {
                List<HProg_Struct_ProgBasicInfo> tvList = new ArrayList<>();
                for (HProg_Struct_ProgBasicInfo pdpInfo_t : pdpInfoList) {
                    if (pdpInfo_t.ServType == 1) {
                        tvList.add(pdpInfo_t);
                    }
                }
                return tvList;
            }

            private List<HProg_Struct_ProgBasicInfo> getRadioList(ArrayList<HProg_Struct_ProgBasicInfo> pdpInfoList) {
                List<HProg_Struct_ProgBasicInfo> radioList = new ArrayList<>();
                for (HProg_Struct_ProgBasicInfo pdpInfo_t : pdpInfoList) {
                    if (pdpInfo_t.ServType == 2) {
                        radioList.add(pdpInfo_t);
                    }
                }
                return radioList;
            }

            @Override
            public void SEARCH_onStartSearch(int allNum, int currIndex, int sat, int freq, int symbol, int qam, int plp) {
                updateScan(freq, symbol, qam, allNum, currIndex);
            }

            @Override
            public void SEARCH_onSearchFinish(int allNum, int currIndex, int plp) {
                if (mutiSateIndex != -1 && mutiSateIndex < mSatList.size() - 1) {
                    mutiSateIndex++;
                    stopSearch(true);
                    searchMultiSatellite();
                    return;
                }
                SatelliteActivity.satList.clear();
                DTVProgramManager.getInstance().setCurrProgType(DTVSettingManager.getInstance().getCurrScanMode() == 2 ? HProg_Enum_Type.GBPROG : HProg_Enum_Type.TVPROG, 0);
                stopSearch(true);
                showSearchResultDialog();
            }

            private void updateScan(int freq, int symbol, int qam, int num, int index) {
                String tpName;
                if (isFromT2AutoSearch() || isFromT2ManualSearchActivity()) {
                    tpName = freq / 10 + "." + freq % 10 + "MHz" + " / " + symbol + "M";
                } else {
                    tpName = freq + Utils.getVorH(ScanTVandRadioActivity.this, qam) + symbol;
                }
                String tp = tpName + "(" + index + "/" + num + ")";
                mTvScanTp.setText(tp);
            }
        });
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    private void unregisterReceiveScanTVAndRadioMsg() {
        DTVDVBManager.getInstance().unregisterMsgEvent(Constants.MsgCallbackId.SCAN);
    }

    private TvAndRadioRecycleViewAdapter mTvAdapter;
    private TvAndRadioRecycleViewAdapter mRadioAdapter;

    private List<HProg_Struct_SatInfo> mSatList;

    private int mutiSateIndex = -1;

    private CheckSignalHelper mCheckSignalHelper;

    private int mScanMode;
    private int mNitOpen;
    private int mCaFilter;
    private int mLcn;

    @Override
    public int getLayoutId() {
        return R.layout.activity_scan_tv_and_radio;
    }

    @Override
    protected void setup() {
        mScanMode = DTVSettingManager.getInstance().getCurrScanMode();
        mNitOpen = DTVSettingManager.getInstance().getCurrNetwork();
        mCaFilter = DTVSettingManager.getInstance().getCurrCAS();
        mLcn = DTVSettingManager.getInstance().getDTVProperty(HSetting_Enum_Property.ShowNoType);

        registerReceiveScanTVAndRadioMsg();

        initIntent();
        initRecyclerView();
    }

    @Override
    protected LifecycleObserver provideLifecycleObserver() {
        return this;
    }

    private void initIntent() {
        mSatList = SatelliteActivity.satList;

        if (mSatList.size() > 0 && isFromSatelliteActivity()) {
            mutiSateIndex = 0;
            searchMultiSatellite();
        } else if (mSatList.size() == 0 && isFromSatelliteActivity()) {
            setSatInfo();
            mTvSatelliteName.setText(DTVProgramManager.getInstance().getSatInfo(getSatelliteIndex()).sat_name);
            DTVSearchManager.getInstance().searchByNet(getSatelliteIndex(), mScanMode, mNitOpen, mCaFilter);
        }

        if (isFromEditManualActivity()) {
            mTvSatelliteName.setText(DTVProgramManager.getInstance().getSatInfo(getSatelliteIndex()).sat_name);
            DTVSearchManager.getInstance().searchByNet(getSatelliteIndex(), mScanMode, mNitOpen, mCaFilter);
        }

        if (isFromTpListingActivity()) {
            int freq = getFreq();
            int satIndex = getSatelliteIndex();
            int Symbol = getSymbol();
            int Qam = getQam();

            mTvSatelliteName.setText(DTVProgramManager.getInstance().getSatInfo(satIndex).sat_name);
            if (DTVSettingManager.getInstance().getCurrNetwork() == 0) {
                DTVSearchManager.getInstance().searchByOneTS(satIndex, freq, Symbol, Qam, mScanMode, mNitOpen, mCaFilter);
            } else {
                DTVSearchManager.getInstance().searchByNIT(satIndex, freq, Symbol, Qam, mScanMode, mNitOpen, mCaFilter);
            }

        }

        if (isFromT2AutoSearch()) {
            mTvSatelliteName.setText(R.string.installation_t2);
            DTVSearchManager.getInstance().searchByNet(getSatelliteIndex(), mScanMode, mNitOpen, mCaFilter);
        }

        if (isFromT2ManualSearchActivity()) {
            mTvSatelliteName.setText(R.string.installation_t2);
            int freq = getFreq();
            int satIndex = getSatelliteIndex();
            int Symbol = getSymbol();
            if (DTVSettingManager.getInstance().getCurrNetwork() == 0) {
                DTVSearchManager.getInstance().searchByOneTS(satIndex, freq, Symbol, 0, mScanMode, mNitOpen, mCaFilter);
            } else {
                DTVSearchManager.getInstance().searchByNIT(satIndex, freq, Symbol, 0, mScanMode, mNitOpen, mCaFilter);
            }
        }

    }

    private boolean isFromSatelliteActivity() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_SEARCH_TYPE, -1) == Constants.IntentValue.SEARCH_TYPE_SATELLITE;
    }

    private boolean isFromTpListingActivity() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_SEARCH_TYPE, -1) == Constants.IntentValue.SEARCH_TYPE_TPLISTING;
    }

    private boolean isFromEditManualActivity() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_SEARCH_TYPE, -1) == Constants.IntentValue.SEARCH_TYPE_EDITMANUAL;
    }

    private boolean isFromT2ManualSearchActivity() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_SEARCH_TYPE, -1) == Constants.IntentValue.SEARCH_TYPE_T2MANUAL;
    }

    private boolean isFromT2AutoSearch() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_SEARCH_TYPE, -1) == Constants.IntentValue.SEARCH_TYPE_T2AUTO;
    }

    private int getFreq() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_FREQ, -1);
    }

    private int getSatelliteIndex() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, -1);
    }

    private int getSymbol() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_SYMBOL, -1);
    }

    private int getQam() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_QAM, -1);
    }

    private void initRecyclerView() {
        mRvTv.setHasFixedSize(true);
        mRvTv.setLayoutManager(new LinearLayoutManager(this));
        mTvAdapter = new TvAndRadioRecycleViewAdapter(this, new ArrayList<>());
        mRvTv.setAdapter(mTvAdapter);

        mRvRadio.setHasFixedSize(true);
        mRvRadio.setLayoutManager(new LinearLayoutManager(this));
        mRadioAdapter = new TvAndRadioRecycleViewAdapter(this, new ArrayList<>());
        mRvRadio.setAdapter(mRadioAdapter);
    }

    private void searchMultiSatellite() {
        String satelliteName = mSatList.get(mutiSateIndex).sat_name + "(" + (mutiSateIndex + 1) + "/" + mSatList.size() + ")";
        mTvSatelliteName.setText(satelliteName);
        DTVSearchManager.getInstance().searchByNet(mSatList.get(mutiSateIndex).SatIndex, mScanMode, mNitOpen, mCaFilter);
    }

    /**
     * 卫星信息设置到bean中
     */
    public void setSatInfo() {
        int sat = DTVProgramManager.getInstance().findPositionBySatIndex(getSatelliteIndex());
        HProg_Struct_SatInfo satInfo = DTVProgramManager.getInstance().getSatList().get(sat);

        HProg_Struct_SatInfo updateInfo = DTVProgramManager.getInstance().getSatInfo(sat);
        updateInfo.LnbType = satInfo.LnbType;
        updateInfo.LnbPower = satInfo.LnbPower;
        updateInfo.diseqc10_pos = satInfo.diseqc10_pos;
        updateInfo.switch_22k = satInfo.switch_22k;
        updateInfo.lnb_low = satInfo.lnb_low;
        updateInfo.lnb_high = satInfo.lnb_high;
        updateInfo.Enable = satInfo.Enable;

        DTVProgramManager.getInstance().setSatInfo(sat, updateInfo);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new CommRemindDialog()
                    .content(getString(R.string.back_infomation))
                    .setOnPositiveListener("", this::finish).show(getSupportFragmentManager(), CommRemindDialog.TAG);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void showSearchResultDialog() {
        int tvSize = Integer.valueOf(mTvTvNum.getText().toString());
        int radioSize = Integer.valueOf(mTvRadioNum.getText().toString());
        RxBus.getInstance().post(new ProgramUpdateEvent(tvSize, radioSize));

        new SearchResultDialog()
                .tvSize(tvSize)
                .radioSize(radioSize)
                .setOnConfirmResultListener(v -> startPlayTV()).show(getSupportFragmentManager(), SearchResultDialog.TAG);
    }

    /**
     * 开始播放，进入播放界面
     */
    private void startPlayTV() {
        Intent intent = new Intent(this, Topmost.class);
        startActivity(intent);
        finish();
    }
}
