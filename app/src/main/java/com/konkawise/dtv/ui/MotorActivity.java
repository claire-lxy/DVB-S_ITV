package com.konkawise.dtv.ui;

import android.os.Message;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.PreferenceManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.WeakToolManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommRemindDialog;
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

import static java.lang.Math.abs;
import static java.lang.Math.acos;
import static java.lang.Math.cos;
import static java.lang.Math.sqrt;


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
    static final int Step_32_Time = 500;
    static final int Step_16_Time = 250;
    static final int Step_8_Time = 125;
    static final int Step_4_Time = 68;
    static final int Step_1_Time = 8;
    static final int Second_4_Time = 4000;
    static final int Second_3_Time = 3000;
    static final int Second_2_Time = 2000;
    static final int Second_1_Time = 1000;
    static final int Stop_Move = 1;

    @BindView(R.id.imgeview_satellite_left)
    ImageView mImgeview_satellite_left;

    @BindView(R.id.tv_satellite)
    TextView mTv_satellite;

    @BindView(R.id.imgeview_satellite_right)
    ImageView mImgeview_satellite_right;

    @BindView(R.id.rl_satellite)
    RelativeLayout mRl_satellite;

    @BindView(R.id.imgeview_motor_type_left)
    ImageView imgeview_motor_type_left;

    @BindView(R.id.tv_motor_type)
    TextView tv_motor_type;

    @BindView(R.id.imgeview_motor_type_right)
    ImageView imgeview_motor_type_right;

    @BindView(R.id.ll_motor_type)
    RelativeLayout ll_motor_type;

    @BindView(R.id.image_tp_left)
    ImageView image_tp_left;

    @BindView(R.id.tv_tp)
    TextView tv_tp;

    @BindView(R.id.image_tp_right)
    ImageView image_tp_right;

    @BindView(R.id.ll_tp_root)
    RelativeLayout ll_tp_root;

    @BindView(R.id.tv_move_steps)
    TextView tv_move_steps;

    @BindView(R.id.image_step_left)
    ImageView mImage_step_left;

    @BindView(R.id.tv_step)
    TextView tv_step;

    @BindView(R.id.image_step_right)
    ImageView image_step_right;

    @BindView(R.id.ll_step_root)
    RelativeLayout ll_step_root;

    @BindView(R.id.tv_step_size)
    TextView tv_step_size;

    @BindView(R.id.image_step_size_left)
    ImageView image_step_size_left;

    @BindView(R.id.image_step_size_right)
    ImageView image_step_size_right;

    @BindView(R.id.ll_step_size_root)
    RelativeLayout ll_step_size_root;

    @BindView(R.id.tv_position1)
    TextView tv_position1;

    @BindView(R.id.image_position_left)
    ImageView image_position_left;

    @BindView(R.id.tv_position)
    TextView tv_position;

    @BindView(R.id.image_position_right)
    ImageView image_position_right;

    @BindView(R.id.ll_sat_position_root)
    RelativeLayout ll_sat_position_root;

    @BindView(R.id.tv_diseqc_common)
    TextView tv_diseqc_common;

    @BindView(R.id.image_command_left)
    ImageView image_command_left;

    @BindView(R.id.tv_command)
    TextView tv_command;

    @BindView(R.id.image_command_right)
    ImageView image_command_right;

    @BindView(R.id.ll_sat_command_root)
    RelativeLayout ll_sat_command_root;


    @BindArray(R.array.step_size)
    String[] mStepSize;

    @BindArray(R.array.diseqc_command)
    String[] mDISEqcCommand;

    @BindArray(R.array.command)
    String[] mCommand;

    @BindArray(R.array.type_list)
    String[] typeList;

    @BindArray(R.array.move_step_list)
    String[] moveStepList;

    @BindArray(R.array.step_size_data)
    int[] StepSizeData;

    @BindView(R.id.tv_progress_strength)
    TextView mTv_edit_progress_i;

    @BindView(R.id.pb_strength)
    ProgressBar progress_edit_i;

    @BindView(R.id.tv_progress_quality)
    TextView mTv_edit_progress_q;

    @BindView(R.id.pb_quality)
    ProgressBar progress_edit_q;

    @BindView(R.id.ll_local_longitude_latitude)
    LinearLayout ll_local_longitude_latitude;

    @BindView(R.id.rl_local_longitude_root)
    RelativeLayout rl_local_longitude_root;

    @BindView(R.id.local_longitude)
    TextView local_longitude;

    @BindView(R.id.tv_local_longitude)
    TextView tv_local_longitude;

    @BindView(R.id.et_local_longitude)
    TextView et_local_longitude;

    @BindView(R.id.image_local_longitude_right)
    ImageView image_local_longitude_right;

    @BindView(R.id.rl_local_latitude_root)
    RelativeLayout rl_local_latitude_root;

    @BindView(R.id.local_latitude)
    TextView local_latitude;

    @BindView(R.id.tv_local_latitude)
    TextView tv_local_latitude;

    @BindView(R.id.et_local_latitude)
    TextView et_local_latitude;

    @BindView(R.id.image_local_latitude_right)
    ImageView image_local_latitude_right;

    @BindView(R.id.rl_sat_longitude_root)
    RelativeLayout rl_sat_longitude_root;

    @BindView(R.id.sat_longitude)
    TextView sat_longitude;

    @BindView(R.id.tv_sat_longitude)
    TextView tv_sat_longitude;

    @BindView(R.id.et_sat_longitude)
    TextView et_sat_longitude;

    @BindView(R.id.image_sat_longitude_right)
    ImageView image_sat_longitude_right;

    int[] data;
    private int position = 1;

    private int mCurrntTp;
    private int currnt;
    private String satName;
    private String tpName;
    private List<ChannelNew_t> mTpList;
    private String mTpName;
    private int mMotorType = 0;

    private int mMoveStep = 0;
    private int mSetpSizeStep = 0;
    private int mPositionStep = 1;//Position 1到Position 51
    private int mCommandStep = 0;
    private int mDISEqcCommandStep = 0;
    private CheckSignalHelper mCheckSignalHelper;
    private MotorHandler mMotorHandler;
    private MotorRunnable mMotorRunnable;
    private SatInfo_t satInfo_t;

    private double mSatLongitude;
    private double mLocalLongitude;
    private double mLocalLatitude;

    @Override
    public int getLayoutId() {
        return R.layout.activity_mortor;
    }

    @Override
    protected void setup() {
        //控制Motor执行转动
        data = new int[1];
        initMotorUi();
        initCheckSignal();
        mMotorHandler = new MotorHandler(this);
        //锁EditManualActivity传进来的频点
        if (mTpList != null && mTpList.size() != 0) {
            SWFtaManager.getInstance().tunerLockFreq(currnt, mTpList.get(mCurrntTp).Freq, mTpList.get(mCurrntTp).Symbol, mTpList.get(mCurrntTp).Qam, 1, 0);
        }
        initMotorRunnable();
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
        SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_STOP, 0, new int[]{0});
        sendStopMessage(new int[]{0});
        //退出时保存经纬度
        satInfo_t.diseqc12_longitude = (int) mSatLongitude;
        SWPDBaseManager.getInstance().setSatInfo(currnt, satInfo_t);
        SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Longitude.ordinal(), (int)mLocalLongitude);
        SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Latitude.ordinal(), (int) mLocalLatitude);

        ThreadPoolManager.getInstance().remove(mMotorRunnable);
        WeakToolManager.getInstance().removeWeakTool(this);
    }

    private void initCheckSignal() {
        mCheckSignalHelper = new CheckSignalHelper(this);
        mCheckSignalHelper.setOnCheckSignalListener(new CheckSignalHelper.OnCheckSignalListener() {
            @Override
            public void signal(int strength, int quality) {
                String strengthPercent = strength + "%";
                mTv_edit_progress_i.setText(strengthPercent);
                progress_edit_i.setProgress(strength);

                String qualityPercent = quality + "%";
                mTv_edit_progress_q.setText(qualityPercent);
                progress_edit_q.setProgress(quality);
            }
        });
    }

    private void initMotorRunnable() {
        mMotorRunnable = new MotorRunnable(this);
    }

    private void initMotorUi() {
        satName = getIntent().getStringExtra(Constants.IntentKey.INTENT_SATELLITE_NAME);
        tpName = getIntent().getStringExtra(Constants.IntentKey.INTENT_TP_NAME);
        mCurrntTp = getIntent().getIntExtra(Constants.IntentKey.INTENT_CURRNT_TP, -1);
        currnt = getIntent().getIntExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, -1);
        satInfo_t = SWPDBaseManager.getInstance().getSatList().get(currnt);
        mTpList = SWPDBaseManager.getInstance().getSatChannelInfoList(currnt);

        mSatLongitude = satInfo_t.diseqc12_longitude > 1800 ? (satInfo_t.diseqc12_longitude - 3600) : satInfo_t.diseqc12_longitude;
        mLocalLongitude = (SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Longitude.ordinal())) > 1800 ?
                (SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Longitude.ordinal()) - 3600) :
                SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Longitude.ordinal());
        mLocalLatitude = (SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Latitude.ordinal())) > 900 ?
                (SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Latitude.ordinal()) - 1800) :
                SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Latitude.ordinal());

        tv_step.setText(moveStepList[0]);
        mTv_satellite.setText(satName);
        tv_tp.setText(tpName);
        tv_step_size.setText(mStepSize[0]);
        tv_position.bringToFront();

        //取出上一次存的position
        mPositionStep = PreferenceManager.getInstance().getInt(Constants.PrefsKey.SAVE_POSITION);
        tv_position.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
    }

    private static class MotorHandler extends WeakHandler<MotorActivity> {

        public MotorHandler(MotorActivity view) {
            super(view);
        }

        @Override
        protected void handleMsg(Message msg) {
            MotorActivity context = mWeakReference.get();
            switch (msg.what) {
                case Stop_Move:
                    context.moveStop();
            }

        }
    }

    private static class MotorRunnable extends WeakRunnable<MotorActivity> {

        public MotorRunnable(MotorActivity view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            MotorActivity context = mWeakReference.get();
            switch (context.moveStepList[context.mMoveStep]) {
                case "Stop":
                    SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_STOP, 0, new int[]{0});
                    break;
                case "West":
                    if (context.data[0] == 0) { //data[0] == 0 Continue持续转动
                        SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_RIGHT_CONTINUE, 0, context.data);
                    } else {
                        SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_RIGHT_STEP, 0, context.data);
                        context.sendStopMessage(context.data);
                    }
                    break;
                case "East":
                    if (context.data[0] == 0) {
                        SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_LEFT_CONTINUE, 0, context.data);

                    } else {
                        SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_LEFT_STEP, 0, context.data);
                        context.sendStopMessage(context.data);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    private void moveStop() {
        mMoveStep = 0;
        tv_step.setText(moveStepList[mMoveStep]);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            invisiableAll();
            if (mMotorType == OFF) {
                switch (position) {
                    case TP:
                        --position;
                        selecteMotorType();
                        break;
                    case Motor_Type:
                        selecteMotorType();
                        break;
                    default:
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
                    default:
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
                    default:
                        break;
                }
            }

            Log.e(TAG, "Position: " + position);

        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {

            invisiableAll();
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
                    default:
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
                    default:
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
                    default:
                        break;
                }
            }
            Log.e(TAG, "Position: " + position);

        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {

            if (mMotorType == OFF) {
                switch (position) {
                    case Motor_Type:
                        --mMotorType;
                        if (mMotorType < 0) {
                            mMotorType = typeList.length - 1;
                        }
                        selecteMotorTypeUsals();
                        break;
                    case TP:
                        //控制Tp栏左键
                        --mCurrntTp;
                        Log.e("left= ", mCurrntTp + "");
                        if (mCurrntTp < 0) {
                            mCurrntTp = mTpList.size() - 1;
                        }
                        getTPName(mCurrntTp);
                        break;
                    default:
                        break;
                }
            } else if (mMotorType == USALS) {
                switch (position) {
                    case Motor_Type:
                        --mMotorType;
                        if (mMotorType < 0) {
                            mMotorType = typeList.length - 1;
                        }
                        selecteMotorTypeDisEqc();
                        break;
                    case TP:
                        --mCurrntTp;
                        if (mCurrntTp < 0) {
                            mCurrntTp = mTpList.size() - 1;
                        }
                        getTPName(mCurrntTp);
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
                        tv_position.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
                        break;
                    case Command:
                        --mCommandStep;
                        if (mCommandStep < 0) {
                            mCommandStep = mCommand.length - 1;
                        }
                        tv_command.setText(mCommand[mCommandStep]);
                        break;
                    default:
                        break;
                }
            } else if (mMotorType == DisEqc) {
                switch (position) {
                    case Motor_Type:
                        --mMotorType;
                        if (mMotorType < 0) {
                            mMotorType = typeList.length - 1;
                        }
                        selecteMotorTypeOff();
                        break;
                    case TP:
                        --mCurrntTp;
                        if (mCurrntTp < 0) {
                            mCurrntTp = mTpList.size() - 1;
                        }
                        getTPName(mCurrntTp);
                        break;
                    case Move_Steps:
                        --mMoveStep;
                        if (mMoveStep < 0) {
                            mMoveStep = moveStepList.length - 1;
                        }
                        tv_step.setText(moveStepList[mMoveStep]);
                        //控制马达转动，异步处理防止ANR
                        data[0] = StepSizeData[mSetpSizeStep];
                        ThreadPoolManager.getInstance().remove(mMotorRunnable);
                        ThreadPoolManager.getInstance().execute(mMotorRunnable);
                        break;
                    case Step_Size:
                        --mSetpSizeStep;
                        if (mSetpSizeStep < 0) {
                            mSetpSizeStep = mStepSize.length - 1;
                        }
                        tv_step_size.setText(mStepSize[mSetpSizeStep]);
                        break;
                    case Position_Dis:
                        --mPositionStep;
                        if (mPositionStep < 1) {
                            mPositionStep = 51;
                        }
                        tv_position.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
                        break;
                    case DisEqc_Command:
                        --mDISEqcCommandStep;
                        if (mDISEqcCommandStep < 0) {
                            mDISEqcCommandStep = mDISEqcCommand.length - 1;
                        }
                        tv_command.setText(mDISEqcCommand[mDISEqcCommandStep]);
                        break;
                    default:
                        break;
                }
            }
        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (mMotorType == OFF) {
                switch (position) {
                    case Motor_Type:
                        ++mMotorType;
                        if (mMotorType > typeList.length - 1) {
                            mMotorType = DisEqc;
                        }
                        selecteMotorTypeDisEqc();
                        break;
                    case TP:
                        //控制Tp栏左键
                        ++mCurrntTp;
                        if (mCurrntTp > mTpList.size() - 1) {
                            mCurrntTp = 0;
                        }
                        getTPName(mCurrntTp);
                        break;
                    default:
                        break;
                }
            } else if (mMotorType == USALS) {
                switch (position) {
                    case Motor_Type:
                        ++mMotorType;
                        if (mMotorType > typeList.length - 1) {
                            mMotorType = DisEqc;
                        }
                        selecteMotorTypeOff();
                        break;
                    case TP:
                        ++mCurrntTp;
                        if (mCurrntTp > mTpList.size() - 1) {
                            mCurrntTp = 0;
                        }
                        getTPName(mCurrntTp);
                        break;
                    case Sat_Longitude:
                        tv_sat_longitude.setText(tv_sat_longitude.getText().toString().equals("W") ? "E" : "W");
                        mSatLongitude = -mSatLongitude;
                        break;
                    case Local_Longitude:
                        tv_local_longitude.setText(tv_local_longitude.getText().toString().equals("W") ? "E" : "W");
                        mLocalLongitude = -mLocalLongitude;
                        break;
                    case Local_Latitude:
                        tv_local_latitude.setText(tv_local_latitude.getText().toString().equals("S") ? "N" : "S");
                        mLocalLatitude = -mLocalLatitude;
                        break;
                    case Position:
                        ++mPositionStep;
                        if (mPositionStep > 51) {
                            mPositionStep = 1;
                        }
                        tv_position.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
                        break;
                    case Command:
                        ++mCommandStep;
                        if (mCommandStep > mCommand.length - 1) {
                            mCommandStep = 0;
                        }
                        tv_command.setText(mCommand[mCommandStep]);
                        break;
                    default:
                        break;
                }
            } else if (mMotorType == DisEqc) {
                switch (position) {
                    case Motor_Type:
                        ++mMotorType;
                        if (mMotorType > typeList.length - 1) {
                            mMotorType = DisEqc;
                        }
                        selecteMotorTypeUsals();
                        break;
                    case TP:
                        ++mCurrntTp;
                        if (mCurrntTp > mTpList.size() - 1) {
                            mCurrntTp = 0;
                        }
                        getTPName(mCurrntTp);
                        break;
                    case Move_Steps:
                        ++mMoveStep;
                        if (mMoveStep > moveStepList.length - 1) {
                            mMoveStep = 0;
                        }
                        tv_step.setText(moveStepList[mMoveStep]);
                        data[0] = StepSizeData[mSetpSizeStep];
                        ThreadPoolManager.getInstance().remove(mMotorRunnable);
                        ThreadPoolManager.getInstance().execute(mMotorRunnable);
                        break;
                    case Step_Size:
                        ++mSetpSizeStep;
                        if (mSetpSizeStep > mStepSize.length - 1) {
                            mSetpSizeStep = 0;
                        }
                        tv_step_size.setText(mStepSize[mSetpSizeStep]);
                        break;
                    case Position_Dis:
                        ++mPositionStep;
                        if (mPositionStep > 51) {
                            mPositionStep = 1;
                        }
                        tv_position.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
                        break;
                    case DisEqc_Command:
                        ++mDISEqcCommandStep;
                        if (mDISEqcCommandStep > mDISEqcCommand.length - 1) {
                            mDISEqcCommandStep = 0;
                        }
                        tv_command.setText(mDISEqcCommand[mDISEqcCommandStep]);
                        break;
                    default:
                        break;
                }
            }
        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((mMotorType == DisEqc) && ((position == DisEqc_Command))) {
                switch (tv_command.getText().toString()) {
                    case "Save Position":
                        showDialog(getString(R.string.dialog_save_position), new PositiveCallback() {
                            @Override
                            public void onConfirmCallback() {
                                saveSatMotorTypeInfo();
                                saveSatPositionInfo();
                            }
                        });

                        break;
                    case "(Re-)Calculate":
                        showDialog(getString(R.string.dialog_calculate), new PositiveCallback() {
                            @Override
                            public void onConfirmCallback() {
                                SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_RECALCULATE, 0, new int[]{0});
                            }
                        });

                        break;
                    case "Disable Limit":
                        showDialog(getString(R.string.dialog_disable_limit), new PositiveCallback() {
                            @Override
                            public void onConfirmCallback() {
                                SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_DISABLE_LIMIT, 0, new int[]{0});
                            }
                        });

                        break;
                    case "East Limit":
                        showDialog(getString(R.string.dialog_east_limit), new PositiveCallback() {
                            @Override
                            public void onConfirmCallback() {
                                SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_EAST_LIMIT, 0, new int[]{0});
                            }
                        });

                        break;
                    case "West Limit":
                        showDialog(getString(R.string.dialog_west_limit), new PositiveCallback() {
                            @Override
                            public void onConfirmCallback() {
                                SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_WEST_LIMIT, 0, new int[]{0});
                            }
                        });

                        break;
                    case "Goto Ref":
                        showDialog(getString(R.string.dialog_goto_ref), new PositiveCallback() {
                            @Override
                            public void onConfirmCallback() {
                                SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_GO_REFERENCE, 0, new int[]{0});
                            }
                        });

                        break;
                    default:
                        break;

                }
            } else if ((mMotorType == USALS) && (position == Command)) {
                switch (tv_command.getText().toString()) {
                    case "Goto XX":
                        showDialog(getString(R.string.dialog_goto_xx), new PositiveCallback() {
                            @Override
                            public void onConfirmCallback() {

                                calcxxDegree(mSatLongitude, mLocalLongitude, mLocalLatitude);

                            }
                        });

                        break;
                    case "Save Pos":
                        showDialog(getString(R.string.dialog_save_pos), new PositiveCallback() {
                            @Override
                            public void onConfirmCallback() {
                                saveSatPositionInfo();
                            }
                        });

                        break;
                    case "Calculate":
                        showDialog(getString(R.string.dialog_commannd_calculate), new PositiveCallback() {
                            @Override
                            public void onConfirmCallback() {
                                SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_RECALCULATE, 0, new int[]{0});
                            }
                        });

                        break;
                    case "Shift":
                        showDialog(getString(R.string.dialog_shift), new PositiveCallback() {
                            @Override
                            public void onConfirmCallback() {
                                SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_USAL_SHIFT, 0, new int[]{0});
                            }
                        });

                    case "Goto Ref":
                        showDialog(getString(R.string.dialog_command_goto_ref), new PositiveCallback() {
                            @Override
                            public void onConfirmCallback() {
                                SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_GO_REFERENCE, 0, new int[]{0});
                            }
                        });

                        break;

                    default:
                        break;

                }
            }
        }
        if ((keyCode == KeyEvent.KEYCODE_0) && (ll_local_longitude_latitude.getVisibility() == View.VISIBLE)) {
            inputNumber(0, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_1) && (ll_local_longitude_latitude.getVisibility() == View.VISIBLE)) {
            inputNumber(1, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_2) && (ll_local_longitude_latitude.getVisibility() == View.VISIBLE)) {
            inputNumber(2, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_3) && (ll_local_longitude_latitude.getVisibility() == View.VISIBLE)) {
            inputNumber(3, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_4) && (ll_local_longitude_latitude.getVisibility() == View.VISIBLE)) {
            inputNumber(4, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_5) && (ll_local_longitude_latitude.getVisibility() == View.VISIBLE)) {
            inputNumber(5, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_6) && (ll_local_longitude_latitude.getVisibility() == View.VISIBLE)) {
            inputNumber(6, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_7) && (ll_local_longitude_latitude.getVisibility() == View.VISIBLE)) {
            inputNumber(7, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_8) && (ll_local_longitude_latitude.getVisibility() == View.VISIBLE)) {
            inputNumber(8, position);
            return true;
        }

        if ((keyCode == KeyEvent.KEYCODE_9) && (ll_local_longitude_latitude.getVisibility() == View.VISIBLE)) {
            inputNumber(9, position);
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void showDialog(String content, PositiveCallback callback) {
        new CommRemindDialog()
                .content(content)
                .setOnPositiveListener("", new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        if (callback != null) {
                            callback.onConfirmCallback();
                        }
                    }
                }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
    }

    public interface PositiveCallback {
        void onConfirmCallback();
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
                et_sat_longitude.setText((mSatLongitude >= 0) ? String.valueOf((mSatLongitude) / 10) : String.valueOf((-mSatLongitude) / 10));
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
                et_local_longitude.setText((mLocalLongitude >= 0) ? String.valueOf((mLocalLongitude) / 10) : String.valueOf((-mLocalLongitude) / 10));
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
                et_local_latitude.setText((mLocalLatitude >= 0) ? String.valueOf((mLocalLatitude) / 10) : String.valueOf((-mLocalLatitude) / 10));
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
        switch (tv_motor_type.getText().toString()) {
            case "OFF":
                satInfo_t.diseqc12 = 0;
                break;
            case "DisEqc1.2":
                satInfo_t.diseqc12 = 1;
                break;
            case "USALS":
                satInfo_t.diseqc12 = 2;
                break;
            default:
                break;
        }

        SWPDBaseManager.getInstance().setSatInfo(currnt, satInfo_t);  //将卫星Motor Type信息设置到对应的bean类中,保存更改的信息

    }

    private void saveSatPositionInfo() {
        SatInfo_t satInfo_t = SWPDBaseManager.getInstance().getSatList().get(currnt);
        satInfo_t.diseqc12_pos = mPositionStep;
        SWPDBaseManager.getInstance().setSatInfo(currnt, satInfo_t);  //将卫星Motor Type信息设置到对应的bean类中,保存更改的信息
        //将位置保存到SharedPreferences中，进入页面时写入Position
        PreferenceManager.getInstance().putInt(Constants.PrefsKey.SAVE_POSITION, satInfo_t.diseqc12_pos);
    }

    /**
     * motor TYPE  =USALS
     */
    private void selecteMotorTypeUsals() {
        tv_motor_type.setText(typeList[mMotorType]);
        tv_motor_type.requestFocus();
        ll_step_root.setVisibility(View.GONE);
        ll_step_size_root.setVisibility(View.GONE);
        ll_sat_position_root.setVisibility(View.VISIBLE);
        ll_sat_command_root.setVisibility(View.VISIBLE);
        tv_diseqc_common.setText("Command");
        tv_command.setText(mCommand[0]);

        ll_local_longitude_latitude.setVisibility(View.VISIBLE);
        tv_sat_longitude.setText((mSatLongitude >= 0) ? "E" : "W");
        et_sat_longitude.setText((mSatLongitude >= 0) ? String.valueOf((mSatLongitude) / 10) : String.valueOf((-mSatLongitude) / 10));
        tv_local_longitude.setText((mLocalLongitude >= 0) ? "E" : "W");
        et_local_longitude.setText((mLocalLongitude >= 0) ? String.valueOf((mLocalLongitude) / 10) : String.valueOf((-mLocalLongitude) / 10));
        tv_local_latitude.setText((mLocalLatitude >= 0) ? "N" : "S");
        et_local_latitude.setText((mLocalLatitude >= 0) ? String.valueOf((mLocalLatitude) / 10) : String.valueOf((-mLocalLatitude) / 10));
        saveSatMotorTypeInfo();
    }

    /**
     * motor TYPE  = DisEqc1.2
     */
    private void selecteMotorTypeDisEqc() {
        tv_motor_type.setText(typeList[mMotorType]);
        ll_step_root.setVisibility(View.VISIBLE);
        tv_move_steps.setText("Move Steps");
        ll_step_size_root.setVisibility(View.VISIBLE);
        ll_sat_position_root.setVisibility(View.VISIBLE);
        ll_sat_command_root.setVisibility(View.VISIBLE);
        tv_diseqc_common.setText("DisEqc Command");
        tv_command.setText(mDISEqcCommand[0]);
        tv_step_size.setText(R.string.motor_continue);

        ll_local_longitude_latitude.setVisibility(View.GONE);
        saveSatMotorTypeInfo();
    }

    /**
     * motor TYPE  = OFF
     */
    private void selecteMotorTypeOff() {
        tv_motor_type.setText(typeList[mMotorType]);
        ll_step_root.setVisibility(View.GONE);
        ll_step_size_root.setVisibility(View.GONE);
        ll_sat_position_root.setVisibility(View.GONE);
        ll_sat_command_root.setVisibility(View.GONE);
        ll_local_longitude_latitude.setVisibility(View.GONE);
        saveSatMotorTypeInfo();
    }

    /**
     * 获取TP数据
     */
    private void getTPName(int index) {
        if (mTpList == null || mTpList.size() == 0) {
            tv_tp.setText("");
            return;
        }
        ChannelNew_t channelNew_t = mTpList.get(index);
        mTpName = channelNew_t.Freq + Utils.getVorH(this, channelNew_t.Qam) + channelNew_t.Symbol;
        Log.e("tpname1", mTpName);
        tv_tp.setText(mTpName);
        SWFtaManager.getInstance().tunerLockFreq(currnt, channelNew_t.Freq, channelNew_t.Symbol, channelNew_t.Qam, 1, 0);
    }


    public int calcxxDegree(double sat_long, double loc_long, double loc_lat) {
        double RATIO = 6.619;    //Ratio Value of Orbit's Radius and Earth's Radius
        double PI = 3.1415926535;
        double DEGREE = (PI / 180);
        double sat_longitude = ((double) sat_long) / 10;
        double loc_longitude = ((double) loc_long) / 10;
        double a = 0;//different angle
        double b = ((double) loc_lat) / 10; //latitude
        double cos_b = cos(b * DEGREE);
        double cos_a = 0;
        double two_acosb = 2 * RATIO * cos_b;
        double denominator;
        double numerator;
        double diff_angle;

        int flag = 0;
        if (abs(sat_long) > 1800 || abs(loc_long) > 1800 || abs(loc_lat) > 900)
            return 1;
        a = abs(sat_longitude - loc_longitude);
        if (a > 180 && a <= 360)//180~360
        {
            a = 360 - a;
            flag = 1;
        }
        cos_a = cos(a * DEGREE);
        denominator = sqrt((1 + RATIO * RATIO - two_acosb) * (1 + RATIO * RATIO - two_acosb * cos_a));
        numerator = 1 + RATIO * RATIO * cos_a - RATIO * cos_b * cos_a - RATIO * cos_b;
        diff_angle = acos(numerator / denominator) / DEGREE;
        if (diff_angle > 180)
            return 1;
        double pAngleDiff = (diff_angle * 100 + 5) / 10;//Calculate a round number
        if (loc_lat >= 0)    //Northern Hemisphere
        {
            if (((sat_long - loc_long) > 0 && (flag == 0)) || ((sat_long - loc_long) < 0 && (flag == 1))) {
                pAngleDiff = -pAngleDiff;
            }
        } else        //Southern Hemisphere
        {
            if (((sat_long - loc_long) > 0 && (flag == 1)) || ((sat_long - loc_long) < 0 && (flag == 0))) {
                pAngleDiff = -pAngleDiff;
            }
        }
        SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_USAL_GOTOXX, 0, new int[]{(int) pAngleDiff});
        return 0;
    }

    /**
     * TP 被选中
     */
    private void selecteMotorTp() {
        ll_tp_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        image_tp_left.setVisibility(View.VISIBLE);
        tv_tp.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_tp_right.setVisibility(View.VISIBLE);
    }

    /**
     * motor Type 被选中
     */
    private void selecteMotorType() {
        ll_motor_type.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        imgeview_motor_type_left.setVisibility(View.VISIBLE);
        tv_motor_type.setBackgroundResource(R.drawable.btn_red_bg_shape);
        imgeview_motor_type_right.setVisibility(View.VISIBLE);
    }

    /**
     * Move Step 被选中
     */
    private void selecteMoveSteps() {
        //Move Step
        ll_step_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        mImage_step_left.setVisibility(View.VISIBLE);
        tv_step.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_step_right.setVisibility(View.VISIBLE);
    }

    private void selecteStepSize() {
        ll_step_size_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        image_step_size_left.setVisibility(View.VISIBLE);
        tv_step_size.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_step_size_right.setVisibility(View.VISIBLE);
    }

    private void selectePosition() {
        ll_sat_position_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        image_position_left.setVisibility(View.VISIBLE);
        tv_position.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_position_right.setVisibility(View.VISIBLE);
    }

    /**
     * command 被选中
     */
    private void selecteCommand() {
        //Command
        ll_sat_command_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        image_command_left.setVisibility(View.VISIBLE);
        tv_command.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_command_right.setVisibility(View.VISIBLE);
    }


    /**
     * Sat Longitude 被选中
     */
    private void selecteSatLongitude() {
        rl_sat_longitude_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        tv_sat_longitude.setBackgroundResource(R.drawable.btn_red_bg_shape);
        tv_sat_longitude.bringToFront();
        et_sat_longitude.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_sat_longitude_right.setVisibility(View.VISIBLE);
    }

    /**
     * Local Longitude 被选中
     */
    private void selecteLocalLongitude() {
        rl_local_longitude_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        tv_local_longitude.setBackgroundResource(R.drawable.btn_red_bg_shape);
        tv_local_longitude.bringToFront();
        et_local_longitude.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_local_longitude_right.setVisibility(View.VISIBLE);
    }

    /**
     * Local Latitude 被选中
     */
    private void selecteLocalLatitude() {
        rl_local_latitude_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        tv_local_latitude.setBackgroundResource(R.drawable.btn_red_bg_shape);
        tv_local_latitude.bringToFront();
        et_local_latitude.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_local_latitude_right.setVisibility(View.VISIBLE);
    }

    /**
     * 遥控器上下键切换焦点之前，先隐藏所有背景，再显示焦点所在位置的背景
     */
    private void invisiableAll() {
        ll_motor_type.setBackgroundColor(0);
        imgeview_motor_type_left.setVisibility(View.INVISIBLE);
        tv_motor_type.setBackgroundColor(0);
        imgeview_motor_type_right.setVisibility(View.INVISIBLE);

        ll_tp_root.setBackgroundColor(0);
        image_tp_left.setVisibility(View.INVISIBLE);
        tv_tp.setBackgroundColor(0);
        image_tp_right.setVisibility(View.INVISIBLE);

        //Move Step
        ll_step_root.setBackgroundColor(0);
        mImage_step_left.setVisibility(View.INVISIBLE);
        tv_step.setBackgroundColor(0);
        image_step_right.setVisibility(View.INVISIBLE);

        //Step Size
        ll_step_size_root.setBackgroundColor(0);
        image_step_size_left.setVisibility(View.INVISIBLE);
        tv_step_size.setBackgroundColor(0);
        image_step_size_right.setVisibility(View.INVISIBLE);

        //position
        ll_sat_position_root.setBackgroundColor(0);
        image_position_left.setVisibility(View.INVISIBLE);
        tv_position.setBackgroundColor(0);
        image_position_right.setVisibility(View.INVISIBLE);

        //Command
        ll_sat_command_root.setBackgroundColor(0);
        image_command_left.setVisibility(View.INVISIBLE);
        tv_command.setBackgroundColor(0);
        image_command_right.setVisibility(View.INVISIBLE);

        rl_local_longitude_root.setBackgroundColor(0);
        tv_local_longitude.setBackgroundColor(0);
        et_local_longitude.setBackgroundColor(0);
        image_local_longitude_right.setVisibility(View.INVISIBLE);

        rl_local_latitude_root.setBackgroundColor(0);
        tv_local_latitude.setBackgroundColor(0);
        et_local_latitude.setBackgroundColor(0);
        image_local_latitude_right.setVisibility(View.INVISIBLE);

        rl_sat_longitude_root.setBackgroundColor(0);
        tv_sat_longitude.setBackgroundColor(0);
        et_sat_longitude.setBackgroundColor(0);
        image_sat_longitude_right.setVisibility(View.INVISIBLE);
    }

}
