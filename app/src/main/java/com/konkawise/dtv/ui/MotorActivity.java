package com.konkawise.dtv.ui;

import android.content.Intent;
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
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.LatLngModel;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.OnCommCallback;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.CheckSignalHelper;
import com.konkawise.dtv.weaktool.WeakHandler;
import com.konkawise.dtv.weaktool.WeakRunnable;
import com.sw.dvblib.SWFta;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.ChannelNew_t;
import vendor.konka.hardware.dtvmanager.V1_0.HMotorCtrlCode;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

public class MotorActivity extends BaseActivity {
    private static final String TAG = "MotorActivity";
    private static final int ITEM_MOTOR_TYPE = 1;
    private static final int ITEM_TP = 2;

    private static final int ITEM_MOVE_STEPS = 3;
    private static final int ITEM_STEP_SIZE = 4;

    private static final int ITEM_SAT_LONGITUDE = 3;
    private static final int ITEM_LOCAL_LONGITUDE = 4;
    private static final int ITEM_LOCAL_LATITUDE = 5;

    private static final int ITEM_POSITION_DIS = 5;
    private static final int ITEM_POSITION = 6;

    private static final int ITEM_DISEQC_COMMAND = 6;
    private static final int ITEM_COMMAND = 7;

    private static final int MOROT_TYPE_OFF = 0;
    private static final int MOROT_TYPE_DISEQC = 1;
    private static final int MOROT_TYPE_USALS = 2;

    private static final int MIN_POSITION = 1;
    private static final int MAX_POSITION = 51;

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

    @BindView(R.id.item_command)
    ViewGroup mItemCommand;

    @BindView(R.id.tv_command_title)
    TextView mTvCommandTitle;

    @BindView(R.id.iv_command_left)
    ImageView mIvDiSEqCCommandLeft;

    @BindView(R.id.tv_command)
    TextView mTvCommand;

    @BindView(R.id.iv_command_right)
    ImageView mIvCommandRight;

    @BindView(R.id.ll_local_longitude_latitude)
    LinearLayout mLocalLongitudeLayout;

    @BindView(R.id.item_local_longitude)
    ViewGroup mItemLocalLongitude;

    @BindView(R.id.tv_local_longitude_title)
    TextView mTvLocalLongitudeTitle;

    @BindView(R.id.tv_local_longitude)
    TextView mTvLocalLongitude;

    @BindView(R.id.iv_local_longitude_right)
    ImageView mIvLocalLongitudeRight;

    @BindView(R.id.item_local_latitude)
    ViewGroup mItemLocalLatitude;

    @BindView(R.id.tv_local_latitude_title)
    TextView mTvLocalLatitudeTitle;

    @BindView(R.id.tv_local_latitude)
    TextView mTvLocalLatitude;

    @BindView(R.id.iv_local_latitude_right)
    ImageView mIvLocalLatitudeRight;

    @BindView(R.id.item_sat_longitude)
    ViewGroup mItemSatLongitude;

    @BindView(R.id.tv_sat_longitude_title)
    TextView mTvSatLongitudeTitle;

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

    @BindArray(R.array.stop_move_time)
    int[] mStopMoveTimeArray;

    private int position = 1;

    private int mCurrentTp;
    private int mSatelliteIndex;
    private List<ChannelNew_t> mTpList;
    private int mMotorType;

    private int mMoveStep;
    private int mStepSizeStep;
    private int mPositionStep = MIN_POSITION;
    private int mUsalsCommandStep;
    private int mDISEqcCommandStep;
    private CheckSignalHelper mCheckSignalHelper;
    private MotorHandler mMotorHandler;
    private MotorCtrlRunnable mMotorRunnable;
    private SatInfo_t mSatInfo;

    private LatLngModel mSatLongitudeModel = new LatLngModel();
    private LatLngModel mLocalLongitudeModel = new LatLngModel();
    private LatLngModel mLocalLatitudeModel = new LatLngModel();

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
        if (mSatInfo != null) saveLongitude();
        SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Longitude.ordinal(), mLocalLongitudeModel.getValueForStorage());
        SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Latitude.ordinal(), mLocalLatitudeModel.getValueForStorage());
    }

    private void stopMotorCtrl() {
        ThreadPoolManager.getInstance().remove(mMotorRunnable);
        mMotorRunnable.ctrlCode = HMotorCtrlCode.DIRECT_STOP;
        mMotorRunnable.data = new int[]{0};
        ThreadPoolManager.getInstance().execute(mMotorRunnable);

        sendStopMessage(0);
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
        mTpList = SWPDBaseManager.getInstance().getSatChannelInfoList(mSatelliteIndex);
        List<SatInfo_t> satList = SWPDBaseManager.getInstance().getSatList();
        int position = SWPDBaseManager.getInstance().findPositionBySatIndex(mSatelliteIndex);
        if (satList != null && !satList.isEmpty() && position < satList.size()) {
            mSatInfo = satList.get(position);
            if (mSatInfo != null) {
                mSatLongitudeModel = new LatLngModel(LatLngModel.MODE_LONGITUDE, LatLngModel.LONGITUDE_THRESHOLD, mSatInfo.diseqc12_longitude);
                mLocalLongitudeModel = new LatLngModel(LatLngModel.MODE_LONGITUDE, LatLngModel.LONGITUDE_THRESHOLD, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Longitude.ordinal()));
                mLocalLatitudeModel = new LatLngModel(LatLngModel.MODE_LATITUDE, LatLngModel.LATITUDE_THRESHOLD, SWFtaManager.getInstance().getCommE2PInfo(SWFta.E_E2PP.E2P_SAT_Latitude.ordinal()));
            }
        }
    }

    private void initMotorUi() {
        mTvSatellite.setText(mSatInfo == null ? "" : mSatInfo.sat_name);

        mTvMotorType.setText(Utils.getMotorType(this, mSatInfo));
        mMotorType = mSatInfo.diseqc12;
        motorTypeChange();

        String tpName = getIntent().getStringExtra(Constants.IntentKey.INTENT_TP_NAME);
        mTvTp.setText(TextUtils.isEmpty(tpName) ? getString(R.string.empty_tp) : tpName);

        mTvMoveStep.setText(mMoveStepArray[0]);
        mTvStepSize.setText(mStepSizeArray[0]);

        mPositionStep = mSatInfo != null ? mSatInfo.diseqc12 : 0;
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
        static final int MSG_STOP_MOVE = 0;

        MotorHandler(MotorActivity view) {
            super(view);
        }

        @Override
        protected void handleMsg(Message msg) {
            if (msg.what == MSG_STOP_MOVE) {
                MotorActivity context = mWeakReference.get();
                context.mMoveStep = 0;
                context.mTvMoveStep.setText(context.mMoveStepArray[context.mMoveStep]);
            }
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
                    context.sendStopMessage(data[0]);
                }
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            if (mMotorType == MOROT_TYPE_OFF) {
                switch (position) {
                    case ITEM_TP:
                        position--;
                        break;
                }
            } else if (mMotorType == MOROT_TYPE_USALS) {
                switch (position) {
                    case ITEM_TP:
                    case ITEM_SAT_LONGITUDE:
                    case ITEM_LOCAL_LONGITUDE:
                    case ITEM_LOCAL_LATITUDE:
                    case ITEM_POSITION:
                    case ITEM_COMMAND:
                        position--;
                        break;
                }
            } else if (mMotorType == MOROT_TYPE_DISEQC) {
                switch (position) {
                    case ITEM_TP:
                    case ITEM_MOVE_STEPS:
                    case ITEM_STEP_SIZE:
                    case ITEM_POSITION_DIS:
                    case ITEM_DISEQC_COMMAND:
                        position--;
                        break;
                }
            }

            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (mMotorType == MOROT_TYPE_OFF) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        position++;
                        break;
                }
            } else if (mMotorType == MOROT_TYPE_USALS) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                    case ITEM_TP:
                    case ITEM_SAT_LONGITUDE:
                    case ITEM_LOCAL_LONGITUDE:
                    case ITEM_LOCAL_LATITUDE:
                    case ITEM_POSITION:
                        position++;
                        break;
                }
            } else if (mMotorType == MOROT_TYPE_DISEQC) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                    case ITEM_TP:
                    case ITEM_MOVE_STEPS:
                    case ITEM_STEP_SIZE:
                    case ITEM_POSITION_DIS:
                        position++;
                        break;
                }
            }

            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (mMotorType == MOROT_TYPE_OFF) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        if (--mMotorType < 0) mMotorType = mTypeListArray.length - 1;
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        if (--mCurrentTp < 0) mCurrentTp = mTpList.size() - 1;
                        tpChange();
                        break;
                }
            } else if (mMotorType == MOROT_TYPE_USALS) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        if (--mMotorType < 0) mMotorType = mTypeListArray.length - 1;
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        if (--mCurrentTp < 0) mCurrentTp = mTpList.size() - 1;
                        tpChange();
                        break;
                    case ITEM_SAT_LONGITUDE:
                        mTvSatLongitude.setText(mSatLongitudeModel.deleteNumber());
                        break;
                    case ITEM_LOCAL_LONGITUDE:
                        mTvLocalLongitude.setText(mLocalLongitudeModel.deleteNumber());
                        break;
                    case ITEM_LOCAL_LATITUDE:
                        mTvLocalLatitude.setText(mLocalLatitudeModel.deleteNumber());
                        break;
                    case ITEM_POSITION:
                        if (--mPositionStep < MIN_POSITION) mPositionStep = MAX_POSITION;
                        positionChange();
                        break;
                    case ITEM_COMMAND:
                        if (--mUsalsCommandStep < 0)
                            mUsalsCommandStep = mUsalsCommandArray.length - 1;
                        commandChange();
                        break;
                }
            } else if (mMotorType == MOROT_TYPE_DISEQC) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        if (--mMotorType < 0) mMotorType = mTypeListArray.length - 1;
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        if (--mCurrentTp < 0) mCurrentTp = mTpList.size() - 1;
                        tpChange();
                        break;
                    case ITEM_MOVE_STEPS:
                        if (--mMoveStep < 0) mMoveStep = mMoveStepArray.length - 1;
                        moveStepChange();
                        moveStep();
                        break;
                    case ITEM_STEP_SIZE:
                        if (--mStepSizeStep < 0) mStepSizeStep = mStepSizeArray.length - 1;
                        stepSizeChange();
                        break;
                    case ITEM_POSITION_DIS:
                        if (--mPositionStep < MIN_POSITION) mPositionStep = MAX_POSITION;
                        positionChange();
                        break;
                    case ITEM_DISEQC_COMMAND:
                        if (--mDISEqcCommandStep < 0)
                            mDISEqcCommandStep = mDiSEqCCommandArray.length - 1;
                        commandChange();
                        break;
                }
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (mMotorType == MOROT_TYPE_OFF) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        if (++mMotorType > mTypeListArray.length - 1)
                            mMotorType = MOROT_TYPE_OFF;
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        if (++mCurrentTp > mTpList.size() - 1) mCurrentTp = 0;
                        tpChange();
                        break;
                }
            } else if (mMotorType == MOROT_TYPE_USALS) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        if (++mMotorType > mTypeListArray.length - 1)
                            mMotorType = MOROT_TYPE_OFF;
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        if (++mCurrentTp > mTpList.size() - 1) mCurrentTp = 0;
                        tpChange();
                        break;
                    case ITEM_SAT_LONGITUDE:
                        mSatLongitudeModel.switchDirection();
                        satLongitudeChange();
                        break;
                    case ITEM_LOCAL_LONGITUDE:
                        mLocalLongitudeModel.switchDirection();
                        localLongitudeChange();
                        break;
                    case ITEM_LOCAL_LATITUDE:
                        mLocalLatitudeModel.switchDirection();
                        localLatitudeChange();
                        break;
                    case ITEM_POSITION:
                        if (++mPositionStep > MAX_POSITION) mPositionStep = MIN_POSITION;
                        positionChange();
                        break;
                    case ITEM_COMMAND:
                        if (++mUsalsCommandStep > mUsalsCommandArray.length - 1)
                            mUsalsCommandStep = 0;
                        commandChange();
                        break;
                }
            } else if (mMotorType == MOROT_TYPE_DISEQC) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        if (++mMotorType > mTypeListArray.length - 1)
                            mMotorType = MOROT_TYPE_OFF;
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        if (++mCurrentTp > mTpList.size() - 1) mCurrentTp = 0;
                        tpChange();
                        break;
                    case ITEM_MOVE_STEPS:
                        if (++mMoveStep > mMoveStepArray.length - 1) mMoveStep = 0;
                        moveStepChange();
                        moveStep();
                        break;
                    case ITEM_STEP_SIZE:
                        if (++mStepSizeStep > mStepSizeArray.length - 1) mStepSizeStep = 0;
                        stepSizeChange();
                        break;
                    case ITEM_POSITION_DIS:
                        if (++mPositionStep > MAX_POSITION) mPositionStep = MIN_POSITION;
                        positionChange();
                        break;
                    case ITEM_DISEQC_COMMAND:
                        if (++mDISEqcCommandStep > mDiSEqCCommandArray.length - 1)
                            mDISEqcCommandStep = 0;
                        commandChange();
                        break;
                }
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if ((mMotorType == MOROT_TYPE_DISEQC && position == ITEM_DISEQC_COMMAND) ||
                    mMotorType == MOROT_TYPE_USALS && position == ITEM_COMMAND) {
                MotorCtrlModel motorCtrlModel = getMotorCtrlModelByCommand();
                showCommandDialog(motorCtrlModel.title, new OnCommCallback() {
                    @Override
                    public void callback(Object object) {
                        if (TextUtils.equals(mTvCommand.getText().toString(), getString(R.string.motor_command_savepos))) {
                            if (mMotorType == MOROT_TYPE_DISEQC && position == ITEM_DISEQC_COMMAND) {
                                saveMotorType();
                            }
                            savePosition();
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

        if (mLocalLongitudeLayout.getVisibility() == View.VISIBLE) {
            if (keyCode == KeyEvent.KEYCODE_0) {
                latLngEditChange(0, position);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_1) {
                latLngEditChange(1, position);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_2) {
                latLngEditChange(2, position);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_3) {
                latLngEditChange(3, position);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_4) {
                latLngEditChange(4, position);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_5) {
                latLngEditChange(5, position);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_6) {
                latLngEditChange(6, position);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_7) {
                latLngEditChange(7, position);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_8) {
                latLngEditChange(8, position);
                return true;
            }

            if (keyCode == KeyEvent.KEYCODE_9) {
                latLngEditChange(9, position);
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            Intent intent = new Intent();
            intent.putExtra(Constants.IntentKey.INTENT_LONGITUDE, mSatLongitudeModel.getValueForStorage());
            setResult(Constants.RequestCode.REQUEST_CODE_MOTOR, intent);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void motorTypeChange() {
        mTvMotorType.setText(mTypeListArray[mMotorType]);
        mTvMotorType.requestFocus();

        moveStepChange();
        stepSizeChange();

        satLongitudeChange();
        localLongitudeChange();
        localLatitudeChange();

        positionChange();
        commandChange();

        saveMotorType();
    }

    private void satLongitudeChange() {
        mLocalLongitudeLayout.setVisibility(mMotorType == MOROT_TYPE_USALS ? View.VISIBLE : View.GONE);
        mTvSatLongitude.setText(mSatLongitudeModel.getLatLngText());
    }

    private void localLongitudeChange() {
        mLocalLongitudeLayout.setVisibility(mMotorType == MOROT_TYPE_USALS ? View.VISIBLE : View.GONE);
        mTvLocalLongitude.setText(mLocalLongitudeModel.getLatLngText());
    }

    private void localLatitudeChange() {
        mLocalLongitudeLayout.setVisibility(mMotorType == MOROT_TYPE_USALS ? View.VISIBLE : View.GONE);
        mTvLocalLatitude.setText(mLocalLatitudeModel.getLatLngText());
    }

    private void moveStepChange() {
        mItemMoveStep.setVisibility(mMotorType == MOROT_TYPE_DISEQC ? View.VISIBLE : View.GONE);
        mTvMoveStep.setText(mMoveStepArray[mMoveStep]);
    }

    private void moveStep() {
        MotorCtrlModel motorCtrlModel = getMotorCtrlModelByMoveStep();
        ThreadPoolManager.getInstance().remove(mMotorRunnable);
        mMotorRunnable.ctrlCode = motorCtrlModel.ctrlCode;
        mMotorRunnable.repeat = motorCtrlModel.repeat;
        mMotorRunnable.data = motorCtrlModel.data;
        ThreadPoolManager.getInstance().execute(mMotorRunnable);
    }

    private void stepSizeChange() {
        mItemStepSize.setVisibility(mMotorType == MOROT_TYPE_DISEQC ? View.VISIBLE : View.GONE);
        mTvStepSize.setText(mMotorType == MOROT_TYPE_DISEQC ? getString(R.string.motor_continue) : "");
    }

    private void positionChange() {
        mItemPosition.setVisibility(mMotorType != MOROT_TYPE_OFF ? View.VISIBLE : View.GONE);
        mTvPosition.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
    }

    private void commandChange() {
        mItemCommand.setVisibility(mMotorType != MOROT_TYPE_OFF ? View.VISIBLE : View.GONE);
        mTvCommandTitle.setText(getString(mMotorType == MOROT_TYPE_DISEQC ? R.string.motor_diseqc_command_title : R.string.motor_usals_command_title));
        if (mMotorType == MOROT_TYPE_USALS) {
            mTvCommand.setText(mUsalsCommandArray[mUsalsCommandStep]);
        } else if (mMotorType == MOROT_TYPE_DISEQC) {
            mTvCommand.setText(mDiSEqCCommandArray[mDISEqcCommandStep]);
        }
    }

    private MotorCtrlModel getMotorCtrlModelByCommand() {
        String command = mTvCommand.getText().toString();
        String title = "";
        int ctrlCode = 0;
        int repeat = 0;
        int[] data = new int[1];

        if (TextUtils.equals(mTvCommand.getText().toString(), getString(R.string.motor_command_savepos))) {
            if (mMotorType == MOROT_TYPE_DISEQC && position == ITEM_DISEQC_COMMAND) {
                title = getString(R.string.dialog_save_position);
            } else if (mMotorType == MOROT_TYPE_USALS && position == ITEM_COMMAND) {
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
            if (mMotorType == MOROT_TYPE_DISEQC && position == ITEM_DISEQC_COMMAND) {
                title = getString(R.string.dialog_goto_ref);
            } else if (mMotorType == MOROT_TYPE_USALS && position == ITEM_COMMAND) {
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

    public void latLngEditChange(int num, int position) {
        switch (position) {
            case ITEM_SAT_LONGITUDE:
                mTvSatLongitude.setText(mSatLongitudeModel.inputNumber(num));
                break;
            case ITEM_LOCAL_LONGITUDE:
                mTvLocalLongitude.setText(mLocalLongitudeModel.inputNumber(num));
                break;
            case ITEM_LOCAL_LATITUDE:
                mTvLocalLatitude.setText(mLocalLatitudeModel.inputNumber(num));
                break;
        }
    }

    private void sendStopMessage(int data) {
        int stepSize = Collections.singletonList(mStepSizeDataArray).indexOf(data);
        if (stepSize >= 0) {
            mMotorHandler.sendEmptyMessageDelayed(MotorHandler.MSG_STOP_MOVE, mStopMoveTimeArray[stepSize]);
        }
    }

    private void saveMotorType() {
        if (mSatInfo != null) {
            String motorType = mTvMotorType.getText().toString();
            if (TextUtils.equals(motorType, getString(R.string.motor_type_off))) {
                mSatInfo.diseqc12 = 0;
            } else if (TextUtils.equals(motorType, getString(R.string.motor_type_diseqc))) {
                mSatInfo.diseqc12 = 1;
            } else if (TextUtils.equals(motorType, getString(R.string.motor_type_usals))) {
                mSatInfo.diseqc12 = 2;
            }

            SWPDBaseManager.getInstance().setSatInfo(mSatelliteIndex, mSatInfo);
        }
    }

    private void savePosition() {
        if (mSatInfo != null) {
            mSatInfo.diseqc12_pos = mPositionStep;
            SWPDBaseManager.getInstance().setSatInfo(mSatelliteIndex, mSatInfo);
        }
    }

    private void saveLongitude() {
        if (mSatInfo != null) {
            mSatInfo.diseqc12_longitude = mSatLongitudeModel.getValueForStorage();
            SWPDBaseManager.getInstance().setSatInfo(mSatelliteIndex, mSatInfo);
        }
    }

    private void tpChange() {
        if (mTpList == null || mTpList.size() == 0) {
            mTvTp.setText(getString(R.string.empty_tp));
            return;
        }
        ChannelNew_t channelNew_t = mTpList.get(mCurrentTp);
        String tp = channelNew_t.Freq + Utils.getVorH(this, channelNew_t.Qam) + channelNew_t.Symbol;
        mTvTp.setText(tp);
        SWFtaManager.getInstance().tunerLockFreq(mSatelliteIndex, channelNew_t.Freq, channelNew_t.Symbol, channelNew_t.Qam, 1, 0);
    }

    private void itemFocusChange() {
        itemChange(ITEM_MOTOR_TYPE, mItemMotorType, mIvMotorTypeLeft, mIvMotorTypeRight, mTvMotorType);
        itemChange(ITEM_TP, mItemTp, mIvTpLeft, mIvTpRight, mTvTp);

        itemChange(ITEM_MOVE_STEPS, mItemMoveStep, mIvMoveStepLeft, mIvMoveStepRight, mTvMoveStep);
        itemChange(ITEM_STEP_SIZE, mItemStepSize, mIvStepSizeLeft, mIvStepSizeRight, mTvStepSize);

        satLongitudeItemFocusChange();
        localLongitudeItemFocusChange();
        localLatitudeItemFocusChange();

        positionItemFocusChange();
        commandItemFocusChange();
    }

    private void satLongitudeItemFocusChange() {
        itemChange(ITEM_SAT_LONGITUDE, mItemSatLongitude, null, mIvSatLongitudeRight, mTvSatLongitude);
    }

    private void localLongitudeItemFocusChange() {
        itemChange(ITEM_LOCAL_LONGITUDE, mItemLocalLongitude, null, mIvLocalLongitudeRight, mTvLocalLongitude);
    }

    private void localLatitudeItemFocusChange() {
        itemChange(ITEM_LOCAL_LATITUDE, mItemLocalLatitude, null, mIvLocalLatitudeRight, mTvLocalLatitude);
    }

    private void commandItemFocusChange() {
        int selectItem = ITEM_COMMAND;
        if (mMotorType == MOROT_TYPE_DISEQC) {
            selectItem = ITEM_DISEQC_COMMAND;
        }
        itemChange(selectItem, mItemCommand, mIvDiSEqCCommandLeft, mIvCommandRight, mTvCommand);
    }

    private void positionItemFocusChange() {
        int selectItem = ITEM_POSITION;
        if (mMotorType == MOROT_TYPE_DISEQC) {
            selectItem = ITEM_POSITION_DIS;
        }
        itemChange(selectItem, mItemPosition, mIvPositionLeft, mIvPositionRight, mTvPosition);
    }

    private void itemChange(int selectItem, ViewGroup itemGroup, ImageView ivLeft, ImageView ivRight, TextView textView) {
        if (itemGroup != null) {
            itemGroup.setBackgroundResource(position == selectItem ? R.drawable.btn_translate_bg_select_shape : 0);
        }
        if (ivLeft != null) {
            ivLeft.setVisibility(position == selectItem ? View.VISIBLE : View.INVISIBLE);
        }
        if (ivRight != null) {
            ivRight.setVisibility(position == selectItem ? View.VISIBLE : View.INVISIBLE);
        }
        if (textView != null) {
            textView.setBackgroundResource(position == selectItem ? R.drawable.btn_red_bg_shape : 0);
        }
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
