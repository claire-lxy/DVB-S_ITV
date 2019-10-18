package com.konkawise.dtv.ui;

import android.content.Intent;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
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

public class T2ManualSearchActivity extends BaseItemFocusChangeActivity {
    private static final int ITEM_TRANSPONDER = 1;
    private static final int ITEM_FREQUENCY = 2;
    private static final int ITEM_BANDWIDTH = 3;

    private int mCurrntChannel = 0;

    @BindView(R.id.item_transponder)
    RelativeLayout rlItemTransponder;

    @BindView(R.id.item_frequency)
    RelativeLayout rlItemFrequency;

    @BindView(R.id.item_bandwidth)
    RelativeLayout rlItemBandwidth;

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

    @BindView(R.id.iv_bandwidth_left)
    ImageView mIvBandWidthLeft;

    @BindView(R.id.tv_bandwidth)
    TextView mTvBandWidth;

    @BindView(R.id.iv_bandwidth_right)
    ImageView mIvBandWidthRight;

    @BindView(R.id.tv_progress_strength)
    TextView mTvStrengthProgress;

    @BindView(R.id.pb_strength)
    ProgressBar mPbStrength;

    @BindView(R.id.tv_progress_quality)
    TextView mTvQualityProgress;

    @BindView(R.id.pb_quality)
    ProgressBar mPbQuality;

    private int mCurrSelectItem = ITEM_TRANSPONDER;

    private List<HProg_Struct_TP> satChannelInfoList;

    private HProg_Struct_TP channel;

    private CheckSignalHelper mCheckSignalHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_t2_manual_search;
    }

    @Override
    protected void setup() {
        initT2Data();
        initT2Ui();
        initCheckSignal();
    }

    private void initT2Data() {
        satChannelInfoList = DTVProgramManager.getInstance().getSatTPInfo(Constants.T2_SATELLITE_INDEX);
        if (satChannelInfoList != null) {
            channel = satChannelInfoList.get(mCurrntChannel);
            mCurrntChannel = PreferenceManager.getInstance().getInt(Constants.PrefsKey.SAVE_CHANNEL);
            DTVSearchManager.getInstance().tunerLockFreq(Constants.T2_SATELLITE_INDEX, channel.Freq, channel.Symbol, channel.Qam, 1, 0);
        }
    }

    private void initT2Ui() {
        mTvTransponder.setText(String.valueOf(mCurrntChannel));
        channel = satChannelInfoList.get(mCurrntChannel);
        mTvFrequency.setText(MessageFormat.format(getString(R.string.frequency_text), (channel.Freq / 10) + "." + (channel.Freq % 10)));
        mTvBandWidth.setText(MessageFormat.format(getString(R.string.bandwidth_text), channel.Symbol));
        DTVSearchManager.getInstance().tunerLockFreq(Constants.T2_SATELLITE_INDEX, channel.Freq, channel.Symbol, channel.Qam, 1, 0);
        Log.e("T2ManualSearchActivity", "satChannelInfoList.size:  " + satChannelInfoList.size() + "channel.Freq  " + channel.Freq + "channel.Symbol  " +
                channel.Symbol + "channel.Qam  " + channel.Symbol + "TsID  " + channel.TsID + "channel.NetID  " + channel.NetID + "channel.ChannelIndex  " + channel.TPIndex);
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

    @Override
    protected void onResume() {
        super.onResume();
        mCheckSignalHelper.startCheckSignal();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mCheckSignalHelper.stopCheckSignal();
        PreferenceManager.getInstance().putInt(Constants.PrefsKey.SAVE_CHANNEL, mCurrntChannel);
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
                    mCurrSelectItem++;
                    break;
                case ITEM_BANDWIDTH:
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
                case ITEM_BANDWIDTH:
                    mCurrSelectItem--;
                    break;
                case ITEM_TRANSPONDER:
                    mCurrSelectItem = ITEM_BANDWIDTH;
                    rlItemBandwidth.requestFocus();
                    itemFocusChange();
                    return true;
            }
            itemFocusChange();
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            switch (mCurrSelectItem) {
                case ITEM_TRANSPONDER:
                    --mCurrntChannel;
                    if (mCurrntChannel < 0) {
                        mCurrntChannel = satChannelInfoList.size() - 1;
                    }
                    initT2Ui();
                    break;

                case ITEM_FREQUENCY:

                    break;

                case ITEM_BANDWIDTH:

                    break;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            switch (mCurrSelectItem) {
                case ITEM_TRANSPONDER:
                    ++mCurrntChannel;
                    if (mCurrntChannel > satChannelInfoList.size() - 1) {
                        mCurrntChannel = 0;
                    }
                    initT2Ui();
                    break;

                case ITEM_FREQUENCY:

                    break;

                case ITEM_BANDWIDTH:

                    break;
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showScanDialog() {
        new ScanDialog()
                .installationType(ScanDialog.INSTALLATION_TYPE_S2_SEARCH)
                .setOnScanSearchListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(T2ManualSearchActivity.this, ScanTVandRadioActivity.class);
                        intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, Constants.T2_SATELLITE_INDEX);//mCurrentSatellite == 0
                        intent.putExtra(Constants.IntentKey.INTENT_FREQ, channel.Freq);
                        intent.putExtra(Constants.IntentKey.INTENT_SYMBOL, channel.Symbol);
                        intent.putExtra(Constants.IntentKey.INTENT_SEARCH_TYPE, Constants.SEARCH_TYPE_T2MANUAL);
                        startActivity(intent);
                        finish();
                    }
                }).show(getSupportFragmentManager(), ScanDialog.TAG);
    }

    private void itemFocusChange() {
        itemChange(mCurrSelectItem, ITEM_TRANSPONDER, mIvTransponderLeft, mIvTransponderRight, mTvTransponder);
        itemChange(mCurrSelectItem, ITEM_FREQUENCY, mIvFrequencyLeft, mIvFrequencyRight, mTvFrequency);
        itemChange(mCurrSelectItem, ITEM_BANDWIDTH, mIvBandWidthLeft, mIvBandWidthRight, mTvBandWidth);
    }
}
