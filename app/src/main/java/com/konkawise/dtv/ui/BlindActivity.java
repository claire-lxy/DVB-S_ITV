package com.konkawise.dtv.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.PreferenceManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.ScanDialog;
import com.konkawise.dtv.utils.Utils;

import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

/**
 * 盲扫界面
 */
public class BlindActivity extends BaseActivity {
    private static final String TAG = "BlindActivity";
    private static final int ITEM_SATELLITE = 1;
    private static final int ITEM_LNB = 2;
    private static final int ITEM_DISEQC = 3;
    private static final int ITEM_22K = 4;
    private static final int ITEM_LNB_POWER = 5;

    @BindView(R.id.imageview_blind_satellite_left)
    ImageView mImageview_blind_satellite_left;

    @BindView(R.id.tv_blind_satellite)
    TextView tv_blind_satellite;

    @BindView(R.id.imageview_blind_satellite_right)
    ImageView imageview_blind_satellite_right;

    @BindView(R.id.ll_blind_satellite)
    LinearLayout mLl_blind_satellite;

    @BindView(R.id.imageview_blind_lnb_left)
    ImageView imageview_blind_lnb_left;

    @BindView(R.id.tv_blind_lnb_mode)
    TextView tv_blind_lnb_mode;

    @BindView(R.id.et_edit_lnb_mode)
    EditText et_edit_lnb_mode;

    @BindView(R.id.rl_edit_lnb_mode)
    RelativeLayout rl_edit_lnb_mode;

    @BindView(R.id.imageview_blind_lnb_right)
    ImageView imageview_blind_lnb_right;

    @BindView(R.id.ll_blind_satellite_lnb)
    LinearLayout ll_blind_satellite_lnb;

    @BindView(R.id.imageview_blind_diseqc_left)
    ImageView imageview_blind_diseqc_left;

    @BindView(R.id.tv_blind_diseqc_mode)
    TextView tv_blind_diseqc_mode;

    @BindView(R.id.imageview_blind_diseqc_right)
    ImageView imageview_blind_diseqc_right;

    @BindView(R.id.ll_diseqc_blind_satellite)
    LinearLayout ll_diseqc_blind_satellite;

    @BindView(R.id.imageview_blind_khz_left)
    ImageView imageview_blind_khz_left;

    @BindView(R.id.tv_blind_khz_mode)
    TextView tv_blind_khz_mode;

    @BindView(R.id.imageview_blind_khz_right)
    ImageView imageview_blind_khz_right;

    @BindView(R.id.ll_blind_22khz)
    LinearLayout ll_blind_22khz;

    @BindView(R.id.imageview_blind_lnb_power_left)
    ImageView imageview_blind_lnb_power_left;

    @BindView(R.id.tv_blind_lnb_power_mode)
    TextView tv_blind_lnb_power_mode;

    @BindView(R.id.imageview_blind_lnb_power_right)
    ImageView imageview_blind_lnb_power_right;

    @BindView(R.id.ll_blind_lnb_power)
    LinearLayout ll_blind_lnb_power;

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

                    if (mCurrentLnb == 0) et_edit_lnb_mode.requestFocus();
                    else tv_blind_lnb_mode.requestFocus();
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

                    if (mCurrentLnb == 0) et_edit_lnb_mode.requestFocus();
                    else tv_blind_lnb_mode.requestFocus();
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
                intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mCurrentSatellite);
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

        SatInfo_t satInfo_t = SWPDBaseManager.getInstance().getSatList().get(mCurrentSatellite);

        String lnb = et_edit_lnb_mode.getText().toString();
        if (TextUtils.isEmpty(lnb)) lnb = "0";
        Utils.satLNB(satInfo_t, mCurrentLnb, mCurrentLnb == 0 ? Integer.parseInt(lnb) : 0);
        if (mCurrentLnb == 0) {
            PreferenceManager.getInstance().putString(String.valueOf(mCurrentSatellite), lnb);
        }

        // diseqc
        Utils.setDescNum(satInfo_t, mCurrentDiseqc, mDiseqcArray);
        // 22KHZ
        satInfo_t.switch_22k = is22kHzOn() ? 1 : 0;
        // LNB POWER
        satInfo_t.LnbPower = isLnbPowerOn() ? 1 : 0;

        SWPDBaseManager.getInstance().setSatInfo(mCurrentSatellite, satInfo_t);  //将卫星信息设置到对应的bean类中,保存更改的信息
        mSatList = SWPDBaseManager.getInstance().getSatList(); // 更新卫星列表
    }

    /**
     * Lnb参数修改
     */
    private void lnbChange() {
        rl_edit_lnb_mode.setVisibility(mCurrentLnb == 0 ? View.VISIBLE : View.GONE);
        et_edit_lnb_mode.setText(mLnbArray[0]);
        tv_blind_lnb_mode.setVisibility(mCurrentLnb == 0 ? View.GONE : View.VISIBLE);
        tv_blind_lnb_mode.setText(mLnbArray[mCurrentLnb]);

        notify22kChange();
    }

    /**
     * Diseqc参数修改
     */
    private void diseqcChange() {
        tv_blind_diseqc_mode.setText(mDiseqcArray[mCurrentDiseqc]);
    }

    /**
     * Lnb参数修改，22KHz同步参数修改
     */
    private void notify22kChange() {
        hz22KItemFocusChange();
        recordLastFocusable22KHz();
        tv_blind_khz_mode.setFocusable(!is22KHzUnFocusable());
        if (is22KHzUnFocusable()) {
            tv_blind_khz_mode.setText(getResources().getString(R.string.auto));
        } else {
            tv_blind_khz_mode.setText(mLastFocusable22KHz);
        }
    }

    /**
     * 22KHz参数修改
     */
    private void hz22KChange() {
        tv_blind_khz_mode.setText(getResources().getString(is22kHzOn() ? R.string.off : R.string.on));
    }

    private boolean is22kHzOn() {
        return TextUtils.equals(tv_blind_khz_mode.getText().toString(), getResources().getString(R.string.on));
    }

    private boolean is22KHzUnFocusable() {
        return mCurrentLnb == mLnbArray.length - 1;
    }

    private void recordLastFocusable22KHz() {
        String current22KHz = tv_blind_khz_mode.getText().toString();
        if (TextUtils.equals(current22KHz, getResources().getString(R.string.auto))) return;
        mLastFocusable22KHz = current22KHz;
    }

    /**
     * LnbPower参数修改
     */
    private void lnbPowerChange() {
        tv_blind_lnb_power_mode.setText(getResources().getString(isLnbPowerOn() ? R.string.off : R.string.on));
    }

    private boolean isLnbPowerOn() {
        return TextUtils.equals(tv_blind_lnb_power_mode.getText().toString(), getResources().getString(R.string.on));
    }

    /**
     * Satellite参数改变
     */
    public void satelliteChange() {
        if (isSatelliteEmpty()) return;

        tv_blind_satellite.setText(getSatList().get(mCurrentSatellite).sat_name);

        mLnbArray[0] = getLnbO();
        mCurrentLnb = getCurrLnb();
        lnbChange();

        mCurrentDiseqc = getCurrDiseqc();
        String diseqc = Utils.getDiseqc(getSatList().get(mCurrentSatellite), mDiseqcArray);
        tv_blind_diseqc_mode.setText(TextUtils.isEmpty(diseqc) ? mDiseqcArray[0] : diseqc);

        tv_blind_lnb_power_mode.setText(getSatList().get(mCurrentSatellite).LnbPower == 1 ?
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

        String diseqc = Utils.getDiseqc(getSatList().get(mCurrentSatellite), mDiseqcArray);
        for (int i = 0; i < mDiseqcArray.length; i++) {
            if (diseqc.equals(mDiseqcArray[i])) return i;
        }

        return 0;
    }

    private int getCurrLnb() {
        if (isSatelliteEmpty()) return 0;

        String lnb = Utils.getLNB(getSatList().get(mCurrentSatellite));
        for (int i = 0; i < mLnbArray.length; i++) {
            if (TextUtils.equals(lnb, mLnbArray[i])) {
                return i;
            }
        }

        return 0;
    }

    private void itemFocusChange() {
        satelliteItemFocusChange();
        lnbItemFocusChange();
        diseqcItemFocusChange();
        notify22kChange();
        lnbPowerItemFocusChange();
    }

    private void satelliteItemFocusChange() {
        mLl_blind_satellite.setBackgroundResource(mCurrentSelectItem == ITEM_SATELLITE ? R.drawable.btn_translate_bg_select_shape : 0);
        mImageview_blind_satellite_left.setVisibility(mCurrentSelectItem == ITEM_SATELLITE ? View.VISIBLE : View.INVISIBLE);
        imageview_blind_satellite_right.setVisibility(mCurrentSelectItem == ITEM_SATELLITE ? View.VISIBLE : View.INVISIBLE);
        tv_blind_satellite.setBackgroundResource(mCurrentSelectItem == ITEM_SATELLITE ? R.drawable.btn_red_bg_shape : 0);
    }

    private void lnbItemFocusChange() {
        ll_blind_satellite_lnb.setBackgroundResource(mCurrentSelectItem == ITEM_LNB ? R.drawable.btn_translate_bg_select_shape : 0);
        imageview_blind_lnb_left.setVisibility(mCurrentSelectItem == ITEM_LNB ? View.VISIBLE : View.INVISIBLE);
        imageview_blind_lnb_right.setVisibility(mCurrentSelectItem == ITEM_LNB ? View.VISIBLE : View.INVISIBLE);
        tv_blind_lnb_mode.setBackgroundResource(mCurrentSelectItem == ITEM_LNB ? R.drawable.btn_red_bg_shape : 0);
        rl_edit_lnb_mode.setBackgroundResource(mCurrentSelectItem == ITEM_LNB ? R.drawable.btn_red_bg_shape : 0);
    }

    private void diseqcItemFocusChange() {
        ll_diseqc_blind_satellite.setBackgroundResource(mCurrentSelectItem == ITEM_DISEQC ? R.drawable.btn_translate_bg_select_shape : 0);
        imageview_blind_diseqc_left.setVisibility(mCurrentSelectItem == ITEM_DISEQC ? View.VISIBLE : View.INVISIBLE);
        imageview_blind_diseqc_right.setVisibility(mCurrentSelectItem == ITEM_DISEQC ? View.VISIBLE : View.INVISIBLE);
        tv_blind_diseqc_mode.setBackgroundResource(mCurrentSelectItem == ITEM_DISEQC ? R.drawable.btn_red_bg_shape : 0);
    }

    private void hz22KItemFocusChange() {
        if (is22KHzUnFocusable()) {
            ll_blind_22khz.setBackgroundColor(getResources().getColor(R.color.dialog_bg));
            imageview_blind_khz_left.setVisibility(View.INVISIBLE);
            imageview_blind_khz_right.setVisibility(View.INVISIBLE);
            tv_blind_khz_mode.setBackgroundColor(0);
            tv_blind_khz_mode.setText(getResources().getString(R.string.auto));
        } else {
            ll_blind_22khz.setBackgroundResource(mCurrentSelectItem == ITEM_22K ? R.drawable.btn_translate_bg_select_shape : 0);
            imageview_blind_khz_left.setVisibility(mCurrentSelectItem == ITEM_22K ? View.VISIBLE : View.INVISIBLE);
            imageview_blind_khz_right.setVisibility(mCurrentSelectItem == ITEM_22K ? View.VISIBLE : View.INVISIBLE);
            tv_blind_khz_mode.setBackgroundResource(mCurrentSelectItem == ITEM_22K ? R.drawable.btn_red_bg_shape : 0);
        }
    }

    private void lnbPowerItemFocusChange() {
        ll_blind_lnb_power.setBackgroundResource(mCurrentSelectItem == ITEM_LNB_POWER ? R.drawable.btn_translate_bg_select_shape : 0);
        imageview_blind_lnb_power_left.setVisibility(mCurrentSelectItem == ITEM_LNB_POWER ? View.VISIBLE : View.INVISIBLE);
        imageview_blind_lnb_power_right.setVisibility(mCurrentSelectItem == ITEM_LNB_POWER ? View.VISIBLE : View.INVISIBLE);
        tv_blind_lnb_power_mode.setBackgroundResource(mCurrentSelectItem == ITEM_LNB_POWER ? R.drawable.btn_red_bg_shape : 0);
    }
}
