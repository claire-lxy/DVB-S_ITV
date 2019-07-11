package com.konkawise.dtv.ui;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.ChannelNew_t;

/**
 * motor界面
 */

public class MotorActivity extends BaseActivity {
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

    private List<String> typeList = new ArrayList<>();
    private List<String> moveStepList = new ArrayList<>();

    private int position = 1;

    private int mCurrntTp;
    private int currnt;
    private List<ChannelNew_t> mTpList;
    private String mTpName;
    private int mMotorType = 0;

    private int mMoveStep = 0;

    @Override
    public int getLayoutId() {
        return R.layout.activity_mortor;
    }

    @Override
    protected void setup() {
        typeList.add("DisEqc1.2");
        typeList.add("USALS");
        typeList.add("OFF");

        moveStepList.add("Stop");
        moveStepList.add("East");
        moveStepList.add("West");

        String satName = getIntent().getStringExtra("edit");
        String tpName = getIntent().getStringExtra("tpname");
        mCurrntTp = getIntent().getIntExtra("currntTp", -1);
        currnt = getIntent().getIntExtra("currnt", -1);

        Log.e("Motor", "satName=" + satName + "tpName=" + tpName + "currntTp=" + mCurrntTp);

        tv_step.setText(moveStepList.get(0));
        mTpList = SWPDBaseManager.getInstance().getSatChannelInfoList(currnt);
        mTv_satellite.setText(satName);
        tv_tp.setText(tpName);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {

            if (position == 6) {
                --position;
                selecteStepSize();
            } else if (position == 5) {
                --position;
                selecteSatLongitude();

            } else if (position == 4) {
                --position;
                if (mMotorType == 1) {
                    position = 3;
                    selecteMotorTp();
                } else {
                    selecteMoveStep();
                }

            } else if (position == 3) {
                --position;
                selecteMotorTp();
            } else {
                position = 1;
                selecteMotorType();
            }

        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {

            if (position == 1) {
                position = position + 1;
                selecteMotorTp();

            } else if (position == 2) {
                position = position + 1;
                if (mMotorType == 2) {
                    position = 1;
                    Log.e("mMotorType", position + "");
                    selecteMotorTp();
                } else if (mMotorType == 1) {
                    position = 3;
                    selecteSatLongitude();

                } else {
                    selecteMoveStep();
                }

                //第三条选项选中

            } else if (position == 3) {

                position = position + 1;
                //第三条选项选中
                selecteSatLongitude();
            } else if (position == 4) {
                ++position;
                selecteStepSize();
            } else {
                position = 6;
                selecteCommand();
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (position == 1) {

                --mMotorType;
                if (mMotorType < 0) {
                    mMotorType = typeList.size() - 1;
                }

                if (mMotorType == 0) {
                    selecteMotorType2();

                } else if (mMotorType == 1) {
                    selecteMotorType1();

                } else if (mMotorType == 2) {
                    ll_step_root.setVisibility(View.GONE);
                    ll_sat_longitude_root.setVisibility(View.GONE);
                    ll_sat_position_root.setVisibility(View.GONE);
                    ll_sat_command_root.setVisibility(View.GONE);
                }

                tv_motor_type.setText(typeList.get(mMotorType));

            } else if (position == 2) {
                //控制Tp栏左键
                --mCurrntTp;
                Log.e("left= ", mCurrntTp + "");
                if (mCurrntTp < 0) {
                    mCurrntTp = mTpList.size() - 1;
                }

                getTPName(mCurrntTp);

            } else if (position == 3) {

                --mMoveStep;

                if (mMoveStep < 0) {
                    mMoveStep = moveStepList.size() - 1;
                }
                tv_step.setText(moveStepList.get(mMoveStep));

            }

            Log.e("LJ", position + "LJ点击了左键");

        } else if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            Log.e("LJ", position + "LJ点击了右键");

            if (position == 1) {

                ++mMotorType;
                if (mMotorType > typeList.size() - 1) {
                    mMotorType = 0;
                }
                if (mMotorType == 0) {

                    selecteMotorType2();

                } else if (mMotorType == 1) {
                    selecteMotorType1();

                } else if (mMotorType == 2) {
                    ll_step_root.setVisibility(View.GONE);
                    ll_sat_longitude_root.setVisibility(View.GONE);
                    ll_sat_position_root.setVisibility(View.GONE);
                    ll_sat_command_root.setVisibility(View.GONE);
                }

                tv_motor_type.setText(typeList.get(mMotorType));


            } else if (position == 2) {
                //控制Tp栏右键
                Log.e("Right= ", mCurrntTp + "");

                ++mCurrntTp;

                if (mCurrntTp > mTpList.size() - 1) {
                    mCurrntTp = 0;
                }

                getTPName(mCurrntTp);

            } else if (position == 3) {

                ++mMoveStep;

                if (mMoveStep > moveStepList.size() - 1) {
                    mMoveStep = 0;
                }
                tv_step.setText(moveStepList.get(mMoveStep));

            }
        }

        return super.onKeyDown(keyCode, event);
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
        tv_command.setText("Goto XX");
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
    }

    /**
     * 获取TP数据
     */
    private void getTPName(int index) {
        ChannelNew_t channelNew_t = mTpList.get(index);

        mTpName = channelNew_t.Freq + Utils.getVorH(this, channelNew_t.Qam) + channelNew_t.Symbol;

        Log.e("tpname1", mTpName);
        tv_tp.setText(mTpName);   //TP
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
        tv_position.setBackgroundColor(0);
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
        tv_position.setBackgroundColor(0);
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
        tv_position.setBackgroundColor(0);
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
        tv_position.setBackgroundColor(0);
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
        tv_position.setBackgroundResource(R.drawable.btn_red_bg_shape);
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
        tv_position.setBackgroundColor(0);
        image_position_right.setVisibility(View.INVISIBLE);

        //Command
        ll_sat_command_root.setBackgroundResource(R.drawable.btn_translate_bg_select_shape);
        image_command_left.setVisibility(View.VISIBLE);
        tv_command.setBackgroundResource(R.drawable.btn_red_bg_shape);
        image_command_right.setVisibility(View.VISIBLE);
    }
}
