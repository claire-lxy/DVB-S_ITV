package com.konkawise.dtv.ui;

import android.content.Intent;
import android.os.Looper;
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
import com.konkawise.dtv.adapter.BlindTpAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.BlindTpModel;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.SearchResultDialog;
import com.konkawise.dtv.event.ProgramUpdateEvent;
import com.konkawise.dtv.weaktool.WeakTimerTask;
import com.sw.dvblib.msg.cb.SearchMsgCB;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.PSRNum_t;
import vendor.konka.hardware.dtvmanager.V1_0.PSSParam_t;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;
import vendor.konka.hardware.dtvmanager.V1_0.ScanProgress_t;

/**
 * 盲扫点击点击start按钮之后显示盲扫结果
 */
public class TpBlindActivity extends BaseActivity {
    public static String TAG = TpBlindActivity.class.getSimpleName();

    @BindView(R.id.txt_tp_tv)
    TextView txt_tp_tv;

    @BindView(R.id.ll_tv_radio)
    LinearLayout ll_tv_radio;

    @BindView(R.id.blind_recycleview)
    RecyclerView mRecyclerView;

    @BindView(R.id.blind_tv_recycleview_left)
    RecyclerView blind_tv_recycleview_left;

    @BindView(R.id.blind_radio_recycleview_right)
    RecyclerView blind_radio_recycleview_right;

    @BindView(R.id.ll_recycel)
    LinearLayout ll_recycel;

    @BindView(R.id.tv_new_tp)
    TextView tv_new_tp;

    @BindView(R.id.tv_tp_num)
    TextView mTvTp_num;

    @BindView(R.id.radio_tp)
    TextView radio_tp;

    @BindView(R.id.radio_tp_num)
    TextView radio_tp_num;

    @BindView(R.id.tv_progress_tp)
    TextView tv_progress_tp;

    @BindView(R.id.progress_tp_blind)
    ProgressBar progress_tp_blind;

    private BlindTpAdapter mBlindTpAdapter;
    private BlindTpAdapter mBlindTvAdapter;
    private BlindTpAdapter mBlindRadioAdapter;

    private Timer mBlindScanProgressTimer;
    private BlindScanProgressTimerTask mBlindScanProgressTimerTask;

    private SearchMsgCB mSearchMsgCB = new BlindSearchMsgCB();

    @Override
    public int getLayoutId() {
        return R.layout.activity_tp_blind;
    }

    @Override
    protected void setup() {
        mTvTp_num.setText("0");
        radio_tp_num.setText("0");
        tv_progress_tp.setText("0%");

        initRecyclerView();
        setupBlindSatInfo();

        SWPSearchManager.getInstance().config(SWFtaManager.getInstance().getCurrScanMode(),
                SWFtaManager.getInstance().getCurrCAS(), SWFtaManager.getInstance().getCurrNetwork());
        SWDVBManager.getInstance().regMsgHandler(Constants.SCAN_CALLBACK_MSG_ID, Looper.getMainLooper(), mSearchMsgCB);
        SWFtaManager.getInstance().blindScanStart(getSatelliteIndex());

        startBlindScanProgressTimer();
    }

    @Override
    protected void onPause() {
        super.onPause();
        SWDVBManager.getInstance().unRegMsgHandler(Constants.SCAN_CALLBACK_MSG_ID, mSearchMsgCB);
        SWPSearchManager.getInstance().seatchStop(false);
        SWFtaManager.getInstance().blindScanStop();
        stopBlindScanProgressTimer();
    }

    private void initRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mBlindTpAdapter = new BlindTpAdapter(this, new ArrayList<BlindTpModel>());
        mRecyclerView.setAdapter(mBlindTpAdapter);

        blind_tv_recycleview_left.setHasFixedSize(true);
        blind_tv_recycleview_left.setLayoutManager(new LinearLayoutManager(this));
        mBlindTvAdapter = new BlindTpAdapter(this, new ArrayList<BlindTpModel>());
        blind_tv_recycleview_left.setAdapter(mBlindTvAdapter);

        blind_radio_recycleview_right.setHasFixedSize(true);
        blind_radio_recycleview_right.setLayoutManager(new LinearLayoutManager(this));
        mBlindRadioAdapter = new BlindTpAdapter(this, new ArrayList<BlindTpModel>());
        blind_radio_recycleview_right.setAdapter(mBlindRadioAdapter);
    }

    private void setupBlindSatInfo() {
        // 盲扫没有固定频点
        SatInfo_t satInfo = SWPDBaseManager.getInstance().getSatList().get(getSatelliteIndex());
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
                        if (scanProgress.currStep <= scanProgress.endStep) {
                            String progress = scanProgress.currStep + "%";
                            context.tv_progress_tp.setText(progress);
                            context.progress_tp_blind.setMax(scanProgress.endStep);
                            context.progress_tp_blind.setProgress(scanProgress.currStep);
                        }
                    }
                }
            });
        }
    }

    private class BlindSearchMsgCB extends SearchMsgCB {

        @Override
        public int PSearch_PROG_ONETSFAIL(int AllNum, int CurrIndex, int Sat,
                                          int freq, int symbol, int qam) {
            int curr = 0;
            if (mBlindTpAdapter.getItemCount() > 0) {
                curr = CurrIndex * 100 / mBlindTpAdapter.getItemCount();
            }

            String percent = curr + "%";
            tv_progress_tp.setText(percent);
            progress_tp_blind.setMax(100);
            progress_tp_blind.setProgress(curr);
            return 0;
        }

        /**
         * 搜所频道
         */
        @Override
        public int PSearch_PROG_ONETSOK(int AllNum, int CurrIndex, int Sat,
                                        int freq, int symbol, int qam) {
            stopBlindScanProgressTimer();
            PSRNum_t psr = SWPSearchManager.getInstance().getProgNumOfThisSarch(Sat, freq);
            if (null == psr) return 1;

            ArrayList<PDPInfo_t> list = SWPSearchManager.getInstance().getTsSearchResInfo(Sat, freq, symbol, qam);
            if (list == null) return 0;

            updateTvList(list);
            updateRadioList(list);

            mTvTp_num.setText(String.valueOf(mBlindTvAdapter.getItemCount()));
            radio_tp_num.setText(String.valueOf(mBlindRadioAdapter.getItemCount()));
            return 0;
        }

        private void updateTvList(ArrayList<PDPInfo_t> pdpInfoList) {
            List<BlindTpModel> tvList = getTvList(pdpInfoList);
            mBlindTvAdapter.addData(tvList);
            blind_tv_recycleview_left.scrollToPosition(mBlindTvAdapter.getItemCount() - 1);
        }

        private void updateRadioList(ArrayList<PDPInfo_t> pdpInfoList) {
            List<BlindTpModel> radioList = getRadioList(pdpInfoList);
            mBlindRadioAdapter.addData(radioList);
            blind_radio_recycleview_right.scrollToPosition(mBlindRadioAdapter.getItemCount() - 1);
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
        public int PSearch_PROG_SEARCHFINISH(int AllNum, int Curr) {
            SWPDBaseManager.getInstance().setCurrProgType(SWFtaManager.getInstance().getCurrScanMode() == 2 ? 1 : 0, 0);
            SWPSearchManager.getInstance().seatchStop(true);
            showSearchResultDialog();
            return 0;
        }

        @Override
        public int PSearch_PROG_STARTSEARCH(int AllNum, int CurrIndex, int Sat,
                                            int freq, int symbol, int qam) {
            int curr = 0;
            if (mBlindTpAdapter.getItemCount() > 0) {
                curr = CurrIndex * 100 / mBlindTpAdapter.getItemCount();
            }

            String percent = curr + "%";
            tv_progress_tp.setText(percent);
            progress_tp_blind.setMax(100);
            progress_tp_blind.setProgress(curr);
            return 0;
        }

        /**
         * 盲扫出tp
         */
        @Override
        public void PSearch_BlindScanNewTP(int freq, int polarization, int symbol) {
            updateTpList(freq, symbol, polarization);
            txt_tp_tv.setText(getResources().getString(R.string.tp));
            mTvTp_num.setText(String.valueOf(mBlindTpAdapter.getItemCount()));
        }

        private void updateTpList(int freq, int symbol, int qam) {
            BlindTpModel model = new BlindTpModel();
            model.pssParam_t = new PSSParam_t();
            model.pssParam_t.Sat = getSatelliteIndex();
            model.pssParam_t.Freq = freq;
            model.pssParam_t.Rate = symbol;
            model.pssParam_t.Qam = qam;
            mBlindTpAdapter.addData(mBlindTpAdapter.getItemCount(), model);
            mRecyclerView.scrollToPosition(mBlindTpAdapter.getItemCount() - 1);
        }

        /**
         * 盲扫进度
         */
        @Override
        public void PSearch_BlindScanProgress(int progress) {
            String percent = progress + "%";
            tv_progress_tp.setText(percent);
            progress_tp_blind.setMax(100);
            progress_tp_blind.setProgress(progress);
        }

        /**
         * 盲扫完成
         */
        @Override
        public void PSearch_BlindScanFinish() {
            txt_tp_tv.setVisibility(View.GONE);

            ll_tv_radio.setVisibility(View.VISIBLE);
            ll_recycel.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            tv_new_tp.setText(getResources().getText(R.string.scan_tp_tv));
            mTvTp_num.setText("0");
            radio_tp_num.setText("0");

            radio_tp.setVisibility(View.VISIBLE);
            radio_tp_num.setVisibility(View.VISIBLE);
            SWPSearchManager.getInstance().searchByNet(getSatelliteIndex(), getPsList());
        }
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
        int tvSize = Integer.valueOf(mTvTp_num.getText().toString());
        int radioSize = Integer.valueOf(radio_tp_num.getText().toString());
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

    private void startPlayTV() {
        Intent intent = new Intent(this, Topmost.class);
        startActivity(intent);
        finish();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            new CommRemindDialog()
                    .content(getString(R.string.back_infomation))
                    .setOnPositiveListener("", new OnCommPositiveListener() {
                        @Override
                        public void onPositiveListener() {
                            SWFtaManager.getInstance().blindScanStop();
                            SWPSearchManager.getInstance().seatchStop(false);
                            finish();
                        }
                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
