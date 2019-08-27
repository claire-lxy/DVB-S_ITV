package com.konkawise.dtv.ui;

import android.content.Intent;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.PasswordDialog;
import com.konkawise.dtv.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.OnClick;
import vendor.konka.hardware.dtvmanager.V1_0.PDPInfo_t;

public class DTVSettingActivity extends BaseActivity {

    private List<PDPInfo_t> mProgList = new ArrayList<>();

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
        if (mProgList.isEmpty())  {
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
                List<PDPInfo_t> progList = SWPDBaseManager.getInstance().getCurrGroupProgInfoList();
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
}
