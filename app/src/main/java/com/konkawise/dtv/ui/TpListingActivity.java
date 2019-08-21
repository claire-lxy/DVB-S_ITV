package com.konkawise.dtv.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.adapter.TpListingAdapter;
import com.konkawise.dtv.annotation.TpType;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.TpParamDialog;
import com.konkawise.dtv.dialog.ScanDialog;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.CheckSignalHelper;
import com.konkawise.dtv.weaktool.WeakRunnable;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemSelected;
import vendor.konka.hardware.dtvmanager.V1_0.ChannelNew_t;
import vendor.konka.hardware.dtvmanager.V1_0.Channel_t;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

/**
 * Manual Installation 点击TP按钮界面
 */
public class TpListingActivity extends BaseActivity {
    private static final String TAG = "TpListingActivity";
    private static final int FREQ_SYMBOL_MAX_LIMIT = 65535;

    @BindView(R.id.lv_tp_list)
    ListView mListView;

    @BindView(R.id.tv_lnb)
    TextView mTvLnb;

    @BindView(R.id.tv_lnb_power)
    TextView mTvLnbPower;

    @BindView(R.id.tv_freq)
    TextView mTvFreq;

    @BindView(R.id.tv_diseqc)
    TextView mTvDiSEqC;

    @BindView(R.id.tv_motor_type)
    TextView mTvMotorType;

    @BindView(R.id.tv_strength_progress)
    TextView mTvStrengthProgress;

    @BindView(R.id.pb_strength)
    ProgressBar mPbStrength;

    @BindView(R.id.tv_quality_progress)
    TextView mTvQualityProgress;

    @BindView(R.id.pb_quality)
    ProgressBar mPbQuality;

    @BindView(R.id.tv_bottom_bar_green)
    TextView mTvBottomBarGreen;

    @BindView(R.id.tv_bottom_bar_yellow)
    TextView mTvBottomBarYellow;

    @BindView(R.id.tv_bottom_bar_blue)
    TextView mTvBottomBarBlue;

    @OnItemSelected(R.id.lv_tp_list)
    void onItemSelect(int position) {
        mSelectPosition = position;

        if (SWPDBaseManager.getInstance().getSatList().size() - 1 > 0 && position < SWPDBaseManager.getInstance().getSatList().size()) {
            SatInfo_t satInfo_t = SWPDBaseManager.getInstance().getSatList().get(position);
            mTvLnbPower.setText(Utils.getOnorOff(this, satInfo_t.LnbPower));
        }
        mTvFreq.setText(getTpName());
        SWFtaManager.getInstance().tunerLockFreq(getIndex(), getFreq(), getSymbol(), getQam(), 1, 0);
    }

    private TpListingAdapter mAdapter;
    private int mSelectPosition;

    private CheckSignalHelper mCheckSignalHelper;
    private LoadTpRunnable mLoadTpRunnable;

    @Override
    public int getLayoutId() {
        return R.layout.activity_tp_listing;
    }

    @Override
    protected void setup() {
        initBottomBar();
        initCheckSignal();
        initTpList();

        mTvLnb.setText(getLnb());
        mTvDiSEqC.setText(getIntent().getStringExtra(Constants.IntentKey.INTENT_DISEQC));
        mTvMotorType.setText(getIntent().getStringExtra(Constants.IntentKey.INTENT_MOTOR_TYPE));
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCheckSignalHelper.startCheckSignal();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mCheckSignalHelper.stopCheckSignal();
    }

    private void initBottomBar() {
        mTvBottomBarGreen.setText(getString(R.string.add));
        mTvBottomBarYellow.setText(getString(R.string.edit));
        mTvBottomBarBlue.setText(getString(R.string.delete));
    }

    private void initCheckSignal() {
        mCheckSignalHelper = new CheckSignalHelper(this);
        mCheckSignalHelper.setOnCheckSignalListener(new CheckSignalHelper.OnCheckSignalListener() {
            @Override
            public void signal(int strength, int quality) {
                String strengthPercent = strength + "%";
                mTvStrengthProgress.setText(strengthPercent);
                mPbStrength.setProgress(strength);

                String qualityPercent = quality + "%";
                mTvQualityProgress.setText(qualityPercent);
                mPbQuality.setProgress(quality);
            }
        });
    }

    private void initTpList() {
        mAdapter = new TpListingAdapter(this, new ArrayList<>());
        mListView.setAdapter(mAdapter);
        loadTp(0);
    }

    private void updateTpList(int selection) {
        loadTp(selection);
    }

    private void loadTp(int position) {
        if (mLoadTpRunnable == null) {
            mLoadTpRunnable = new LoadTpRunnable(this);
        }
        ThreadPoolManager.getInstance().remove(mLoadTpRunnable);
        mLoadTpRunnable.position = position;
        ThreadPoolManager.getInstance().execute(mLoadTpRunnable);
    }

    private static class LoadTpRunnable extends WeakRunnable<TpListingActivity> {
        int position;

        LoadTpRunnable(TpListingActivity view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            TpListingActivity context = mWeakReference.get();
            List<ChannelNew_t> satChannelInfoList = SWPDBaseManager.getInstance().getSatChannelInfoList(context.getIndex());

            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (satChannelInfoList != null && !satChannelInfoList.isEmpty()) {
                        context.mAdapter.updateData(satChannelInfoList);
                        context.mListView.setSelection(position);
                    }
                }
            });
        }
    }

    private int getIndex() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, 0);
    }

    private String getLnb() {
        String lnb = getIntent().getStringExtra(Constants.IntentKey.INTENT_LNB);
        if (TextUtils.isEmpty(lnb)) return "";
        return lnb;
    }

    private String getTpName() {
        if (mAdapter.getCount() <= 0) return "";

        ChannelNew_t channel = mAdapter.getItem(mSelectPosition);
        if (channel == null || channel.Freq <= 0) return "";
        return channel.Freq + Utils.getVorH(this, channel.Qam) + channel.Symbol;
    }

    private int getFreq() {
        if (mAdapter.getCount() <= 0) return 0;
        return mAdapter.getItem(mSelectPosition).Freq;
    }

    private int getSymbol() {
        if (mAdapter.getCount() <= 0) return 0;
        return mAdapter.getItem(mSelectPosition).Symbol;
    }

    private int getQam() {
        if (mAdapter.getCount() <= 0) return 0;
        return mAdapter.getItem(mSelectPosition).Qam;
    }

    private void showScanDialog() {
        new ScanDialog()
                .setOnScanSearchListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(TpListingActivity.this, ScanTVandRadioActivity.class);
                        intent.putExtra(Constants.IntentKey.INTENT_FREQ, getFreq());
                        intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, getIndex());
                        intent.putExtra(Constants.IntentKey.INTENT_SYMBOL, getSymbol());
                        intent.putExtra(Constants.IntentKey.INTENT_QAM, getQam());
                        intent.putExtra(Constants.IntentKey.INTENT_TP_NAME, getTpName());
                        intent.putExtra(Constants.IntentKey.INTENT_TPLIST_ACTIVITY, 2);
                        startActivity(intent);
                    }
                }).show(getSupportFragmentManager(), ScanDialog.TAG);
    }

    private void showTpDialog(@TpType final int tpType) {
        String freq = tpType == Constants.TP_TYPE_ADD ? "" : String.valueOf(getFreq());
        String symbol = tpType == Constants.TP_TYPE_ADD ? "" : String.valueOf(getSymbol());
        String qam = tpType == Constants.TP_TYPE_ADD ? "" : Utils.getVorH(this, getQam());

        new TpParamDialog()
                .freq(freq)
                .symbol(symbol)
                .qam(qam)
                .tpType(tpType)
                .setOnTpParamListener(new TpParamDialog.OnTpParamListener() {
                    @Override
                    public void onEditTp(String freq, String symbol, String qam) {
                        if (tpType == Constants.TP_TYPE_ADD) {
                            newTp(freq, symbol, qam);
                        } else {
                            editTp(freq, symbol, qam);
                        }
                    }
                }).show(getSupportFragmentManager(), TpParamDialog.TAG);
    }

    private void showDeleteTpDialog() {
        new CommRemindDialog()
                .content(getString(R.string.delete_selected_transponder))
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        deleteTp();
                    }
                })
                .show(getSupportFragmentManager(), CommRemindDialog.TAG);
    }

    private void newTp(String freq, String symbol, String qam) {
        if (isParamEmpty(freq, symbol)) return;
        if (isParamOver(freq, symbol)) {
            ToastUtils.showToast(R.string.add_failure);
            return;
        }

        Channel_t newTp = new Channel_t();
        newTp.SatIndex = getIndex();
        newTp.Freq = TextUtils.isEmpty(freq) ? 0 : Integer.parseInt(freq);
        newTp.Symbol = TextUtils.isEmpty(symbol) ? 0 : Integer.parseInt(symbol);
        newTp.NetID = 0;
        newTp.TsID = 0;
        if (qam.equals(getString(R.string.h))) {
            newTp.Qam = 0;
        } else if (qam.equals(getString(R.string.v))) {
            newTp.Qam = 1;
        }
        SWPDBaseManager.getInstance().addChannelInfo(newTp);
        int position = findTpPosition(newTp.Freq, newTp.Symbol, newTp.Qam);
        updateTpList(position <= -1 ? mAdapter.getCount() : position);
    }

    private void editTp(String freq, String symbol, String qam) {
        if (isParamEmpty(freq, symbol)) return;
        if (isParamOver(freq, symbol)) {
            ToastUtils.showToast(R.string.add_failure);
            return;
        }

        ChannelNew_t editTp = mAdapter.getItem(mSelectPosition);
        editTp.Freq = TextUtils.isEmpty(freq) ? 0 : Integer.parseInt(freq);
        editTp.Symbol = TextUtils.isEmpty(symbol) ? 0 : Integer.parseInt(symbol);
        if (qam.equals(getString(R.string.h))) {
            editTp.Qam = 0;
        } else if (qam.equals(getString(R.string.v))) {
            editTp.Qam = 1;
        }
        SWPDBaseManager.getInstance().setSatChannelInfo(editTp);
        updateTpList(mSelectPosition);

        mTvFreq.setText(getTpName());
    }

    private void deleteTp() {
        if (mAdapter.getCount() > 0) {
            ChannelNew_t channelNew_t = mAdapter.getItem(mSelectPosition);
            SWPDBaseManager.getInstance().delChannelInfo(channelNew_t);
            updateTpList(0);
        }
    }

    private boolean isParamEmpty(String freq, String symbol) {
        return TextUtils.isEmpty(freq) && TextUtils.isEmpty(symbol);
    }

    private boolean isParamOver(String freq, String symbol) {
        return Integer.parseInt(freq) >= FREQ_SYMBOL_MAX_LIMIT && Integer.parseInt(symbol) >= FREQ_SYMBOL_MAX_LIMIT;
    }

    private int findTpPosition(int freq, int symbol, int qam) {
        if (mAdapter.getCount() >= 0) {
            for (int i = 0; i < mAdapter.getCount(); i++) {
                ChannelNew_t tp = mAdapter.getItem(i);
                if (tp.Freq == freq && tp.Symbol == symbol && tp.Qam == qam) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            showScanDialog();
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            showTpDialog(Constants.TP_TYPE_ADD);
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            showTpDialog(Constants.TP_TYPE_EDIT);
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            showDeleteTpDialog();
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (mSelectPosition == mAdapter.getCount() - 1) {
                mListView.setSelection(0);
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mSelectPosition == 0) {
                mListView.setSelection(mAdapter.getCount() - 1);
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            setResult(RESULT_OK);
            finish();
        }

        return super.onKeyDown(keyCode, event);
    }
}
