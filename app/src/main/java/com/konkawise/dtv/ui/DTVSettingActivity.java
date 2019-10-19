package com.konkawise.dtv.ui;

import android.content.Intent;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.PasswordDialog;
import com.konkawise.dtv.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_ProgBasicInfo;

public class DTVSettingActivity extends BaseActivity {

    private List<HProg_Struct_ProgBasicInfo> mProgList = new ArrayList<>();

    @BindView(R.id.rl_general_settings)
    RelativeLayout rlGeneralSetting;

    @BindView(R.id.rl_record_list)
    RelativeLayout rlRecordList;

    @OnClick(R.id.rl_general_settings)
    void generalSetting() {
        Intent intent = new Intent(this, GeneralSettingsActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.rl_t2_settings)
    void t2Setting() {
        startActivity(new Intent(this, T2SettingsActivity.class));
    }

    @OnClick(R.id.rl_parental_control)
    void parentalControl() {
        showPasswordDialog();
    }

    @OnClick(R.id.rl_pvr_settings)
    void pvrSetting() {
        startActivity(new Intent(this, PVRSettingActivity.class));
    }

    @OnClick(R.id.rl_book_list)
    void bookList() {
        if (mProgList.isEmpty()) {
            ToastUtils.showToast(R.string.dialog_no_search);
            return;
        }
        startActivity(new Intent(this, BookListActivity.class));
    }

    @OnClick(R.id.rl_record_list)
    void recordList() {
        startActivity(new Intent(this, RecordListActivity.class));
    }

    @Override
    public int getLayoutId() {
        return R.layout.activity_dtv_setting;
    }

    @Override
    protected void setup() {
        ThreadPoolManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                List<HProg_Struct_ProgBasicInfo> progList = DTVProgramManager.getInstance().getCurrGroupProgInfoList();
                if (progList != null && !progList.isEmpty()) {
                    mProgList.addAll(progList);
                }
            }
        });
    }

    private void showPasswordDialog() {
        new PasswordDialog()
                .setInvalidClose(true)
                .setOnPasswordInputListener(new PasswordDialog.OnPasswordInputListener() {
                    @Override
                    public void onPasswordInput(String inputPassword, String currentPassword, boolean isValid) {
                        if (isValid) {
                            Intent intent = new Intent(DTVSettingActivity.this, ParentalControlActivity.class);
                            startActivity(intent);
                        } else {
                            ToastUtils.showToast(R.string.toast_invalid_password);
                        }
                    }
                }).show(getSupportFragmentManager(), PasswordDialog.TAG);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_UP && rlGeneralSetting.isFocused()) {
            rlRecordList.requestFocus();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN && rlRecordList.isFocused()) {
            rlGeneralSetting.requestFocus();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
