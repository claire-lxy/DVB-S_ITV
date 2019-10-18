package com.konkawise.dtv.ui;

import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.DTVSettingManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.DTVDVBManager;
import com.konkawise.dtv.DTVSearchManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.adapter.BlindTpAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.BlindTpModel;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.SearchResultDialog;
import com.konkawise.dtv.event.ProgramUpdateEvent;
import com.konkawise.dtv.weaktool.WeakTimerTask;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.listener.CallbackListenerAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Enum_Type;
import vendor.konka.hardware.dtvmanager.V1_0.HSearch_Enum_StoreType;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgBasicInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HSearch_Struct_ProgNumStat;
import vendor.konka.hardware.dtvmanager.V1_0.HSearch_Struct_TP;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_SatInfo;
import vendor.konka.hardware.dtvmanager.V1_0.HSearch_Struct_Progress;

public class TpBlindActivity extends BaseActivity {
    public static String TAG = TpBlindActivity.class.getSimpleName();

    @BindView(R.id.tv_blind_title)
    TextView mTvBlindTitle;

    @BindView(R.id.ll_scan_tv_and_radio)
    LinearLayout mScanTvAndRadioLayout;

    @BindView(R.id.rv_blind_list)
    RecyclerView mRvBlind;

    @BindView(R.id.rv_tv_list)
    RecyclerView mRvTv;

    @BindView(R.id.rv_radio_list)
    RecyclerView mRvRadio;

    @BindView(R.id.ll_tv_and_radio_list)
    LinearLayout mTvAndRadioListLayout;

    @BindView(R.id.tv_tp_num_title)
    TextView mTvTpNumTitle;

    @BindView(R.id.tv_tp_num)
    TextView mTvTpNum;

    @BindView(R.id.tv_radio_title)
    TextView mTvRadioTitle;

    @BindView(R.id.tv_radio_num)
    TextView mTvRadioNum;

    @BindView(R.id.tv_blind_progress)
    TextView mTvBlindProgress;

    @BindView(R.id.pb_blind)
    ProgressBar mPbBlind;

    private BlindTpAdapter mBlindTpAdapter;
    private BlindTpAdapter mBlindTvAdapter;
    private BlindTpAdapter mBlindRadioAdapter;

    private Timer mBlindScanProgressTimer;
    private BlindScanProgressTimerTask mBlindScanProgressTimerTask;

    private int mScanMode;
    private int mNitOpen;
    private int mCaFilter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_tp_blind;
    }

    @Override
    protected void setup() {
        mTvTpNum.setText("0");
        mTvRadioNum.setText("0");
        mTvBlindProgress.setText("0%");

        initRecyclerView();
        setupBlindSatInfo();

        mScanMode = DTVSettingManager.getInstance().getCurrScanMode();
        mNitOpen = DTVSettingManager.getInstance().getCurrNetwork();
        mCaFilter = DTVSettingManager.getInstance().getCurrCAS();
        DTVSearchManager.getInstance().blindScanStart(getSatelliteIndex());

        startBlindScanProgressTimer();
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerMsgEvent();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterMsgEvent();
        stopSearch(false, mNitOpen);
        stopBlindScanProgressTimer();
        stopBlindScan();
    }

    private void stopSearch(boolean storeProgram, int nit) {
        if (nit == 0) {
            DTVSearchManager.getInstance().searchStop(storeProgram, HSearch_Enum_StoreType.BY_SERVID);
        } else {
            DTVSearchManager.getInstance().searchStop(storeProgram, HSearch_Enum_StoreType.BY_LOGIC_NUM);
        }
    }

    private void registerMsgEvent() {
        MsgEvent msgEvent = DTVDVBManager.getInstance().registerMsgEvent(Constants.SCAN_CALLBACK_MSG_ID);
        msgEvent.registerCallbackListener(new CallbackListenerAdapter() {
            @Override
            public void SEARCH_onOneTsFailed(int allNum, int currIndex, int sat, int freq, int symbol, int qam, int plp) {
                int curr = 0;
                if (mBlindTpAdapter.getItemCount() > 0) {
                    curr = currIndex * 100 / mBlindTpAdapter.getItemCount();
                }

                String percent = curr + "%";
                mTvBlindProgress.setText(percent);
                mPbBlind.setMax(100);
                mPbBlind.setProgress(curr);
            }

            @Override
            public void SEARCH_onOneTSOk(int allNum, int currIndex, int sat, int freq, int symbol, int qam, int plp) {
                HSearch_Struct_ProgNumStat psr = DTVSearchManager.getInstance().getProgNumOfThisSarch(sat, freq);
                if (null == psr) return;

                ArrayList<HProg_Struct_ProgBasicInfo> list = DTVSearchManager.getInstance().getTsSearchResInfo(sat, freq, symbol, qam, plp);
                if (list == null) return;

                updateTvList(list);
                updateRadioList(list);

                mTvTpNum.setText(String.valueOf(mBlindTvAdapter.getItemCount()));
                mTvRadioNum.setText(String.valueOf(mBlindRadioAdapter.getItemCount()));
            }

            private void updateTvList(ArrayList<HProg_Struct_ProgBasicInfo> pdpInfoList) {
                List<BlindTpModel> tvList = getTvList(pdpInfoList);
                mBlindTvAdapter.addData(tvList);
                mRvTv.scrollToPosition(mBlindTvAdapter.getItemCount() - 1);
            }

            private void updateRadioList(ArrayList<HProg_Struct_ProgBasicInfo> pdpInfoList) {
                List<BlindTpModel> radioList = getRadioList(pdpInfoList);
                mBlindRadioAdapter.addData(radioList);
                mRvRadio.scrollToPosition(mBlindRadioAdapter.getItemCount() - 1);
            }

            private List<BlindTpModel> getTvList(ArrayList<HProg_Struct_ProgBasicInfo> pdpInfoList) {
                List<BlindTpModel> tvList = new ArrayList<>();
                for (HProg_Struct_ProgBasicInfo pdpInfo_t : pdpInfoList) {
                    if (pdpInfo_t.ServType == 1) {
                        BlindTpModel blindTpModel = new BlindTpModel();
                        blindTpModel.pdpInfo_t = pdpInfo_t;
                        blindTpModel.type = BlindTpModel.VIEW_TYPE_PRO;
                        tvList.add(blindTpModel);
                    }
                }
                return tvList;
            }

            private List<BlindTpModel> getRadioList(ArrayList<HProg_Struct_ProgBasicInfo> pdpInfoList) {
                List<BlindTpModel> radioList = new ArrayList<>();
                for (HProg_Struct_ProgBasicInfo pdpInfo_t : pdpInfoList) {
                    if (pdpInfo_t.ServType == 2) {
                        BlindTpModel blindTpModel = new BlindTpModel();
                        blindTpModel.pdpInfo_t = pdpInfo_t;
                        blindTpModel.type = BlindTpModel.VIEW_TYPE_PRO;
                        radioList.add(blindTpModel);
                    }
                }
                return radioList;
            }

            @Override
            public void SEARCH_onSearchFinish(int allNum, int currIndex, int plp) {
                DTVProgramManager.getInstance().setCurrProgType(DTVSettingManager.getInstance().getCurrScanMode() == 2 ? HProg_Enum_Type.GBPROG : HProg_Enum_Type.TVPROG, 0);
                stopSearch(true, mNitOpen);
                showSearchResultDialog();
            }

            @Override
            public void SEARCH_onStartSearch(int allNum, int currIndex, int sat, int freq, int symbol, int qam, int plp) {
                int curr = 0;
                if (mBlindTpAdapter.getItemCount() > 0) {
                    curr = currIndex * 100 / mBlindTpAdapter.getItemCount();
                }

                String percent = curr + "%";
                mTvBlindProgress.setText(percent);
                mPbBlind.setMax(100);
                mPbBlind.setProgress(curr);
            }

            @Override
            public void SEARCH_onBlindScanNewTp(int freq, int polarization, int symbol) {
                updateTpList(freq, symbol, polarization);
                mTvTpNum.setText(String.valueOf(mBlindTpAdapter.getItemCount()));
            }

            private void updateTpList(int freq, int symbol, int qam) {
                BlindTpModel model = new BlindTpModel();
                model.pssParam_t = new HSearch_Struct_TP();
                model.pssParam_t.Sat = getSatelliteIndex();
                model.pssParam_t.Freq = freq;
                model.pssParam_t.Rate = symbol;
                model.pssParam_t.Qam = qam;
                mBlindTpAdapter.addData(mBlindTpAdapter.getItemCount(), model);
                mRvBlind.scrollToPosition(mBlindTpAdapter.getItemCount() - 1);
            }

            @Override
            public void SEARCH_onBlindScanProgress(int progress) {
                String percent = progress + "%";
                mTvBlindProgress.setText(percent);
                mPbBlind.setMax(100);
                mPbBlind.setProgress(progress);
            }

            @Override
            public void SEARCH_onBlindScanFinish() {
                if (mBlindTpAdapter.getItemCount() <= 0) {
                    showNoTpFoundDialog();
                    return;
                }

                mTvBlindTitle.setVisibility(View.GONE);

                mScanTvAndRadioLayout.setVisibility(View.VISIBLE);
                mTvAndRadioListLayout.setVisibility(View.VISIBLE);
                mRvBlind.setVisibility(View.GONE);
                mTvTpNumTitle.setText(getResources().getText(R.string.scan_tp_tv));
                mTvTpNum.setText("0");
                mTvRadioNum.setText("0");

                mTvRadioTitle.setVisibility(View.VISIBLE);
                mTvRadioNum.setVisibility(View.VISIBLE);
                DTVSearchManager.getInstance().searchByNet(getSatelliteIndex(), getPsList(), mScanMode, mNitOpen, mCaFilter);
            }
        });
    }

    private void unregisterMsgEvent() {
        DTVDVBManager.getInstance().unregisterMsgEvent(Constants.SCAN_CALLBACK_MSG_ID);
    }

    private void initRecyclerView() {
        mRvBlind.setHasFixedSize(true);
        mRvBlind.setLayoutManager(new LinearLayoutManager(this));
        mBlindTpAdapter = new BlindTpAdapter(this, new ArrayList<BlindTpModel>());
        mRvBlind.setAdapter(mBlindTpAdapter);

        mRvTv.setHasFixedSize(true);
        mRvTv.setLayoutManager(new LinearLayoutManager(this));
        mBlindTvAdapter = new BlindTpAdapter(this, new ArrayList<BlindTpModel>());
        mRvTv.setAdapter(mBlindTvAdapter);

        mRvRadio.setHasFixedSize(true);
        mRvRadio.setLayoutManager(new LinearLayoutManager(this));
        mBlindRadioAdapter = new BlindTpAdapter(this, new ArrayList<BlindTpModel>());
        mRvRadio.setAdapter(mBlindRadioAdapter);
    }

    private void setupBlindSatInfo() {
        HProg_Struct_SatInfo satInfo = DTVProgramManager.getInstance().getSatList().get(DTVProgramManager.getInstance().findPositionBySatIndex(getSatelliteIndex()));
        HProg_Struct_SatInfo setupInfo = DTVProgramManager.getInstance().getSatInfo(getSatelliteIndex());
        setupInfo.LnbType = satInfo.LnbType;
        setupInfo.LnbPower = satInfo.LnbPower;
        setupInfo.diseqc10_pos = satInfo.diseqc10_pos;
        setupInfo.switch_22k = satInfo.switch_22k;
        setupInfo.lnb_low = satInfo.lnb_low;
        setupInfo.lnb_high = satInfo.lnb_high;
        setupInfo.Enable = satInfo.Enable;
        DTVProgramManager.getInstance().setSatInfo(getSatelliteIndex(), setupInfo);
    }

    private int getSatelliteIndex() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, -1);
    }

    private void startBlindScanProgressTimer() {
        mBlindScanProgressTimer = new Timer();
        mBlindScanProgressTimerTask = new BlindScanProgressTimerTask(this);
        mBlindScanProgressTimer.schedule(mBlindScanProgressTimerTask, 0, 1000);
    }

    private void stopBlindScanProgressTimer() {
        if (mBlindScanProgressTimer != null) {
            mBlindScanProgressTimer.cancel();
            mBlindScanProgressTimer.purge();
            mBlindScanProgressTimerTask.release();
            mBlindScanProgressTimer = null;
            mBlindScanProgressTimerTask = null;
        }
    }

    private static class BlindScanProgressTimerTask extends WeakTimerTask<TpBlindActivity> {

        BlindScanProgressTimerTask(TpBlindActivity view) {
            super(view);
        }

        @Override
        protected void runTimer() {
            TpBlindActivity context = mWeakReference.get();

            HSearch_Struct_Progress scanProgress = DTVSearchManager.getInstance().blindScanProgress();
            mWeakReference.get().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (scanProgress != null) {
                        if (scanProgress.currStep < scanProgress.endStep) {
                            String progress = scanProgress.currStep + "%";
                            context.mTvBlindProgress.setText(progress);
                            context.mPbBlind.setMax(scanProgress.endStep);
                            context.mPbBlind.setProgress(scanProgress.currStep);
                        } else {
                            if (scanProgress.currStep < 1000) {
                                context.mTvBlindProgress.setText("0%");
                                context.mPbBlind.setProgress(0);
                                context.stopBlindScanProgressTimer();
                            }
                        }
                    }
                }
            });
        }
    }

    private void stopBlindScan() {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                DTVSearchManager.getInstance().blindScanStop();
            }
        });
    }

    private ArrayList<HSearch_Struct_TP> getPsList() {
        ArrayList<HSearch_Struct_TP> psList = new ArrayList<>();
        if (mBlindTpAdapter.getItemCount() > 0) {
            for (BlindTpModel model : mBlindTpAdapter.getData()) {
                if (model.type == BlindTpModel.VIEW_TYPE_TP) {
                    psList.add(model.pssParam_t);
                }
            }
        }
        return psList;
    }

    private void showSearchResultDialog() {
        int tvSize = Integer.valueOf(mTvTpNum.getText().toString());
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

    private void showNoTpFoundDialog() {
        new SearchResultDialog().searchNoProgramContent(getString(R.string.dialog_search_no_tp))
                .setOnConfirmResultListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startPlayTV();
                    }
                }).show(getSupportFragmentManager(), SearchResultDialog.TAG);
    }

    private void startPlayTV() {
        Intent intent = new Intent(this, Topmost.class);
        startActivity(intent);
        finish();
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
}
