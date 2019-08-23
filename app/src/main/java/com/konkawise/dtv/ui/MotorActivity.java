package com.konkawise.dtv.ui;

import android.os.Message;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.PreferenceManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.OnCommCallback;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.CheckSignalHelper;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.sw.dvblib.SWFta;

import java.text.MessageFormat;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.ChannelNew_t;
import vendor.konka.hardware.dtvmanager.V1_0.HMotorCtrlCode;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;


/**
 * motor界面
 */

public class MotorActivity extends BaseActivity {
    private static final String TAG = "MotorActivity";
    //MotorType为DisEqc1.2时，当前焦点位置，即position,但切换MotorType，会改变数值代表的意义
    private static final int Command = 7;
    private static final int Position = 6;
    private static final int DisEqc_Command = 6;
    private static final int Local_Latitude = 5;
    private static final int Position_Dis = 5;
    private static final int Local_Longitude = 4;
    private static final int Step_Size = 4;
    private static final int Move_Steps = 3;
    private static final int Sat_Longitude = 3;
    private static final int TP = 2;
    private static final int Motor_Type = 1;

    //当前选中的MotorType，结合position做遥控器按键切换
    private static final int DisEqc = 0;
    private static final int USALS = 1;
    private static final int OFF = 2;

    //控制切换回stop的时间
    private static final int Step_32_Time = 500;
    private static final int Step_16_Time = 250;
    private static final int Step_8_Time = 125;
    private static final int Step_4_Time = 68;
    private static final int Step_1_Time = 8;
    private static final int Second_4_Time = 4000;
    private static final int Second_3_Time = 3000;
    private static final int Second_2_Time = 2000;
    private static final int Second_1_Time = 1000;
    private static final int Stop_Move = 1;

    @BindView(R.id.iv_satellite_left)
    ImageView mIvSatelliteLeft;

    @BindView(R.id.tv_satellite)
    TextView mTvSatellite;

    @BindView(R.id.iv_satellite_right)
    ImageView mIvSatelliteRight;

    @BindView(R.id.item_motor_type)
    ViewGroup mItemMotorType;

    @BindView(R.id.iv_motor_type_left)
    ImageView mIvMotorTypeLeft;

    @BindView(R.id.tv_motor_type)
    TextView mTvMotorType;

    @BindView(R.id.iv_motor_type_right)
    ImageView mIvMotorTypeRight;

    @BindView(R.id.item_tp)
    ViewGroup mItemTp;

    @BindView(R.id.iv_tp_left)
    ImageView mIvTpLeft;

    @BindView(R.id.tv_tp)
    TextView mTvTp;

    @BindView(R.id.iv_tp_right)
    ImageView mIvTpRight;

    @BindView(R.id.item_move_step)
    ViewGroup mItemMoveStep;

    @BindView(R.id.iv_move_step_left)
    ImageView mIvMoveStepLeft;

    @BindView(R.id.tv_move_step)
    TextView mTvMoveStep;

    @BindView(R.id.iv_move_step_right)
    ImageView mIvMoveStepRight;

    @BindView(R.id.item_step_size)
    ViewGroup mItemStepSize;

    @BindView(R.id.iv_step_size_left)
    ImageView mIvStepSizeLeft;

    @BindView(R.id.tv_step_size)
    TextView mTvStepSize;

    @BindView(R.id.iv_step_size_right)
    ImageView mIvStepSizeRight;

    @BindView(R.id.item_position)
    ViewGroup mItemPosition;

    @BindView(R.id.iv_position_left)
    ImageView mIvPositionLeft;

    @BindView(R.id.tv_position)
    TextView mTvPosition;

    @BindView(R.id.iv_position_right)
    ImageView mIvPositionRight;

    @BindView(R.id.item_diseqc_command)
    ViewGroup mItemDiSEqCCommand;

    @BindView(R.id.tv_diseqc_common_title)
    TextView mTvDiSEqCCommandTitle;

    @BindView(R.id.iv_diseqc_command_left)
    ImageView mIvDiSEqCCommandLeft;

    @BindView(R.id.tv_diseqc_command)
    TextView mTvDiSEqCCommand;

    @BindView(R.id.iv_diseqc_command_right)
    ImageView mIvDiSEqCCommandRight;

    @BindView(R.id.ll_local_longitude_latitude)
    LinearLayout mLocalLongitudeLayout;

    @BindView(R.id.item_local_longitude)
    ViewGroup mItemLocalLongitude;

    @BindView(R.id.tv_local_longitude_title)
    TextView mTvLocalLongitudeTitle;

    @BindView(R.id.tv_local_longitude_arrow)
    TextView mTvLocalLongitudeArrow;

    @BindView(R.id.tv_local_longitude)
    TextView mTvLocalLongitude;

    @BindView(R.id.iv_local_longitude_right)
    ImageView mIvLocalLongitudeRight;

    @BindView(R.id.item_local_latitude)
    ViewGroup mItemLocalLatitude;

    @BindView(R.id.tv_local_latitude_title)
    TextView mTvLocalLatitudeTitle;

    @BindView(R.id.tv_local_latitude_arrow)
    TextView mTvLocalLatitudeArrow;

    @BindView(R.id.tv_local_latitude)
    TextView mTvLocalLatitude;

    @BindView(R.id.iv_local_latitude_right)
    ImageView mIvLocalLatitudeRight;

    @BindView(R.id.item_sat_longitude)
    ViewGroup mItemSatLongitude;

    @BindView(R.id.tv_sat_longitude_title)
    TextView mTvSatLongitudeTitle;

    @BindView(R.id.tv_sat_longitude_arrow)
    TextView mTvSatLongitudeArrow;

    @BindView(R.id.tv_sat_longitude)
    TextView mTvSatLongitude;

    @BindView(R.id.iv_sat_longitude_right)
    ImageView mIvSatLongitudeRight;

    @BindView(R.id.tv_progress_strength)
    TextView mTvProgressStrength;

    @BindView(R.id.pb_strength)
    ProgressBar mPbStrength;

    @BindView(R.id.tv_progress_quality)
    TextView mTvProgressQuality;

    @BindView(R.id.pb_quality)
    ProgressBar mPbQuality;

    @BindArray(R.array.step_size)
    String[] mStepSizeArray;

    @BindArray(R.array.diseqc_command)
    String[] mDiSEqCCommandArray;

    @BindArray(R.array.usals_command)
    String[] mUsalsCommandArray;

    @BindArray(R.array.motor_type)
    String[] mTypeListArray;

    @BindArray(R.array.move_step)
    String[] mMoveStepArray;

    @BindArray(R.array.step_size_data)
    int[] mStepSizeDataArray;

    private int position = 1;

    private int mCurrentTp;
    private int mSatelliteIndex;
    private List<ChannelNew_t> mTpList;
    private int mMotorType;

    private int mMoveStep;
    private int mStepSizeStep;
    private int mPositionStep = 1;//Position 1到Position 51
    private int mCommandStep;
    private int mDISEqcCommandStep;
    private CheckSignalHelper mCheckSignalHelper;
    private MotorHandler mMotorHandler;
    private MotorCtrlRunnable mMotorRunnable;
    private SatInfo_t mSatInfo;

    private double mSatLongitude;
    private double mLocalLongitude;
    private double mLocalLatitude;

    @Override
    public int getLayoutId() {
        return R.layout.activity_mortor;
    }

    @Override
    protected void setup() {
        initCheckSignal();
        initIntent();
        initMotorUi();
        tryLockTp();
        motorCtrlReady();
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

    @Override
    protected void onStop() {
        super.onStop();
        stopMotorCtrl();
        //退出时保存经纬度
        mSatInfo.diseqc12_longitude = (int) mSatLongitude;
        SWPDBaseManager.getInstance().setSatInfo(mSatelliteIndex, mSatInfo);
        SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Longitude.ordinal(), (int) mLocalLongitude);
        SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Latitude.ordinal(), (int) mLocalLatitude);
    }

    private void stopMotorCtrl() {
        ThreadPoolManager.getInstance().remove(mMotorRunnable);
        mMotorRunnable.ctrlCode = HMotorCtrlCode.DIRECT_STOP;
        mMotorRunnable.data = new int[]{0};
        ThreadPoolManager.getInstance().execute(mMotorRunnable);

        sendStopMessage(new int[]{0});
    }

    private void initCheckSignal() {
        mCheckSignalHelper = new CheckSignalHelper(this);
        mCheckSignalHelper.setOnCheckSignalListener(new CheckSignalHelper.OnCheckSignalListener() {
            @Override
            public void signal(int strength, int quality) {
                String strengthPercent = strength + "%";
                mTvProgressStrength.setText(strengthPercent);
                mPbStrength.setProgress(strength);

                String qualityPercent = quality + "%";
                mTvProgressQuality.setText(qualityPercent);
                mPbQuality.setProgress(quality);
            }
        });
    }

    private void initIntent() {
        mCurrentTp = getIntent().getIntExtra(Constants.IntentKey.INTENT_CURRNT_TP, -1);
        mSatelliteIndex = getIntent().getIntExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, -1);
        mSatInfo = SWPDBaseManager.getInstance().getSatList().get(SWPDBaseManager.getInstance().findPositionBySatIndex(mSatelliteIndex));
        mTpList = SWPDBaseManager.getInstance().getSatChannelInfoList(mSatelliteIndex);

        mSatLongitude = mSatInfo.diseqc12_longitude > 1800 ? (mSatInfo.diseqc12_longitude - 3600) : mSatInfo.diseqc12_longitude;
        mLocalLongitude = SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Longitude.ordinal()) > 1800 ?
                SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Longitude.ordinal()) - 3600 :
                SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Longitude.ordinal());
        mLocalLatitude = SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Latitude.ordinal()) > 900 ?
                SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Latitude.ordinal()) - 1800 :
                SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Latitude.ordinal());
    }

    private void initMotorUi() {
        mTvMoveStep.setText(mMoveStepArray[0]);

        String satelliteName = getIntent().getStringExtra(Constants.IntentKey.INTENT_SATELLITE_NAME);
        mTvSatellite.setText(TextUtils.isEmpty(satelliteName) ? "" : satelliteName);

        String tpName = getIntent().getStringExtra(Constants.IntentKey.INTENT_TP_NAME);
        mTvTp.setText(TextUtils.isEmpty(tpName) ? getString(R.string.empty_tp) : tpName);

        mTvStepSize.setText(mStepSizeArray[0]);
        mTvPosition.bringToFront();

        //取出上一次存的position
        mPositionStep = PreferenceManager.getInstance().getInt(Constants.PrefsKey.SAVE_POSITION);
        mTvPosition.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
    }

    private void tryLockTp() {
        if (mTpList != null && mTpList.size() != 0) {
            SWFtaManager.getInstance().tunerLockFreq(mSatelliteIndex, mTpList.get(mCurrentTp).Freq, mTpList.get(mCurrentTp).Symbol, mTpList.get(mCurrentTp).Qam, 1, 0);
        }
    }

    private void motorCtrlReady() {
        mMotorHandler = new MotorHandler(this);
        mMotorRunnable = new MotorCtrlRunnable(this);
    }

    private static class MotorHandler extends WeakHandler<MotorActivity> {

        MotorHandler(MotorActivity view) {
            super(view);
        }

        @Override
        protected void handleMsg(Message msg) {
            if (msg.what == Stop_Move) {
                moveStop();
            }
        }

        private void moveStop() {
            MotorActivity context = mWeakReference.get();
            context.mMoveStep = 0;
            context.mTvMoveStep.setText(context.mMoveStepArray[context.mMoveStep]);
        }
    }

    private static class MotorCtrlRunnable extends WeakRunnable<MotorActivity> {
        int ctrlCode;
        int repeat;
        int[] data;

        MotorCtrlRunnable(MotorActivity view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            MotorActivity context = mWeakReference.get();

            SWFta.GetInstance().tunerMotorControl(ctrlCode, repeat, data);
            String moveStep = context.mMoveStepArray[context.mMoveStep];
            if (TextUtils.equals(moveStep, context.getResources().getString(R.string.motor_move_step_west)) ||
                    TextUtils.equals(moveStep, context.getResources().getString(R.string.motor_move_step_east))) {
                if (data[0] != 0) {
                    context.sendStopMessage(data);
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            invisableAll();
            if (mMotorType == OFF) {
                switch (position) {
                    case TP:
                        --position;
                        selecteMotorType();
                        break;
                    case Motor_Type:
                        selecteMotorType();
                        break;
                }
            } else if (mMotorType == USALS) {
                switch (position) {
                    case Command:
                        --position;
                        selectePosition();
                        break;
                    case Position:
                        --position;
                        selecteLocalLatitude();
                        break;
                    case Local_Latitude:
                        --position;
                        selecteLocalLongitude();
                        break;
                    case Local_Longitude:
                        --position;
                        selecteSatLongitude();
                        break;
                    case Sat_Longitude:
                        --position;
                        selecteMotorTp();
                        break;
                    case TP:
                        --position;
                        selecteMotorType();
                        break;
                    case Motor_Type:
                        selecteMotorType();
                        break;
                }
            } else if (mMotorType == DisEqc) {
                switch (position) {
                    case DisEqc_Command:
                        --position;
                        selectePosition();
                        break;
                    case Position_Dis:
                        --position;
                        selecteStepSize();
                        break;
                    case Step_Size:
                        --position;
                        selecteMoveSteps();
                        break;
                    case Move_Steps:
                        --position;
                        selecteMotorTp();
                        break;
                    case TP:
                        --position;
                        selecteMotorType();
                        break;
                    case Motor_Type:
                        selecteMotorType();
                        break;
                }
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            invisableAll();
            if (mMotorType == OFF) {
                switch (position) {
                    case Motor_Type:
                        position = position + 1;
                        selecteMotorTp();
                        break;
                    case TP:
                        position = position + 1;
                        position = Motor_Type;
                        selecteMotorTp();
                        break;
                }
            } else if (mMotorType == USALS) {
                switch (position) {
                    case Motor_Type:
                        position = position + 1;
                        selecteMotorTp();
                        break;
                    case TP:
                        position = position + 1;
                        selecteSatLongitude();
                        break;
                    case Sat_Longitude:
                        position = position + 1;
                        selecteLocalLongitude();
                        break;
                    case Local_Longitude:
                        position = position + 1;
                        selecteLocalLatitude();
                        break;
                    case Local_Latitude:
                        position = position + 1;
                        selectePosition();
                        break;
                    case Position:
                        position = position + 1;
                        selecteCommand();
                        break;
                    case Command:
                        selecteCommand();
                        break;
                }
            } else if (mMotorType == DisEqc) {
                switch (position) {
                    case Motor_Type:
                        position = position + 1;
                        selecteMotorTp();
                        break;
                    case TP:
                        position = position + 1;
                        selecteMoveSteps();
                        break;
                    case Move_Steps:
                        position = position + 1;
                        selecteStepSize();
                        break;
                    case Step_Size:
                        position = position + 1;
                        selectePosition();
                        break;
                    case Position_Dis:
                        position = position + 1;
                        selecteCommand();
                        break;
                    case DisEqc_Command:
                        selecteCommand();
                        break;
                }
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (mMotorType == OFF) {
                switch (position) {
                    case Motor_Type:
                        --mMotorType;
                        if (mMotorType < 0) {
                            mMotorType = mTypeListArray.length - 1;
                        }
                        selecteMotorTypeUsals();
                        break;
                    case TP:
                        //控制Tp栏左键
                        --mCurrentTp;
                        if (mCurrentTp < 0) {
                            mCurrentTp = mTpList.size() - 1;
                        }
                        getTPName(mCurrentTp);
                        break;
                }
            } else if (mMotorType == USALS) {
                switch (position) {
                    case Motor_Type:
                        --mMotorType;
                        if (mMotorType < 0) {
                            mMotorType = mTypeListArray.length - 1;
                        }
                        selecteMotorTypeDisEqc();
                        break;
                    case TP:
                        --mCurrentTp;
                        if (mCurrentTp < 0) {
                            mCurrentTp = mTpList.size() - 1;
                        }
                        getTPName(mCurrentTp);
                        break;
                    case Sat_Longitude:
                        break;
                    case Local_Longitude:
                        break;
                    case Local_Latitude:
                        break;
                    case Position:
                        --mPositionStep;
                        if (mPositionStep < 1) {
                            mPositionStep = 51;
                        }
                        mTvPosition.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
                        break;
                    case Command:
                        --mCommandStep;
                        if (mCommandStep < 0) {
                            mCommandStep = mUsalsCommandArray.length - 1;
                        }
                        mTvDiSEqCCommand.setText(mUsalsCommandArray[mCommandStep]);
                        break;
                }
            } else if (mMotorType == DisEqc) {
                switch (position) {
                    case Motor_Type:
                        --mMotorType;
                        if (mMotorType < 0) {
                            mMotorType = mTypeListArray.length - 1;
                        }
                        selecteMotorTypeOff();
                        break;
                    case TP:
                        --mCurrentTp;
                        if (mCurrentTp < 0) {
                            mCurrentTp = mTpList.size() - 1;
                        }
                        getTPName(mCurrentTp);
                        break;
                    case Move_Steps:
                        --mMoveStep;
                        if (mMoveStep < 0) {
                            mMoveStep = mMoveStepArray.length - 1;
                        }
                        mTvMoveStep.setText(mMoveStepArray[mMoveStep]);
                        MotorCtrlModel motorCtrlModel = getMotorCtrlModelByMoveStep();
                        ThreadPoolManager.getInstance().remove(mMotorRunnable);
                        mMotorRunnable.ctrlCode = motorCtrlModel.ctrlCode;
                        mMotorRunnable.repeat = motorCtrlModel.repeat;
                        mMotorRunnable.data = motorCtrlModel.data;
                        ThreadPoolManager.getInstance().execute(mMotorRunnable);
                        break;
                    case Step_Size:
                        --mStepSizeStep;
                        if (mStepSizeStep < 0) {
                            mStepSizeStep = mStepSizeArray.length - 1;
                        }
                        mTvStepSize.setText(mStepSizeArray[mStepSizeStep]);
                        break;
                    case Position_Dis:
                        --mPositionStep;
                        if (mPositionStep < 1) {
                            mPositionStep = 51;
                        }
                        mTvPosition.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
                        break;
                    case DisEqc_Command:
                        --mDISEqcCommandStep;
                        if (mDISEqcCommandStep < 0) {
                            mDISEqcCommandStep = mDiSEqCCommandArray.length - 1;
                        }
                        mTvDiSEqCCommand.setText(mDiSEqCCommandArray[mDISEqcCommandStep]);
                        break;
                }
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (mMotorType == OFF) {
                switch (position) {
                    case Motor_Type:
                        ++mMotorType;
                        if (mMotorType > mTypeListArray.length - 1) {
                            mMotorType = DisEqc;
                        }
                        selecteMotorTypeDisEqc();
                        break;
                    case TP:
                        //控制Tp栏左键
                        ++mCurrentTp;
                        if (mCurrentTp > mTpList.size() - 1) {
                            mCurrentTp = 0;
                        }
                        getTPName(mCurrentTp);
                        break;
                }
            } else if (mMotorType == USALS) {
                switch (position) {
                    case Motor_Type:
                        ++mMotorType;
                        if (mMotorType > mTypeListArray.length - 1) {
                            mMotorType = DisEqc;
                        }
                        selecteMotorTypeOff();
                        break;
                    case TP:
                        ++mCurrentTp;
                        if (mCurrentTp > mTpList.size() - 1) {
                            mCurrentTp = 0;
                        }
                        getTPName(mCurrentTp);
                        break;
                    case Sat_Longitude:
                        mTvSatLongitudeArrow.setText(mTvSatLongitudeArrow.getText().toString().equals("W") ? "E" : "W");
                        mSatLongitude = -mSatLongitude;
                        break;
                    case Local_Longitude:
                        mTvLocalLongitudeArrow.setText(mTvLocalLongitudeArrow.getText().toString().equals("W") ? "E" : "W");
                        mLocalLongitude = -mLocalLongitude;
                        break;
                    case Local_Latitude:
                        mTvLocalLatitudeArrow.setText(mTvLocalLatitudeArrow.getText().toString().equals("S") ? "N" : "S");
                        mLocalLatitude = -mLocalLatitude;
                        break;
                    case Position:
                        ++mPositionStep;
                        if (mPositionStep > 51) {
                            mPositionStep = 1;
                        }
                        mTvPosition.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
                        break;
                    case Command:
                        ++mCommandStep;
                        if (mCommandStep > mUsalsCommandArray.length - 1) {
                            mCommandStep = 0;
                        }
                        mTvDiSEqCCommand.setText(mUsalsCommandArray[mCommandStep]);
                        break;
                }
            } else if (mMotorType == DisEqc) {
                switch (position) {
                    case Motor_Type:
                        ++mMotorType;
                        if (mMotorType > mTypeListArray.length - 1) {
                            mMotorType = DisEqc;
                        }
                        selecteMotorTypeUsals();
                        break;
                    case TP:
                        ++mCurrentTp;
                        if (mCurrentTp > mTpList.size() - 1) {
                            mCurrentTp = 0;
                        }
                        getTPName(mCurrentTp);
                        break;
                    case Move_Steps:
                        ++mMoveStep;
                        if (mMoveStep > mMoveStepArray.length - 1) {
                            mMoveStep = 0;
                        }
                        mTvMoveStep.setText(mMoveStepArray[mMoveStep]);
                        MotorCtrlModel motorCtrlModel = getMotorCtrlModelByMoveStep();
                        ThreadPoolManager.getInstance().remove(mMotorRunnable);
                        mMotorRunnable.ctrlCode = motorCtrlModel.ctrlCode;
                        mMotorRunnable.repeat = motorCtrlModel.repeat;
                        mMotorRunnable.data = motorCtrlModel.data;
                        ThreadPoolManager.getInstance().execute(mMotorRunnable);
                        break;
                    case Step_Size:
                        ++mStepSizeStep;
                        if (mStepSizeStep > mStepSizeArray.length - 1) {
                            mStepSizeStep = 0;
                        }
                        mTvStepSize.setText(mStepSizeArray[mStepSizeStep]);
                        break;
                    case Position_Dis:
                        ++mPositionStep;
                        if (mPositionStep > 51) {
                            mPositionStep = 1;
                        }
                        mTvPosition.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
                        break;
                    case DisEqc_Command:
                        ++mDISEqcCommandStep;
                        if (mDISEqcCommandStep > mDiSEqCCommandArray.length - 1) {
                            mDISEqcCommandStep = 0;
                        }
                        mTvDiSEqCCommand.setText(mDiSEqCCommandArray[mDISEqcCommandStep]);
                        break;
                }
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if ((mMotorType == DisEqc && position == DisEqc_Command) ||
                    mMotorType == USALS && position == Command) {
                MotorCtrlModel motorCtrlModel = getMotorCtrlModelByCommand();
                showCommandDialog(motorCtrlModel.title, new OnCommCallback() {
                    @Override
                    public void callback(Object object) {
                        if (TextUtils.equals(mTvDiSEqCCommand.getText().toString(), getString(R.string.motor_command_savepos))) {
                            if (mMotorType == DisEqc && position == DisEqc_Command) {
                                saveSatMotorTypeInfo();
                            }
                            saveSatPositionInfo();
                        } else {
                            ThreadPoolManager.getInstance().remove(mMotorRunnable);
                            mMotorRunnable.ctrlCode = motorCtrlModel.ctrlCode;
                            mMotorRunnable.repeat = motorCtrlModel.repeat;
                            mMotorRunnable.data = motorCtrlModel.data;
                            ThreadPoolManager.getInstance().execute(mMotorRunnable);
                        }
                    }
                });
                return true;
            }
        }

        if ((keyCode == KeyEvent.KEYCODE_0) && (mLocalLongitudeLayout.getVisibility() == View.VISIBLE)) {
            inputNumber(0, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_1) && (mLocalLongitudeLayout.getVisibility() == View.VISIBLE)) {
            inputNumber(1, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_2) && (mLocalLongitudeLayout.getVisibility() == View.VISIBLE)) {
            inputNumber(2, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_3) && (mLocalLongitudeLayout.getVisibility() == View.VISIBLE)) {
            inputNumber(3, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_4) && (mLocalLongitudeLayout.getVisibility() == View.VISIBLE)) {
            inputNumber(4, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_5) && (mLocalLongitudeLayout.getVisibility() == View.VISIBLE)) {
            inputNumber(5, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_6) && (mLocalLongitudeLayout.getVisibility() == View.VISIBLE)) {
            inputNumber(6, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_7) && (mLocalLongitudeLayout.getVisibility() == View.VISIBLE)) {
            inputNumber(7, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_8) && (mLocalLongitudeLayout.getVisibility() == View.VISIBLE)) {
            inputNumber(8, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_9) && (mLocalLongitudeLayout.getVisibility() == View.VISIBLE)) {
            inputNumber(9, position);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private MotorCtrlModel getMotorCtrlModelByCommand() {
        String command = mTvDiSEqCCommand.getText().toString();
        String title = "";
        int ctrlCode = 0;
        int repeat = 0;
        int[] data = new int[1];

        if (TextUtils.equals(mTvDiSEqCCommand.getText().toString(), getString(R.string.motor_command_savepos))) {
            if (mMotorType == DisEqc && position == DisEqc_Command) {
                title = getString(R.string.dialog_save_position);
            } else if (mMotorType == USALS && position == Command) {
                title = getString(R.string.dialog_save_pos);
            }
        } else if (TextUtils.equals(command, getString(R.string.motor_diseqc_command_recalculate))) {
            title = getString(R.string.dialog_calculate);
            ctrlCode = HMotorCtrlCode.DIRECT_RECALCULATE;
        } else if (TextUtils.equals(command, getString(R.string.motor_diseqc_command_disablelimit))) {
            title = getString(R.string.dialog_disable_limit);
            ctrlCode = HMotorCtrlCode.DIRECT_DISABLE_LIMIT;
        } else if (TextUtils.equals(command, getString(R.string.motor_diseqc_command_eastlimit))) {
            title = getString(R.string.dialog_east_limit);
            ctrlCode = HMotorCtrlCode.DIRECT_EAST_LIMIT;
        } else if (TextUtils.equals(command, getString(R.string.motor_diseqc_command_westlimit))) {
            title = getString(R.string.dialog_west_limit);
            ctrlCode = HMotorCtrlCode.DIRECT_WEST_LIMIT;
        } else if (TextUtils.equals(command, getString(R.string.motor_command_gotoref))) {
            if (mMotorType == DisEqc && position == DisEqc_Command) {
                title = getString(R.string.dialog_goto_ref);
            } else if (mMotorType == USALS && position == Command) {
                title = getString(R.string.dialog_command_goto_ref);
            }
            ctrlCode = HMotorCtrlCode.DIRECT_GO_REFERENCE;
        } else if (TextUtils.equals(command, getString(R.string.motor_usals_command_gotoxx))) {
            title = getString(R.string.dialog_goto_xx);
            ctrlCode = HMotorCtrlCode.DIRECT_USAL_GOTOXX;
        } else if (TextUtils.equals(command, getString(R.string.motor_usals_command_calculate))) {
            title = getString(R.string.dialog_commannd_calculate);
            ctrlCode = HMotorCtrlCode.DIRECT_RECALCULATE;
        } else if (TextUtils.equals(command, getString(R.string.motor_usals_command_shift))) {
            title = getString(R.string.dialog_shift);
            ctrlCode = HMotorCtrlCode.DIRECT_USAL_SHIFT;
        }
        return new MotorCtrlModel(title, ctrlCode, repeat, data);
    }

    private MotorCtrlModel getMotorCtrlModelByMoveStep() {
        int ctrlCode = 0;
        int repeat = 0;
        int[] data = new int[]{mStepSizeDataArray[mStepSizeStep]};

        String moveStep = mMoveStepArray[mMoveStep];

        if (TextUtils.equals(moveStep, getResources().getString(R.string.motor_move_step_stop))) {
            ctrlCode = HMotorCtrlCode.DIRECT_STOP;
        } else if (TextUtils.equals(moveStep, getResources().getString(R.string.motor_move_step_west))) {
            if (data[0] == 0) { // data[0] == 0 Continue持续转动
                ctrlCode = HMotorCtrlCode.DIRECT_RIGHT_CONTINUE;
            } else {
                ctrlCode = HMotorCtrlCode.DIRECT_RIGHT_STEP;
            }
        } else if (TextUtils.equals(moveStep, getResources().getString(R.string.motor_move_step_east))) {
            if (data[0] == 0) {
                ctrlCode = HMotorCtrlCode.DIRECT_LEFT_CONTINUE;
            } else {
                ctrlCode = HMotorCtrlCode.DIRECT_LEFT_STEP;
            }
        }
        return new MotorCtrlModel(ctrlCode, repeat, data);
    }

    private void showCommandDialog(String content, OnCommCallback callback) {
        new CommRemindDialog()
                .content(content)
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        if (callback != null) {
                            callback.callback(null);
                        }
                    }
                }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
    }

    /**
     * 根据用户输入的数字转换为经纬度，存入缓存并显示在界面上
     * 做了阈值限定，并且在界面上显示的是正数
     */
    public void inputNumber(int num, int position) {
        switch (position) {
            case Sat_Longitude:
                if (mSatLongitude >= 0) {
                    mSatLongitude = 10 * mSatLongitude + num;
                } else if (mSatLongitude < 0) {
                    mSatLongitude = 10 * mSatLongitude - num;
                }

                if (mSatLongitude > 1800) {
                    mSatLongitude = num;
                } else if (mSatLongitude < -1800) {
                    mSatLongitude = -num;
                }
                mTvSatLongitude.setText((mSatLongitude >= 0) ? String.valueOf((mSatLongitude) / 10) : String.valueOf((-mSatLongitude) / 10));
                break;
            case Local_Longitude:
                if (mLocalLongitude >= 0) {
                    mLocalLongitude = 10 * mLocalLongitude + num;
                } else if (mLocalLongitude < 0) {
                    mLocalLongitude = 10 * mLocalLongitude - num;
                }

                if (mLocalLongitude > 1800) {
                    mLocalLongitude = num;
                } else if (mLocalLongitude < -1800) {
                    mLocalLongitude = -num;
                }
                mTvLocalLongitude.setText((mLocalLongitude >= 0) ? String.valueOf((mLocalLongitude) / 10) : String.valueOf((-mLocalLongitude) / 10));
                break;
            case Local_Latitude:
                if (mLocalLatitude >= 0) {
                    mLocalLatitude = 10 * mLocalLatitude + num;
                } else if (mLocalLatitude < 0) {
                    mLocalLatitude = 10 * mLocalLatitude - num;
                }

                if (mLocalLatitude > 900) {
                    mLocalLatitude = num;
                } else if (mLocalLatitude < -900) {
                    mLocalLatitude = -num;
                }
                mTvLocalLatitude.setText((mLocalLatitude >= 0) ? String.valueOf((mLocalLatitude) / 10) : String.valueOf((-mLocalLatitude) / 10));
                break;
            default:
                break;
        }
    }

    //发送延时消息，将Move Steps转为Stop
    private void sendStopMessage(int[] data) {
        switch (data[0]) {
            case 0xff:
                mMotorHandler.sendEmptyMessageDelayed(Stop_Move, Step_1_Time);
                break;
            case 0xfc:
                mMotorHandler.sendEmptyMessageDelayed(Stop_Move, Step_4_Time);
                break;
            case 0xf8:
                mMotorHandler.sendEmptyMessageDelayed(Stop_Move, Step_8_Time);
                break;
            case 0xe0:
                mMotorHandler.sendEmptyMessageDelayed(Stop_Move, Step_16_Time);
                break;
            case 0xd0:
                mMotorHandler.sendEmptyMessageDelayed(Stop_Move, Step_32_Time);
                break;
            case 1:
                mMotorHandler.sendEmptyMessageDelayed(Stop_Move, Second_1_Time);
                break;
            case 2:
                mMotorHandler.sendEmptyMessageDelayed(Stop_Move, Second_2_Time);
                break;
            case 3:
                mMotorHandler.sendEmptyMessageDelayed(Stop_Move, Second_3_Time);
                break;
            case 4:
                mMotorHandler.sendEmptyMessageDelayed(Stop_Move, Second_4_Time);
                break;
            default:
                mMotorHandler.sendEmptyMessageDelayed(Stop_Move, 0);
                break;
        }
    }

    private void saveSatMotorTypeInfo() {
        String motorType = mTvMotorType.getText().toString();
        if (TextUtils.equals(motorType, getString(R.string.motor_type_off))) {
            mSatInfo.diseqc12 = 0;
        } else if (TextUtils.equals(motorType, getString(R.string.motor_type_diseqc))) {
            mSatInfo.diseqc12 = 1;
        } else if (TextUtils.equals(motorType, getString(R.string.motor_type_usals))) {
            mSatInfo.diseqc12 = 2;
        }

        SWPDBaseManager.getInstance().setSatInfo(mSatelliteIndex, mSatInfo);  //将卫星Motor Type信息设置到对应的bean类中,保存更改的信息

    }

    private void saveSatPositionInfo() {
        SatInfo_t satInfo_t = SWPDBaseManager.getInstance().getSatList().get(mSatelliteIndex);
        satInfo_t.diseqc12_pos = mPositionStep;
        SWPDBaseManager.getInstance().setSatInfo(mSatelliteIndex, satInfo_t);  //将卫星Motor Type信息设置到对应的bean类中,保存更改的信息
        //将位置保存到SharedPreferences中，进入页面时写入Position
        PreferenceManager.getInstance().putInt(Constants.PrefsKey.SAVE_POSITION, satInfo_t.diseqc12_pos);
    }

    /**
     * motor TYPE  =USALS
     */
    private void selecteMotorTypeUsals() {
        mTvMotorType.setText(mTypeListArray[mMotorType]);
        mTvMotorType.requestFocus();
        mItemMoveStep.setVisibility(View.GONE);
        mItemStepSize.setVisibility(View.GONE);
        mItemPosition.setVisibility(View.VISIBLE);
        mItemDiSEqCCommand.setVisibility(View.VISIBLE);
        mTvDiSEqCCommandTitle.setText(getString(R.string.motor_usals_command_title));
        mTvDiSEqCCommand.setText(mUsalsCommandArray[0]);

        mLocalLongitudeLayout.setVisibility(View.VISIBLE);
        mTvSatLongitudeArrow.setText((mSatLongitude >= 0) ? "E" : "W");
        mTvSatLongitude.setText((mSatLongitude >= 0) ? String.valueOf((mSatLongitude) / 10) : String.valueOf((-mSatLongitude) / 10));
        mTvLocalLongitudeArrow.setText((mLocalLongitude >= 0) ? "E" : "W");
        mTvLocalLongitude.setText((mLocalLongitude >= 0) ? String.valueOf((mLocalLongitude) / 10) : String.valueOf((-mLocalLongitude) / 10));
        mTvLocalLatitudeArrow.setText((mLocalLatitude >= 0) ? "N" : "S");
        mTvLocalLatitude.setText((mLocalLatitude >= 0) ? String.valueOf((mLocalLatitude) / 10) : String.valueOf((-mLocalLatitude) / 10));
        saveSatMotorTypeInfo();
    }

    /**
     * motor TYPE  = DisEqc1.2
     */
    private void selecteMotorTypeDisEqc() {
        mTvMotorType.setText(mTypeListArray[mMotorType]);
        mItemMoveStep.setVisibility(View.VISIBLE);
        mItemStepSize.setVisibility(View.VISIBLE);
        mItemPosition.setVisibility(View.VISIBLE);
        mItemDiSEqCCommand.setVisibility(View.VISIBLE);
        mTvDiSEqCCommandTitle.setText(getString(R.string.motor_diseqc_command_title));
        mTvDiSEqCCommand.setText(mDiSEqCCommandArray[0]);
        mTvStepSize.setText(R.string.motor_continue);

        mLocalLongitudeLayout.setVisibility(View.GONE);
        saveSatMotorTypeInfo();
    }

    /**
     * motor TYPE  = OFF
     */
    private void selecteMotorTypeOff() {
        mTvMotorType.setText(mTypeListArray[mMotorType]);
        mItemMoveStep.setVisibility(View.GONE);
        mItemStepSize.setVisibility(View.GONE);
        mItemPosition.setVisibility(View.GONE);
        mItemDiSEqCCommand.setVisibility(View.GONE);
        mLocalLongitudeLayout.setVisibility(View.GONE);
        saveSatMotorTypeInfo();
    }

    /**
     * 获取TP数据
     */
    private void getTPName(int index) {
        if (mTpList == null || mTpList.size() == 0) {
            mTvTp.setText(getString(R.string.empty_tp));
            return;
        }
        ChannelNew_t channelNew_t = mTpList.get(index);
        String tp = channelNew_t.Freq + Utils.getVorH(this, channelNew_t.Qam) + channelNew_t.Symbol;
        mTvTp.setText(tp);
        SWFtaManager.getInstance().tunerLockFreq(mSatelliteIndex, channelNew_t.Freq, channelNew_t.Symbol, channelNew_t.Qam, 1, 0);
    }

    /**
     * TP 被选中
     */
    private void selecteMotorTp() {
        mItemTp.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        mIvTpLeft.setVisibility(View.VISIBLE);
        mTvTp.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mIvTpRight.setVisibility(View.VISIBLE);
    }

    /**
     * motor Type 被选中
     */
    private void selecteMotorType() {
        mItemMotorType.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        mIvMotorTypeLeft.setVisibility(View.VISIBLE);
        mTvMotorType.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mIvMotorTypeRight.setVisibility(View.VISIBLE);
    }

    /**
     * Move Step 被选中
     */
    private void selecteMoveSteps() {
        //Move Step
        mItemMoveStep.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        mIvMoveStepLeft.setVisibility(View.VISIBLE);
        mTvMoveStep.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mIvMoveStepRight.setVisibility(View.VISIBLE);
    }

    private void selecteStepSize() {
        mItemStepSize.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        mIvStepSizeLeft.setVisibility(View.VISIBLE);
        mTvStepSize.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mIvStepSizeRight.setVisibility(View.VISIBLE);
    }

    private void selectePosition() {
        mItemPosition.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        mIvPositionLeft.setVisibility(View.VISIBLE);
        mTvPosition.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mIvPositionRight.setVisibility(View.VISIBLE);
    }

    /**
     * command 被选中
     */
    private void selecteCommand() {
        //Command
        mItemDiSEqCCommand.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        mIvDiSEqCCommandLeft.setVisibility(View.VISIBLE);
        mTvDiSEqCCommand.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mIvDiSEqCCommandRight.setVisibility(View.VISIBLE);
    }


    /**
     * Sat Longitude 被选中
     */
    private void selecteSatLongitude() {
        mItemSatLongitude.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        mTvSatLongitudeArrow.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mTvSatLongitudeArrow.bringToFront();
        mTvSatLongitude.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mIvSatLongitudeRight.setVisibility(View.VISIBLE);
    }

    /**
     * Local Longitude 被选中
     */
    private void selecteLocalLongitude() {
        mItemLocalLongitude.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        mTvLocalLongitudeArrow.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mTvLocalLongitudeArrow.bringToFront();
        mTvLocalLongitude.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mIvLocalLongitudeRight.setVisibility(View.VISIBLE);
    }

    /**
     * Local Latitude 被选中
     */
    private void selecteLocalLatitude() {
        mItemLocalLatitude.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        mTvLocalLatitudeArrow.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mTvLocalLatitudeArrow.bringToFront();
        mTvLocalLatitude.setBackgroundResource(R.drawable.btn_red_bg_shape);
        mIvLocalLatitudeRight.setVisibility(View.VISIBLE);
    }

    /**
     * 遥控器上下键切换焦点之前，先隐藏所有背景，再显示焦点所在位置的背景
     */
    private void invisableAll() {
        mItemMotorType.setBackgroundColor(0);
        mIvMotorTypeLeft.setVisibility(View.INVISIBLE);
        mTvMotorType.setBackgroundColor(0);
        mIvMotorTypeRight.setVisibility(View.INVISIBLE);

        mItemTp.setBackgroundColor(0);
        mIvTpLeft.setVisibility(View.INVISIBLE);
        mTvTp.setBackgroundColor(0);
        mIvTpRight.setVisibility(View.INVISIBLE);

        mItemMoveStep.setBackgroundColor(0);
        mIvMoveStepLeft.setVisibility(View.INVISIBLE);
        mTvMoveStep.setBackgroundColor(0);
        mIvMoveStepRight.setVisibility(View.INVISIBLE);

        mItemStepSize.setBackgroundColor(0);
        mIvStepSizeLeft.setVisibility(View.INVISIBLE);
        mTvStepSize.setBackgroundColor(0);
        mIvStepSizeRight.setVisibility(View.INVISIBLE);

        mItemPosition.setBackgroundColor(0);
        mIvPositionLeft.setVisibility(View.INVISIBLE);
        mTvPosition.setBackgroundColor(0);
        mIvPositionRight.setVisibility(View.INVISIBLE);

        mItemDiSEqCCommand.setBackgroundColor(0);
        mIvDiSEqCCommandLeft.setVisibility(View.INVISIBLE);
        mTvDiSEqCCommand.setBackgroundColor(0);
        mIvDiSEqCCommandRight.setVisibility(View.INVISIBLE);

        mItemLocalLongitude.setBackgroundColor(0);
        mTvLocalLongitudeArrow.setBackgroundColor(0);
        mTvLocalLongitude.setBackgroundColor(0);
        mIvLocalLongitudeRight.setVisibility(View.INVISIBLE);

        mItemLocalLatitude.setBackgroundColor(0);
        mTvLocalLatitudeArrow.setBackgroundColor(0);
        mTvLocalLatitude.setBackgroundColor(0);
        mIvLocalLatitudeRight.setVisibility(View.INVISIBLE);

        mItemSatLongitude.setBackgroundColor(0);
        mTvSatLongitudeArrow.setBackgroundColor(0);
        mTvSatLongitude.setBackgroundColor(0);
        mIvSatLongitudeRight.setVisibility(View.INVISIBLE);
    }

    private static class MotorCtrlModel {
        String title;
        int ctrlCode;
        int repeat;
        int[] data;

        MotorCtrlModel(int ctrlCode, int repeat, int[] data) {
            this("", ctrlCode, repeat, data);
        }

        MotorCtrlModel(String title, int ctrlCode, int repeat, int[] data) {
            this.title = title;
            this.ctrlCode = ctrlCode;
            this.repeat = repeat;
            this.data = data;
        }
    }
}
