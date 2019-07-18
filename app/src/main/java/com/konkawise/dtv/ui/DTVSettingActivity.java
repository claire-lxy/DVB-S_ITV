package com.konkawise.dtv.ui;

import android.content.Intent;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.PasswordDialog;
import com.konkawise.dtv.utils.ToastUtils;

import butterknife.OnClick;

/**
 * 创建者      lj DELL
 * 创建时间    2018/12/9 14:51
 * 描述        DTV设置界面
 * <p>
 * 更新者      $Author$
 * <p>
 * 更新时间    $Date$
 * 更新描述    ${TODO}
 */
public class DTVSettingActivity extends BaseActivity {
    @OnClick(R.id.rl_general_settings)
    void generalSetting() {
//        new InstallationSelectDialog().setOnInstallationSelectListener(new InstallationSelectDialog.OnInstallationSelectListener() {
//            @Override
//            public void onInstallationSelect(int installationType) {
//                Intent intent = new Intent(DTVSettingActivity.this, GeneralSettingsActivity.class);
//                intent.putExtra(Constants.IntentKey.INTENT_T2_SETTING, installationType == Constants.INSTALLATION_TYPE_T2);
//                startActivity(intent);
//            }
//        }).show(getSupportFragmentManager(), InstallationSelectDialog.TAG);
        Intent intent = new Intent(this, GeneralSettingsActivity.class);
        startActivity(intent);
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
}
