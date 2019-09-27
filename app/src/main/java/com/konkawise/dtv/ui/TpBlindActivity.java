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
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWDVBManager;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.SWPSearchManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.adapter.BlindTpAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.BlindTpModel;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.SearchResultDialog;
import com.konkawise.dtv.event.ProgramUpdateEvent;
import com.konkawise.dtv.weaktool.WeakTimerTask;
import com.sw.dvblib.SWPDBase;
import com.sw.dvblib.msg.MsgEvent;
import com.sw.dvblib.msg.listener.CallbackListenerAdapter;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.HSearchStoreType_E;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.PSRNum_t;
import vendor.konka.hardware.dtvmanager.V1_0.PSSParam_t;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.ScanProgress_t;

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

    private int nit;

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

        nit = SWFtaManager.getInstance().getCurrNetwork();
        SWPSearchManager.getInstance().config(SWFtaManager.getInstance().getCurrScanMode(),
                SWFtaManager.getInstance().getCurrCAS(), nit);
        SWFtaManager.getInstance().blindScanStart(getSatelliteIndex());

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
        stopSearch(false, nit);
        stopBlindScanProgressTimer();
        stopBlindScan();
    }

    private void stopSearch(boolean storeProgram, int nit) {
        if (nit == 0) {
            SWPSearchManager.getInstance().seatchStop(storeProgram, HSearchStoreType_E.BY_SERVID);
        } else {
            SWPSearchManager.getInstance().seatchStop(storeProgram, HSearchStoreType_E.BY_LOGIC_NUM);
        }
    }

    private void registerMsgEvent() {
        MsgEvent msgEvent = SWDVBManager.getInstance().registerMsgEvent(Constants.SCAN_CALLBACK_MSG_ID);
        msgEvent.registerCallbackListener(new CallbackListenerAdapter() {
            @Override
            public int PSearch_PROG_ONETSFAIL(int AllNum, int CurrIndex, int Sat,
                                              int freq, int symbol, int qam, int plpid) {
                int curr = 0;
                if (mBlindTpAdapter.getItemCount() > 0) {
                    curr = CurrIndex * 100 / mBlindTpAdapter.getItemCount();
                }

                String percent = curr + "%";
                mTvBlindProgress.setText(percent);
                mPbBlind.setMax(100);
                mPbBlind.setProgress(curr);
                return 0;
            }

            /**
             * 搜所频道
             */
            @Override
            public int PSearch_PROG_ONETSOK(int AllNum, int CurrIndex, int Sat,
                                            int freq, int symbol, int qam, int plpid) {
                PSRNum_t psr = SWPSearchManager.getInstance().getProgNumOfThisSarch(Sat, freq);
                if (null == psr) return 1;

                ArrayList<PDPInfo_t> list = SWPSearchManager.getInstance().getTsSearchResInfo(Sat, freq, symbol, qam, plpid);
                if (list == null) return 0;

                updateTvList(list);
                updateRadioList(list);

                mTvTpNum.setText(String.valueOf(mBlindTvAdapter.getItemCount()));
                mTvRadioNum.setText(String.valueOf(mBlindRadioAdapter.getItemCount()));
                return 0;
            }

            private void updateTvList(ArrayList<PDPInfo_t> pdpInfoList) {
                List<BlindTpModel> tvList = getTvList(pdpInfoList);
                mBlindTvAdapter.addData(tvList);
                mRvTv.scrollToPosition(mBlindTvAdapter.getItemCount() - 1);
            }

            private void updateRadioList(ArrayList<PDPInfo_t> pdpInfoList) {
                List<BlindTpModel> radioList = getRadioList(pdpInfoList);
                mBlindRadioAdapter.addData(radioList);
                mRvRadio.scrollToPosition(mBlindRadioAdapter.getItemCount() - 1);
            }

            private List<BlindTpModel> getTvList(ArrayList<PDPInfo_t> pdpInfoList) {
                List<BlindTpModel> tvList = new ArrayList<>();
                for (PDPInfo_t pdpInfo_t : pdpInfoList) {
                    if (pdpInfo_t.ServType == 1) {
                        BlindTpModel blindTpModel = new BlindTpModel();
                        blindTpModel.pdpInfo_t = pdpInfo_t;
                        blindTpModel.type = BlindTpModel.VIEW_TYPE_PRO;
                        tvList.add(blindTpModel);
                    }
                }
                return tvList;
            }

            private List<BlindTpModel> getRadioList(ArrayList<PDPInfo_t> pdpInfoList) {
                List<BlindTpModel> radioList = new ArrayList<>();
                for (PDPInfo_t pdpInfo_t : pdpInfoList) {
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
            public int PSearch_PROG_SEARCHFINISH(int AllNum, int Curr, int plpid) {
                SWPDBaseManager.getInstance().setCurrProgType(SWFtaManager.getInstance().getCurrScanMode() == 2 ? SWPDBase.SW_GBPROG : SWPDBase.SW_TVPROG, 0);
                stopSearch(true, nit);
                showSearchResultDialog();
                return 0;
            }

            @Override
            public int PSearch_PROG_STARTSEARCH(int AllNum, int CurrIndex, int Sat,
                                                int freq, int symbol, int qam, int plpid) {
                int curr = 0;
                if (mBlindTpAdapter.getItemCount() > 0) {
                    curr = CurrIndex * 100 / mBlindTpAdapter.getItemCount();
                }

                String percent = curr + "%";
                mTvBlindProgress.setText(percent);
                mPbBlind.setMax(100);
                mPbBlind.setProgress(curr);
                return 0;
            }

            /**
             * 盲扫出tp
             */
            @Override
            public void PSearch_BlindScanNewTP(int freq, int polarization, int symbol) {
                updateTpList(freq, symbol, polarization);
                mTvTpNum.setText(String.valueOf(mBlindTpAdapter.getItemCount()));
            }

            private void updateTpList(int freq, int symbol, int qam) {
                BlindTpModel model = new BlindTpModel();
                model.pssParam_t = new PSSParam_t();
                model.pssParam_t.Sat = getSatelliteIndex();
                model.pssParam_t.Freq = freq;
                model.pssParam_t.Rate = symbol;
                model.pssParam_t.Qam = qam;
                mBlindTpAdapter.addData(mBlindTpAdapter.getItemCount(), model);
                mRvBlind.scrollToPosition(mBlindTpAdapter.getItemCount() - 1);
            }

            /**
             * 盲扫进度
             */
            @Override
            public void PSearch_BlindScanProgress(int progress) {
                String percent = progress + "%";
                mTvBlindProgress.setText(percent);
                mPbBlind.setMax(100);
                mPbBlind.setProgress(progress);
            }

            /**
             * 盲扫完成
             */
            @Override
            public void PSearch_BlindScanFinish() {
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
                SWPSearchManager.getInstance().searchByNet(getSatelliteIndex(), getPsList());
            }
        });
    }

    private void unregisterMsgEvent() {
        SWDVBManager.getInstance().unregisterMsgEvent(Constants.SCAN_CALLBACK_MSG_ID);
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
        SatInfo_t satInfo = SWPDBaseManager.getInstance().getSatList().get(SWPDBaseManager.getInstance().findPositionBySatIndex(getSatelliteIndex()));
        SatInfo_t setupInfo = SWPDBaseManager.getInstance().getSatInfo(getSatelliteIndex());
        setupInfo.LnbType = satInfo.LnbType;
        setupInfo.LnbPower = satInfo.LnbPower;
        setupInfo.diseqc10_pos = satInfo.diseqc10_pos;
        setupInfo.switch_22k = satInfo.switch_22k;
        setupInfo.lnb_low = satInfo.lnb_low;
        setupInfo.lnb_high = satInfo.lnb_high;
        setupInfo.Enable = satInfo.Enable;
        SWPDBaseManager.getInstance().setSatInfo(getSatelliteIndex(), setupInfo);
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

            ScanProgress_t scanProgress = SWFtaManager.getInstance().blindScanProgress();
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
                SWFtaManager.getInstance().blindScanStop();
            }
        });
    }

    private ArrayList<PSSParam_t> getPsList() {
        ArrayList<PSSParam_t> psList = new ArrayList<>();
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
