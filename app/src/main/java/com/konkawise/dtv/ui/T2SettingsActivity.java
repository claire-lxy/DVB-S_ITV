package com.konkawise.dtv.ui;

import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.sw.dvblib.SWFta;

import java.util.Arrays;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;

public class T2SettingsActivity extends BaseActivity {

    private static final String TAG = "KKDVB_" + T2SettingsActivity.class.getSimpleName();
	private static final int ITEM_ANTENNA_POWER = 1;
	private static final int ITEM_AREA_SETTING = 2;
	private static final int ITEM_LCN = 3;

    @BindView(R.id.iv_antenna_power_left)
    ImageView mIvAntennaPowerLeft;

    @BindView(R.id.tv_antenna_power)
    TextView mTvAntennaPower;

    @BindView(R.id.iv_antenna_power_right)
    ImageView mIvAntennaPowerRight;

    @BindView(R.id.iv_area_setting_left)
    ImageView mIvAreaSettingLeft;

    @BindView(R.id.tv_area_setting)
    TextView mTvAreaSetting;

    @BindView(R.id.iv_area_setting_right)
    ImageView mIvAreaSettingRight;

    @BindView(R.id.iv_lcn_left)
    ImageView mIvLcnLeft;

    @BindView(R.id.tv_lcn)
    TextView mTvLcn;

    @BindView(R.id.iv_lcn_right)
    ImageView mIvLcnRight;

	@BindArray(R.array.area_setting)
    String[] mAreaSettingArray;

	@BindArray(R.array.general_switch)
	String[] mGeneralSwitchArray;

    @OnClick(R.id.item_antenna_power)
    void antennaPower() {
        showGeneralSettingDialog(getString(R.string.antenna_power), Arrays.asList(mGeneralSwitchArray), antennaPowerPosition);
    }

    @OnClick(R.id.item_area_setting)
    void areaSetting() {
        showGeneralSettingDialog(getString(R.string.area_setting), Arrays.asList(mAreaSettingArray), areaSettingPosition);
    }

    @OnClick(R.id.item_lcn)
    void lcn() {
        showGeneralSettingDialog(getString(R.string.lcn), Arrays.asList(mGeneralSwitchArray), lcnPosition);
    }

    private int mCurrentSelectItem = ITEM_ANTENNA_POWER;
    private int antennaPowerPosition;
    private int areaSettingPosition;
    private int lcnPosition;

    @Override
    public int getLayoutId() {
        return R.layout.activity_t2_settings;
    }

    @Override
    protected void setup() {
        initData();

		mTvAntennaPower.setText(mGeneralSwitchArray[antennaPowerPosition]);
		mTvAreaSetting.setText(mAreaSettingArray[areaSettingPosition]);
		mTvLcn.setText(mGeneralSwitchArray[lcnPosition]);
    }

    private void initData() {
        antennaPowerPosition = getSelectPosition(new int[]{0, 1}, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_AntennaPower.ordinal()));
    }

    private int getSelectPosition(int[] datas, int value) {
        if (datas == null || datas.length <= 0) return 0;

        for (int i = 0; i < datas.length; i++) {
            if (datas[i] == value) return i;
        }
        return 0;
    }

    private void showGeneralSettingDialog(String title, List<String> content, int selectPosition) {
        new CommCheckItemDialog()
                .title(title)
                .content(content)
                .position(selectPosition)
                .setOnDismissListener(new CommCheckItemDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(CommCheckItemDialog dialog, int position, String checkContent) {
                        switch (mCurrentSelectItem) {
                            case ITEM_ANTENNA_POWER:
                                mTvAntennaPower.setText(checkContent);
                                antennaPowerPosition = Arrays.asList(mGeneralSwitchArray).indexOf(checkContent);
                                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_AntennaPower.ordinal(), antennaPowerPosition);
                                break;
                            case ITEM_AREA_SETTING:
                                mTvAreaSetting.setText(checkContent);
                                areaSettingPosition = Arrays.asList(mAreaSettingArray).indexOf(checkContent);
                                break;
                            case ITEM_LCN:
                                mTvLcn.setText(checkContent);
                                lcnPosition = Arrays.asList(mGeneralSwitchArray).indexOf(checkContent);
                                break;
                            default:
                                break;
                        }
                    }
                }).show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
			switch (mCurrentSelectItem) {
				case ITEM_AREA_SETTING:
				case ITEM_LCN:
					mCurrentSelectItem--;
					break;
			}
            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
			switch (mCurrentSelectItem) {
				case ITEM_ANTENNA_POWER:
				case ITEM_AREA_SETTING:
					mCurrentSelectItem++;
					break;
			}
            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            switch (mCurrentSelectItem) {

                case ITEM_ANTENNA_POWER:
                    if (--antennaPowerPosition < 0)
                        antennaPowerPosition = mGeneralSwitchArray.length - 1;
                    mTvAntennaPower.setText(mGeneralSwitchArray[antennaPowerPosition]);
					SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_AntennaPower.ordinal(), antennaPowerPosition);
                    break;

                case ITEM_AREA_SETTING:
                    if (--areaSettingPosition < 0)
                        areaSettingPosition = mAreaSettingArray.length - 1;
                    mTvAreaSetting.setText(mAreaSettingArray[areaSettingPosition]);
                    break;

                case ITEM_LCN:
                    if (--lcnPosition < 0) lcnPosition = mGeneralSwitchArray.length - 1;
                    mTvLcn.setText(mGeneralSwitchArray[lcnPosition]);
                    break;

            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            switch (mCurrentSelectItem) {

                case ITEM_ANTENNA_POWER:
                    if (++antennaPowerPosition > mGeneralSwitchArray.length - 1)
                        antennaPowerPosition = 0;
                    mTvAntennaPower.setText(mGeneralSwitchArray[antennaPowerPosition]);
					SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_AntennaPower.ordinal(), antennaPowerPosition);
                    break;

                case ITEM_AREA_SETTING:
                    if (++areaSettingPosition > mAreaSettingArray.length - 1)
                        areaSettingPosition = 0;
                    mTvAreaSetting.setText(mAreaSettingArray[areaSettingPosition]);
                    break;

                case ITEM_LCN:
                    if (++lcnPosition > mGeneralSwitchArray.length - 1)
                    	lcnPosition = 0;
                    mTvLcn.setText(mGeneralSwitchArray[lcnPosition]);
                    break;

            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void itemFocusChange() {
		itemChange(ITEM_ANTENNA_POWER, mIvAntennaPowerLeft, mIvAntennaPowerRight, mTvAntennaPower);
		itemChange(ITEM_AREA_SETTING, mIvAreaSettingLeft, mIvAreaSettingRight, mTvAreaSetting);
		itemChange(ITEM_LCN, mIvLcnLeft, mIvLcnRight, mTvLcn);
    }

    private void itemChange(int selectItem, ImageView ivLeft, ImageView ivRight, TextView textView) {
        ivLeft.setVisibility(mCurrentSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
        textView.setBackgroundResource(mCurrentSelectItem == selectItem ? R.drawable.btn_red_bg_shape : 0);
        ivRight.setVisibility(mCurrentSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
    }
}
