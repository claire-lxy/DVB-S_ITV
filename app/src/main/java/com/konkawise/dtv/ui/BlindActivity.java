package com.konkawise.dtv.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.PreferenceManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.base.BaseItemFocusChangeActivity;
import com.konkawise.dtv.dialog.ScanDialog;
import com.konkawise.dtv.utils.Utils;

import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

public class BlindActivity extends BaseItemFocusChangeActivity {
    private static final String TAG = "BlindActivity";
    private static final int ITEM_SATELLITE = 1;
    private static final int ITEM_LNB = 2;
    private static final int ITEM_DISEQC = 3;
    private static final int ITEM_22K = 4;
    private static final int ITEM_LNB_POWER = 5;

    @BindView(R.id.item_satellite)
    ViewGroup mItemSatellite;

    @BindView(R.id.iv_satellite_left)
    ImageView mIvSatelliteLeft;

    @BindView(R.id.tv_satellite)
    TextView mTvSatellite;

    @BindView(R.id.iv_satellite_right)
    ImageView mIvSatelliteRight;

    @BindView(R.id.item_lnb)
    ViewGroup mItemLnb;

    @BindView(R.id.iv_lnb_left)
    ImageView mIvLnbLeft;

    @BindView(R.id.tv_lnb)
    TextView mTvLnb;

    @BindView(R.id.et_lnb)
    EditText mEtLnb;

    @BindView(R.id.iv_lnb_right)
    ImageView mIvLnbRight;

    @BindView(R.id.item_diseqc)
    ViewGroup mItemDiSEqC;

    @BindView(R.id.iv_diseqc_left)
    ImageView mIvDiSEqCLeft;

    @BindView(R.id.tv_diseqc)
    TextView mTvDiSEqC;

    @BindView(R.id.iv_diseqc_right)
    ImageView mIvDiSEqCRight;

    @BindView(R.id.item_22khz)
    ViewGroup mItem22khz;

    @BindView(R.id.iv_22khz_left)
    ImageView mIv22khzLeft;

    @BindView(R.id.tv_22khz)
    TextView mTv22khz;

    @BindView(R.id.iv_22khz_right)
    ImageView mIv22khzRight;

    @BindView(R.id.item_lnb_power)
    ViewGroup mItemLnbPower;

    @BindView(R.id.iv_lnb_power_left)
    ImageView mIvLnbPowerLeft;

    @BindView(R.id.tv_lnb_power)
    TextView mTvLnbPower;

    @BindView(R.id.iv_lnb_power_right)
    ImageView mIvLnbPowerRight;

    @BindArray(R.array.LNB)
    String[] mLnbArray;

    @BindArray(R.array.DISEQC)
    String[] mDiseqcArray;

    private int mCurrentSelectItem = ITEM_SATELLITE;
    private int mCurrentSatellite;
    private int mCurrentLnb;
    private int mCurrentDiseqc;
    private List<SatInfo_t> mSatList;
    // 22KHz为Auto之前，上一个卫星22KHz的开关状态
    private String mLastFocusable22KHz;

    @Override
    public int getLayoutId() {
        return R.layout.activity_blind_select;
    }

    @Override
    protected void setup() {
        mLastFocusable22KHz = getResources().getString(R.string.on);
        satelliteChange();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) saveSatInfo();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            switch (mCurrentSelectItem) {
                case ITEM_SATELLITE:
                case ITEM_LNB:
                    mCurrentSelectItem++;
                    break;
                case ITEM_DISEQC:
                    if (is22KHzUnFocusable()) {
                        mCurrentSelectItem += 2;
                    } else {
                        mCurrentSelectItem++;
                    }
                    break;
                case ITEM_22K:
                    mCurrentSelectItem = ITEM_LNB_POWER;
                    break;
            }
            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            switch (mCurrentSelectItem) {
                case ITEM_LNB:
                    mCurrentSelectItem = ITEM_SATELLITE;
                    break;
                case ITEM_DISEQC:
                case ITEM_22K:
                    mCurrentSelectItem--;
                    break;
                case ITEM_LNB_POWER:
                    if (is22KHzUnFocusable()) {
                        mCurrentSelectItem -= 2;
                    } else {
                        mCurrentSelectItem--;
                    }
                    break;
            }
            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            switch (mCurrentSelectItem) {
                case ITEM_SATELLITE:
                    saveSatInfo();
                    if (--mCurrentSatellite < 0) mCurrentSatellite = getSatList().size() - 1;
                    satelliteChange();
                    break;
                case ITEM_LNB:
                    if (--mCurrentLnb < 0) mCurrentLnb = mLnbArray.length - 1;
                    lnbChange();

                    if (mCurrentLnb == 0) mEtLnb.requestFocus();
                    else mTvLnb.requestFocus();
                    break;
                case ITEM_DISEQC:
                    if (--mCurrentDiseqc < 0) mCurrentDiseqc = mDiseqcArray.length - 1;
                    diseqcChange();
                    break;
                case ITEM_22K:
                    hz22KChange();
                    break;
                case ITEM_LNB_POWER:
                    lnbPowerChange();
                    break;
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            switch (mCurrentSelectItem) {
                case ITEM_SATELLITE:
                    saveSatInfo();
                    if (++mCurrentSatellite > getSatList().size() - 1) mCurrentSatellite = 0;
                    satelliteChange();
                    break;
                case ITEM_LNB:
                    if (++mCurrentLnb > mLnbArray.length - 1) mCurrentLnb = 0;
                    lnbChange();

                    if (mCurrentLnb == 0) mEtLnb.requestFocus();
                    else mTvLnb.requestFocus();
                    break;
                case ITEM_DISEQC:
                    if (++mCurrentDiseqc > mDiseqcArray.length - 1) mCurrentDiseqc = 0;
                    diseqcChange();
                    break;
                case ITEM_22K:
                    hz22KChange();
                    break;
                case ITEM_LNB_POWER:
                    lnbPowerChange();
                    break;
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_RED) {
            saveSatInfo();

            showScanDialog();
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showScanDialog() {
        new ScanDialog().setOnScanSearchListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BlindActivity.this, TpBlindActivity.class);
                intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, getSatList().get(mCurrentSatellite).SatIndex);
                startActivity(intent);
                finish();
            }
        }).show(getSupportFragmentManager(), ScanDialog.TAG);
    }

    /**
     * 保存设置的卫星信息
     */
    private void saveSatInfo() {
        if (isSatelliteEmpty()) return;

        SatInfo_t satInfo = SWPDBaseManager.getInstance().getSatList().get(mCurrentSatellite);

        String lnb = mEtLnb.getText().toString();
        if (TextUtils.isEmpty(lnb)) lnb = "0";
        satInfo.LnbType = Utils.getLnbType(mCurrentLnb);
        satInfo.lnb_low = Utils.getLnbLow(mCurrentLnb, mCurrentLnb == 0 ? Integer.parseInt(lnb) : 0);
        satInfo.lnb_high = Utils.getLnbHeight(mCurrentLnb);
        if (mCurrentLnb == 0) {
            PreferenceManager.getInstance().putString(String.valueOf(mCurrentSatellite), lnb);
        }

        satInfo.diseqc10_pos = Utils.getDiSEqC10Pos(mCurrentDiseqc);
        satInfo.diseqc10_tone = Utils.getDiSEqC10Tone(mCurrentDiseqc);
//        satInfo.diseqc12_pos = Utils.getDiSEqC12Pos(mCurrentDiseqc);
//        satInfo.diseqc12 = Utils.getDiSEqC12(mCurrentDiseqc);
        satInfo.skewonoff = Utils.getSkewOnOff(mCurrentDiseqc);

        if (TextUtils.equals(mTv22khz.getText().toString(), getString(R.string.off))) {
            satInfo.switch_22k = 0;
        } else if (TextUtils.equals(mTv22khz.getText().toString(), getString(R.string.on))) {
            satInfo.switch_22k = 1;
        } else {
            satInfo.switch_22k = 2;
        }
        satInfo.LnbPower = isLnbPowerOn() ? 1 : 0;

        SWPDBaseManager.getInstance().setSatInfo(satInfo.SatIndex, satInfo);
        mSatList = SWPDBaseManager.getInstance().getSatList(); // 更新卫星列表
    }

    /**
     * Lnb参数修改
     */
    private void lnbChange() {
        mEtLnb.setVisibility(mCurrentLnb == 0 ? View.VISIBLE : View.GONE);
        mEtLnb.setText(mLnbArray[0]);
        mTvLnb.setVisibility(mCurrentLnb == 0 ? View.GONE : View.VISIBLE);
        mTvLnb.setText(mLnbArray[mCurrentLnb]);
        RelativeLayout.LayoutParams lp = (RelativeLayout.LayoutParams) mIvLnbLeft.getLayoutParams();
        if (lp != null) {
            if (mCurrentLnb == 0) {
                lp.addRule(RelativeLayout.LEFT_OF, 0);
                lp.leftMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 255, getResources().getDisplayMetrics());
            } else {
                lp.addRule(RelativeLayout.LEFT_OF, R.id.tv_lnb);
                lp.leftMargin = 0;
            }
            mIvLnbLeft.setLayoutParams(lp);
        }

        notify22kChange();
    }

    /**
     * Diseqc参数修改
     */
    private void diseqcChange() {
        mTvDiSEqC.setText(mDiseqcArray[mCurrentDiseqc]);
    }

    /**
     * Lnb参数修改，22KHz同步参数修改
     */
    private void notify22kChange() {
        hz22KItemFocusChange();
        recordLastFocusable22KHz();
        mTv22khz.setFocusable(!is22KHzUnFocusable());
        if (is22KHzUnFocusable()) {
            mTv22khz.setText(getResources().getString(R.string.auto));
        } else {
            mTv22khz.setText(mLastFocusable22KHz);
        }
    }

    /**
     * 22KHz参数修改
     */
    private void hz22KChange() {
        mTv22khz.setText(getResources().getString(is22kHzOn() ? R.string.off : R.string.on));
    }

    private boolean is22kHzOn() {
        return TextUtils.equals(mTv22khz.getText().toString(), getResources().getString(R.string.on));
    }

    private boolean is22KHzUnFocusable() {
        return mCurrentLnb == mLnbArray.length - 1;
    }

    private void recordLastFocusable22KHz() {
        String current22KHz = mTv22khz.getText().toString();
        if (!TextUtils.equals(current22KHz, getResources().getString(R.string.auto))) {
            mLastFocusable22KHz = current22KHz;
        }
    }

    /**
     * LnbPower参数修改
     */
    private void lnbPowerChange() {
        mTvLnbPower.setText(getResources().getString(isLnbPowerOn() ? R.string.off : R.string.on));
    }

    private boolean isLnbPowerOn() {
        return TextUtils.equals(mTvLnbPower.getText().toString(), getResources().getString(R.string.on));
    }

    /**
     * Satellite参数改变
     */
    public void satelliteChange() {
        if (isSatelliteEmpty()) return;

        mTvSatellite.setText(getSatList().get(mCurrentSatellite).sat_name);

        mLnbArray[0] = getLnbO();
        mCurrentLnb = getCurrLnb();
        mLastFocusable22KHz = getString(getSatList().get(mCurrentSatellite).switch_22k == 1 ? R.string.on : R.string.off);
        mTv22khz.setText(mLastFocusable22KHz);
        lnbChange();

        mCurrentDiseqc = getCurrDiseqc();
        String diseqc = Utils.getDiSEqC(getSatList().get(mCurrentSatellite), mDiseqcArray);
        mTvDiSEqC.setText(TextUtils.isEmpty(diseqc) ? mDiseqcArray[0] : diseqc);

        mTvLnbPower.setText(getSatList().get(mCurrentSatellite).LnbPower == 1 ?
                getResources().getString(R.string.on) : getResources().getString(R.string.off));
    }

    private List<SatInfo_t> getSatList() {
        if (mSatList == null) {
            mSatList = SWPDBaseManager.getInstance().getSatList();
        }
        return mSatList;
    }

    private boolean isSatelliteEmpty() {
        return getSatList() == null || mCurrentSatellite >= getSatList().size();
    }

    private String getLnbO() {
        String lnb0 = PreferenceManager.getInstance().getString(String.valueOf(mCurrentSatellite));
        if (TextUtils.isEmpty(lnb0)) lnb0 = "0";
        return lnb0;
    }

    private int getCurrDiseqc() {
        if (isSatelliteEmpty()) return 0;

        String diseqc = Utils.getDiSEqC(getSatList().get(mCurrentSatellite), mDiseqcArray);
        for (int i = 0; i < mDiseqcArray.length; i++) {
            if (diseqc.equals(mDiseqcArray[i])) return i;
        }

        return 0;
    }

    private int getCurrLnb() {
        if (isSatelliteEmpty()) return 0;

        String lnb = Utils.getLnb(getSatList().get(mCurrentSatellite));
        for (int i = 0; i < mLnbArray.length; i++) {
            if (TextUtils.equals(lnb, mLnbArray[i])) {
                return i;
            }
        }

        return 0;
    }

    private void itemFocusChange() {
        itemChange(mCurrentSelectItem, ITEM_SATELLITE, mItemSatellite, mIvSatelliteLeft, mIvSatelliteRight, mTvSatellite);
        itemChange(mCurrentSelectItem, ITEM_LNB, mItemLnb, mIvLnbLeft, mIvLnbRight, mTvLnb);
        itemChange(mCurrentSelectItem, ITEM_DISEQC, mItemDiSEqC, mIvDiSEqCLeft, mIvDiSEqCRight, mTvDiSEqC);
        itemChange(mCurrentSelectItem, ITEM_LNB_POWER, mItemLnbPower, mIvLnbPowerLeft, mIvLnbPowerRight, mTvLnbPower);
        notify22kChange();
    }

    private void hz22KItemFocusChange() {
        if (is22KHzUnFocusable()) {
            mItem22khz.setBackgroundColor(getResources().getColor(R.color.dialog_bg));
            mIv22khzLeft.setVisibility(View.INVISIBLE);
            mIv22khzRight.setVisibility(View.INVISIBLE);
            mTv22khz.setBackgroundColor(0);
            mTv22khz.setText(getResources().getString(R.string.auto));
        } else {
            itemChange(mCurrentSelectItem, ITEM_22K, mItem22khz, mIv22khzLeft, mIv22khzRight, mTv22khz);
        }
    }
}
