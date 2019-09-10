package com.konkawise.dtv.ui;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWDVBManager;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.SWPSearchManager;
import com.konkawise.dtv.adapter.TvAndRadioRecycleViewAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.SearchResultDialog;
import com.konkawise.dtv.event.ProgramUpdateEvent;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.CheckSignalHelper;
import com.sw.dvblib.SWPDBase;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.listener.CallbackListenerAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

public class ScanTVandRadioActivity extends BaseActivity {
    private final static String TAG = ScanTVandRadioActivity.class.getSimpleName();

    @BindView(R.id.rv_tv_list)
    RecyclerView mRvTv;

    @BindView(R.id.rv_radio_list)
    RecyclerView mRvRadio;

    @BindView(R.id.tv_tv_and_radio_progress)
    TextView mTvTvAndRadioProgress;

    @BindView(R.id.pb_tv_and_radio)
    ProgressBar mPbTvAndRadio;

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

    private TvAndRadioRecycleViewAdapter mTvAdapter;
    private TvAndRadioRecycleViewAdapter mRadioAdapter;

    private List<SatInfo_t> mSatList;

    private int mutiSateIndex = -1;

    private CheckSignalHelper mCheckSignalHelper;

    @Override
    public int getLayoutId() {
        return R.layout.activity_scan_tv_and_radio;
    }

    @Override
    protected void setup() {
        SWPSearchManager.getInstance().config(SWFtaManager.getInstance().getCurrScanMode(),
                SWFtaManager.getInstance().getCurrCAS(), SWFtaManager.getInstance().getCurrNetwork());

        initIntent();
        initCheckSignal();
        initRecyclerView();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCheckSignalHelper.startCheckSignal();
        registerMsgEvent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCheckSignalHelper.stopCheckSignal();
        unregisterMsgEvent();
        SWPSearchManager.getInstance().seatchStop(false);
        SatelliteActivity.satList.clear();
    }

    private void registerMsgEvent() {
        MsgEvent msgEvent = SWDVBManager.getInstance().registerMsgEvent(Constants.SCAN_CALLBACK_MSG_ID);
        msgEvent.registerCallbackListener(new CallbackListenerAdapter() {
            private void onUpdateSearchProgress(int step, int max_step) {
                int bf = step * 100 / max_step;
                if (bf < 0) {
                    bf = 0;
                }
                if (bf > 100) {
                    bf = 100;
                }
                String percent = bf + "%";
                mTvTvAndRadioProgress.setText(percent);
                mPbTvAndRadio.setMax(max_step);
                mPbTvAndRadio.setProgress(step);
            }

            @Override
            public int PSearch_PROG_NITDATAOK(int AllNum, int Curr) {
                onUpdateSearchProgress(AllNum, Curr);
                return 0;
            }

            @Override
            public int PSearch_PROG_NITRECVTIMEOUT(int AllNum, int Curr) {
                onUpdateSearchProgress(AllNum, Curr);
                return 0;
            }

            /**
             * 搜索 TS Fail  搜索节目失败
             */
            @Override
            public int PSearch_PROG_ONETSFAIL(int AllNum, int CurrIndex, int Sat,
                                              int freq, int symbol, int qam) {
                updateScan(freq, symbol, qam, AllNum, CurrIndex);
                return 0;
            }


            /**
             * 搜索 TS ok   搜索节目成功回调
             *
             * @param AllNum    搜索TP个数
             * @param CurrIndex 当前tp
             * @param Sat       卫星
             */
            @Override
            public int PSearch_PROG_ONETSOK(int AllNum, int CurrIndex, int Sat,
                                            int freq, int symbol, int qam) {
                onUpdateSearchProgress(AllNum, CurrIndex);

                ArrayList<PDPInfo_t> pdpInfo_ts = SWPSearchManager.getInstance().getTsSearchResInfo(Sat, freq, symbol, qam);
                if (pdpInfo_ts == null) return 0;

                updateTvList(pdpInfo_ts);
                updateRadioList(pdpInfo_ts);

                mTvTvNum.setText(String.valueOf(mTvAdapter.getItemCount()));
                mTvRadioNum.setText(String.valueOf(mRadioAdapter.getItemCount()));
                return 0;
            }

            private void updateTvList(ArrayList<PDPInfo_t> pdpInfoList) {
                List<PDPInfo_t> tvList = getTvList(pdpInfoList);
                mTvAdapter.addData(tvList);
                mRvTv.scrollToPosition(mTvAdapter.getItemCount() - 1);
            }

            private void updateRadioList(ArrayList<PDPInfo_t> pdpInfoList) {
                List<PDPInfo_t> radioList = getRadioList(pdpInfoList);
                mRadioAdapter.addData(radioList);
                mRvRadio.scrollToPosition(mRadioAdapter.getItemCount() - 1);
            }

            private List<PDPInfo_t> getTvList(ArrayList<PDPInfo_t> pdpInfoList) {
                List<PDPInfo_t> tvList = new ArrayList<>();
                for (PDPInfo_t pdpInfo_t : pdpInfoList) {
                    if (pdpInfo_t.ServType == 1) {
                        tvList.add(pdpInfo_t);
                    }
                }
                return tvList;
            }

            private List<PDPInfo_t> getRadioList(ArrayList<PDPInfo_t> pdpInfoList) {
                List<PDPInfo_t> radioList = new ArrayList<>();
                for (PDPInfo_t pdpInfo_t : pdpInfoList) {
                    if (pdpInfo_t.ServType == 2) {
                        radioList.add(pdpInfo_t);
                    }
                }
                return radioList;
            }

            /**
             * 开始搜索
             */
            @Override
            public int PSearch_PROG_STARTSEARCH(int AllNum, int CurrIndex, int Sat,
                                                int freq, int symbol, int qam) {
                updateScan(freq, symbol, qam, AllNum, CurrIndex);
                return 0;
            }

            /**
             * 搜索完成
             */
            @Override
            public int PSearch_PROG_SEARCHFINISH(int AllNum, int Curr) {
                if (mutiSateIndex != -1 && mutiSateIndex < mSatList.size() - 1) {
                    mutiSateIndex++;
                    SWPSearchManager.getInstance().seatchStop(true);
                    searchMultiSatellite();
                    return 1;
                }
                SatelliteActivity.satList.clear();
                SWPDBaseManager.getInstance().setCurrProgType(SWFtaManager.getInstance().getCurrScanMode() == 2 ? SWPDBase.SW_GBPROG : SWPDBase.SW_TVPROG, 0);
                SWPSearchManager.getInstance().seatchStop(true);
                showSearchResultDialog();
                return 0;
            }

            private void updateScan(int freq, int symbol, int qam, int num, int index) {
                String tpName;
                if (isFromT2AutoSearch() || isFromT2ManualSearchActivity()) {
                    tpName = freq/10+"."+freq%10+"MHz" + " / " +symbol+"M";
                } else {
                    tpName = freq + Utils.getVorH(ScanTVandRadioActivity.this, qam) + symbol;
                }
                String tp = tpName + "(" + index + "/" + num + ")";
                mTvScanTp.setText(tp);
                onUpdateSearchProgress(num, index);
            }
        });
    }

    private void unregisterMsgEvent() {
        SWDVBManager.getInstance().unregisterMsgEvent(Constants.SCAN_CALLBACK_MSG_ID);
    }

    private void initIntent() {
        mSatList = SatelliteActivity.satList;

        if (mSatList.size() > 0 && isFromSatelliteActivity()) {
            mutiSateIndex = 0;
            searchMultiSatellite();
        } else if (mSatList.size() == 0 && isFromSatelliteActivity()) {
            setSatInfo();
            mTvSatelliteName.setText(SWPDBaseManager.getInstance().getSatInfo(getSatelliteIndex()).sat_name);
            SWPSearchManager.getInstance().searchByNet(getSatelliteIndex());
        }

        if (isFromEditManualActivity()) {
            mTvSatelliteName.setText(SWPDBaseManager.getInstance().getSatInfo(getSatelliteIndex()).sat_name);
            SWPSearchManager.getInstance().searchByNet(getSatelliteIndex());
        }

        if (isFromTpListingActivity()) {
            int freq = getFreq();
            int satIndex = getSatelliteIndex();
            int Symbol = getSymbol();
            int Qam = getQam();

            mTvSatelliteName.setText(SWPDBaseManager.getInstance().getSatInfo(satIndex).sat_name);
            SWPSearchManager.getInstance().searchByOneTS(satIndex, freq, Symbol, Qam);
        }

        if (isFromT2AutoSearch()) {
            mTvSatelliteName.setText(R.string.installation_t2);
            SWPSearchManager.getInstance().searchByNet(getSatelliteIndex());
        }

        if (isFromT2ManualSearchActivity()) {
            mTvSatelliteName.setText(R.string.installation_t2);
            int freq = getFreq();
            int satIndex = getSatelliteIndex();
            int Symbol = getSymbol();
            SWPSearchManager.getInstance().searchByOneTS(satIndex, freq, Symbol, 0);
        }

    }

    private boolean isFromSatelliteActivity() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_SATELLITE_ACTIVITY, -1) == 1;
    }

    private boolean isFromTpListingActivity() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_TPLIST_ACTIVITY, -1) == 2;
    }

    private boolean isFromEditManualActivity() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_EDIT_MANUAL_ACTIVITY, -1) == 3;
    }

    private boolean isFromT2ManualSearchActivity() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_T2_MANUAL_SEARCH_ACTIVITY, -1) == 4;
    }

    private boolean isFromT2AutoSearch() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_T2_AUTO_SEARCH, -1) == 5;
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

    private void initCheckSignal() {
        mCheckSignalHelper = new CheckSignalHelper(this);
        mCheckSignalHelper.setOnCheckSignalListener(new CheckSignalHelper.OnCheckSignalListener() {
            @Override
            public void signal(int strength, int quality) {
                String strengthPercent = strength + "%";
                mTvScanStrengthProgress.setText(strengthPercent);
                mPbScanStrength.setProgress(strength);

                String qualityPercent = quality + "%";
                mTvScanQualityProgress.setText(qualityPercent);
                mPbScanQuality.setProgress(quality);
            }
        });
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
        SWPSearchManager.getInstance().searchByNet(mSatList.get(mutiSateIndex).SatIndex);
    }

    /**
     * 卫星信息设置到bean中
     */
    public void setSatInfo() {
        int sat = SWPDBaseManager.getInstance().findPositionBySatIndex(getSatelliteIndex());
        SatInfo_t satInfo = SWPDBaseManager.getInstance().getSatList().get(sat);

        SatInfo_t updateInfo = SWPDBaseManager.getInstance().getSatInfo(sat);
        updateInfo.LnbType = satInfo.LnbType;
        updateInfo.LnbPower = satInfo.LnbPower;
        updateInfo.diseqc10_pos = satInfo.diseqc10_pos;
        updateInfo.switch_22k = satInfo.switch_22k;
        updateInfo.lnb_low = satInfo.lnb_low;
        updateInfo.lnb_high = satInfo.lnb_high;
        updateInfo.Enable = satInfo.Enable;

        SWPDBaseManager.getInstance().setSatInfo(sat, updateInfo);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new CommRemindDialog()
                    .content(getString(R.string.back_infomation))
                    .setOnPositiveListener("", new OnCommPositiveListener() {
                        @Override
                        public void onPositiveListener() {
                            finish();
                        }
                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    private void showSearchResultDialog() {
        int tvSize = Integer.valueOf(mTvTvNum.getText().toString());
        int radioSize = Integer.valueOf(mTvRadioNum.getText().toString());
        EventBus.getDefault().post(new ProgramUpdateEvent(tvSize, radioSize));

        new SearchResultDialog()
                .tvSize(tvSize)
                .radioSize(radioSize)
                .setOnConfirmResultListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPlayTV();
                    }
                }).show(getSupportFragmentManager(), SearchResultDialog.TAG);
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
