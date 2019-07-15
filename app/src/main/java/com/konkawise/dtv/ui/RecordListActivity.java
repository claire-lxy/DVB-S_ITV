package com.konkawise.dtv.ui;

import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ListView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.UsbManager;
import com.konkawise.dtv.adapter.DeviceGroupAdapter;
import com.konkawise.dtv.adapter.RecordListAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.UsbInfo;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.PasswordDialog;
import com.konkawise.dtv.dialog.RenameDialog;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.OnFocusChange;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

public class RecordListActivity extends BaseActivity implements UsbManager.OnUsbReceiveListener {
    private static final String TAG = "RecordListActivity";

    @BindView(R.id.lv_deivce)
    ListView mDeviceListView;

    @BindView(R.id.lv_record_channel_list)
    ListView mListView;

    @OnItemSelected(R.id.lv_deivce)
    void onDeviceItemSelect(int position) {
        Log.i(TAG, "refresh record data--");
        mCurrRecordPosition = 0;
        updateRecordGroup(queryRecordFiles(""), mCurrRecordPosition, false);
        mCurrDevicePostion = position;
    }

    @OnFocusChange(R.id.lv_deivce)
    void onDeviceItemFocus(boolean focus) {
        Log.i(TAG, "device focus--" + focus);
        updateDeviceGroup(mCurrDevicePostion, !focus);
    }

    @OnItemSelected(R.id.lv_record_channel_list)
    void onChannelItemSelect(int position) {
        Log.i(TAG, "Record selection:" + position);
        mCurrRecordPosition = position;
    }

    @OnFocusChange(R.id.lv_record_channel_list)
    void onRecordItemFocus(boolean focus) {
        Log.i(TAG, "Record focus:" + mCurrRecordPosition + " focus:" + focus);
        updateRecordGroup(mCurrRecordPosition, !focus);
    }

    @OnItemClick(R.id.lv_record_channel_list)
    void onChannelItemClick(int position) {
        if (mAdapter.getData().get(mCurrRecordPosition).LockFlag == 1) {
            showPasswordDialog();
        }
    }

    private int mCurrDevicePostion = -1;
    private int mCurrRecordPosition;
    private Set<UsbInfo> mUsbInfos = new LinkedHashSet<>();
    private DeviceGroupAdapter deviceGroupAdapter;
    private RecordListAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_record_list;
    }

    @Override
    protected void setup() {
        UsbManager.getInstance().registerUsbReceiveListener(this);

        mAdapter = new RecordListAdapter(this, new ArrayList<>());
        mListView.setAdapter(mAdapter);
        deviceGroupAdapter = new DeviceGroupAdapter(this, new ArrayList<>());
        mDeviceListView.setAdapter(deviceGroupAdapter);
        mUsbInfos = UsbManager.getInstance().getUsbInfos();
        updateDeviceGroup(mUsbInfos, 0, false);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UsbManager.getInstance().unregisterUsbReceiveListener(this);
    }

    private void updateDeviceGroup(Set<UsbInfo> tUsbInfos, int selectPosition, boolean darked) {
        deviceGroupAdapter.setSelectPosition(selectPosition);
        deviceGroupAdapter.setDarked(darked);
        deviceGroupAdapter.updateData(transUsbInfoToString(tUsbInfos));
        if (deviceGroupAdapter.getData().size() > 0) {
            updateRecordGroup(queryRecordFiles(""), mCurrRecordPosition, false);
            mCurrDevicePostion = selectPosition;
        } else {
            mAdapter.clearSelect();
            mAdapter.clearData();
            mCurrDevicePostion = 0;
            mCurrRecordPosition = 0;
        }
    }

    private void updateDeviceGroup(int selectPosition, boolean darked) {
        deviceGroupAdapter.setSelectPosition(selectPosition);
        deviceGroupAdapter.setDarked(darked);
        deviceGroupAdapter.notifyDataSetChanged();
    }

    private void updateRecordGroup(List<PDPMInfo_t> ltRecordFiles, int selectPosition, boolean darked) {
        mAdapter.setSelectPosition(selectPosition);
        mAdapter.setDarked(darked);
        mAdapter.updateData(ltRecordFiles);
        if (selectPosition < mAdapter.getData().size())
            mListView.setSelection(selectPosition);
    }

    private void updateRecordGroup(int selectPosition, boolean darked) {
        mAdapter.setDarked(darked);
        mAdapter.setSelectPosition(selectPosition);
        mAdapter.notifyDataSetChanged();
    }

    private List<String> transUsbInfoToString(Set<UsbInfo> tUsbInfos) {
        List<String> ltDeviceNames = new ArrayList<>();
        for (UsbInfo usbInfo : tUsbInfos) {
            ltDeviceNames.add(usbInfo.fsLabel);
        }
        return ltDeviceNames;
    }

    private void renameChannel(String newName) {
        mAdapter.getItem(mCurrRecordPosition).Name = newName;
        mAdapter.notifyDataSetChanged();
    }

    private void lockChannels() {
        List<PDPMInfo_t> recordList = mAdapter.getData();
        if (recordList == null || recordList.isEmpty()) return;

        if (isMulti()) {
            for (int i = 0; i < recordList.size(); i++) {
                if (mAdapter.getSelectMap().get(i)) {
                    lockChannel(recordList, i);
                }
            }
        } else {
            lockChannel(recordList, mCurrRecordPosition);
        }

        mAdapter.clearSelect();
    }

    private void lockChannel(List<PDPMInfo_t> recordList, int position) {
        PDPMInfo_t recordInfo = recordList.get(position);
        recordInfo.LockFlag = recordInfo.LockFlag == 1 ? 0 : 1;
        mAdapter.updateData(position, recordInfo);
    }

    private void deleteChannels() {
        List<PDPMInfo_t> recordList = mAdapter.getData();
        if (recordList == null || recordList.isEmpty()) return;

        if (isMulti()) {
            for (int i = 0; i < recordList.size(); i++) {
                if (mAdapter.getSelectMap().get(i)) {
                    deleteChannel(recordList, i);
                }
            }
        } else {
            deleteChannel(recordList, mCurrRecordPosition);
        }
    }

    private void deleteChannel(List<PDPMInfo_t> recordList, int position) {

    }

    private void showPasswordDialog() {
        new PasswordDialog()
                .setOnPasswordInputListener(new PasswordDialog.OnPasswordInputListener() {
                    @Override
                    public void onPasswordInput(String inputPassword, String currentPassword, boolean isValid) {

                    }
                }).show(getSupportFragmentManager(), PasswordDialog.TAG);
    }

    private void showRenameDialog() {
        if (mAdapter.getCount() <= 0 || mCurrRecordPosition >= mAdapter.getCount()) return;
        new RenameDialog()
                .setProgNo(mAdapter.getItem(mCurrRecordPosition).PShowNo)
                .setOldName(mAdapter.getItem(mCurrRecordPosition).Name)
                .setEditLisener(new RenameDialog.EditTextLisener() {
                    @Override
                    public void setEdit(String newName) {
                        if (TextUtils.isEmpty(newName)) return;

                        renameChannel(newName);
                    }
                });
    }

    private void showDeleteDialog() {
        new CommTipsDialog()
                .title(getString(R.string.delete))
                .content(getString(R.string.dialog_delete_channel_content))
                .setOnPositiveListener(getString(R.string.ok), new OnCommPositiveListener() {
                    @Override
                    public void onPositiveListener() {
                        deleteChannels();
                    }
                }).show(getSupportFragmentManager(), CommTipsDialog.TAG);
    }

    private boolean isMulti() {
        return mAdapter.getSelectMap().indexOfValue(true) != -1;
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_PROG_RED) {
            mAdapter.setSelect(mCurrRecordPosition);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_GREEN) {
            showRenameDialog();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            showDeleteDialog();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_YELLOW) {
            lockChannels();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (mListView.hasFocus()) {
                if (mAdapter.getCount() > 0 && mCurrRecordPosition >= mAdapter.getCount() - 1) {
                    mListView.setSelection(0);
                    return true;
                }
            }
            if (mDeviceListView.hasFocus()) {
                if (deviceGroupAdapter.getCount() > 0 && mCurrDevicePostion >= deviceGroupAdapter.getCount() - 1) {
                    mDeviceListView.setSelection(0);
                    return true;
                }
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mListView.hasFocus()) {
                if (mAdapter.getCount() > 0 && mCurrRecordPosition <= 0) {
                    mListView.setSelection(mAdapter.getCount() - 1);
                    return true;
                }
            }
            if (mDeviceListView.hasFocus()) {
                if (deviceGroupAdapter.getCount() > 0 && mCurrDevicePostion <= 0) {
                    mDeviceListView.setSelection(deviceGroupAdapter.getCount() - 1);
                    return true;
                }
            }
        }

        return super.onKeyDown(keyCode, event);
    }

    private List<PDPMInfo_t> queryRecordFiles(String path) {
        List<PDPMInfo_t> ltPdpminfo = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            PDPMInfo_t pdpmInfo_t = new PDPMInfo_t();
            pdpmInfo_t.Name = "name" + (i + 1);
            pdpmInfo_t.PShowNo = i + 1;
            ltPdpminfo.add(pdpmInfo_t);
        }
        return ltPdpminfo;
    }

    @Override
    public void onUsbReceive(int usbObserveType, Set<UsbInfo> usbInfos, UsbInfo currUsbInfo) {
        if (usbObserveType == Constants.USB_TYPE_DETACH) {
            mCurrRecordPosition = 0;
            mAdapter.clearData();
            mAdapter.clearSelect();
            updateDeviceGroup(usbInfos, 0, false);
        } else {
            updateDeviceGroup(usbInfos, mCurrDevicePostion < 0 ? 0 : mCurrDevicePostion, false);
        }
    }
}
