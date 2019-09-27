package com.konkawise.dtv.ui;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.ScanDialog;

import butterknife.BindView;
import butterknife.OnClick;

public class InstallationT2Activity extends BaseActivity {
    @BindView(R.id.rl_installation_auto_search)
    RelativeLayout rlInstallationAutoSearch;

    @BindView(R.id.rl_installation_manual_search)
    RelativeLayout rlInstallationManualSearch;

    @OnClick(R.id.rl_installation_auto_search)
    void autoSearch() {
        showScanDialog();
    }

    @OnClick(R.id.rl_installation_manual_search)
    void manualSearch() {
        startActivity(new Intent(this, T2ManualSearchActivity.class));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_installation_t2;
    }

    @Override
    protected void setup() {

    }

    private void showScanDialog() {
        new ScanDialog()
                .installationType(ScanDialog.INSTALLATION_TYPE_AUTO_SEARCH)
                .setOnScanSearchListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(InstallationT2Activity.this, ScanTVandRadioActivity.class);
                        intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, Constants.T2_SATELLITE_INDEX);//mCurrentSatellite == 0
                        intent.putExtra(Constants.IntentKey.INTENT_T2_AUTO_SEARCH, 5);
                        startActivity(intent);
                        finish();
                    }
                }).show(getSupportFragmentManager(), ScanDialog.TAG);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN && rlInstallationManualSearch.isFocused()){
            rlInstallationAutoSearch.requestFocus();
            return true;
        }

        if(keyCode == KeyEvent.KEYCODE_DPAD_UP && rlInstallationAutoSearch.isFocused()){
            rlInstallationManualSearch.requestFocus();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
