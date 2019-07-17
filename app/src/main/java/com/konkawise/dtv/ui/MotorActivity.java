package com.konkawise.dtv.ui;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konkawise.dtv.PreferenceManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.WeakToolManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;

import com.konkawise.dtv.utils.LogUtils;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.CheckSignalHelper;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.konkawise.dtv.weaktool.WeakToolInterface;
import com.sw.dvblib.SWFta;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.ChannelNew_t;
import vendor.konka.hardware.dtvmanager.V1_0.HMotorCtrlCode;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

import static vendor.konka.hardware.dtvmanager.V1_0.HMotorCtrlCode.DIRECT_LEFT_STEP;

/**
 * motor界面
 */

public class MotorActivity extends BaseActivity {
    private static final String TAG = "MotorActivity";
    //MotorType为DisEqc1.2时，当前焦点位置，即position,但切换MotorType，会改变数值代表的意义
    private static final int DisEqc_Command = 6;
    private static final int Position = 5;
    private static final int Step_Size = 4;
    private static final int Move_Steps = 3;
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

    @BindView(R.id.image_Longitude_left)
    ImageView image_Longitude_left;

    @BindView(R.id.tv_Longitude)
    TextView tv_Longitude;

    @BindView(R.id.image_Longitude_right)
    ImageView image_Longitude_right;

    @BindView(R.id.ll_sat_longitude_root)
    RelativeLayout ll_sat_longitude_root;

    @BindView(R.id.tv_position1)
    TextView tv_position1;

    @BindView(R.id.image_position_left)
    ImageView image_position_left;

    @BindView(R.id.tv_position)
    TextView tv_position;

    @BindView(R.id.tv_position_bg)
    TextView tv_position_bg;

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

    @BindView(R.id.tv_edit_progress_i)
    TextView mTv_edit_progress_i;

    @BindView(R.id.progress_edit_i)
    ProgressBar progress_edit_i;

    @BindView(R.id.tv_edit_progress_q)
    TextView mTv_edit_progress_q;

    @BindView(R.id.progress_edit_q)
    ProgressBar progress_edit_q;

    private int[] StepSizeData;
    int[] data;
    private int position = 1;

    private int mCurrntTp;
    private int currnt;
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
    private boolean stopThread = false;
    private MotorRunnable mMotorRunnable;

    @Override
    public int getLayoutId() {
        return R.layout.activity_mortor;
    }

    @Override
    protected void setup() {

        String satName = getIntent().getStringExtra("edit");
        String tpName = getIntent().getStringExtra("tpname");
        mCurrntTp = getIntent().getIntExtra("currntTp", -1);
        currnt = getIntent().getIntExtra("currnt", -1);
        tv_step.setText(moveStepList[0]);
        mTpList = SWPDBaseManager.getInstance().getSatChannelInfoList(currnt);
        mTv_satellite.setText(satName);
        tv_tp.setText(tpName);
        //锁EditManualActivity传进来的频点
        SWFtaManager.getInstance().tunerLockFreq(currnt, mTpList.get(mCurrntTp).Freq, mTpList.get(mCurrntTp).Symbol, mTpList.get(mCurrntTp).Qam, 1, 0);
        tv_Longitude.setText(mStepSize[0]);
        tv_position.bringToFront();
        //控制Motor执行转动
        StepSizeData = getResources().getIntArray(R.array.step_size_data);
        data = new int[1];
        initCheckSignal();
        mMotorHandler = new MotorHandler(this);
        //取出上一次存的position
        SharedPreferences preferences = getSharedPreferences("data", MODE_PRIVATE);
        int savePosition = preferences.getInt("position", 1);
        tv_position.setText(Integer.toString(savePosition));
        mMotorRunnable = new MotorRunnable(this);
        ThreadPoolManager.getInstance().execute(mMotorRunnable);
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
                    Log.e(TAG, "onKeyDown: Stop执行前");
                    SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_STOP, 0, new int[]{0});
                    Log.e(TAG, "onKeyDown: Stop执行后");
                    break;
                case "West":
                    if (context.data[0] == 0) { //data[0] == 0 Continue持续转动
                        Log.e(TAG, "onKeyDown: West执行前");
                        SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_RIGHT_CONTINUE, 0, context.data);
                        Log.e(TAG, "onKeyDown: West执行后");
                    } else {
                        SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_RIGHT_STEP, 0, context.data);
                        context.sendStopMessage(context.data);
                    }
                    break;
                case "East":
                    if (context.data[0] == 0) {
                        Log.e(TAG, "onKeyDown: East执行前");
                        SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_LEFT_CONTINUE, 0, context.data);
                        Log.e(TAG, "onKeyDown: East执行后");

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

            if (position == DisEqc_Command) {
                --position;
                selecteStepSize();
            } else if (position == Position) {
                --position;
                selecteSatLongitude();

            } else if (position == Step_Size) {
                --position;
                if (mMotorType == USALS) {
                    position = TP;
                    selecteMotorTp();
                } else {
                    position = Move_Steps;
                    selecteMoveStep();
                }

            } else if (position == Move_Steps) {
                --position;
                selecteMotorTp();
            } else {
                position = Motor_Type;
                selecteMotorType();
            }

        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {

            if (position == Motor_Type) {
                position = position + 1;
                selecteMotorTp();

            } else if (position == TP) {
                position = position + 1;
                if (mMotorType == OFF) {
                    //mMotorTyp为OFF,焦点在TP按下键时,将position置为Motor_Type，但焦点还在TP上
                    position = Motor_Type;
                    Log.e("mMotorType", position + "");
                    selecteMotorTp();
                } else if (mMotorType == USALS) {
                    position = Move_Steps;
                    selecteSatLongitude();

                } else {
                    selecteMoveStep();
                }

                //第三条选项选中

            } else if (position == Move_Steps) {

                position = position + 1;
                //第三条选项选中
                selecteSatLongitude();
            } else if (position == Step_Size) {
                ++position;
                selecteStepSize();
            } else {
                position = DisEqc_Command;
                selecteCommand();
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (position == Motor_Type) {

                --mMotorType;
                if (mMotorType < 0) {
                    mMotorType = typeList.length - 1;
                }

                if (mMotorType == DisEqc) {
                    selecteMotorType2();

                } else if (mMotorType == USALS) {
                    selecteMotorType1();

                } else if (mMotorType == OFF) {
                    ll_step_root.setVisibility(View.GONE);
                    ll_sat_longitude_root.setVisibility(View.GONE);
                    ll_sat_position_root.setVisibility(View.GONE);
                    ll_sat_command_root.setVisibility(View.GONE);
                }

                tv_motor_type.setText(typeList[mMotorType]);
                saveSatMotorTypeInfo();

            } else if (position == TP) {
                //控制Tp栏左键
                --mCurrntTp;
                Log.e("left= ", mCurrntTp + "");
                if (mCurrntTp < 0) {
                    mCurrntTp = mTpList.size() - 1;
                }

                getTPName(mCurrntTp);

            } else if (position == Move_Steps) {

                --mMoveStep;

                if (mMoveStep < 0) {
                    mMoveStep = moveStepList.length - 1;
                }
                tv_step.setText(moveStepList[mMoveStep]);
                //控制马达转动，异步处理防止ANR
                data[0] = StepSizeData[mSetpSizeStep];
                ThreadPoolManager.getInstance().remove(mMotorRunnable);
                ThreadPoolManager.getInstance().execute(mMotorRunnable);

            } else if (position == Step_Size)

            {

                if (mMotorType == DisEqc) {
                    --mSetpSizeStep;

                    if (mSetpSizeStep < 0) {
                        mSetpSizeStep = mStepSize.length - 1;
                    }
                    tv_Longitude.setText(mStepSize[mSetpSizeStep]);
                }
            } else if (position == Position)

            {
                --mPositionStep;
                if (mPositionStep < 1) {
                    mPositionStep = 51;
                }
                tv_position.setText(Integer.toString(mPositionStep));
            } else if (position == DisEqc_Command)

            {

                if (mMotorType == DisEqc) {
                    --mDISEqcCommandStep;
                    if (mDISEqcCommandStep < 0) {
                        mDISEqcCommandStep = mDISEqcCommand.length - 1;
                    }
                    tv_command.setText(mDISEqcCommand[mDISEqcCommandStep]);
                } else if (mMotorType == USALS) {
                    --mCommandStep;
                    if (mCommandStep < 0) {
                        mCommandStep = mCommand.length - 1;
                    }
                    tv_command.setText(mCommand[mCommandStep]);
                }
            }


        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {

            if (position == Motor_Type) {

                ++mMotorType;
                if (mMotorType > typeList.length - 1) {
                    mMotorType = DisEqc;
                }
                if (mMotorType == DisEqc) {

                    selecteMotorType2();

                } else if (mMotorType == USALS) {
                    selecteMotorType1();

                } else if (mMotorType == OFF) {
                    ll_step_root.setVisibility(View.GONE);
                    ll_sat_longitude_root.setVisibility(View.GONE);
                    ll_sat_position_root.setVisibility(View.GONE);
                    ll_sat_command_root.setVisibility(View.GONE);
                }

                tv_motor_type.setText(typeList[mMotorType]);
                saveSatMotorTypeInfo();

            } else if (position == TP) {
                //控制Tp栏右键
                Log.e("Right= ", mCurrntTp + "");

                ++mCurrntTp;

                if (mCurrntTp > mTpList.size() - 1) {
                    mCurrntTp = 0;
                }

                getTPName(mCurrntTp);

            } else if (position == Move_Steps) {

                ++mMoveStep;

                if (mMoveStep > moveStepList.length - 1) {
                    mMoveStep = 0;
                }
                tv_step.setText(moveStepList[mMoveStep]);

                data[0] = StepSizeData[mSetpSizeStep];
                ThreadPoolManager.getInstance().remove(mMotorRunnable);
                ThreadPoolManager.getInstance().execute(mMotorRunnable);

            } else if (position == Step_Size) {
                if (mMotorType == DisEqc) {
                    ++mSetpSizeStep;

                    if (mSetpSizeStep > mStepSize.length - 1) {
                        mSetpSizeStep = 0;
                    }
                    tv_Longitude.setText(mStepSize[mSetpSizeStep]);
                }
            } else if (position == Position) {
                ++mPositionStep;
                if (mPositionStep > 51) {
                    mPositionStep = 1;
                }
                tv_position.setText(Integer.toString(mPositionStep));
            } else if (position == DisEqc_Command) {

                if (mMotorType == DisEqc) {
                    ++mDISEqcCommandStep;
                    if (mDISEqcCommandStep > mDISEqcCommand.length - 1) {
                        mDISEqcCommandStep = 0;
                    }
                    tv_command.setText(mDISEqcCommand[mDISEqcCommandStep]);
                } else if (mMotorType == USALS) {
                    ++mCommandStep;
                    if (mCommandStep > mCommand.length - 1) {
                        mCommandStep = 0;
                    }
                    tv_command.setText(mCommand[mCommandStep]);
                }
            }

        }
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (position == DisEqc_Command) {

                if (mMotorType == DisEqc) {
                    switch (tv_command.getText().toString()) {
                        case "Save Position":
                            new CommRemindDialog()
                                    .content(getString(R.string.dialog_save_position))
                                    .setOnPositiveListener("", new OnCommPositiveListener() {
                                        @Override
                                        public void onPositiveListener() {
                                            saveSatMotorTypeInfo();
                                            saveSatPositionInfo();
                                        }
                                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
                            break;
                        case "(Re-)Calculate":
                            new CommRemindDialog()
                                    .content(getString(R.string.dialog_calculate))
                                    .setOnPositiveListener("", new OnCommPositiveListener() {
                                        @Override
                                        public void onPositiveListener() {
                                            SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_RECALCULATE, 0, new int[]{0});
                                        }
                                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
                            break;
                        case "Disable Limit":
                            new CommRemindDialog()
                                    .content(getString(R.string.dialog_disable_limit))
                                    .setOnPositiveListener("", new OnCommPositiveListener() {
                                        @Override
                                        public void onPositiveListener() {
                                            SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_DISABLE_LIMIT, 0, new int[]{0});
                                        }
                                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
                            break;
                        case "East Limit":
                            new CommRemindDialog()
                                    .content(getString(R.string.dialog_east_limit))
                                    .setOnPositiveListener("", new OnCommPositiveListener() {
                                        @Override
                                        public void onPositiveListener() {
                                            SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_EAST_LIMIT, 0, new int[]{0});
                                        }
                                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
                            break;
                        case "West Limit":
                            new CommRemindDialog()
                                    .content(getString(R.string.dialog_west_limit))
                                    .setOnPositiveListener("", new OnCommPositiveListener() {
                                        @Override
                                        public void onPositiveListener() {
                                            SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_WEST_LIMIT, 0, new int[]{0});
                                        }
                                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
                            break;
                        case "Goto Ref":
                            new CommRemindDialog()
                                    .content(getString(R.string.dialog_goto_ref))
                                    .setOnPositiveListener("", new OnCommPositiveListener() {
                                        @Override
                                        public void onPositiveListener() {
                                            SWFta.GetInstance().tunerMotorControl(HMotorCtrlCode.DIRECT_GO_REFERENCE, 0, new int[]{0});
                                        }
                                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
                            break;
                        default:
                            break;

                    }
                } else if (mMotorType == USALS) {
                    switch (tv_command.getText().toString()) {
                        case "Goto XX":
                            new CommRemindDialog()
                                    .content(getString(R.string.dialog_goto_xx))
                                    .setOnPositiveListener("", new OnCommPositiveListener() {
                                        @Override
                                        public void onPositiveListener() {

                                        }
                                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
                            break;
                        case "Save Pos":
                            new CommRemindDialog()
                                    .content(getString(R.string.dialog_save_pos))
                                    .setOnPositiveListener("", new OnCommPositiveListener() {
                                        @Override
                                        public void onPositiveListener() {

                                        }
                                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
                            break;
                        case "Calculate":
                            new CommRemindDialog()
                                    .content(getString(R.string.dialog_commannd_calculate))
                                    .setOnPositiveListener("", new OnCommPositiveListener() {
                                        @Override
                                        public void onPositiveListener() {

                                        }
                                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
                            break;
                        case "Shift":
                            new CommRemindDialog()
                                    .content(getString(R.string.dialog_shift))
                                    .setOnPositiveListener("", new OnCommPositiveListener() {
                                        @Override
                                        public void onPositiveListener() {

                                        }
                                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
                            break;
                        case "Goto Ref":
                            new CommRemindDialog()
                                    .content(getString(R.string.dialog_command_goto_ref))
                                    .setOnPositiveListener("", new OnCommPositiveListener() {
                                        @Override
                                        public void onPositiveListener() {

                                        }
                                    }).show(getSupportFragmentManager(), CommRemindDialog.TAG);
                            break;

                        default:
                            break;

                    }
                }
            }
        }

        return super.onKeyDown(keyCode, event);
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
        SatInfo_t satInfo_t = SWPDBaseManager.getInstance().getSatList().get(currnt);
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
        satInfo_t.diseqc12_pos = Integer.parseInt(tv_position.getText().toString());
        SWPDBaseManager.getInstance().setSatInfo(currnt, satInfo_t);  //将卫星Motor Type信息设置到对应的bean类中,保存更改的信息
        //将位置保存到SharedPreferences中，进入页面时写入Position
        SharedPreferences.Editor editor = getSharedPreferences("data", Context.MODE_PRIVATE).edit();
        editor.putInt("position", satInfo_t.diseqc12_pos);
        editor.apply();
    }

    /**
     * motor TYPE  =DisEqc1.2
     */
    private void selecteMotorType1() {
        ll_step_root.setVisibility(View.GONE);
        ll_sat_longitude_root.setVisibility(View.VISIBLE);
        tv_step_size.setText("Sat Longitude");
        tv_Longitude.setText("0.0`E");
        ll_sat_position_root.setVisibility(View.VISIBLE);
        ll_sat_command_root.setVisibility(View.VISIBLE);
        tv_diseqc_common.setText("Command");
        tv_command.setText(mCommand[0]);
    }

    /**
     * motor TYPE  = USALS
     */
    private void selecteMotorType2() {

        ll_step_root.setVisibility(View.VISIBLE);
        tv_move_steps.setText("Move Steps");
        ll_sat_longitude_root.setVisibility(View.VISIBLE);
        tv_step_size.setText("Step Size");
        ll_sat_position_root.setVisibility(View.VISIBLE);
        ll_sat_command_root.setVisibility(View.VISIBLE);
        tv_diseqc_common.setText("DisEqc Command");
        tv_command.setText(mDISEqcCommand[0]);
        tv_Longitude.setText(R.string.motor_continue);
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
        Log.e(TAG, "onKeyDown: TP切换前");
        SWFtaManager.getInstance().tunerLockFreq(currnt, channelNew_t.Freq, channelNew_t.Symbol, channelNew_t.Qam, 1, 0);
        Log.e(TAG, "onKeyDown: TP切换后");
    }

    /**
     * TP 被选中
     */
    private void selecteMotorTp() {
        ll_motor_type.setBackgroundColor(0);
        imgeview_motor_type_left.setVisibility(View.INVISIBLE);
        tv_motor_type.setBackgroundColor(0);
        imgeview_motor_type_right.setVisibility(View.INVISIBLE);

        ll_tp_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        image_tp_left.setVisibility(View.VISIBLE);
        tv_tp.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_tp_right.setVisibility(View.VISIBLE);

        //Move Step
        ll_step_root.setBackgroundColor(0);
        mImage_step_left.setVisibility(View.INVISIBLE);
        tv_step.setBackgroundColor(0);
        image_step_right.setVisibility(View.INVISIBLE);

        //Sat Longitude
        ll_sat_longitude_root.setBackgroundColor(0);
        image_Longitude_left.setVisibility(View.INVISIBLE);
        tv_Longitude.setBackgroundColor(0);
        image_Longitude_right.setVisibility(View.INVISIBLE);

        // Step Size
        ll_sat_position_root.setBackgroundColor(0);
        image_position_left.setVisibility(View.INVISIBLE);
        tv_position_bg.setBackgroundColor(0);
        image_position_right.setVisibility(View.INVISIBLE);

        //Command
        ll_sat_command_root.setBackgroundColor(0);
        image_command_left.setVisibility(View.INVISIBLE);
        tv_command.setBackgroundColor(0);
        image_command_right.setVisibility(View.INVISIBLE);
    }

    /**
     * motor Type 被选中
     */
    private void selecteMotorType() {
        ll_motor_type.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        imgeview_motor_type_left.setVisibility(View.VISIBLE);
        tv_motor_type.setBackgroundResource(R.drawable.btn_red_bg_shape);
        imgeview_motor_type_right.setVisibility(View.VISIBLE);

        ll_tp_root.setBackgroundColor(0);
        image_tp_left.setVisibility(View.INVISIBLE);
        tv_tp.setBackgroundColor(0);
        image_tp_right.setVisibility(View.INVISIBLE);

        //Move Step
        ll_step_root.setBackgroundColor(0);
        mImage_step_left.setVisibility(View.INVISIBLE);
        tv_step.setBackgroundColor(0);
        image_step_right.setVisibility(View.INVISIBLE);

        //Sat Longitude
        ll_sat_longitude_root.setBackgroundColor(0);
        image_Longitude_left.setVisibility(View.INVISIBLE);
        tv_Longitude.setBackgroundColor(0);
        image_Longitude_right.setVisibility(View.INVISIBLE);

        // Step Size
        ll_sat_position_root.setBackgroundColor(0);
        image_position_left.setVisibility(View.INVISIBLE);
        tv_position_bg.setBackgroundColor(0);
        image_position_right.setVisibility(View.INVISIBLE);

        //Command
        ll_sat_command_root.setBackgroundColor(0);
        image_command_left.setVisibility(View.INVISIBLE);
        tv_command.setBackgroundColor(0);
        image_command_right.setVisibility(View.INVISIBLE);
    }

    /**
     * Move Step 被选中
     */
    private void selecteMoveStep() {
        ll_motor_type.setBackgroundColor(0);
        imgeview_motor_type_left.setVisibility(View.INVISIBLE);
        tv_motor_type.setBackgroundColor(0);
        imgeview_motor_type_right.setVisibility(View.INVISIBLE);

        ll_tp_root.setBackgroundColor(0);
        image_tp_left.setVisibility(View.INVISIBLE);
        tv_tp.setBackgroundColor(0);
        image_tp_right.setVisibility(View.INVISIBLE);

        //Move Step
        ll_step_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        mImage_step_left.setVisibility(View.VISIBLE);
        tv_step.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_step_right.setVisibility(View.VISIBLE);

        //Sat Longitude
        ll_sat_longitude_root.setBackgroundColor(0);
        image_Longitude_left.setVisibility(View.INVISIBLE);
        tv_Longitude.setBackgroundColor(0);
        image_Longitude_right.setVisibility(View.INVISIBLE);

        // Step Size
        ll_sat_position_root.setBackgroundColor(0);
        image_position_left.setVisibility(View.INVISIBLE);
        tv_position_bg.setBackgroundColor(0);
        image_position_right.setVisibility(View.INVISIBLE);

        //Command
        ll_sat_command_root.setBackgroundColor(0);
        image_command_left.setVisibility(View.INVISIBLE);
        tv_command.setBackgroundColor(0);
        image_command_right.setVisibility(View.INVISIBLE);
    }

    /**
     * Sat Longitude 被选中
     */
    private void selecteSatLongitude() {
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

        //Sat Longitude
        ll_sat_longitude_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        image_Longitude_left.setVisibility(View.VISIBLE);
        tv_Longitude.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_Longitude_right.setVisibility(View.VISIBLE);

        // Step Size
        ll_sat_position_root.setBackgroundColor(0);
        image_position_left.setVisibility(View.INVISIBLE);
        tv_position_bg.setBackgroundColor(0);
        image_position_right.setVisibility(View.INVISIBLE);

        //Command
        ll_sat_command_root.setBackgroundColor(0);
        image_command_left.setVisibility(View.INVISIBLE);
        tv_command.setBackgroundColor(0);
        image_command_right.setVisibility(View.INVISIBLE);
    }

    /**
     * Step Size 被选中
     */
    private void selecteStepSize() {
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

        //Sat Longitude
        ll_sat_longitude_root.setBackgroundColor(0);
        image_Longitude_left.setVisibility(View.INVISIBLE);
        tv_Longitude.setBackgroundColor(0);
        image_Longitude_right.setVisibility(View.INVISIBLE);

        // Step Size
        ll_sat_position_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        image_position_left.setVisibility(View.VISIBLE);
        tv_position_bg.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_position_right.setVisibility(View.VISIBLE);

        //Command
        ll_sat_command_root.setBackgroundColor(0);
        image_command_left.setVisibility(View.INVISIBLE);
        tv_command.setBackgroundColor(0);
        image_command_right.setVisibility(View.INVISIBLE);
    }

    /**
     * command 被选中
     */
    private void selecteCommand() {
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

        //Sat Longitude
        ll_sat_longitude_root.setBackgroundColor(0);
        image_Longitude_left.setVisibility(View.INVISIBLE);
        tv_Longitude.setBackgroundColor(0);
        image_Longitude_right.setVisibility(View.INVISIBLE);

        // Step Size
        ll_sat_position_root.setBackgroundColor(0);
        image_position_left.setVisibility(View.INVISIBLE);
        tv_position_bg.setBackgroundColor(0);
        image_position_right.setVisibility(View.INVISIBLE);

        //Command
        ll_sat_command_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        image_command_left.setVisibility(View.VISIBLE);
        tv_command.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_command_right.setVisibility(View.VISIBLE);
    }
}
