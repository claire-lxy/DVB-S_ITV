package com.konkawise.dtv.ui;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.widget.ListView;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.RecordListAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.CommTipsDialog;
import com.konkawise.dtv.dialog.OnCommPositiveListener;
import com.konkawise.dtv.dialog.PasswordDialog;
import com.konkawise.dtv.dialog.RenameDialog;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

public class RecordListActivity extends BaseActivity {
    private static final String TAG = "RecordListActivity";

    @BindView(R.id.tv_first_storage_device)
    TextView mTvFirstStorageDevice;

    @BindView(R.id.tv_second_storage_device)
    TextView mTvSecondStorageDevice;

    @BindView(R.id.tv_third_storage_device)
    TextView mTvThirdStorageDevice;

    @BindView(R.id.lv_record_channel_list)
    ListView mListView;

    @OnItemSelected(R.id.lv_record_channel_list)
    void onChannelItemSelect(int position) {
        mCurrSelectPosition = position;
    }

    @OnItemClick(R.id.lv_record_channel_list)
    void onChannelItemClick(int position) {
        if (mAdapter.getData().get(mCurrSelectPosition).LockFlag == 1) {
            showPasswordDialog();
        }
    }

    private int mCurrSelectPosition;
    private RecordListAdapter mAdapter;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_record_list;
    }

    @Override
    protected void setup() {
        mAdapter = new RecordListAdapter(this, new ArrayList<>());
        mListView.setAdapter(mAdapter);
    }

    private void renameChannel(String newName) {
        mAdapter.getItem(mCurrSelectPosition).Name = newName;
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
            lockChannel(recordList, mCurrSelectPosition);
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
            deleteChannel(recordList, mCurrSelectPosition);
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
        if (mAdapter.getCount() <= 0 || mCurrSelectPosition >= mAdapter.getCount()) return;
        new RenameDialog()
                .setProgNo(mAdapter.getItem(mCurrSelectPosition).PShowNo)
                .setOldName(mAdapter.getItem(mCurrSelectPosition).Name)
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
            mAdapter.setSelect(mCurrSelectPosition);
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
            if (mAdapter.getCount() > 0 && mCurrSelectPosition >= mAdapter.getCount() - 1) {
                mListView.setSelection(0);
                return true;
            }
        }

        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
            if (mAdapter.getCount() > 0 && mCurrSelectPosition <= 0) {
                mListView.setSelection(mAdapter.getCount() - 1);
                return true;
            }
        }

        return super.onKeyDown(keyCode, event);
    }
}
