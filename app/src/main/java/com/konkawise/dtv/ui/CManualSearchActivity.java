package com.konkawise.dtv.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Intent;
import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.DTVSearchManager;
import com.konkawise.dtv.PreferenceManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseItemFocusChangeActivity;
import com.konkawise.dtv.dialog.ScanDialog;
import com.konkawise.dtv.weaktool.CheckSignalHelper;

import java.text.MessageFormat;
import java.util.List;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_TP;

public class CManualSearchActivity extends BaseItemFocusChangeActivity implements LifecycleObserver {

    private static final int ITEM_TRANSPONDER = 1;
    private static final int ITEM_FREQUENCY = 2;
    private static final int ITEM_SYMBOLRATE = 3;
    private static final int ITEM_QAM = 4;
    private static final String TAG = CManualSearchActivity.class.getSimpleName();

    private int mCurrntChannel = 0;

    @BindView(R.id.item_transponder)
    RelativeLayout rlItemTransponder;

    @BindView(R.id.item_frequency)
    RelativeLayout rlItemFrequency;

    @BindView(R.id.item_symbol_rate)
    RelativeLayout rlItemSymbolRate;

    @BindView(R.id.item_qam)
    RelativeLayout rlItemQam;

    @BindView(R.id.iv_transponder_left)
    ImageView mIvTransponderLeft;

    @BindView(R.id.tv_transponder)
    TextView mTvTransponder;

    @BindView(R.id.iv_transponder_right)
    ImageView mIvTransponderRight;

    @BindView(R.id.iv_frequency_left)
    ImageView mIvFrequencyLeft;

    @BindView(R.id.tv_frequency)
    TextView mTvFrequency;

    @BindView(R.id.iv_frequency_right)
    ImageView mIvFrequencyRight;

    @BindView(R.id.iv_symbol_rate_left)
    ImageView mIvSymbolRateLeft;

    @BindView(R.id.tv_symbol_rate)
    TextView mTvSymbolRate;

    @BindView(R.id.iv_symbol_rate_right)
    ImageView mIvSymbolRateRight;

    @BindView(R.id.iv_qam_left)
    ImageView mIvQamLeft;

    @BindView(R.id.tv_qam)
    TextView mTvQam;

    @BindView(R.id.iv_qam_right)
    ImageView mIvQamRight;

    @BindView(R.id.tv_progress_strength)
    TextView mTvStrengthProgress;

    @BindView(R.id.pb_strength)
    ProgressBar mPbStrength;

    @BindView(R.id.tv_progress_quality)
    TextView mTvQualityProgress;

    @BindView(R.id.pb_quality)
    ProgressBar mPbQuality;

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void startCheckSignal() {
        stopCheckSignal();

        mCheckSignalHelper = new CheckSignalHelper();
        mCheckSignalHelper.setOnCheckSignalListener((strength, quality) -> {
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

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void saveCurrentChannel() {
        PreferenceManager.getInstance().putInt(Constants.PrefsKey.SAVE_CHANNEL, mCurrntChannel);
    }

    private int mCurrSelectItem = ITEM_TRANSPONDER;

    private List<HProg_Struct_TP> satChannelInfoList;

    private HProg_Struct_TP channel;

    private CheckSignalHelper mCheckSignalHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_c_manual_search;
    }

    @Override
    protected void setup() {
        initCableData();
        initCableUi();
    }

    @Override
    protected LifecycleObserver provideLifecycleObserver() {
        return this;
    }

    private void initCableData() {
        satChannelInfoList = DTVProgramManager.getInstance().getSatTPInfo(Constants.SatIndex.CABLE);
        if (satChannelInfoList != null && satChannelInfoList.size() > 0) {
            channel = satChannelInfoList.get(mCurrntChannel);
            mCurrntChannel = PreferenceManager.getInstance().getInt(Constants.PrefsKey.SAVE_CHANNEL);
            DTVSearchManager.getInstance().tunerLockFreq(Constants.SatIndex.CABLE, channel.Freq,
                    channel.Symbol, channel.Qam, 1, 0);
        } else {
            initChannel();
        }
    }

    private void initCableUi() {
        mTvTransponder.setText(String.valueOf(mCurrntChannel));
        if (satChannelInfoList != null && mCurrntChannel < satChannelInfoList.size()
                && satChannelInfoList.size() > 0) {
            channel = satChannelInfoList.get(mCurrntChannel);
        } else {
            initChannel();
        }
        mTvFrequency.setText(MessageFormat.format(getString(R.string.frequency_text),
                (channel.Freq / 10) + "." + (channel.Freq % 10)));
        mTvSymbolRate.setText(MessageFormat.format(getString(R.string.symbol_rate_text),
                channel.Symbol));
        mTvQam.setText(String.valueOf(channel.Qam));
        DTVSearchManager.getInstance().tunerLockFreq(Constants.SatIndex.CABLE, channel.Freq, channel.Symbol, channel.Qam, 1, 0);
    }

    private void initChannel() {
        channel = new HProg_Struct_TP();
        channel.Freq = 0;
        channel.Symbol = 0;
        channel.Qam = 0;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            showScanDialog();
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            switch (mCurrSelectItem) {
                case ITEM_TRANSPONDER:
                case ITEM_FREQUENCY:
                case ITEM_SYMBOLRATE:
                    mCurrSelectItem++;
                    break;
                case ITEM_QAM:
                    mCurrSelectItem = ITEM_TRANSPONDER;
                    rlItemTransponder.requestFocus();
                    itemFocusChange();
                    return true;
            }
            itemFocusChange();
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            switch (mCurrSelectItem) {
                case ITEM_FREQUENCY:
                case ITEM_SYMBOLRATE:
                case ITEM_QAM:
                    mCurrSelectItem--;
                    break;
                case ITEM_TRANSPONDER:
                    mCurrSelectItem = ITEM_QAM;
                    rlItemQam.requestFocus();
                    itemFocusChange();
                    return true;
            }
            itemFocusChange();
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (mCurrSelectItem == ITEM_TRANSPONDER) {
                mCurrntChannel = getMinusStep(mCurrntChannel,
                        satChannelInfoList.size() - 1);
                initCableUi();
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (mCurrSelectItem == ITEM_TRANSPONDER) {
                mCurrntChannel = getPlusStep(mCurrntChannel,
                        satChannelInfoList.size() - 1);
                initCableUi();
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showScanDialog() {
        new ScanDialog()
                .installationType(ScanDialog.INSTALLATION_TYPE_S2_SEARCH)
                .setOnScanSearchListener(v -> {
                    Intent intent = new Intent(CManualSearchActivity.this,
                            ScanTVandRadioActivity.class);
                    intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX,
                            Constants.SatIndex.CABLE);
                    intent.putExtra(Constants.IntentKey.INTENT_FREQ, channel.Freq);
                    intent.putExtra(Constants.IntentKey.INTENT_SYMBOL, channel.Symbol);
                    intent.putExtra(Constants.IntentKey.INTENT_QAM, channel.Qam);
                    intent.putExtra(Constants.IntentKey.INTENT_SEARCH_TYPE,
                            Constants.IntentValue.SEARCH_TYPE_CMANUAL);
                    startActivity(intent);
                    finish();
                }).show(getSupportFragmentManager(), ScanDialog.TAG);
    }

    private void itemFocusChange() {
        itemChange(mCurrSelectItem, ITEM_TRANSPONDER, mIvTransponderLeft, mIvTransponderRight, mTvTransponder);
        itemChange(mCurrSelectItem, ITEM_FREQUENCY, mIvFrequencyLeft, mIvFrequencyRight, mTvFrequency);
        itemChange(mCurrSelectItem, ITEM_SYMBOLRATE, mIvSymbolRateLeft, mIvSymbolRateRight,
                mTvSymbolRate);
        itemChange(mCurrSelectItem, ITEM_QAM, mIvQamLeft, mIvQamRight, mTvQam);
    }
}
