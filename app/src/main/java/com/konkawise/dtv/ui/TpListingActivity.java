package com.konkawise.dtv.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.DTVSearchManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.TpListingAdapter;
import com.konkawise.dtv.annotation.TpType;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.TpParamDialog;
import com.konkawise.dtv.dialog.ScanDialog;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.CheckSignalHelper;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.OnItemSelected;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_TP;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_SatInfo;

public class TpListingActivity extends BaseActivity implements LifecycleObserver {
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

        if (DTVProgramManager.getInstance().getSatList().size() - 1 > 0 && position < DTVProgramManager.getInstance().getSatList().size()) {
            HProg_Struct_SatInfo satInfo = DTVProgramManager.getInstance().getSatList().get(position);
            mTvLnbPower.setText(satInfo.LnbPower == 0 ? R.string.off : R.string.on);
        }
        mTvFreq.setText(getTpName());
        DTVSearchManager.getInstance().tunerLockFreq(getIndex(), getFreq(), getSymbol(), getQam(), 1, 0);
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void startCheckSignal() {
        stopCheckSignal();

        mCheckSignalHelper = new CheckSignalHelper();
        mCheckSignalHelper.setOnCheckSignalListener((strength, quality) -> {
            if (mAdapter.getCount() <= 0) {
                strength = 0;
                quality = 0;
            }
            String strengthPercent = strength + "%";
            mTvStrengthProgress.setText(strengthPercent);
            mPbStrength.setProgress(strength);

            String qualityPercent = quality + "%";
            mTvQualityProgress.setText(qualityPercent);
            mPbQuality.setProgress(quality);
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

    private TpListingAdapter mAdapter;
    private int mSelectPosition;

    private CheckSignalHelper mCheckSignalHelper;

    @Override
    public int getLayoutId() {
        return R.layout.activity_tp_listing;
    }

    @Override
    protected void setup() {
        initBottomBar();
        initTpList();

        mTvLnb.setText(getLnb());
        mTvDiSEqC.setText(getDiSEqC());
        mTvMotorType.setText(getMotorType());
    }

    @Override
    protected LifecycleObserver provideLifecycleObserver() {
        return this;
    }

    private void initBottomBar() {
        mTvBottomBarGreen.setText(getString(R.string.add));
        mTvBottomBarYellow.setText(getString(R.string.edit));
        mTvBottomBarBlue.setText(getString(R.string.delete));
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
        addObservable(Observable.just(DTVProgramManager.getInstance().getSatTPInfo(getIndex()))
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(satChannelInfoList -> {
                    if (satChannelInfoList != null && !satChannelInfoList.isEmpty()) {
                        mAdapter.updateData(satChannelInfoList);
                        mListView.setSelection(position);

                        HProg_Struct_TP channel = mAdapter.getItem(position);
                        if (channel != null) {
                            DTVSearchManager.getInstance().tunerLockFreq(getIndex(), channel.Freq, channel.Symbol, channel.Qam, 1, 0);
                        }
                    }
                }));
    }

    private int getIndex() {
        return getIntent().getIntExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, 0);
    }

    private String getLnb() {
        String lnb = getIntent().getStringExtra(Constants.IntentKey.INTENT_LNB);
        if (TextUtils.isEmpty(lnb)) return "";
        return lnb;
    }

    private String getDiSEqC() {
        String diseqc = getIntent().getStringExtra(Constants.IntentKey.ITENT_DISEQC);
        if (TextUtils.isEmpty(diseqc)) return "";
        return diseqc;
    }

    private String getMotorType() {
        String motorType = getIntent().getStringExtra(Constants.IntentKey.INTENT_MOTOR_TYPE);
        if (TextUtils.isEmpty(motorType)) return "";
        return motorType;
    }

    private String getTpName() {
        if (mAdapter.getCount() <= 0) return "";

        HProg_Struct_TP channel = mAdapter.getItem(mSelectPosition);
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
                .setOnScanSearchListener(v -> {
                    Intent intent = new Intent(TpListingActivity.this, ScanTVandRadioActivity.class);
                    intent.putExtra(Constants.IntentKey.INTENT_FREQ, getFreq());
                    intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, getIndex());
                    intent.putExtra(Constants.IntentKey.INTENT_SYMBOL, getSymbol());
                    intent.putExtra(Constants.IntentKey.INTENT_QAM, getQam());
                    intent.putExtra(Constants.IntentKey.INTENT_SEARCH_TYPE, Constants.IntentValue.SEARCH_TYPE_TPLISTING);
                    startActivity(intent);
                }).show(getSupportFragmentManager(), ScanDialog.TAG);
    }

    private void showTpDialog(@TpType final int tpType) {
        if (tpType == Constants.TpType.EDIT && mAdapter.isEmpty()) return;

        String freq = tpType == Constants.TpType.ADD ? "" : String.valueOf(getFreq());
        String symbol = tpType == Constants.TpType.ADD ? "" : String.valueOf(getSymbol());
        String qam = tpType == Constants.TpType.ADD ? "" : Utils.getVorH(this, getQam());

        new TpParamDialog()
                .freq(freq)
                .symbol(symbol)
                .qam(qam)
                .tpType(tpType)
                .setOnTpParamListener((freq1, symbol1, qam1) -> {
                    if (tpType == Constants.TpType.ADD) {
                        newTp(freq1, symbol1, qam1);
                    } else {
                        editTp(freq1, symbol1, qam1);
                    }
                }).show(getSupportFragmentManager(), TpParamDialog.TAG);
    }

    private void showDeleteTpDialog() {
        if (mAdapter.isEmpty()) return;

        new CommRemindDialog()
                .content(getString(R.string.delete_selected_transponder))
                .setOnPositiveListener("", this::deleteTp)
                .show(getSupportFragmentManager(), CommRemindDialog.TAG);
    }

    private void newTp(String freq, String symbol, String qam) {
        if (isParamEmpty(freq, symbol)) return;
        if (isParamOver(freq, symbol)) {
            ToastUtils.showToast(R.string.add_failure);
            return;
        }

        HProg_Struct_TP newTp = new HProg_Struct_TP();
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
        DTVProgramManager.getInstance().addTPInfo(newTp);
        int position = findTpPosition(newTp.Freq, newTp.Symbol, newTp.Qam);
        updateTpList(position <= -1 ? mAdapter.getCount() : position);
    }

    private void editTp(String freq, String symbol, String qam) {
        if (isParamEmpty(freq, symbol)) return;
        if (isParamOver(freq, symbol)) {
            ToastUtils.showToast(R.string.add_failure);
            return;
        }

        HProg_Struct_TP editTp = mAdapter.getItem(mSelectPosition);
        editTp.Freq = TextUtils.isEmpty(freq) ? 0 : Integer.parseInt(freq);
        editTp.Symbol = TextUtils.isEmpty(symbol) ? 0 : Integer.parseInt(symbol);
        if (qam.equals(getString(R.string.h))) {
            editTp.Qam = 0;
        } else if (qam.equals(getString(R.string.v))) {
            editTp.Qam = 1;
        }
        DTVProgramManager.getInstance().setTPInfo(editTp);
        updateTpList(mSelectPosition);

        mTvFreq.setText(getTpName());
    }

    private void deleteTp() {
        if (mAdapter.getCount() > 0) {
            HProg_Struct_TP channelNew_t = mAdapter.getItem(mSelectPosition);
            DTVProgramManager.getInstance().delTPInfo(channelNew_t);
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
                HProg_Struct_TP tp = mAdapter.getItem(i);
                if (tp.Freq == freq && tp.Symbol == symbol && tp.Qam == qam) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!mDispatchKeyUpReady) return super.onKeyUp(keyCode, event);

        if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            showScanDialog();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            showTpDialog(Constants.TpType.ADD);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            showTpDialog(Constants.TpType.EDIT);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            showDeleteTpDialog();
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
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
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
