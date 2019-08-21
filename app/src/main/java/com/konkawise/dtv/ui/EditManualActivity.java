package com.konkawise.dtv.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.PreferenceManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.AutoDiSEqCDialog;
import com.konkawise.dtv.dialog.RenameDialog;
import com.konkawise.dtv.dialog.ScanDialog;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.CheckSignalHelper;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.ChannelNew_t;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

/**
 * 点击编辑按钮进去的界面
 */
public class EditManualActivity extends BaseActivity {
    private static final String TAG = "EditManualActivity";
    private static final int ITEM_SATELLITE = 1;
    private static final int ITEM_TP = 2;
    private static final int ITEM_LNB = 3;
    private static final int ITEM_DISEQC = 4;
    private static final int ITEM_22K = 5;
    private static final int ITEM_LNB_POWER = 6;

    @BindView(R.id.item_satellite)
    ViewGroup mItemSatellite;

    @BindView(R.id.iv_satellite_left)
    ImageView mIvSatelliteLeft;

    @BindView(R.id.tv_satellite)
    TextView mTvSatellite;

    @BindView(R.id.iv_satellite_right)
    ImageView mIvSatelliteRight;

    @BindView(R.id.item_tp)
    ViewGroup mItemTp;

    @BindView(R.id.iv_tp_left)
    ImageView mIvTpLeft;

    @BindView(R.id.tv_tp)
    TextView mTvTp;

    @BindView(R.id.iv_tp_right)
    ImageView mIvTpRight;

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

    @BindView(R.id.item_longitude)
    ViewGroup mItemLongitude;

    @BindView(R.id.iv_longitude_left)
    ImageView mIvLongitudeLeft;

    @BindView(R.id.tv_longitude)
    TextView mTvLongitude;

    @BindView(R.id.iv_longitude_right)
    ImageView mIvLongitudeRight;

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

    @BindView(R.id.tv_progress_strength)
    TextView mTvStrengthProgress;

    @BindView(R.id.pb_strength)
    ProgressBar mPbStrength;

    @BindView(R.id.tv_progress_quality)
    TextView mTvQualityProgress;

    @BindView(R.id.pb_quality)
    ProgressBar mPbQuality;

    @BindView(R.id.tv_bottom_bar_green)
    TextView mTvBottomBarGreen;

    @BindView(R.id.tv_bottom_bar_yellow)
    TextView mTvBottomBarYellow;

    @BindArray(R.array.LNB)
    String[] mLnbArray;

    @BindArray(R.array.DISEQC)
    String[] mDiSEqCArray;

    private int mCurrentSelectItem = ITEM_SATELLITE;
    private int mCurrentSatellite;
    private int mCurrentTp;
    private int mCurrentLnb;
    private int mCurrentDiseqc;
    private List<SatInfo_t> mSatList;
    private List<ChannelNew_t> mTpList;
    // 22KHz为Auto之前，上一个卫星22KHz的开关状态
    private String mLastFocusable22KHz;

    private CheckSignalHelper mCheckSignalHelper;

    @Override
    public int getLayoutId() {
        return R.layout.activity_edit_manual;
    }

    @Override
    protected void setup() {
        mTvBottomBarGreen.setText(getString(R.string.rename));
        mTvBottomBarYellow.setText(getString(R.string.motor_01));

        mLastFocusable22KHz = getResources().getString(R.string.on);
        mCurrentSatellite = SWPDBaseManager.getInstance().findPositionBySatIndex(getIntent().getIntExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, -1));

        initCheckSignal();
        satelliteChange();
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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            switch (mCurrentSelectItem) {
                case ITEM_SATELLITE:
                case ITEM_TP:
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
                case ITEM_TP:
                    mCurrentSelectItem = ITEM_SATELLITE;
                    break;
                case ITEM_LNB:
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
                case ITEM_TP:
                    if (--mCurrentTp < 0) mCurrentTp = getTpList().size() - 1;
                    tpChange();
                    break;
                case ITEM_LNB:
                    if (--mCurrentLnb < 0) mCurrentLnb = mLnbArray.length - 1;
                    lnbChange();

                    if (mCurrentLnb == 0) mEtLnb.requestFocus();
                    else mTvLnb.requestFocus();
                    break;
                case ITEM_DISEQC:
                    if (--mCurrentDiseqc < 0) mCurrentDiseqc = mDiSEqCArray.length - 1;
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
                case ITEM_TP:
                    if (++mCurrentTp > getTpList().size() - 1) mCurrentTp = 0;
                    tpChange();
                    break;
                case ITEM_LNB:
                    if (++mCurrentLnb > mLnbArray.length - 1) mCurrentLnb = 0;
                    lnbChange();

                    if (mCurrentLnb == 0) mEtLnb.requestFocus();
                    else mTvLnb.requestFocus();
                    break;
                case ITEM_DISEQC:
                    if (++mCurrentDiseqc > mDiSEqCArray.length - 1) mCurrentDiseqc = 0;
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

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            saveSatInfo();
            Intent intent = new Intent();
            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_POSITION, mCurrentSatellite);
            setResult(RESULT_OK, intent);
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_RED) {
            saveSatInfo();

            showScanDialog();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_GREEN) {
            showRenameDialog();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_YELLOW) {
            Intent intent=new Intent(EditManualActivity.this,MotorActivity.class);
            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_NAME,mTvSatellite.getText());
            intent.putExtra(Constants.IntentKey.INTENT_TP_NAME,mTvTp.getText());
            intent.putExtra(Constants.IntentKey.INTENT_CURRNT_TP,mCurrentTp);
            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, getSatList().get(mCurrentSatellite).SatIndex);
            startActivity(intent);
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_BLUE) {
            List<SatInfo_t> satList = getSatList();
            List<ChannelNew_t> tpList = getTpList();
            if (satList == null || satList.isEmpty()) return false;
            if (tpList == null || tpList.isEmpty()) return false;

            new AutoDiSEqCDialog()
                    .satIndex(satList.get(mCurrentSatellite).SatIndex)
                    .tpData(tpList.get(mCurrentTp))
                    .setOnAutoDiSEqCResultListener(new AutoDiSEqCDialog.OnAutoDiSEqCResultListener() {
                        @Override
                        public void onAutoDiSEqCResult(int portIndex) {
                            if (portIndex >= 0) {
                                SatInfo_t satInfo = getSatList().get(mCurrentSatellite);
                                satInfo.diseqc10_pos = portIndex + 1;
                                SWPDBaseManager.getInstance().setSatInfo(satInfo.SatIndex, satInfo);

                                mCurrentDiseqc = portIndex + 3; // 和mDiseqcArray位置约定
                                diseqcChange();
                            }
                        }
                    })
                    .show(getSupportFragmentManager(), AutoDiSEqCDialog.TAG);
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showScanDialog() {
        new ScanDialog()
                .setOnScanSearchListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        List<ChannelNew_t> tpList = getTpList();
                        if (tpList != null && !tpList.isEmpty()) {
                            Intent intent = new Intent(EditManualActivity.this, ScanTVandRadioActivity.class);
                            ChannelNew_t channel = getTpList().get(mCurrentTp);
                            intent.putExtra(Constants.IntentKey.INTENT_FREQ, channel.Freq);
                            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, getSatList().get(mCurrentSatellite).SatIndex);
                            intent.putExtra(Constants.IntentKey.INTENT_SYMBOL, channel.Symbol);
                            intent.putExtra(Constants.IntentKey.INTENT_QAM, channel.Qam);
                            intent.putExtra(Constants.IntentKey.INTENT_EDIT_MANUAL_ACTIVITY, 3);
                            startActivity(intent);
                            finish();
                        }
                    }
                }).show(getSupportFragmentManager(), ScanDialog.TAG);
    }

    private void showRenameDialog() {
        new RenameDialog()
                .setNameType(getResources().getString(R.string.edit_satellite_name))
                .setProgNo(mCurrentSatellite + 1)
                .setOldName(getSatList().get(mCurrentSatellite).sat_name)
                .setEditLisener(new RenameDialog.EditTextLisener() {
                    @Override
                    public void setEdit(String name) {
                        if (name != null && name.length() > 0) {
                            mTvSatellite.setText(name);
                        }
                    }
                }).show(getSupportFragmentManager(), RenameDialog.TAG);
    }

    /**
     * 保存设置的卫星信息
     */
    private void saveSatInfo() {
        List<SatInfo_t> satList = getSatList();
        if (satList == null || satList.isEmpty()) return;

        SatInfo_t satInfo_t = satList.get(mCurrentSatellite);

        String lnb = mEtLnb.getText().toString();
        if (TextUtils.isEmpty(lnb)) lnb = "0";
        Utils.satLNB(satInfo_t, mCurrentLnb, mCurrentLnb == 0 ? Integer.parseInt(lnb) : 0);
        if (mCurrentLnb == 0) {
            PreferenceManager.getInstance().putString(String.valueOf(mCurrentSatellite), lnb);
        }

        // Sat
        satInfo_t.sat_name = mTvSatellite.getText().toString();
        // diseqc
        Utils.setDescNum(satInfo_t, mCurrentDiseqc, mDiSEqCArray);
        // 22KHZ
        satInfo_t.switch_22k = is22kHzOn() ? 1 : 0;
        // LNB POWER
        satInfo_t.LnbPower = isLnbPowerOn() ? 1 : 0;

        SWPDBaseManager.getInstance().setSatInfo(satInfo_t.SatIndex, satInfo_t);  //将卫星信息设置到对应的bean类中,保存更改的信息
        mSatList = SWPDBaseManager.getInstance().getSatList(); // 更新卫星列表
    }

    /**
     * TP参数修改
     */
    private void tpChange() {
        getTpList();
        if (mTpList == null || mTpList.size() == 0) {
            mTvTp.setText("");
            return;
        }
        ChannelNew_t channel = getTpList().get(mCurrentTp);
        String tpName = channel.Freq + Utils.getVorH(this, channel.Qam) + channel.Symbol;
        mTvTp.setText(tpName);
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
        mTvDiSEqC.setText(mDiSEqCArray[mCurrentDiseqc]);
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
        if (TextUtils.equals(current22KHz, getResources().getString(R.string.auto))) return;
        mLastFocusable22KHz = current22KHz;
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
        List<SatInfo_t> satList = getSatList();
        if (satList == null || satList.isEmpty()) return;

        mTvSatellite.setText(satList.get(mCurrentSatellite).sat_name);

        mCurrentTp = 0;
        tpChange();

        mLnbArray[0] = getLnbO();
        mCurrentLnb = getCurrLnb();
        lnbChange();

        mCurrentDiseqc = getCurrDiseqc();
        String diseqc = Utils.getDiseqc(satList.get(mCurrentSatellite), mDiSEqCArray);
        mTvDiSEqC.setText(TextUtils.isEmpty(diseqc) ? mDiSEqCArray[0] : diseqc);

        mTvLongitude.setText(Utils.getLongitude(satList.get(mCurrentSatellite)));

        mTvLnbPower.setText(satList.get(mCurrentSatellite).LnbPower == 1 ?
                getResources().getString(R.string.on) : getResources().getString(R.string.off));
    }

    private List<SatInfo_t> getSatList() {
        if (mSatList == null) {
            mSatList = SWPDBaseManager.getInstance().getSatList();
        }
        return mSatList;
    }

    private List<ChannelNew_t> getTpList() {
        List<SatInfo_t> satList = getSatList();
        if (satList == null || satList.isEmpty()) return new ArrayList<>();

        mTpList = SWPDBaseManager.getInstance().getSatChannelInfoList(getSatList().get(mCurrentSatellite).SatIndex);
        return mTpList;
    }

    private String getLnbO() {
        String lnb0 = PreferenceManager.getInstance().getString(String.valueOf(mCurrentSatellite));
        if (TextUtils.isEmpty(lnb0)) lnb0 = "0";
        return lnb0;
    }

    private int getCurrDiseqc() {
        List<SatInfo_t> satList = getSatList();
        if (satList == null || satList.isEmpty()) return 0;

        String diseqc = Utils.getDiseqc(satList.get(mCurrentSatellite), mDiSEqCArray);
        for (int i = 0; i < mDiSEqCArray.length; i++) {
            if (diseqc.equals(mDiSEqCArray[i])) return i;
        }
        return 0;
    }

    private int getCurrLnb() {
        List<SatInfo_t> satList = getSatList();
        if (satList == null || satList.isEmpty()) return 0;

        String lnb = Utils.getLNB(satList.get(mCurrentSatellite));
        for (int i = 0; i < mLnbArray.length; i++) {
            if (TextUtils.equals(lnb, mLnbArray[i])) {
                return i;
            }
        }
        return 0;
    }

    private void itemFocusChange() {
        satelliteItemFocusChange();
        tpItemFocusChange();
        lnbItemFocusChange();
        diseqcItemFocusChange();
        notify22kChange();
        lnbPowerItemFocusChange();
    }

    private void satelliteItemFocusChange() {
        mItemSatellite.setBackgroundResource(mCurrentSelectItem == ITEM_SATELLITE ? R.drawable.btn_translate_bg_select_shape : 0);
        mIvSatelliteLeft.setVisibility(mCurrentSelectItem == ITEM_SATELLITE ? View.VISIBLE : View.INVISIBLE);
        mIvSatelliteRight.setVisibility(mCurrentSelectItem == ITEM_SATELLITE ? View.VISIBLE : View.INVISIBLE);
        mTvSatellite.setBackgroundResource(mCurrentSelectItem == ITEM_SATELLITE ? R.drawable.btn_red_bg_shape : 0);
    }

    private void tpItemFocusChange() {
        mItemTp.setBackgroundResource(mCurrentSelectItem == ITEM_TP ? R.drawable.btn_translate_bg_select_shape : 0);
        mIvTpLeft.setVisibility(mCurrentSelectItem == ITEM_TP ? View.VISIBLE : View.INVISIBLE);
        mIvTpRight.setVisibility(mCurrentSelectItem == ITEM_TP ? View.VISIBLE : View.INVISIBLE);
        mTvTp.setBackgroundResource(mCurrentSelectItem == ITEM_TP ? R.drawable.btn_red_bg_shape : 0);
    }

    private void lnbItemFocusChange() {
        mItemLnb.setBackgroundResource(mCurrentSelectItem == ITEM_LNB ? R.drawable.btn_translate_bg_select_shape : 0);
        mIvLnbLeft.setVisibility(mCurrentSelectItem == ITEM_LNB ? View.VISIBLE : View.INVISIBLE);
        mIvLnbRight.setVisibility(mCurrentSelectItem == ITEM_LNB ? View.VISIBLE : View.INVISIBLE);
        mTvLnb.setBackgroundResource(mCurrentSelectItem == ITEM_LNB ? R.drawable.btn_red_bg_shape : 0);
    }

    private void diseqcItemFocusChange() {
        mItemDiSEqC.setBackgroundResource(mCurrentSelectItem == ITEM_DISEQC ? R.drawable.btn_translate_bg_select_shape : 0);
        mIvDiSEqCLeft.setVisibility(mCurrentSelectItem == ITEM_DISEQC ? View.VISIBLE : View.INVISIBLE);
        mIvDiSEqCRight.setVisibility(mCurrentSelectItem == ITEM_DISEQC ? View.VISIBLE : View.INVISIBLE);
        mTvDiSEqC.setBackgroundResource(mCurrentSelectItem == ITEM_DISEQC ? R.drawable.btn_red_bg_shape : 0);
    }

    private void hz22KItemFocusChange() {
        if (is22KHzUnFocusable()) {
            mItem22khz.setBackgroundColor(getResources().getColor(R.color.dialog_bg));
            mIv22khzLeft.setVisibility(View.INVISIBLE);
            mIv22khzRight.setVisibility(View.INVISIBLE);
            mTv22khz.setBackgroundColor(0);
            mTv22khz.setText(getResources().getString(R.string.auto));
        } else {
            mItem22khz.setBackgroundResource(mCurrentSelectItem == ITEM_22K ? R.drawable.btn_translate_bg_select_shape : 0);
            mIv22khzLeft.setVisibility(mCurrentSelectItem == ITEM_22K ? View.VISIBLE : View.INVISIBLE);
            mIv22khzRight.setVisibility(mCurrentSelectItem == ITEM_22K ? View.VISIBLE : View.INVISIBLE);
            mTv22khz.setBackgroundResource(mCurrentSelectItem == ITEM_22K ? R.drawable.btn_red_bg_shape : 0);
        }
    }

    private void lnbPowerItemFocusChange() {
        mItemLnbPower.setBackgroundResource(mCurrentSelectItem == ITEM_LNB_POWER ? R.drawable.btn_translate_bg_select_shape : 0);
        mIvLnbPowerLeft.setVisibility(mCurrentSelectItem == ITEM_LNB_POWER ? View.VISIBLE : View.INVISIBLE);
        mIvLnbPowerRight.setVisibility(mCurrentSelectItem == ITEM_LNB_POWER ? View.VISIBLE : View.INVISIBLE);
        mTvLnbPower.setBackgroundResource(mCurrentSelectItem == ITEM_LNB_POWER ? R.drawable.btn_red_bg_shape : 0);
    }
}
