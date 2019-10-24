package com.konkawise.dtv.ui;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.DTVSearchManager;
import com.konkawise.dtv.PreferenceManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseItemFocusChangeActivity;
import com.konkawise.dtv.bean.LatLngModel;
import com.konkawise.dtv.dialog.AutoDiSEqCDialog;
import com.konkawise.dtv.dialog.RenameDialog;
import com.konkawise.dtv.dialog.ScanDialog;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.CheckSignalHelper;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_TP;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_SatInfo;

public class EditManualActivity extends BaseItemFocusChangeActivity {
    private static final String TAG = "EditManualActivity";
    private static final int ITEM_SATELLITE = 1;
    private static final int ITEM_TP = 2;
    private static final int ITEM_LNB = 3;
    private static final int ITEM_22K = 4;
    private static final int ITEM_LNB_POWER = 5;
    private static final int ITEM_DISEQC_MODE = 6;
    private static final int ITEM_DISEQC_TYPE = 7; // ToneBurst or DiSEqC or Unicable
    private static final int ITEM_POSITION = 8;
    private static final int ITEM_CHANNEL = 9;
    private static final int ITEM_FREQUENCY = 10;

    // 与DiSEqC_mode位置约定
    private static final int DISEQC_MODE_OFF = 0;
    private static final int DISEQC_MODE_TONE_BURST = 1;
    private static final int DISEQC_MODE_DISEQC10 = 2;
    private static final int DISEQC_MODE_DISEQC11 = 3;
    private static final int DISEQC_MODE_UNICABLE = 4;

    // 与unicable位置约定
    private static final int UNICABLE_1SAT4SCR = 0;
    private static final int UNICABLE_1SAT8SCR = 1;
    private static final int UNICABLE_2SAT4SCR = 2;
    private static final int UNICABLE_2SAT8SCR = 3;
    private static final int UNICABLE_DCSS = 4;

    // channel最大索引
    private static final int MAX_CHANNEL_4SCR = 3;
    private static final int MAX_CHANNEL_8SCR = 7;
    private static final int MAX_CHANNEL_DCSS = 31;

    // unicable为dcss模式下，frequency为0的channel范围17~32
    private static final int DCSS_FREQUENCY_ZERO_MIN_RANGE = 17;
    private static final int DCSS_FREQUENCY_ZERO_MAX_RANGE = 32;

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

    @BindView(R.id.iv_lnb_right)
    ImageView mIvLnbRight;

    @BindView(R.id.tv_longitude)
    TextView mTvLongitude;

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

    @BindView(R.id.item_diseqc_mode)
    ViewGroup mItemDiSEqCMode;

    @BindView(R.id.iv_diseqc_mode_left)
    ImageView mIvDiSEqcModeLeft;

    @BindView(R.id.tv_diseqc_mode)
    TextView mTvDiSEqCMode;

    @BindView(R.id.iv_diseqc_mode_right)
    ImageView mIvDiSEqCModeRight;

    @BindView(R.id.item_toneburst)
    ViewGroup mItemToneBurst;

    @BindView(R.id.iv_toneburst_left)
    ImageView mIvToneBurstLeft;

    @BindView(R.id.tv_toneburst)
    TextView mTvToneBurst;

    @BindView(R.id.iv_toneburst_right)
    ImageView mIvToneBurstRight;

    @BindView(R.id.item_diseqc)
    ViewGroup mItemDiSEqC;

    @BindView(R.id.iv_diseqc_left)
    ImageView mIvDiSEqCLeft;

    @BindView(R.id.tv_diseqc)
    TextView mTvDiSEqC;

    @BindView(R.id.iv_diseqc_right)
    ImageView mIvDiSEqCRight;

    @BindView(R.id.item_unicable)
    ViewGroup mItemUnicable;

    @BindView(R.id.iv_unicable_left)
    ImageView mIvUnicableLeft;

    @BindView(R.id.tv_unicable)
    TextView mTvUnicable;

    @BindView(R.id.iv_unicable_right)
    ImageView mIvUnicableRight;

    @BindView(R.id.item_position)
    ViewGroup mItemPosition;

    @BindView(R.id.iv_position_left)
    ImageView mIvPositionLeft;

    @BindView(R.id.tv_position)
    TextView mTvPosition;

    @BindView(R.id.iv_position_right)
    ImageView mIvPositionRight;

    @BindView(R.id.item_channel)
    ViewGroup mItemChannel;

    @BindView(R.id.iv_channel_left)
    ImageView mIvChannelLeft;

    @BindView(R.id.tv_channel)
    TextView mTvChannel;

    @BindView(R.id.iv_channel_right)
    ImageView mIvChannelRight;

    @BindView(R.id.item_frequency)
    ViewGroup mItemFrequency;

    @BindView(R.id.tv_frequency)
    TextView mTvFrequency;

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

    @BindArray(R.array.lnb)
    String[] mLnbArray;

    @BindArray(R.array.DiSEqc_mode)
    String[] mDiSEqCModeArray;

    @BindArray(R.array.tone_burst)
    String[] mToneBurstArray;

    @BindArray(R.array.DiSEqc10)
    String[] mDiSEqC10Array;

    @BindArray(R.array.DiSEqC11)
    String[] mDiSEqC11Array;

    @BindArray(R.array.unicable)
    String[] mUnicableArray;

    @BindArray(R.array.position)
    String[] mPositionArray;

    @BindArray(R.array.frequency_4SCR)
    int[] mFrequency4SCRArray;

    @BindArray(R.array.frequency_8SCR)
    int[] mFrequency8SCRArray;

    @BindArray(R.array.frequency_dCSS)
    int[] mFrequencyDCSSArray;

    private int mCurrentSelectItem = ITEM_SATELLITE;
    private int mCurrentSatellite;
    private int mCurrentTp;
    private int mCurrentLnb;
    private int mCurrentDiSEqCMode;
    private int mCurrentToneBurst;
    private int mCurrentDiSEqC10;
    private int mCurrentDiSEqC11;
    private int mCurrentUnicable;
    private int mCurrentPosition;
    private int mCurrentChannel;

    private HProg_Struct_SatInfo mSatInfo;
    private List<HProg_Struct_SatInfo> mSatList;
    private List<HProg_Struct_TP> mTpList;

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
        mCurrentSatellite = DTVProgramManager.getInstance().findPositionBySatIndex(getIntent().getIntExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, -1));

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
        if (isFinishing()) saveSatInfo();
    }

    private void initCheckSignal() {
        mCheckSignalHelper = new CheckSignalHelper(this);
        mCheckSignalHelper.setOnCheckSignalListener((strength, quality) -> {
            if (isTpEmpty()) {
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
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            switch (mCurrentSelectItem) {
                case ITEM_SATELLITE:
                case ITEM_TP:
                case ITEM_22K:
                case ITEM_LNB_POWER:
                case ITEM_POSITION:
                case ITEM_CHANNEL:
                    mCurrentSelectItem++;
                    break;

                case ITEM_LNB:
                    if (is22KHzUnFocusable()) {
                        mCurrentSelectItem += 2; // select lnb power
                    } else {
                        mCurrentSelectItem++; // select 22khz
                    }
                    break;

                case ITEM_DISEQC_MODE:
                    if (mCurrentDiSEqCMode == DISEQC_MODE_OFF) {
                        mCurrentSelectItem = ITEM_SATELLITE;
                    } else {
                        mCurrentSelectItem = ITEM_DISEQC_TYPE;
                    }
                    break;

                case ITEM_DISEQC_TYPE:
                    if (mCurrentDiSEqCMode == DISEQC_MODE_TONE_BURST
                            || mCurrentDiSEqCMode == DISEQC_MODE_DISEQC10 || mCurrentDiSEqCMode == DISEQC_MODE_DISEQC11) {
                        mCurrentSelectItem = ITEM_SATELLITE;
                    } else if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
                        if (isMultiSat()) {
                            mCurrentSelectItem = ITEM_POSITION;
                        } else {
                            mCurrentSelectItem = ITEM_CHANNEL;
                        }
                    }
                    break;

                case ITEM_FREQUENCY:
                    mCurrentSelectItem = ITEM_SATELLITE;
                    break;
            }

            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            switch (mCurrentSelectItem) {
                case ITEM_TP:
                case ITEM_LNB:
                case ITEM_22K:
                case ITEM_DISEQC_MODE:
                case ITEM_DISEQC_TYPE:
                case ITEM_POSITION:
                case ITEM_FREQUENCY:
                    mCurrentSelectItem--;
                    break;

                case ITEM_LNB_POWER:
                    if (is22KHzUnFocusable()) {
                        mCurrentSelectItem -= 2; // select lnb
                    } else {
                        mCurrentSelectItem--; // select 22khz
                    }
                    break;

                case ITEM_SATELLITE:
                    if (mCurrentDiSEqCMode == DISEQC_MODE_OFF) {
                        mCurrentSelectItem = ITEM_DISEQC_MODE;
                    } else if (mCurrentDiSEqCMode == DISEQC_MODE_TONE_BURST ||
                            mCurrentDiSEqCMode == DISEQC_MODE_DISEQC10 || mCurrentDiSEqCMode == DISEQC_MODE_DISEQC11) {
                        mCurrentSelectItem = ITEM_DISEQC_TYPE;
                    } else if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
                        mCurrentSelectItem = ITEM_FREQUENCY;
                    }
                    break;

                case ITEM_CHANNEL:
                    if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
                        if (isMultiSat()) {
                            mCurrentSelectItem = ITEM_POSITION;
                        } else {
                            mCurrentSelectItem = ITEM_DISEQC_TYPE;
                        }
                    }
                    break;

            }
            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            switch (mCurrentSelectItem) {
                case ITEM_SATELLITE:
                    saveSatInfo();
                    mCurrentSatellite = getMinusStep(mCurrentSatellite, getSatList().size() - 1);
                    satelliteChange();
                    break;

                case ITEM_TP:
                    mCurrentTp = getMinusStep(mCurrentTp, getTpList().size() - 1);
                    tpChange();
                    break;

                case ITEM_LNB:
                    mCurrentLnb = getMinusStep(mCurrentLnb, mLnbArray.length - 1);
                    lnbChange();
                    break;

                case ITEM_22K:
                    hz22KChange();
                    break;

                case ITEM_LNB_POWER:
                    lnbPowerChange();
                    break;

                case ITEM_DISEQC_MODE:
                    mCurrentDiSEqCMode = getMinusStep(mCurrentDiSEqCMode, mDiSEqCModeArray.length - 1);
                    diseqcModeChange();
                    break;

                case ITEM_DISEQC_TYPE:
                    if (mCurrentDiSEqCMode == DISEQC_MODE_TONE_BURST) {
                        mCurrentToneBurst = getMinusStep(mCurrentToneBurst, mToneBurstArray.length - 1);
                        toneBurstChange();
                    } else if (mCurrentDiSEqCMode == DISEQC_MODE_DISEQC10) {
                        mCurrentDiSEqC10 = getMinusStep(mCurrentDiSEqC10, mDiSEqC10Array.length - 1);
                        diseqcChange();
                    } else if (mCurrentDiSEqCMode == DISEQC_MODE_DISEQC11) {
                        mCurrentDiSEqC11 = getMinusStep(mCurrentDiSEqC11, mDiSEqC11Array.length - 1);
                        diseqcChange();
                    } else if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
                        mCurrentUnicable = getMinusStep(mCurrentUnicable, mUnicableArray.length - 1);
                        unicableChange();
                    }
                    break;

                case ITEM_POSITION:
                    mCurrentPosition = getMinusStep(mCurrentPosition, mPositionArray.length - 1);
                    positionChange();
                    break;

                case ITEM_CHANNEL:
                    if (is4SCRUnicable()) {
                        mCurrentChannel = getMinusStep(mCurrentChannel, MAX_CHANNEL_4SCR);
                    } else if (is8SCRUnicable()) {
                        mCurrentChannel = getMinusStep(mCurrentChannel, MAX_CHANNEL_8SCR);
                    } else {
                        mCurrentChannel = getMinusStep(mCurrentChannel, MAX_CHANNEL_DCSS);
                    }
                    channelChange();
                    break;
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            switch (mCurrentSelectItem) {
                case ITEM_SATELLITE:
                    saveSatInfo();
                    mCurrentSatellite = getPlusStep(mCurrentSatellite, getSatList().size() - 1);
                    satelliteChange();
                    break;

                case ITEM_TP:
                    mCurrentTp = getPlusStep(mCurrentTp, getTpList().size() - 1);
                    tpChange();
                    break;

                case ITEM_LNB:
                    mCurrentLnb = getPlusStep(mCurrentLnb, mLnbArray.length - 1);
                    lnbChange();
                    break;

                case ITEM_22K:
                    hz22KChange();
                    break;

                case ITEM_LNB_POWER:
                    lnbPowerChange();
                    break;

                case ITEM_DISEQC_MODE:
                    mCurrentDiSEqCMode = getPlusStep(mCurrentDiSEqCMode, mDiSEqCModeArray.length - 1);
                    diseqcModeChange();
                    break;

                case ITEM_DISEQC_TYPE:
                    if (mCurrentDiSEqCMode == DISEQC_MODE_TONE_BURST) {
                        mCurrentToneBurst = getPlusStep(mCurrentToneBurst, mToneBurstArray.length - 1);
                        toneBurstChange();
                    } else if (mCurrentDiSEqCMode == DISEQC_MODE_DISEQC10) {
                        mCurrentDiSEqC10 = getPlusStep(mCurrentDiSEqC10, mDiSEqC10Array.length - 1);
                        diseqcChange();
                    } else if (mCurrentDiSEqCMode == DISEQC_MODE_DISEQC11) {
                        mCurrentDiSEqC11 = getPlusStep(mCurrentDiSEqC11, mDiSEqC11Array.length - 1);
                        diseqcChange();
                    } else if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
                        mCurrentUnicable = getPlusStep(mCurrentUnicable, mUnicableArray.length - 1);
                        unicableChange();
                    }
                    break;

                case ITEM_POSITION:
                    mCurrentPosition = getPlusStep(mCurrentPosition, mPositionArray.length - 1);
                    positionChange();
                    break;

                case ITEM_CHANNEL:
                    if (is4SCRUnicable()) {
                        mCurrentChannel = getPlusStep(mCurrentChannel, MAX_CHANNEL_4SCR);
                    } else if (is8SCRUnicable()) {
                        mCurrentChannel = getPlusStep(mCurrentChannel, MAX_CHANNEL_8SCR);
                    } else {
                        mCurrentChannel = getPlusStep(mCurrentChannel, MAX_CHANNEL_DCSS);
                    }
                    channelChange();
                    break;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_0) {
            inputLnb("0");
            inputFrequency("0");
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_1) {
            inputLnb("1");
            inputFrequency("1");
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_2) {
            inputLnb("2");
            inputFrequency("2");
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_3) {
            inputLnb("3");
            inputFrequency("3");
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_4) {
            inputLnb("4");
            inputFrequency("4");
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_5) {
            inputLnb("5");
            inputFrequency("5");
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_6) {
            inputLnb("6");
            inputFrequency("6");
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_7) {
            inputLnb("7");
            inputFrequency("7");
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_8) {
            inputLnb("8");
            inputFrequency("8");
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_9) {
            inputLnb("9");
            inputFrequency("9");
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_POSITION, mCurrentSatellite);
            setResult(RESULT_OK, intent);
            finish();
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_YELLOW) {
            Intent intent = new Intent(EditManualActivity.this, MotorActivity.class);
            intent.putExtra(Constants.IntentKey.INTENT_TP_NAME, mTvTp.getText());
            intent.putExtra(Constants.IntentKey.INTENT_CURRENT_TP, mCurrentTp);
            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, getSatList().get(mCurrentSatellite).SatIndex);
            startActivityForResult(intent, Constants.RequestCode.REQUEST_CODE_MOTOR);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (!mDispatchKeyUpReady) return super.onKeyUp(keyCode, event);

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_RED) {
            saveSatInfo();

            showScanDialog();
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_GREEN) {
            showRenameDialog();
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_BLUE) {
            List<HProg_Struct_SatInfo> satList = getSatList();
            List<HProg_Struct_TP> tpList = getTpList();
            if (satList == null || satList.isEmpty()) return false;
            if (tpList == null || tpList.isEmpty()) return false;

            showAutoDiSEqCDialog(satList, tpList);
            return true;
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Constants.RequestCode.REQUEST_CODE_MOTOR && data != null) {
            int longitude = data.getIntExtra(Constants.IntentKey.INTENT_LONGITUDE, 0);
            int currentTp = data.getIntExtra(Constants.IntentKey.INTENT_CURRENT_TP, -1);
            if (longitude != 0) {
                LatLngModel latLngModel = new LatLngModel(LatLngModel.MODE_LONGITUDE, LatLngModel.LONGITUDE_THRESHOLD, longitude);
                mTvLongitude.setText(latLngModel.getLatLngText());
            }
            if (currentTp != -1) {
                mCurrentTp = currentTp;
                tpChange();
            }
        }
    }

    private void showScanDialog() {
        new ScanDialog()
                .setOnScanSearchListener(v -> {
                    Intent intent = new Intent(EditManualActivity.this, ScanTVandRadioActivity.class);
                    intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, getSatList().get(mCurrentSatellite).SatIndex);
                    intent.putExtra(Constants.IntentKey.INTENT_SEARCH_TYPE, Constants.IntentValue.SEARCH_TYPE_EDITMANUAL);
                    startActivity(intent);
                    finish();
                }).show(getSupportFragmentManager(), ScanDialog.TAG);
    }

    private void showRenameDialog() {
        new RenameDialog()
                .setNameType(getResources().getString(R.string.edit_satellite_name))
                .setProgNo(mCurrentSatellite + 1)
                .setName(getSatList().get(mCurrentSatellite).sat_name)
                .setMaxLength(18)
                .setOnRenameEditListener(newName -> {
                    if (newName != null && newName.length() > 0) {
                        mTvSatellite.setText(newName);
                        saveSatName(newName);
                    }
                }).show(getSupportFragmentManager(), RenameDialog.TAG);
    }

    private void showAutoDiSEqCDialog(List<HProg_Struct_SatInfo> satList, List<HProg_Struct_TP> tpList) {
        new AutoDiSEqCDialog()
                .satIndex(satList.get(mCurrentSatellite).SatIndex)
                .tpData(tpList.get(mCurrentTp))
                .setOnAutoDiSEqCResultListener(portIndex -> {
                    if (portIndex >= 0) {
                        mCurrentDiSEqCMode = DISEQC_MODE_DISEQC10;
                        mCurrentDiSEqC10 = portIndex; // mDiSEqC10Array位置约定
                        diseqcModeChange();
                    }
                })
                .show(getSupportFragmentManager(), AutoDiSEqCDialog.TAG);
    }

    /**
     * 保存设置的卫星信息
     */
    private void saveSatInfo() {
        saveLnbParam();
        save22kHzParam();
        saveLnbPowerParam();
        saveDiSEqCParam();
        saveUnicableParam();
        DTVProgramManager.getInstance().setSatInfo(mSatInfo.SatIndex, mSatInfo);

        mSatList = DTVProgramManager.getInstance().getSatList(); // 更新卫星列表
    }

    private void saveSatName(String newName) {
        if (mSatInfo != null) {
            mSatInfo.sat_name = newName;
            DTVProgramManager.getInstance().setSatInfo(mSatInfo.SatIndex, mSatInfo);
        }
    }

    private void saveLnb() {
        if (mSatInfo != null) {
            saveLnbParam();
            DTVProgramManager.getInstance().setSatInfo(mSatInfo.SatIndex, mSatInfo);
            lockTp();
        }
    }

    private void saveLnbParam() {
        if (mSatInfo != null) {
            String lnb = mTvLnb.getText().toString();
            if (TextUtils.isEmpty(lnb)) lnb = "0";
            mSatInfo.LnbType = Utils.getLnbType(mCurrentLnb);
            mSatInfo.lnb_low = Utils.getLnbLow(mCurrentLnb, mCurrentLnb == Constants.SatInfoValue.LNB_USER ? Integer.parseInt(lnb) : 0);
            mSatInfo.lnb_high = Utils.getLnbHeight(mCurrentLnb);
            if (mCurrentLnb == Constants.SatInfoValue.LNB_USER) {
                PreferenceManager.getInstance().putString(String.valueOf(mCurrentSatellite), lnb);
            }
        }
    }

    private void save22kHz() {
        if (mSatInfo != null) {
            save22kHzParam();
            DTVProgramManager.getInstance().setSatInfo(mSatInfo.SatIndex, mSatInfo);
            lockTp();
        }
    }

    private void save22kHzParam() {
        if (mSatInfo != null) {
            if (TextUtils.equals(mTv22khz.getText().toString(), getString(R.string.off))) {
                mSatInfo.switch_22k = Constants.SatInfoValue.HZ22K_OFF;
            } else if (TextUtils.equals(mTv22khz.getText().toString(), getString(R.string.on))) {
                mSatInfo.switch_22k = Constants.SatInfoValue.HZ22K_ON;
            } else {
                mSatInfo.switch_22k = Constants.SatInfoValue.HZ22K_AUTO;
            }
        }
    }

    private void saveLnbPower() {
        if (mSatInfo != null) {
            saveLnbPowerParam();
            DTVProgramManager.getInstance().setSatInfo(mSatInfo.SatIndex, mSatInfo);
            lockTp();
        }
    }

    private void saveLnbPowerParam() {
        if (mSatInfo != null) {
            mSatInfo.LnbPower = isLnbPowerOn() ? Constants.SatInfoValue.LNB_POWER_ON : Constants.SatInfoValue.LNB_POWER_OFF;
        }
    }

    private void saveDiSEqC() {
        if (mSatInfo != null && mCurrentDiSEqCMode != DISEQC_MODE_UNICABLE) {
            saveDiSEqCParam();
            DTVProgramManager.getInstance().setSatInfo(mSatInfo.SatIndex, mSatInfo);
            lockTp();
        }
    }

    private void saveDiSEqCParam() {
        if (mSatInfo != null && mCurrentDiSEqCMode != DISEQC_MODE_UNICABLE) {
            mSatInfo.diseqc10_pos = getSaveDiSEqCPos();
            mSatInfo.diseqc10_tone = getSaveDiSEqCTone();
        }
    }

    private void saveUnicable() {
        if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
            saveUnicableParam();
            DTVProgramManager.getInstance().setSatInfo(mSatInfo.SatIndex, mSatInfo);
            lockTp();
        }
    }

    private void saveUnicableParam() {
        if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
            if (is4SCRUnicable()) {
                mSatInfo.unicConfig.UnicEnable = Constants.SatInfoValue.UNICABLE_SCR_ENABLE;
                mSatInfo.unicConfig.SatPosition = !isMultiSat() ? Constants.SatInfoValue.SINGLE_SAT_POSITION :
                        (mCurrentPosition == 0 ? Constants.SatInfoValue.MULTI_SAT_POSITION_A : Constants.SatInfoValue.MULTI_SAT_POSITION_B);
                mSatInfo.unicConfig.SCRType = Constants.SatInfoValue.SCR_4;
                mSatInfo.unicConfig.SCRNO = mCurrentChannel;
                mSatInfo.unicConfig.SCR4UBand.clear();
                mSatInfo.unicConfig.SCR4UBand.addAll(getSaveFrequencyList(mFrequency4SCRArray));
            } else if (is8SCRUnicable()) {
                mSatInfo.unicConfig.UnicEnable = Constants.SatInfoValue.UNICABLE_SCR_ENABLE;
                mSatInfo.unicConfig.SatPosition = !isMultiSat() ? Constants.SatInfoValue.SINGLE_SAT_POSITION :
                        (mCurrentPosition == 0 ? Constants.SatInfoValue.MULTI_SAT_POSITION_A : Constants.SatInfoValue.MULTI_SAT_POSITION_B);
                mSatInfo.unicConfig.SCRType = Constants.SatInfoValue.SCR_8;
                mSatInfo.unicConfig.SCRNO = mCurrentChannel;
                mSatInfo.unicConfig.SCR8UBand.clear();
                mSatInfo.unicConfig.SCR8UBand.addAll(getSaveFrequencyList(mFrequency8SCRArray));
            } else if (mCurrentUnicable == UNICABLE_DCSS) {
                mSatInfo.unicConfig.UnicEnable = Constants.SatInfoValue.UNICABLE_DCSS_ENABLE;
                mSatInfo.unicConfig.SatPosition = Constants.SatInfoValue.SINGLE_SAT_POSITION;
                mSatInfo.unicConfig.SCRType = 0;
                mSatInfo.unicConfig.SCRNO = 0;
                mSatInfo.unicConfig.dCSSNO = mCurrentChannel;
                mSatInfo.unicConfig.dCSSUBand.clear();
                mSatInfo.unicConfig.dCSSUBand.addAll(getSaveFrequencyList(mFrequencyDCSSArray));
            }
        } else {
            mSatInfo.unicConfig.UnicEnable = 0;
            mSatInfo.unicConfig.SCRType = 0;
            mSatInfo.unicConfig.SCRNO = 0;
            mSatInfo.unicConfig.dCSSNO = 0;
            mSatInfo.unicConfig.SatPosition = 0;
            mSatInfo.unicConfig.SCR4UBand.clear();
            mSatInfo.unicConfig.SCR8UBand.clear();
            mSatInfo.unicConfig.dCSSUBand.clear();
        }
    }

    private int getSaveDiSEqCPos() {
        if (mCurrentDiSEqCMode == DISEQC_MODE_OFF || mCurrentDiSEqCMode == DISEQC_MODE_TONE_BURST) {
            // diseqc10_tone=0, OFF or ToneBurst
            return Constants.SatInfoValue.OFF_OR_TONEBURST;
        } else if (mCurrentDiSEqCMode == DISEQC_MODE_DISEQC10) {
            // diseqc10_pos=1~4, DiSEqC DISEQC_A~D
            return mCurrentDiSEqC10 + 1; // mDiSEqC10Array位置约定
        } else if (mCurrentDiSEqCMode == DISEQC_MODE_DISEQC11) {
            // diseqc10_pos=5~20, LNB 1~16
            return mCurrentDiSEqC11 + 5; // mDiSEqC11Array位置约定
        }
        return 0;
    }

    private int getSaveDiSEqCTone() {
        if (mCurrentDiSEqCMode == DISEQC_MODE_OFF || mCurrentDiSEqCMode == DISEQC_MODE_DISEQC10 || mCurrentDiSEqCMode == DISEQC_MODE_DISEQC11) {
            // diseqc10_tone=0, OFF
            return 0;
        } else if (mCurrentDiSEqCMode == DISEQC_MODE_TONE_BURST) {
            // diseqc10_tone=1, ToneBurst DISEQC_A
            // diseqc10_tone=2, ToneBurst DISEQC_B
            return mCurrentToneBurst + 1; // mToneBurstArray位置约定
        }
        return 0;
    }

    private List<Integer> getSaveFrequencyList(int[] frequencyArray) {
        List<Integer> values = new ArrayList<>(frequencyArray.length);
        for (int value : frequencyArray) {
            values.add(value);
        }
        return values;
    }

    /**
     * Satellite参数改变
     */
    public void satelliteChange() {
        List<HProg_Struct_SatInfo> satList = getSatList();
        if (satList == null || satList.isEmpty()) return;
        mSatInfo = satList.get(mCurrentSatellite);
        if (mSatInfo == null) return;

        mTvSatellite.setText(mSatInfo.sat_name);

        mCurrentTp = 0;
        tpChange();

        mLnbArray[0] = getLnbO();
        mCurrentLnb = getCurrLnb(mSatInfo);
        mLastFocusable22KHz = getString(mSatInfo.switch_22k == Constants.SatInfoValue.HZ22K_ON ? R.string.on : R.string.off);
        mTv22khz.setText(mLastFocusable22KHz);
        mTvLnbPower.setText(mSatInfo.LnbPower == Constants.SatInfoValue.LNB_POWER_ON ? getResources().getString(R.string.on) : getResources().getString(R.string.off));
        lnbTextChange();
        notify22kChange();

        LatLngModel latLngModel = new LatLngModel(LatLngModel.MODE_LONGITUDE, LatLngModel.LONGITUDE_THRESHOLD, satList.get(mCurrentSatellite).diseqc12_longitude);
        mTvLongitude.setText(latLngModel.getLatLngText());

        mFrequency4SCRArray = getValidArray(mFrequency4SCRArray, mSatInfo.unicConfig.SCR4UBand);
        mFrequency8SCRArray = getValidArray(mFrequency8SCRArray, mSatInfo.unicConfig.SCR8UBand);
        mFrequencyDCSSArray = getValidArray(mFrequencyDCSSArray, mSatInfo.unicConfig.dCSSUBand);

        mCurrentDiSEqCMode = getCurrDiSEqCMode(mSatInfo);
        mCurrentToneBurst = getCurrToneBurst(mSatInfo);
        mCurrentDiSEqC10 = getCurrDiSEqC10(mSatInfo);
        mCurrentDiSEqC11 = getCurrDiSEqC11(mSatInfo);
        mCurrentUnicable = getCurrUnicable(mSatInfo);
        mCurrentPosition = getCurrPosition(mSatInfo);
        mCurrentChannel = getCurrChannel(mSatInfo);
        diseqcModeTextChange();
        notifyDiSEqCItemVisible();
        toneBurstTextChange();
        diseqcTextChange();
        unicableTextChange();
        notifyPositionItemVisible();
        positionChange();
        channelChange();
    }

    private int[] getValidArray(int[] defaultArray, List<Integer> dataArray) {
        if (dataArray == null || dataArray.isEmpty()) return defaultArray;
        boolean isValid = false;
        int[] values = new int[dataArray.size()];
        for (int i = 0; i < dataArray.size(); i++) {
            Integer data = dataArray.get(i);
            values[i] = data;
            if (data > 0) {
                isValid = true;
            }
        }
        return isValid ? values : defaultArray;
    }

    /**
     * TP参数修改
     */
    private void tpChange() {
        getTpList();
        lockTp();
    }

    private void lockTp() {
        if (!isTpEmpty()) {
            HProg_Struct_TP channel = getTpList().get(mCurrentTp);
            String tpName = channel.Freq + Utils.getVorH(this, channel.Qam) + channel.Symbol;
            mTvTp.setText(tpName);

            DTVSearchManager.getInstance().tunerLockFreq(channel.SatIndex, channel.Freq, channel.Symbol, channel.Qam, 1, 0);
        } else {
            mTvTp.setText(getString(R.string.empty_tp));
        }
    }

    private boolean isTpEmpty() {
        return mTpList == null || mTpList.size() == 0;
    }

    /**
     * Lnb参数修改
     */
    private void lnbChange() {
        lnbTextChange();
        saveLnb();

        notify22kChange();
        save22kHz();
    }

    private void lnbTextChange() {
        if (mCurrentLnb == Constants.SatInfoValue.LNB_USER) {
            mTvLnb.setText(mLnbArray[0]);
        } else {
            mTvLnb.setText(mLnbArray[mCurrentLnb]);
        }
    }

    private void inputLnb(String inputNumber) {
        if (mCurrentLnb == Constants.SatInfoValue.LNB_USER && mCurrentSelectItem == ITEM_LNB) {
            if (mTvLnb.getText().toString().length() >= 4) {
                mTvLnb.setText("");
            }
            mTvLnb.append(inputNumber);
        }
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
        hz22kTextChange();
        save22kHz();
    }

    private void hz22kTextChange() {
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
        lnbPowerTextChange();
        saveLnbPower();
    }

    private void lnbPowerTextChange() {
        mTvLnbPower.setText(getResources().getString(isLnbPowerOn() ? R.string.off : R.string.on));
    }

    private boolean isLnbPowerOn() {
        return TextUtils.equals(mTvLnbPower.getText().toString(), getResources().getString(R.string.on));
    }

    /**
     * DiSEqC Mode参数修改
     */
    private void diseqcModeChange() {
        diseqcModeTextChange();
        notifyDiSEqCItemVisible();

        toneBurstTextChange();
        diseqcTextChange();
        unicableTextChange();
        saveDiSEqC();
        saveUnicable();

        notifyPositionItemVisible();
        positionChange();

        mCurrentChannel = 0;
        channelChange();
    }

    private void diseqcModeTextChange() {
        mTvDiSEqCMode.setText(mDiSEqCModeArray[mCurrentDiSEqCMode]);
    }

    private void notifyDiSEqCItemVisible() {
        mItemToneBurst.setVisibility(mCurrentDiSEqCMode == DISEQC_MODE_TONE_BURST ? View.VISIBLE : View.GONE);
        mItemDiSEqC.setVisibility(mCurrentDiSEqCMode == DISEQC_MODE_DISEQC10
                || mCurrentDiSEqCMode == DISEQC_MODE_DISEQC11 ? View.VISIBLE : View.GONE);
        mItemUnicable.setVisibility(mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE ? View.VISIBLE : View.GONE);
        mItemChannel.setVisibility(mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE ? View.VISIBLE : View.GONE);
        mItemFrequency.setVisibility(mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE ? View.VISIBLE : View.GONE);
    }

    /**
     * ToneBurst参数修改
     */
    private void toneBurstChange() {
        toneBurstTextChange();
        saveDiSEqC();
    }

    private void toneBurstTextChange() {
        if (mCurrentDiSEqCMode == DISEQC_MODE_TONE_BURST) {
            mTvToneBurst.setText(mToneBurstArray[mCurrentToneBurst]);
        }
    }

    /**
     * DiSEqC参数修改
     */
    private void diseqcChange() {
        diseqcTextChange();
        saveDiSEqC();
    }

    private void diseqcTextChange() {
        if (mCurrentDiSEqCMode == DISEQC_MODE_DISEQC10) {
            mTvDiSEqC.setText(mDiSEqC10Array[mCurrentDiSEqC10]);
        } else if (mCurrentDiSEqCMode == DISEQC_MODE_DISEQC11) {
            mTvDiSEqC.setText(mDiSEqC11Array[mCurrentDiSEqC11]);
        }
    }

    /**
     * Unicable参数修改
     */
    private void unicableChange() {
        unicableTextChange();

        notifyPositionItemVisible();
        positionChange();

        mCurrentChannel = 0;
        channelChange();

        saveUnicable();
    }

    private void unicableTextChange() {
        if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
            mTvUnicable.setText(mUnicableArray[mCurrentUnicable]);
        }
    }

    /**
     * Position参数修改
     */
    private void positionChange() {
        if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE && isMultiSat()) {
            mTvPosition.setText(mPositionArray[mCurrentPosition]);
        }
    }

    private void notifyPositionItemVisible() {
        mItemPosition.setVisibility(mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE && isMultiSat() ? View.VISIBLE : View.GONE);
    }

    /**
     * Channel参数修改
     */
    private void channelChange() {
        if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
            mTvChannel.setText(MessageFormat.format(getString(R.string.formatter_satellite_param_channel_step), mCurrentChannel + 1));
            frequencyChange();
        }
    }

    /**
     * Frequency参数修改
     */
    private void frequencyChange() {
        if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
            if (is4SCRUnicable()) {
                mTvFrequency.setText(String.valueOf(mFrequency4SCRArray[mCurrentChannel]));
            } else if (is8SCRUnicable()) {
                mTvFrequency.setText(String.valueOf(mFrequency8SCRArray[mCurrentChannel]));
            } else if (mCurrentUnicable == UNICABLE_DCSS) {
                if (mCurrentChannel >= DCSS_FREQUENCY_ZERO_MIN_RANGE && mCurrentChannel <= DCSS_FREQUENCY_ZERO_MAX_RANGE) {
                    mTvFrequency.setText("0");
                } else {
                    mTvFrequency.setText(String.valueOf(mFrequencyDCSSArray[mCurrentChannel]));
                }
            }
        }
    }

    private void inputFrequency(String inputNumber) {
        if (mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE && mCurrentSelectItem == ITEM_FREQUENCY) {
            if (mTvFrequency.getText().toString().length() >= 4) {
                mTvFrequency.setText("");
            }
            mTvFrequency.append(inputNumber);

            saveInputToArray();
        }
    }

    private void saveInputToArray() {
        int frequency = Integer.valueOf(mTvFrequency.getText().toString());
        if (is4SCRUnicable()) {
            mFrequency4SCRArray[mCurrentChannel] = frequency;
        } else if (is8SCRUnicable()) {
            mFrequency8SCRArray[mCurrentChannel] = frequency;
        } else {
            mFrequencyDCSSArray[mCurrentChannel] = frequency;
        }
    }

    private List<HProg_Struct_SatInfo> getSatList() {
        if (mSatList == null) {
            mSatList = DTVProgramManager.getInstance().getSatList();
        }
        return mSatList;
    }

    private List<HProg_Struct_TP> getTpList() {
        List<HProg_Struct_SatInfo> satList = getSatList();
        if (satList == null || satList.isEmpty()) return new ArrayList<>();

        mTpList = DTVProgramManager.getInstance().getSatTPInfo(getSatList().get(mCurrentSatellite).SatIndex);
        return mTpList;
    }

    private String getLnbO() {
        String lnb0 = PreferenceManager.getInstance().getString(String.valueOf(mCurrentSatellite));
        if (TextUtils.isEmpty(lnb0)) lnb0 = "0";
        return lnb0;
    }

    private int getCurrLnb(HProg_Struct_SatInfo satInfo) {
        String lnb = Utils.getLnb(satInfo);
        for (int i = 0; i < mLnbArray.length; i++) {
            if (TextUtils.equals(lnb, mLnbArray[i])) {
                return i;
            }
        }
        return 0;
    }

    private int getCurrDiSEqCMode(HProg_Struct_SatInfo satInfo) {
        if (satInfo.unicConfig.UnicEnable <= Constants.SatInfoValue.UNICABLE_DISABLE) {
            if (satInfo.diseqc10_pos == Constants.SatInfoValue.OFF_OR_TONEBURST) {
                if (satInfo.diseqc10_tone != Constants.SatInfoValue.OFF_OR_TONEBURST) {
                    return DISEQC_MODE_TONE_BURST;
                } else {
                    return DISEQC_MODE_OFF;
                }
            } else if (Utils.isDISEQC10(satInfo.diseqc10_pos)) {
                return DISEQC_MODE_DISEQC10;
            } else if (Utils.isDiSEqc11(satInfo.diseqc10_pos)) {
                return DISEQC_MODE_DISEQC11;
            }
        } else {
            return DISEQC_MODE_UNICABLE;
        }
        return 0;
    }

    private int getCurrToneBurst(HProg_Struct_SatInfo satInfo) {
        if (satInfo.unicConfig.UnicEnable <= Constants.SatInfoValue.UNICABLE_DISABLE
                && satInfo.diseqc10_pos == Constants.SatInfoValue.OFF_OR_TONEBURST && satInfo.diseqc10_tone != Constants.SatInfoValue.OFF_OR_TONEBURST) {
            // diseqc10_pos=0, OFF or ToneBurst
            // diseqc10_tone=0, OFF
            // diseqc10_tone=1, ToneBurst DISEQC_A
            // diseqc10_tone=2, ToneBurst DISEQC_B
            return satInfo.diseqc10_tone - 1;
        }
        return 0;
    }

    private int getCurrDiSEqC10(HProg_Struct_SatInfo satInfo) {
        if (satInfo.unicConfig.UnicEnable <= Constants.SatInfoValue.UNICABLE_DISABLE
                && satInfo.diseqc10_pos != Constants.SatInfoValue.OFF_OR_TONEBURST && Utils.isDISEQC10(satInfo.diseqc10_pos)) {
            // diseqc10_pos=1~4, DiSEqC DISEQC_A~DISEQC_B
            return satInfo.diseqc10_pos - 1;
        }
        return 0;
    }

    private int getCurrDiSEqC11(HProg_Struct_SatInfo satInfo) {
        if (satInfo.unicConfig.UnicEnable <= Constants.SatInfoValue.UNICABLE_DISABLE
                && satInfo.diseqc10_pos != Constants.SatInfoValue.OFF_OR_TONEBURST && Utils.isDiSEqc11(satInfo.diseqc10_pos)) {
            // diseqc10_pos=5~16, LNB 1~16
            return satInfo.diseqc10_pos - 5;
        }
        return 0;
    }

    private int getCurrUnicable(HProg_Struct_SatInfo satInfo) {
        if (satInfo.unicConfig.UnicEnable == Constants.SatInfoValue.UNICABLE_SCR_ENABLE) {
            // SatPosition=0, SCRType=0, 1Sat4SCR
            // SatPosition=0, SCRType=1, 1Sat8SCR
            // SatPosition>=1, SCRType=0, 2Sat4SCR
            // SatPosition>=1, SCRType=1, 2Sat8SCR
            if (satInfo.unicConfig.SatPosition == Constants.SatInfoValue.SINGLE_SAT_POSITION && satInfo.unicConfig.SCRType == Constants.SatInfoValue.SCR_4) {
                return UNICABLE_1SAT4SCR;
            } else if (satInfo.unicConfig.SatPosition == Constants.SatInfoValue.SINGLE_SAT_POSITION && satInfo.unicConfig.SCRType == Constants.SatInfoValue.SCR_8) {
                return UNICABLE_1SAT8SCR;
            } else if (satInfo.unicConfig.SatPosition >= Constants.SatInfoValue.MULTI_SAT_POSITION_A && satInfo.unicConfig.SCRType == Constants.SatInfoValue.SCR_4) {
                return UNICABLE_2SAT4SCR;
            } else if (satInfo.unicConfig.SatPosition >= Constants.SatInfoValue.MULTI_SAT_POSITION_A && satInfo.unicConfig.SCRType == Constants.SatInfoValue.SCR_8) {
                return UNICABLE_2SAT8SCR;
            }
        } else if (satInfo.unicConfig.UnicEnable == Constants.SatInfoValue.UNICABLE_DCSS_ENABLE) {
            return UNICABLE_DCSS;
        }
        return 0;
    }

    private int getCurrPosition(HProg_Struct_SatInfo satInfo) {
        if (satInfo.unicConfig.UnicEnable == Constants.SatInfoValue.UNICABLE_SCR_ENABLE) {
            if (mSatInfo.unicConfig.SatPosition == Constants.SatInfoValue.SINGLE_SAT_POSITION) {
                return Constants.SatInfoValue.SINGLE_SAT_POSITION;
            } else if (mSatInfo.unicConfig.SatPosition == Constants.SatInfoValue.MULTI_SAT_POSITION_A) {
                return Constants.SatInfoValue.MULTI_SAT_POSITION_A - 1; // position显示索引要与实际值-1
            } else if (mSatInfo.unicConfig.SatPosition == Constants.SatInfoValue.MULTI_SAT_POSITION_B) {
                return Constants.SatInfoValue.MULTI_SAT_POSITION_B - 1;
            }
        }
        return Constants.SatInfoValue.SINGLE_SAT_POSITION;
    }

    private int getCurrChannel(HProg_Struct_SatInfo satInfo) {
        if (satInfo.unicConfig.UnicEnable > Constants.SatInfoValue.UNICABLE_DISABLE) {
            return satInfo.unicConfig.SCRNO;
        }
        return 0;
    }

    private boolean is4SCRUnicable() {
        return mCurrentUnicable == UNICABLE_1SAT4SCR || mCurrentUnicable == UNICABLE_2SAT4SCR;
    }

    private boolean is8SCRUnicable() {
        return mCurrentUnicable == UNICABLE_1SAT8SCR || mCurrentUnicable == UNICABLE_2SAT8SCR;
    }

    private boolean isMultiSat() {
        return mCurrentUnicable == UNICABLE_2SAT4SCR || mCurrentUnicable == UNICABLE_2SAT8SCR;
    }

    private void itemFocusChange() {
        itemChange(mCurrentSelectItem, ITEM_SATELLITE, mItemSatellite, mIvSatelliteLeft, mIvSatelliteRight, mTvSatellite);
        itemChange(mCurrentSelectItem, ITEM_TP, mItemTp, mIvTpLeft, mIvTpRight, mTvTp);
        itemChange(mCurrentSelectItem, ITEM_LNB, mItemLnb, mIvLnbLeft, mIvLnbRight, mTvLnb);
        itemChange(mCurrentSelectItem, ITEM_LNB_POWER, mItemLnbPower, mIvLnbPowerLeft, mIvLnbPowerRight, mTvLnbPower);
        itemChange(mCurrentSelectItem, ITEM_DISEQC_MODE, mItemDiSEqCMode, mIvDiSEqcModeLeft, mIvDiSEqCModeRight, mTvDiSEqCMode);
        toneBurstItemFocusChange();
        diSEqcItemFocusChange();
        unicableItemFocusChange();
        positionItemFocusChange();
        channelItemFocusChange();
        frequencyItemFocusChange();

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

    private void toneBurstItemFocusChange() {
        if (mCurrentDiSEqCMode != DISEQC_MODE_OFF) {
            int selectItem = -1;
            if (mCurrentSelectItem == ITEM_DISEQC_TYPE && mCurrentDiSEqCMode == DISEQC_MODE_TONE_BURST) {
                selectItem = ITEM_DISEQC_TYPE;
            }
            itemChange(mCurrentSelectItem, selectItem, mItemToneBurst, mIvToneBurstLeft, mIvToneBurstRight, mTvToneBurst);
        }
    }

    private void diSEqcItemFocusChange() {
        if (mCurrentDiSEqCMode != DISEQC_MODE_OFF) {
            int selectItem = -1;
            if (mCurrentSelectItem == ITEM_DISEQC_TYPE &&
                    (mCurrentDiSEqCMode == DISEQC_MODE_DISEQC10 || mCurrentDiSEqCMode == DISEQC_MODE_DISEQC11)) {
                selectItem = ITEM_DISEQC_TYPE;
            }
            itemChange(mCurrentSelectItem, selectItem, mItemDiSEqC, mIvDiSEqCLeft, mIvDiSEqCRight, mTvDiSEqC);
        }
    }

    private void unicableItemFocusChange() {
        if (mCurrentDiSEqCMode != DISEQC_MODE_OFF) {
            int selectItem = -1;
            if (mCurrentSelectItem == ITEM_DISEQC_TYPE && mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
                selectItem = ITEM_DISEQC_TYPE;
            }
            itemChange(mCurrentSelectItem, selectItem, mItemUnicable, mIvUnicableLeft, mIvUnicableRight, mTvUnicable);
        }
    }

    private void positionItemFocusChange() {
        if (mCurrentDiSEqCMode != DISEQC_MODE_OFF && mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
            int selectItem = -1;
            if (mCurrentSelectItem == ITEM_POSITION && isMultiSat()) {
                selectItem = ITEM_POSITION;
            }
            itemChange(mCurrentSelectItem, selectItem, mItemPosition, mIvPositionLeft, mIvPositionRight, mTvPosition);
        }
    }

    private void channelItemFocusChange() {
        if (mCurrentDiSEqCMode != DISEQC_MODE_OFF && mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
            int selectItem = -1;
            if (mCurrentSelectItem == ITEM_CHANNEL) {
                selectItem = ITEM_CHANNEL;
            }
            itemChange(mCurrentSelectItem, selectItem, mItemChannel, mIvChannelLeft, mIvChannelRight, mTvChannel);
        }
    }

    private void frequencyItemFocusChange() {
        if (mCurrentDiSEqCMode != DISEQC_MODE_OFF && mCurrentDiSEqCMode == DISEQC_MODE_UNICABLE) {
            int selectItem = -1;
            if (mCurrentSelectItem == ITEM_FREQUENCY) {
                selectItem = ITEM_FREQUENCY;
            }
            itemChange(mCurrentSelectItem, selectItem, null, null, mTvFrequency);
        }
    }
}
