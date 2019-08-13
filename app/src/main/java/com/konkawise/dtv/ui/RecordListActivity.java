package com.konkawise.dtv.ui;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.ListView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.UsbManager;
import com.konkawise.dtv.adapter.DeviceGroupAdapter;
import com.konkawise.dtv.adapter.RecordListAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.bean.RecordInfo;
import com.konkawise.dtv.bean.UsbInfo;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.PasswordDialog;
import com.konkawise.dtv.permission.OnRequestPermissionResultListener;
import com.konkawise.dtv.permission.PermissionHelper;
import com.konkawise.dtv.weaktool.WeakRunnable;

import java.io.File;
import java.util.ArrayList;
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
        updateRecordGroupData(mCurrRecordPosition, false);
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
//        if (mAdapter.getData().get(mCurrRecordPosition).LockFlag == 1) {
//            showPasswordDialog();
//        }
        Intent intent = new Intent();
        intent.setClass(this, RecordPlayer.class);
        intent.putExtra(Constants.IntentKey.INTENT_TIMESHIFT_RECORD_FROM, RecordPlayer.FROM_RECORD_LIST);
        intent.putExtra(Constants.IntentKey.INTENT_RECORD_INFO, mAdapter.getItem(position));
        startActivity(intent);
    }

    private int mCurrDevicePostion;
    private int mCurrRecordPosition;
    private List<UsbInfo> mUsbInfos = new ArrayList<>();
    private DeviceGroupAdapter deviceGroupAdapter;
    private RecordListAdapter mAdapter;
    private LoadRecordListRunnable loadRecordListRunnable;

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
        mUsbInfos.addAll(UsbManager.getInstance().getUsbInfos(this));

        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP) {
            if (ActivityCompat.checkSelfPermission(this, Constants.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                new PermissionHelper(this)
                        .permissions(new String[]{Constants.READ_EXTERNAL_STORAGE, Constants.WRITE_EXTERNAL_STORAGE})
                        .request()
                        .result(new OnRequestPermissionResultListener() {
                            @Override
                            public void onRequestResult(List<String> grantedPermissions, List<String> deniedPermissions) {
                                for (int i = 0; i < grantedPermissions.size(); i++) {
                                    if (grantedPermissions.get(i).equals(Constants.WRITE_EXTERNAL_STORAGE)) {
                                        updateDeviceGroup(mUsbInfos, 0, false, false);
                                        break;
                                    }
                                }
                            }
                        });
            } else {
                updateDeviceGroup(mUsbInfos, 0, false, false);
            }
        } else {
            updateDeviceGroup(mUsbInfos, 0, false, false);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        UsbManager.getInstance().unregisterUsbReceiveListener(this);
    }

    private void updateDeviceGroup(List<UsbInfo> tUsbInfos, int selectDevicePosition, boolean darked, boolean needRefresh) {
        deviceGroupAdapter.setSelectPosition(selectDevicePosition);
        deviceGroupAdapter.setDarked(darked);
        deviceGroupAdapter.updateData(transUsbInfoToString(tUsbInfos));
        if (deviceGroupAdapter.getData().size() > 0) {
            if (selectDevicePosition >= tUsbInfos.size())
                selectDevicePosition = 0;
            mCurrDevicePostion = selectDevicePosition;

            if (needRefresh)
                updateRecordGroupData(0, false);
            mDeviceListView.setSelection(selectDevicePosition);
        } else {
            mAdapter.clearSelect();
            mAdapter.clearData();
            mCurrDevicePostion = 0;
            mCurrRecordPosition = 0;
        }
    }

    private void updateDeviceGroup(int selectDevicePosition, boolean darked) {
        deviceGroupAdapter.setSelectPosition(selectDevicePosition);
        deviceGroupAdapter.setDarked(darked);
        deviceGroupAdapter.notifyDataSetChanged();
    }

    private void updateRecordGroupData(int selectRecordPosition, boolean darked) {
        if (loadRecordListRunnable == null) {
            loadRecordListRunnable = new LoadRecordListRunnable(this);
        }
        loadRecordListRunnable.selectRecordPosition = selectRecordPosition;
        loadRecordListRunnable.darked = darked;
        ThreadPoolManager.getInstance().remove(loadRecordListRunnable);
        ThreadPoolManager.getInstance().execute(loadRecordListRunnable);
    }

    private void updateRecordGroup(int selectRecordPosition, boolean darked) {
        mAdapter.setDarked(darked);
        mAdapter.setSelectPosition(selectRecordPosition);
        mAdapter.notifyDataSetChanged();
    }

    private static class LoadRecordListRunnable extends WeakRunnable<RecordListActivity> {
        int selectRecordPosition;
        boolean darked;

        public LoadRecordListRunnable(RecordListActivity view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            RecordListActivity context = mWeakReference.get();
            List<RecordInfo> ltRecordFiles = context.queryRecordFiles(context.mUsbInfos.get(context.mCurrDevicePostion).path + "/PVR/");
            context.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (selectRecordPosition >= ltRecordFiles.size())
                        selectRecordPosition = 0;
                    context.mCurrRecordPosition = selectRecordPosition;
                    context.mAdapter.setDarked(darked);
                    context.mAdapter.clearSelect();
                    context.mAdapter.setSelectPosition(selectRecordPosition);
                    context.mAdapter.updateData(ltRecordFiles);
                    context.mListView.setSelection(selectRecordPosition);

                }
            });
        }
    }

    private List<String> transUsbInfoToString(List<UsbInfo> tUsbInfos) {
        List<String> ltDeviceNames = new ArrayList<>();
        for (UsbInfo usbInfo : tUsbInfos) {
            ltDeviceNames.add(usbInfo.fsLabel);
        }
        return ltDeviceNames;
    }

    private void renameChannel(String newName) {
//        mAdapter.getItem(mCurrRecordPosition).Name = newName;
//        mAdapter.notifyDataSetChanged();
    }

    private void lockChannels() {
//        List<PDPMInfo_t> recordList = mAdapter.getData();
//        if (recordList == null || recordList.isEmpty()) return;
//
//        if (isMulti()) {
//            for (int i = 0; i < recordList.size(); i++) {
//                if (mAdapter.getSelectMap().get(i)) {
//                    lockChannel(recordList, i);
//                }
//            }
//        } else {
//            lockChannel(recordList, mCurrRecordPosition);
//        }
//
//        mAdapter.clearSelect();
    }

    private void lockChannel(List<PDPMInfo_t> recordList, int position) {
//        PDPMInfo_t recordInfo = recordList.get(position);
//        recordInfo.LockFlag = recordInfo.LockFlag == 1 ? 0 : 1;
//        mAdapter.updateData(position, recordInfo);
    }

    private void deleteChannels() {
//        List<PDPMInfo_t> recordList = mAdapter.getData();
//        if (recordList == null || recordList.isEmpty()) return;
//
//        if (isMulti()) {
//            for (int i = 0; i < recordList.size(); i++) {
//                if (mAdapter.getSelectMap().get(i)) {
//                    deleteChannel(recordList, i);
//                }
//            }
//        } else {
//            deleteChannel(recordList, mCurrRecordPosition);
//        }
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
//        if (mAdapter.getCount() <= 0 || mCurrRecordPosition >= mAdapter.getCount()) return;
//        new RenameDialog()
//                .setProgNo(mAdapter.getItem(mCurrRecordPosition).PShowNo)
//                .setOldName(mAdapter.getItem(mCurrRecordPosition).Name)
//                .setEditLisener(new RenameDialog.EditTextLisener() {
//                    @Override
//                    public void setEdit(String newName) {
//                        if (TextUtils.isEmpty(newName)) return;
//
//                        renameChannel(newName);
//                    }
//                });
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

    private List<RecordInfo> queryRecordFiles(String path) {
        Log.i(TAG, "record filePath---:" + path);

        List<RecordInfo> ltRecordInfo = new ArrayList<>();
        File dF = new File(path);
        File[] files = dF.listFiles();
        if (files == null || files.length == 0)
            return ltRecordInfo;

        for (File f : files) {
            if (f.isFile() && filterTSFile(f)) {
                RecordInfo recordInfo = new RecordInfo();
                recordInfo.setRecordFile(f);
                ltRecordInfo.add(recordInfo);
            }
        }
        Log.i(TAG, "record file size:" + ltRecordInfo.size());
        return ltRecordInfo;
    }

    private boolean filterTSFile(File file) {
        String fileName = file.getName();
        String end = fileName
                .substring(fileName.lastIndexOf(".") + 1, fileName.length());
        return end.equals("ts");
    }

    private int getdiskPosition(String uuid, List<UsbInfo> ltUsbInfos, int[] refresh) {
        refresh[0] = -1;
        if (ltUsbInfos == null || ltUsbInfos.size() == 0)
            return 0;
        int position = 0;
        for (int i = 0; i < ltUsbInfos.size(); i++) {
            if (TextUtils.equals(uuid, ltUsbInfos.get(i).uuid)) {
                position = i;
                refresh[0] = 0;
                break;
            }
        }
        return position;
    }

    @Override
    public void onUsbReceive(int usbObserveType, Set<UsbInfo> usbInfos, UsbInfo currUsbInfo) {
        UsbInfo selectInfo = null;
        if (mUsbInfos != null && mUsbInfos.size() > 0)
            selectInfo = mUsbInfos.get(mCurrDevicePostion);

        mUsbInfos.clear();
        if (usbInfos != null && !usbInfos.isEmpty()) {
            mUsbInfos.addAll(usbInfos);
        }

        int position = 0;
        int[] refresh = new int[1];
        if (selectInfo != null)
            position = getdiskPosition(selectInfo.uuid, mUsbInfos, refresh);

        Log.i(TAG, "REFRESH---" + (refresh[0] == -1));
        updateDeviceGroup(mUsbInfos, position, deviceGroupAdapter.getDarked(), refresh[0] == -1);
    }
}
