package com.konkawise.dtv.ui;

import android.view.KeyEvent;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konkawise.dtv.DTVSettingManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseItemFocusChangeActivity;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.event.ProgramUpdateEvent;

import org.greenrobot.eventbus.EventBus;

import java.util.Arrays;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;
import vendor.konka.hardware.dtvmanager.V1_0.HSetting_Enum_Property;

public class T2SettingsActivity extends BaseItemFocusChangeActivity {

    private static final String TAG = "KKDVB_" + T2SettingsActivity.class.getSimpleName();
    private static final int ITEM_ANTENNA_POWER = 1;
    private static final int ITEM_AREA_SETTING = 2;
    private static final int ITEM_LCN = 3;

    @BindView(R.id.item_antenna_power)
    RelativeLayout rlItemAntennaPower;

    @BindView(R.id.item_lcn)
    RelativeLayout rlItemLcn;

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
    private int originalLcnPosition;

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

    @Override
    protected void onStop() {
        super.onStop();
        if (originalLcnPosition != lcnPosition) {
            EventBus.getDefault().post(new ProgramUpdateEvent(true));
        }
    }

    private void initData() {
        antennaPowerPosition = getSelectPosition(new int[]{0, 1}, DTVSettingManager.getInstance().getDTVProperty(HSetting_Enum_Property.AntennaPower));
        lcnPosition = getSelectPosition(new int[]{0, 1}, DTVSettingManager.getInstance().getDTVProperty(HSetting_Enum_Property.ShowNoType));
        originalLcnPosition = lcnPosition;
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
                                DTVSettingManager.getInstance().setDTVProperty(HSetting_Enum_Property.AntennaPower, antennaPowerPosition);
                                break;
                            case ITEM_AREA_SETTING:
                                mTvAreaSetting.setText(checkContent);
                                areaSettingPosition = Arrays.asList(mAreaSettingArray).indexOf(checkContent);
                                break;
                            case ITEM_LCN:
                                mTvLcn.setText(checkContent);
                                lcnPosition = Arrays.asList(mGeneralSwitchArray).indexOf(checkContent);
                                DTVSettingManager.getInstance().setDTVProperty(HSetting_Enum_Property.ShowNoType, lcnPosition);
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

                case ITEM_ANTENNA_POWER:
                    mCurrentSelectItem = ITEM_LCN;
                    rlItemLcn.requestFocus();
                    itemFocusChange();
                    return true;
            }
            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            switch (mCurrentSelectItem) {
                case ITEM_ANTENNA_POWER:
                case ITEM_AREA_SETTING:
                    mCurrentSelectItem++;
                    break;

                case ITEM_LCN:
                    mCurrentSelectItem = ITEM_ANTENNA_POWER;
                    rlItemAntennaPower.requestFocus();
                    itemFocusChange();
                    return true;
            }
            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            switch (mCurrentSelectItem) {

                case ITEM_ANTENNA_POWER:
                    antennaPowerPosition = getMinusStep(antennaPowerPosition, mGeneralSwitchArray.length - 1);
                    mTvAntennaPower.setText(mGeneralSwitchArray[antennaPowerPosition]);
                    DTVSettingManager.getInstance().setDTVProperty(HSetting_Enum_Property.AntennaPower, antennaPowerPosition);
                    break;

                case ITEM_AREA_SETTING:
                    areaSettingPosition = getMinusStep(areaSettingPosition, mAreaSettingArray.length - 1);
                    mTvAreaSetting.setText(mAreaSettingArray[areaSettingPosition]);
                    break;

                case ITEM_LCN:
                    lcnPosition = getMinusStep(lcnPosition, mGeneralSwitchArray.length - 1);
                    mTvLcn.setText(mGeneralSwitchArray[lcnPosition]);
                    DTVSettingManager.getInstance().setDTVProperty(HSetting_Enum_Property.ShowNoType, lcnPosition);
                    break;

            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            switch (mCurrentSelectItem) {

                case ITEM_ANTENNA_POWER:
                    antennaPowerPosition = getPlusStep(antennaPowerPosition, mGeneralSwitchArray.length - 1);
                    mTvAntennaPower.setText(mGeneralSwitchArray[antennaPowerPosition]);
                    DTVSettingManager.getInstance().setDTVProperty(HSetting_Enum_Property.AntennaPower, antennaPowerPosition);
                    break;

                case ITEM_AREA_SETTING:
                    areaSettingPosition = getPlusStep(areaSettingPosition, mAreaSettingArray.length - 1);
                    mTvAreaSetting.setText(mAreaSettingArray[areaSettingPosition]);
                    break;

                case ITEM_LCN:
                    lcnPosition = getPlusStep(lcnPosition, mGeneralSwitchArray.length - 1);
                    mTvLcn.setText(mGeneralSwitchArray[lcnPosition]);
                    DTVSettingManager.getInstance().setDTVProperty(HSetting_Enum_Property.ShowNoType, lcnPosition);
                    break;

            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private void itemFocusChange() {
        itemChange(mCurrentSelectItem, ITEM_ANTENNA_POWER, mIvAntennaPowerLeft, mIvAntennaPowerRight, mTvAntennaPower);
        itemChange(mCurrentSelectItem, ITEM_AREA_SETTING, mIvAreaSettingLeft, mIvAreaSettingRight, mTvAreaSetting);
        itemChange(mCurrentSelectItem, ITEM_LCN, mIvLcnLeft, mIvLcnRight, mTvLcn);
    }
}
