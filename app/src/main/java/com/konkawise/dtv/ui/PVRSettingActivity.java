package com.konkawise.dtv.ui;

import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.UsbManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.UsbInfo;
import com.konkawise.dtv.dialog.CommCheckItemDialog;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.OnCommNegativeListener;
import com.konkawise.dtv.dialog.OnCommPositiveListener;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;

public class PVRSettingActivity extends BaseActivity implements UsbManager.OnUsbReceiveListener {
    private static final String TAG = "PVRSettingActivity";

    private static final int ITEM_TIME_SHIFT_LENGTH = 1;
    private static final int ITEM_RECORD_LENGTH = 2;
    private static final int ITEM_RECORD_TYPE = 3;
    private static final int ITEM_DEVICE_NAME = 4;
    private static final int ITEM_DEVICE_FORMAT = 5;

    @BindView(R.id.item_time_shift_length)
    ViewGroup mItemTimeShiftLengthLeft;

    @BindView(R.id.iv_time_shift_length_left)
    ImageView mIvTimeShiftLengthLeft;

    @BindView(R.id.tv_time_shift_length)
    TextView mTvTimeShiftLength;

    @BindView(R.id.iv_time_shift_length_right)
    ImageView mIvTimeShiftLengthRight;

    @BindView(R.id.iv_record_length_left)
    ImageView mIvRecordLengthLeft;

    @BindView(R.id.tv_record_length)
    TextView mTvRecordLength;

    @BindView(R.id.iv_record_length_right)
    ImageView mIvRecordLengthRight;

    @BindView(R.id.iv_record_type_left)
    ImageView mIvRecordTypeLeft;

    @BindView(R.id.tv_record_type)
    TextView mTvRecordType;

    @BindView(R.id.iv_record_type_right)
    ImageView mIvRecordTypeRight;

    @BindView(R.id.iv_device_name_left)
    ImageView mIvDeviceNameLeft;

    @BindView(R.id.tv_device_name)
    TextView mTvDeviceName;

    @BindView(R.id.iv_device_name_right)
    ImageView mIvDeviceNameRight;

    @BindView(R.id.item_device_format)
    ViewGroup mItemDeviceFormat;

    @BindView(R.id.iv_device_format_left)
    ImageView mIvDeviceFormatLeft;

    @BindView(R.id.tv_device_format)
    TextView mTvDeviceFormat;

    @BindView(R.id.iv_device_format_right)
    ImageView mIvDeviceFormatRight;

    @BindView(R.id.tv_device_file_system)
    TextView mTvDeviceFileSystem;

    @BindView(R.id.tv_device_free_space)
    TextView mTvDeviceFreeSpace;

    @BindView(R.id.tv_device_total_capacity)
    TextView mTvDeviceTotalCapacity;

    @BindArray(R.array.time_shift_length)
    String[] mTimeShiftLengthArray;

    @BindArray(R.array.record_length)
    String[] mRecordLengthArray;

    @BindArray(R.array.record_type)
    String[] mRecordTypeArray;

    @OnClick(R.id.item_time_shift_length)
    void timeShiftLength() {
        showGeneralSettingDialog(getString(R.string.time_shift_length), Arrays.asList(mTimeShiftLengthArray), mTimeShiftLengthPosition);
    }

    @OnClick(R.id.item_record_length)
    void recordLength() {
        showGeneralSettingDialog(getString(R.string.record_length), Arrays.asList(mRecordLengthArray), mRecordLengthPosition);
    }

    @OnClick(R.id.item_record_type)
    void recordType() {
        showGeneralSettingDialog(getString(R.string.record_type), Arrays.asList(mRecordTypeArray), mRecordTypePosition);
    }

    @OnClick(R.id.item_device_name)
    void deviceName() {
        List<String> deviceNameList = new ArrayList<>();
        if (mUsbInfos.size() == 0) {
            deviceNameList.add(getString(R.string.device_none));
        } else {
            for (UsbInfo usbInfo : mUsbInfos) {
                deviceNameList.add(usbInfo.fsLabel);
            }
        }
        showGeneralSettingDialog(getString(R.string.device_name), deviceNameList, mDeviceNamePosition);
    }

    @OnClick(R.id.item_device_format)
    void deviceFormat() {
        mCommTipsDialog = new CommTipsDialog()
                .title(getString(R.string.device_format))
                .content(getString(R.string.device_format_dialog_content))
                .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        mCommTipsDialog = null;
                        sendBroadcast(new Intent(Intent.ACTION_MEDIA_MOUNTED)); // 发送广播重新获取usb信息
                    }
                })
                .setOnNegativeListener(getString(R.string.cancel), new OnCommNegativeListener() {
                    @Override
                    public void onNegativeListener() {
                        mCommTipsDialog = null;
                    }
                });
        mCommTipsDialog.show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private int mCurrSelectItem = ITEM_TIME_SHIFT_LENGTH;
    private int mTimeShiftLengthPosition;
    private int mRecordLengthPosition;
    private int mRecordTypePosition;
    private int mDeviceNamePosition;
    private int mDeviceFormatPosition;

    private CommCheckItemDialog mCommCheckItemDialog;
    private CommTipsDialog mCommTipsDialog;

    private List<UsbInfo> mUsbInfos = new ArrayList<>();

    @Override
    protected int getLayoutId() {
        return R.layout.activity_pvr_setting;
    }

    @Override
    protected void setup() {
        UsbManager.getInstance().registerUsbReceiveListener(this);
        mUsbInfos.addAll(UsbManager.getInstance().getUsbInfos(this));
        Log.i(TAG, "mUsbInfos size:" + mUsbInfos.size());
        initData();
    }

    @Override
    protected void onDestroy() {
        UsbManager.getInstance().unregisterUsbReceiveListener(this);
        super.onDestroy();
    }

    private void initData() {
        mTvTimeShiftLength.setText(mTimeShiftLengthArray[mTimeShiftLengthPosition]);
        mTvRecordLength.setText(mRecordLengthArray[mRecordLengthPosition]);
        mTvRecordType.setText(mRecordTypeArray[mRecordTypePosition]);
        mTvDeviceFormat.setText(getResources().getString(R.string.device_format_OK));
        updateDeviceInfo(0);
    }

    private void updateDeviceInfo(int devicePosition) {
        if (mUsbInfos != null && !mUsbInfos.isEmpty()) {
            mItemDeviceFormat.setVisibility(View.VISIBLE);
            mTvDeviceFileSystem.setVisibility(View.VISIBLE);
            mTvDeviceFreeSpace.setVisibility(View.VISIBLE);
            mTvDeviceTotalCapacity.setVisibility(View.VISIBLE);

            mTvDeviceName.setText(mUsbInfos.get(devicePosition).fsLabel);
            mTvDeviceFileSystem.setText(MessageFormat.format(getString(R.string.device_file_system), mUsbInfos.get(devicePosition).fsType.toUpperCase()));
            mTvDeviceFreeSpace.setText(MessageFormat.format(getString(R.string.device_free_space), mUsbInfos.get(devicePosition).availableSize));
            mTvDeviceTotalCapacity.setText(MessageFormat.format(getString(R.string.device_total_capacity), mUsbInfos.get(devicePosition).totalSize));
        } else {
            if (mItemDeviceFormat.isFocused()) {
                mItemTimeShiftLengthLeft.requestFocus();
                mCurrSelectItem = ITEM_TIME_SHIFT_LENGTH;
                itemFocusChange();
            }
            mItemDeviceFormat.setVisibility(View.GONE);
            mTvDeviceFileSystem.setVisibility(View.GONE);
            mTvDeviceFreeSpace.setVisibility(View.GONE);
            mTvDeviceTotalCapacity.setVisibility(View.GONE);

            mTvDeviceName.setText(getString(R.string.device_none));
        }
    }

    private void updateTipsDialog(UsbInfo currUsbInfo, int usbObserveType) {
        if (mCommTipsDialog != null && mCommTipsDialog.isVisible() && usbObserveType == Constants.USB_TYPE_DETACH) {
            if (mUsbInfos.get(mDeviceNamePosition).uuid.equals(currUsbInfo.uuid)) {
                mCommTipsDialog.dismiss();
                mCommTipsDialog = null;
            }
        }
    }

    private void updateDialogContent() {
        if (mCommCheckItemDialog != null && mCommCheckItemDialog.isVisible()) {
            if (mCurrSelectItem == ITEM_DEVICE_NAME) {
                if (mUsbInfos.isEmpty()) {
                    mCommCheckItemDialog.dismiss();
                } else {
                    List<String> deviceNameList = new ArrayList<>();
                    for (UsbInfo usbInfo : mUsbInfos) {
                        deviceNameList.add(usbInfo.fsLabel);
                    }
                    mCommCheckItemDialog.position(0)
                            .updateContent(deviceNameList);
                }
            }
        }

    }

    private int getSelectPosition(int[] datas, int value) {
        if (datas == null || datas.length <= 0) return 0;

        for (int i = 0; i < datas.length; i++) {
            if (datas[i] == value) return i;
        }
        return 0;
    }

    private void showGeneralSettingDialog(String title, List<String> content, int selectPosition) {
        mCommCheckItemDialog = new CommCheckItemDialog()
                .title(title)
                .content(content)
                .position(selectPosition)
                .setOnDismissListener(new CommCheckItemDialog.OnDismissListener() {
                    @Override
                    public void onDismiss(CommCheckItemDialog dialog, int position, String checkContent) {
                        mCommCheckItemDialog = null;

                        switch (mCurrSelectItem) {
                            case ITEM_TIME_SHIFT_LENGTH:
                                mTvTimeShiftLength.setText(checkContent);
                                mTimeShiftLengthPosition = Arrays.asList(mTimeShiftLengthArray).indexOf(checkContent);
                                break;

                            case ITEM_RECORD_LENGTH:
                                mTvRecordLength.setText(checkContent);
                                mRecordLengthPosition = Arrays.asList(mRecordLengthArray).indexOf(checkContent);
                                break;

                            case ITEM_RECORD_TYPE:
                                mTvRecordType.setText(checkContent);
                                mRecordTypePosition = Arrays.asList(mRecordTypeArray).indexOf(checkContent);
                                break;

                            case ITEM_DEVICE_NAME:
                                mTvDeviceName.setText(checkContent);
                                if (!mUsbInfos.isEmpty()) {
                                    for (int i = 0; i < mUsbInfos.size(); i++) {
                                        if (TextUtils.equals(mUsbInfos.get(i).fsLabel, checkContent)) {
                                            mDeviceNamePosition = i;
                                            break;
                                        }
                                    }
                                    updateDeviceInfo(mDeviceNamePosition);
                                }
                                break;
                        }
                    }
                });
        mCommCheckItemDialog.show(getSupportFragmentManager(), CommCheckItemDialog.TAG);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            switch (mCurrSelectItem) {
                case ITEM_TIME_SHIFT_LENGTH:
                case ITEM_RECORD_LENGTH:
                case ITEM_RECORD_TYPE:
                    mCurrSelectItem++;
                    break;
                case ITEM_DEVICE_NAME:
                    if (mItemDeviceFormat.getVisibility() == View.VISIBLE) {
                        mCurrSelectItem++;
                    }
                    break;
            }
            itemFocusChange();
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            switch (mCurrSelectItem) {
                case ITEM_RECORD_LENGTH:
                case ITEM_RECORD_TYPE:
                case ITEM_DEVICE_NAME:
                case ITEM_DEVICE_FORMAT:
                    mCurrSelectItem--;
                    break;
            }
            itemFocusChange();
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            switch (mCurrSelectItem) {
                case ITEM_TIME_SHIFT_LENGTH:
                    if (--mTimeShiftLengthPosition < 0)
                        mTimeShiftLengthPosition = mTimeShiftLengthArray.length - 1;
                    mTvTimeShiftLength.setText(mTimeShiftLengthArray[mTimeShiftLengthPosition]);
                    break;

                case ITEM_RECORD_LENGTH:
                    if (--mRecordLengthPosition < 0)
                        mRecordLengthPosition = mRecordLengthArray.length - 1;
                    mTvRecordLength.setText(mRecordLengthArray[mRecordLengthPosition]);
                    break;

                case ITEM_RECORD_TYPE:
                    if (--mRecordTypePosition < 0)
                        mRecordTypePosition = mRecordTypeArray.length - 1;
                    mTvRecordType.setText(mRecordTypeArray[mRecordTypePosition]);
                    break;

                case ITEM_DEVICE_NAME:
                    if (mUsbInfos == null || mUsbInfos.size() == 0)
                        return super.onKeyDown(keyCode, event);
                    if (--mDeviceNamePosition < 0)
                        mDeviceNamePosition = mUsbInfos.size() - 1;
                    updateDeviceInfo(mDeviceNamePosition);
                    break;

                case ITEM_DEVICE_FORMAT:

                    break;
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            switch (mCurrSelectItem) {
                case ITEM_TIME_SHIFT_LENGTH:
                    if (++mTimeShiftLengthPosition > mTimeShiftLengthArray.length - 1)
                        mTimeShiftLengthPosition = 0;
                    mTvTimeShiftLength.setText(mTimeShiftLengthArray[mTimeShiftLengthPosition]);
                    break;

                case ITEM_RECORD_LENGTH:
                    if (++mRecordLengthPosition > mRecordLengthArray.length - 1)
                        mRecordLengthPosition = 0;
                    mTvRecordLength.setText(mRecordLengthArray[mRecordLengthPosition]);
                    break;

                case ITEM_RECORD_TYPE:
                    if (++mRecordTypePosition > mRecordTypeArray.length - 1)
                        mRecordTypePosition = 0;
                    mTvRecordType.setText(mRecordTypeArray[mRecordTypePosition]);
                    break;

                case ITEM_DEVICE_NAME:
                    if (mUsbInfos == null || mUsbInfos.size() == 0)
                        return super.onKeyDown(keyCode, event);
                    if (++mDeviceNamePosition > mUsbInfos.size() - 1)
                        mDeviceNamePosition = 0;
                    updateDeviceInfo(mDeviceNamePosition);
                    break;

                case ITEM_DEVICE_FORMAT:

                    break;
            }
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mCommCheckItemDialog = null;
        }

        return super.onKeyDown(keyCode, event);
    }

    private void itemFocusChange() {
        itemChange(ITEM_TIME_SHIFT_LENGTH, mIvTimeShiftLengthLeft, mIvTimeShiftLengthRight, mTvTimeShiftLength);
        itemChange(ITEM_RECORD_LENGTH, mIvRecordLengthLeft, mIvRecordLengthRight, mTvRecordLength);
        itemChange(ITEM_RECORD_TYPE, mIvRecordTypeLeft, mIvRecordTypeRight, mTvRecordType);
        itemChange(ITEM_DEVICE_NAME, mIvDeviceNameLeft, mIvDeviceNameRight, mTvDeviceName);
        itemChange(ITEM_DEVICE_FORMAT, mIvDeviceFormatLeft, mIvDeviceFormatRight, mTvDeviceFormat);
    }

    private void itemChange(int selectItem, ImageView ivLeft, ImageView ivRight, TextView textView) {
        ivLeft.setVisibility(mCurrSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
        textView.setBackgroundResource(mCurrSelectItem == selectItem ? R.drawable.btn_red_bg_shape : 0);
        ivRight.setVisibility(mCurrSelectItem == selectItem ? View.VISIBLE : View.INVISIBLE);
    }

    @Override
    public void onUsbReceive(int usbObserveType, Set<UsbInfo> usbInfos, UsbInfo currUsbInfo) {
        updateTipsDialog(currUsbInfo, usbObserveType);

        mUsbInfos.clear();
        if (usbInfos != null && !usbInfos.isEmpty()) {
            mUsbInfos.addAll(usbInfos);
        }

        updateDialogContent();
        updateDeviceInfo(0);
    }
}
