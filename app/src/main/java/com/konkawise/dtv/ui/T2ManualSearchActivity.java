package com.konkawise.dtv.ui;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.ScanDialog;
import com.konkawise.dtv.weaktool.CheckSignalHelper;

import butterknife.BindView;

public class T2ManualSearchActivity extends BaseActivity {
    private static final int ITEM_TRANSPONDER = 1;
    private static final int ITEM_FREQUENCY = 2;
    private static final int ITEM_BANDWIDTH = 3;

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

    @BindView(R.id.tv_signal_percent)
    TextView mTvSignalPercent;

    @BindView(R.id.pb_signal)
    ProgressBar mPbSignal;

    @BindView(R.id.tv_quality_percent)
    TextView mTvQualityPercent;

    @BindView(R.id.pb_quality)
    ProgressBar mPbQuality;

    private int mCurrSelectItem = ITEM_TRANSPONDER;

    private CheckSignalHelper mCheckSignalHelper;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_t2_manual_search;
    }

    @Override
    protected void setup() {
        mCheckSignalHelper = new CheckSignalHelper(this);
        mCheckSignalHelper.setOnCheckSignalListener(new CheckSignalHelper.OnCheckSignalListener() {
            @Override
            public void signal(int strength, int quality) {
                String strengthPercent = strength + "%";
                mTvSignalPercent.setText(strengthPercent);
                mPbSignal.setProgress(strength);

                String qualityPercent = quality + "%";
                mTvQualityPercent.setText(qualityPercent);
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            switch (mCurrSelectItem) {
                case ITEM_TRANSPONDER:
                case ITEM_FREQUENCY:
                    mCurrSelectItem++;
                    break;
            }
            itemFocusChange();
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            switch (mCurrSelectItem) {
                case ITEM_FREQUENCY:
                case ITEM_BANDWIDTH:
                    mCurrSelectItem--;
                    break;
            }
            itemFocusChange();
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            switch (mCurrSelectItem) {
                case ITEM_TRANSPONDER:

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

                    break;

                case ITEM_FREQUENCY:

                    break;

                case ITEM_BANDWIDTH:

                    break;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            showScanDialog();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showScanDialog() {
        new ScanDialog()
                .installationType(ScanDialog.INSTALLATION_TYPE_MANUAL_SEARCH)
                .setOnScanSearchListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                    }
                }).show(getSupportFragmentManager(), ScanDialog.TAG);
    }

    private void itemFocusChange() {
        itemChange(ITEM_TRANSPONDER, mIvTransponderLeft, mIvTransponderRight, mTvTransponder);
        itemChange(ITEM_FREQUENCY, mIvFrequencyLeft, mIvFrequencyRight, mTvFrequency);
        itemChange(ITEM_BANDWIDTH, mIvBandWidthLeft, mIvBandWidthRight, mTvBandWidth);
    }

    private void itemChange(int selectItem, ImageView ivLeft, ImageView ivRight, TextView textView) {
        ivLeft.setVisibility(mCurrSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
        textView.setBackgroundResource(mCurrSelectItem == selectItem ? R.drawable.btn_red_bg_shape : 0);
        ivRight.setVisibility(mCurrSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
    }
}
