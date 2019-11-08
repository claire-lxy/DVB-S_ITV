package com.konkawise.dtv.ui;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
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
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.DTVSearchManager;
import com.konkawise.dtv.DTVSettingManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseItemFocusChangeActivity;
import com.konkawise.dtv.bean.LatLngModel;
import com.konkawise.dtv.dialog.CommRemindDialog;
import com.konkawise.dtv.dialog.OnCommCallback;
import com.konkawise.dtv.rx.RxTransformer;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.weaktool.CheckSignalHelper;
import com.konkawise.dtv.weaktool.WeakHandler;

import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;

import butterknife.BindArray;
import butterknife.BindView;
import io.reactivex.Observable;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_TP;
import vendor.konka.hardware.dtvmanager.V1_0.HTuner_Enum_MotorCtrlCode;
import vendor.konka.hardware.dtvmanager.V1_0.HSetting_Enum_Property;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_SatInfo;

public class MotorActivity extends BaseItemFocusChangeActivity implements LifecycleObserver {
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

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    private void startCheckSignal() {
        stopCheckSignal();

        mCheckSignalHelper = new CheckSignalHelper();
        mCheckSignalHelper.setOnCheckSignalListener((strength, quality) -> {
            if (isTpEmpty()) {
                strength = 0;
                quality = 0;
            }
            String strengthPercent = strength + "%";
            mTvProgressStrength.setText(strengthPercent);
            mPbStrength.setProgress(strength);

            String qualityPercent = quality + "%";
            mTvProgressQuality.setText(qualityPercent);
            mPbQuality.setProgress(quality);
        });
        mCheckSignalHelper.startCheckSignal();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    private void stopCheckSignal() {
        if (mCheckSignalHelper != null) {
            mCheckSignalHelper.stopCheckSignal();
            mCheckSignalHelper = null;
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void saveSatLongitude() {
        if (mSatInfo != null) {
            mSatInfo.diseqc12_longitude = mSatLongitudeModel.getValueForStorage();
            DTVProgramManager.getInstance().setSatInfo(mSatelliteIndex, mSatInfo);
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void saveLocalLatLng() {
        DTVSettingManager.getInstance().setDTVProperty(HSetting_Enum_Property.SAT_Longitude, mLocalLongitudeModel.getValueForStorage());
        DTVSettingManager.getInstance().setDTVProperty(HSetting_Enum_Property.SAT_Latitude, mLocalLatitudeModel.getValueForStorage());
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private void stopMotorCtrl() {
        ctrlMotor(new MotorCtrlModel(HTuner_Enum_MotorCtrlCode.STOP, 0, new int[]{0}));

        sendStopMessage(0);
    }

    private int position = 1;

    private int mCurrentTp;
    private int mSatelliteIndex;
    private List<HProg_Struct_TP> mTpList;
    private int mMotorType;

    private int mMoveStep;
    private int mStepSizeStep;
    private int mPositionStep = MIN_POSITION;
    private int mUsalsCommandStep;
    private int mDISEqcCommandStep;
    private CheckSignalHelper mCheckSignalHelper;
    private MotorHandler mMotorHandler;
    private HProg_Struct_SatInfo mSatInfo;

    private LatLngModel mSatLongitudeModel = new LatLngModel();
    private LatLngModel mLocalLongitudeModel = new LatLngModel();
    private LatLngModel mLocalLatitudeModel = new LatLngModel();

    @Override
    public int getLayoutId() {
        return R.layout.activity_mortor;
    }

    @Override
    protected void setup() {
        initIntent();
        initMotorUi();
        tryLockTp();
        mMotorHandler = new MotorHandler(this);
    }

    @Override
    protected LifecycleObserver provideLifecycleObserver() {
        return this;
    }

    private void initIntent() {
        mCurrentTp = getIntent().getIntExtra(Constants.IntentKey.INTENT_CURRENT_TP, -1);
        mSatelliteIndex = getIntent().getIntExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, -1);
        mTpList = DTVProgramManager.getInstance().getSatTPInfo(mSatelliteIndex);
        List<HProg_Struct_SatInfo> satList = DTVProgramManager.getInstance().getSatList();
        int position = DTVProgramManager.getInstance().findPositionBySatIndex(mSatelliteIndex);
        if (satList != null && !satList.isEmpty() && position < satList.size()) {
            mSatInfo = satList.get(position);
            if (mSatInfo != null) {
                mSatLongitudeModel = new LatLngModel(LatLngModel.MODE_LONGITUDE, LatLngModel.LONGITUDE_THRESHOLD, mSatInfo.diseqc12_longitude);
                mLocalLongitudeModel = new LatLngModel(LatLngModel.MODE_LONGITUDE, LatLngModel.LONGITUDE_THRESHOLD, DTVSettingManager.getInstance().getDTVProperty(HSetting_Enum_Property.SAT_Longitude));
                mLocalLatitudeModel = new LatLngModel(LatLngModel.MODE_LATITUDE, LatLngModel.LATITUDE_THRESHOLD, DTVSettingManager.getInstance().getDTVProperty(HSetting_Enum_Property.SAT_Latitude));
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

        mPositionStep = mSatInfo != null ? mSatInfo.diseqc12_pos : 0;
        mTvPosition.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
    }

    private void tryLockTp() {
        if (mTpList != null && mTpList.size() != 0) {
            DTVSearchManager.getInstance().tunerLockFreq(mSatelliteIndex, mTpList.get(mCurrentTp).Freq, mTpList.get(mCurrentTp).Symbol, mTpList.get(mCurrentTp).Qam, 1, 0);
        }
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

    private void ctrlMotor(MotorCtrlModel motorCtrlModel) {
        addObservable(Observable.just(DTVSearchManager.getInstance().tunerMotorControl(motorCtrlModel.ctrlCode, motorCtrlModel.repeat, motorCtrlModel.data))
                .compose(RxTransformer.threadTransformer())
                .subscribe(integer -> {
                    String moveStep = mMoveStepArray[mMoveStep];
                    if (TextUtils.equals(moveStep, getResources().getString(R.string.motor_move_step_west)) ||
                            TextUtils.equals(moveStep, getResources().getString(R.string.motor_move_step_east))) {
                        if (motorCtrlModel.data[0] != 0) {
                            sendStopMessage(motorCtrlModel.data[0]);
                        }
                    }
                }));
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            if (mMotorType == Constants.MotorType.OFF) {
                switch (position) {
                    case ITEM_TP:
                        position--;
                        break;
                    case ITEM_MOTOR_TYPE:
                        position = ITEM_TP;
                        mItemTp.requestFocus();
                        itemFocusChange();
                        return true;
                }
            } else if (mMotorType == Constants.MotorType.USALS) {
                switch (position) {
                    case ITEM_TP:
                    case ITEM_SAT_LONGITUDE:
                    case ITEM_LOCAL_LONGITUDE:
                    case ITEM_LOCAL_LATITUDE:
                    case ITEM_POSITION:
                    case ITEM_COMMAND:
                        position--;
                        break;
                    case ITEM_MOTOR_TYPE:
                        position = ITEM_COMMAND;
                        mItemCommand.requestFocus();
                        itemFocusChange();
                        return true;
                }
            } else if (mMotorType == Constants.MotorType.DISEQC) {
                switch (position) {
                    case ITEM_TP:
                    case ITEM_MOVE_STEPS:
                    case ITEM_STEP_SIZE:
                    case ITEM_POSITION_DIS:
                    case ITEM_DISEQC_COMMAND:
                        position--;
                        break;
                    case ITEM_MOTOR_TYPE:
                        position = ITEM_DISEQC_COMMAND;
                        mItemCommand.requestFocus();
                        itemFocusChange();
                        return true;
                }
            }

            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (mMotorType == Constants.MotorType.OFF) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        position++;
                        break;
                    case ITEM_TP:
                        position = ITEM_MOTOR_TYPE;
                        mItemMotorType.requestFocus();
                        itemFocusChange();
                        return true;
                }
            } else if (mMotorType == Constants.MotorType.USALS) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                    case ITEM_TP:
                    case ITEM_SAT_LONGITUDE:
                    case ITEM_LOCAL_LONGITUDE:
                    case ITEM_LOCAL_LATITUDE:
                    case ITEM_POSITION:
                        position++;
                        break;
                    case ITEM_COMMAND:
                        position = ITEM_MOTOR_TYPE;
                        mItemMotorType.requestFocus();
                        itemFocusChange();
                        return true;
                }
            } else if (mMotorType == Constants.MotorType.DISEQC) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                    case ITEM_TP:
                    case ITEM_MOVE_STEPS:
                    case ITEM_STEP_SIZE:
                    case ITEM_POSITION_DIS:
                        position++;
                        break;
                    case ITEM_DISEQC_COMMAND:
                        position = ITEM_MOTOR_TYPE;
                        mItemMotorType.requestFocus();
                        itemFocusChange();
                        return true;
                }
            }

            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT) {
            if (mMotorType == Constants.MotorType.OFF) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        mMotorType = getMinusStep(mMotorType, mTypeListArray.length - 1);
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        mCurrentTp = getMinusStep(mCurrentTp, mTpList.size() - 1);
                        tpChange();
                        break;
                }
            } else if (mMotorType == Constants.MotorType.USALS) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        mMotorType = getMinusStep(mMotorType, mTypeListArray.length - 1);
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        mCurrentTp = getMinusStep(mCurrentTp, mTpList.size() - 1);
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
                        mPositionStep = getMinusStep(mPositionStep, MAX_POSITION, MIN_POSITION);
                        positionChange();
                        break;
                    case ITEM_COMMAND:
                        mUsalsCommandStep = getMinusStep(mUsalsCommandStep, mUsalsCommandArray.length - 1);
                        commandChange();
                        break;
                }
            } else if (mMotorType == Constants.MotorType.DISEQC) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        mMotorType = getMinusStep(mMotorType, mTypeListArray.length - 1);
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        mCurrentTp = getMinusStep(mCurrentTp, mTpList.size() - 1);
                        tpChange();
                        break;
                    case ITEM_MOVE_STEPS:
                        mMoveStep = getMinusStep(mMoveStep, mMoveStepArray.length - 1);
                        moveStepChange();
                        moveStep();
                        break;
                    case ITEM_STEP_SIZE:
                        mStepSizeStep = getMinusStep(mStepSizeStep, mStepSizeArray.length - 1);
                        stepSizeChange();
                        break;
                    case ITEM_POSITION_DIS:
                        mPositionStep = getMinusStep(mPositionStep, MAX_POSITION, MIN_POSITION);
                        positionChange();
                        break;
                    case ITEM_DISEQC_COMMAND:
                        mDISEqcCommandStep = getMinusStep(mDISEqcCommandStep, mDiSEqCCommandArray.length - 1);
                        commandChange();
                        break;
                }
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT) {
            if (mMotorType == Constants.MotorType.OFF) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        mMotorType = getPlusStep(mMotorType, mTypeListArray.length - 1);
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        mCurrentTp = getPlusStep(mCurrentTp, mTpList.size() - 1);
                        tpChange();
                        break;
                }
            } else if (mMotorType == Constants.MotorType.USALS) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        mMotorType = getPlusStep(mMotorType, mTypeListArray.length - 1);
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        mCurrentTp = getPlusStep(mCurrentTp, mTpList.size() - 1);
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
                        mPositionStep = getPlusStep(mPositionStep, MAX_POSITION);
                        if (mPositionStep <= 0) mPositionStep = MIN_POSITION;
                        positionChange();
                        break;
                    case ITEM_COMMAND:
                        mUsalsCommandStep = getPlusStep(mUsalsCommandStep, mUsalsCommandArray.length - 1);
                        commandChange();
                        break;
                }
            } else if (mMotorType == Constants.MotorType.DISEQC) {
                switch (position) {
                    case ITEM_MOTOR_TYPE:
                        mMotorType = getPlusStep(mMotorType, mTypeListArray.length - 1);
                        motorTypeChange();
                        break;
                    case ITEM_TP:
                        mCurrentTp = getPlusStep(mCurrentTp, mTpList.size() - 1);
                        tpChange();
                        break;
                    case ITEM_MOVE_STEPS:
                        mMoveStep = getPlusStep(mMoveStep, mMoveStepArray.length - 1);
                        moveStepChange();
                        moveStep();
                        break;
                    case ITEM_STEP_SIZE:
                        mStepSizeStep = getPlusStep(mStepSizeStep, mStepSizeArray.length - 1);
                        stepSizeChange();
                        break;
                    case ITEM_POSITION_DIS:
                        mPositionStep = getPlusStep(mPositionStep, MAX_POSITION);
                        if (mPositionStep <= 0) mPositionStep = MIN_POSITION;
                        positionChange();
                        break;
                    case ITEM_DISEQC_COMMAND:
                        mDISEqcCommandStep = getPlusStep(mDISEqcCommandStep, mDiSEqCCommandArray.length - 1);
                        commandChange();
                        break;
                }
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
            intent.putExtra(Constants.IntentKey.INTENT_CURRENT_TP, mCurrentTp);
            setResult(Constants.RequestCode.REQUEST_CODE_MOTOR, intent);
            finish();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
            if ((mMotorType == Constants.MotorType.DISEQC && position == ITEM_DISEQC_COMMAND) ||
                    mMotorType == Constants.MotorType.USALS && position == ITEM_COMMAND) {
                MotorCtrlModel motorCtrlModel = getMotorCtrlModelByCommand();
                showCommandDialog(motorCtrlModel.title, object -> {
                    if (TextUtils.equals(mTvCommand.getText().toString(), getString(R.string.motor_command_savepos))) {
                        if (mMotorType == Constants.MotorType.DISEQC && position == ITEM_DISEQC_COMMAND) {
                            saveMotorType();
                        }
                        savePosition();
                    } else {
                        ctrlMotor(motorCtrlModel);
                    }
                });
                return true;
            }
        }

        return super.onKeyUp(keyCode, event);
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
        mLocalLongitudeLayout.setVisibility(mMotorType == Constants.MotorType.USALS ? View.VISIBLE : View.GONE);
        mTvSatLongitude.setText(mSatLongitudeModel.getLatLngText());
    }

    private void localLongitudeChange() {
        mLocalLongitudeLayout.setVisibility(mMotorType == Constants.MotorType.USALS ? View.VISIBLE : View.GONE);
        mTvLocalLongitude.setText(mLocalLongitudeModel.getLatLngText());
    }

    private void localLatitudeChange() {
        mLocalLongitudeLayout.setVisibility(mMotorType == Constants.MotorType.USALS ? View.VISIBLE : View.GONE);
        mTvLocalLatitude.setText(mLocalLatitudeModel.getLatLngText());
    }

    private void moveStepChange() {
        mItemMoveStep.setVisibility(mMotorType == Constants.MotorType.DISEQC ? View.VISIBLE : View.GONE);
        mTvMoveStep.setText(mMoveStepArray[mMoveStep]);
    }

    private void moveStep() {
        ctrlMotor(getMotorCtrlModelByMoveStep());
    }

    private void stepSizeChange() {
        mItemStepSize.setVisibility(mMotorType == Constants.MotorType.DISEQC ? View.VISIBLE : View.GONE);
        mTvStepSize.setText(mMotorType == Constants.MotorType.DISEQC ? getString(R.string.motor_continue) : "");
    }

    private void positionChange() {
        mItemPosition.setVisibility(mMotorType != Constants.MotorType.OFF ? View.VISIBLE : View.GONE);
        mTvPosition.setText(MessageFormat.format(getString(R.string.motor_position_text), String.valueOf(mPositionStep)));
    }

    private void commandChange() {
        mItemCommand.setVisibility(mMotorType != Constants.MotorType.OFF ? View.VISIBLE : View.GONE);
        mTvCommandTitle.setText(getString(mMotorType == Constants.MotorType.DISEQC ? R.string.motor_diseqc_command_title : R.string.motor_usals_command_title));
        if (mMotorType == Constants.MotorType.USALS) {
            mTvCommand.setText(mUsalsCommandArray[mUsalsCommandStep]);
        } else if (mMotorType == Constants.MotorType.DISEQC) {
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
            if (mMotorType == Constants.MotorType.DISEQC && position == ITEM_DISEQC_COMMAND) {
                title = getString(R.string.dialog_save_position);
            } else if (mMotorType == Constants.MotorType.USALS && position == ITEM_COMMAND) {
                title = getString(R.string.dialog_save_pos);
            }
        } else if (TextUtils.equals(command, getString(R.string.motor_diseqc_command_recalculate))) {
            title = getString(R.string.dialog_calculate);
            ctrlCode = HTuner_Enum_MotorCtrlCode.RECALCULATE;
        } else if (TextUtils.equals(command, getString(R.string.motor_diseqc_command_disablelimit))) {
            title = getString(R.string.dialog_disable_limit);
            ctrlCode = HTuner_Enum_MotorCtrlCode.DISABLE_LIMIT;
        } else if (TextUtils.equals(command, getString(R.string.motor_diseqc_command_eastlimit))) {
            title = getString(R.string.dialog_east_limit);
            ctrlCode = HTuner_Enum_MotorCtrlCode.EAST_LIMIT;
        } else if (TextUtils.equals(command, getString(R.string.motor_diseqc_command_westlimit))) {
            title = getString(R.string.dialog_west_limit);
            ctrlCode = HTuner_Enum_MotorCtrlCode.WEST_LIMIT;
        } else if (TextUtils.equals(command, getString(R.string.motor_command_gotoref))) {
            if (mMotorType == Constants.MotorType.DISEQC && position == ITEM_DISEQC_COMMAND) {
                title = getString(R.string.dialog_goto_ref);
            } else if (mMotorType == Constants.MotorType.USALS && position == ITEM_COMMAND) {
                title = getString(R.string.dialog_command_goto_ref);
            }
            ctrlCode = HTuner_Enum_MotorCtrlCode.GO_REFERENCE;
        } else if (TextUtils.equals(command, getString(R.string.motor_usals_command_gotoxx))) {
            title = getString(R.string.dialog_goto_xx);
            ctrlCode = HTuner_Enum_MotorCtrlCode.USAL_GOTOXX;
        } else if (TextUtils.equals(command, getString(R.string.motor_usals_command_calculate))) {
            title = getString(R.string.dialog_commannd_calculate);
            ctrlCode = HTuner_Enum_MotorCtrlCode.RECALCULATE;
        } else if (TextUtils.equals(command, getString(R.string.motor_usals_command_shift))) {
            title = getString(R.string.dialog_shift);
            ctrlCode = HTuner_Enum_MotorCtrlCode.USAL_SHIFT;
        }
        return new MotorCtrlModel(title, ctrlCode, repeat, data);
    }

    private MotorCtrlModel getMotorCtrlModelByMoveStep() {
        int ctrlCode = 0;
        int repeat = 0;
        int[] data = new int[]{mStepSizeDataArray[mStepSizeStep]};

        String moveStep = mMoveStepArray[mMoveStep];

        if (TextUtils.equals(moveStep, getResources().getString(R.string.motor_move_step_stop))) {
            ctrlCode = HTuner_Enum_MotorCtrlCode.STOP;
        } else if (TextUtils.equals(moveStep, getResources().getString(R.string.motor_move_step_west))) {
            if (data[0] == 0) { // data[0] == 0 Continue持续转动
                ctrlCode = HTuner_Enum_MotorCtrlCode.RIGHT_CONTINUE;
            } else {
                ctrlCode = HTuner_Enum_MotorCtrlCode.RIGHT_STEP;
            }
        } else if (TextUtils.equals(moveStep, getResources().getString(R.string.motor_move_step_east))) {
            if (data[0] == 0) {
                ctrlCode = HTuner_Enum_MotorCtrlCode.LEFT_CONTINUE;
            } else {
                ctrlCode = HTuner_Enum_MotorCtrlCode.LEFT_STEP;
            }
        }
        return new MotorCtrlModel(ctrlCode, repeat, data);
    }

    private void showCommandDialog(String content, OnCommCallback callback) {
        new CommRemindDialog()
                .content(content)
                .setOnPositiveListener("", () -> {
                    if (callback != null) {
                        callback.callback(null);
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

            DTVProgramManager.getInstance().setSatInfo(mSatelliteIndex, mSatInfo);
        }
    }

    private void savePosition() {
        if (mSatInfo != null) {
            mSatInfo.diseqc12_pos = mPositionStep;
            DTVProgramManager.getInstance().setSatInfo(mSatelliteIndex, mSatInfo);
        }
    }

    private void tpChange() {
        if (isTpEmpty()) {
            mTvTp.setText(getString(R.string.empty_tp));
            return;
        }
        HProg_Struct_TP channelNew_t = mTpList.get(mCurrentTp);
        String tp = channelNew_t.Freq + Utils.getVorH(this, channelNew_t.Qam) + channelNew_t.Symbol;
        mTvTp.setText(tp);
        DTVSearchManager.getInstance().tunerLockFreq(mSatelliteIndex, channelNew_t.Freq, channelNew_t.Symbol, channelNew_t.Qam, 1, 0);
    }

    private boolean isTpEmpty() {
        return mTpList == null || mTpList.size() == 0;
    }

    private void itemFocusChange() {
        itemChange(position, ITEM_MOTOR_TYPE, mItemMotorType, mIvMotorTypeLeft, mIvMotorTypeRight, mTvMotorType);
        itemChange(position, ITEM_TP, mItemTp, mIvTpLeft, mIvTpRight, mTvTp);

        itemChange(position, ITEM_MOVE_STEPS, mItemMoveStep, mIvMoveStepLeft, mIvMoveStepRight, mTvMoveStep);
        itemChange(position, ITEM_STEP_SIZE, mItemStepSize, mIvStepSizeLeft, mIvStepSizeRight, mTvStepSize);

        itemChange(position, ITEM_SAT_LONGITUDE, mItemSatLongitude, null, mIvSatLongitudeRight, mTvSatLongitude);
        itemChange(position, ITEM_LOCAL_LONGITUDE, mItemLocalLongitude, null, mIvLocalLongitudeRight, mTvLocalLongitude);
        itemChange(position, ITEM_LOCAL_LATITUDE, mItemLocalLatitude, null, mIvLocalLatitudeRight, mTvLocalLatitude);

        positionItemFocusChange();
        commandItemFocusChange();
    }

    private void positionItemFocusChange() {
        int selectItem = ITEM_POSITION;
        if (mMotorType == Constants.MotorType.DISEQC) {
            selectItem = ITEM_POSITION_DIS;
        }
        itemChange(position, selectItem, mItemPosition, mIvPositionLeft, mIvPositionRight, mTvPosition);
    }

    private void commandItemFocusChange() {
        int selectItem = ITEM_COMMAND;
        if (mMotorType == Constants.MotorType.DISEQC) {
            selectItem = ITEM_DISEQC_COMMAND;
        }
        itemChange(position, selectItem, mItemCommand, mIvDiSEqCCommandLeft, mIvCommandRight, mTvCommand);
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
