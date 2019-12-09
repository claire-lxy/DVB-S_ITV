package com.konkawise.dtv.ui;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.RelativeLayout;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.ScanDialog;

import butterknife.BindView;
import butterknife.OnClick;

public class InstallationCActivity extends BaseActivity {

    @BindView(R.id.rl_installation_c_auto_search)
    RelativeLayout rlInstallationCAutoSearch;

    @BindView(R.id.rl_installation_c_manual_search)
    RelativeLayout rlInstallationCManualSearch;

    @OnClick(R.id.rl_installation_c_auto_search)
    void autoSearch() {
        showScanDialog();
    }

    @OnClick(R.id.rl_installation_c_manual_search)
    void manualSearch() { startActivity(new Intent(this, CManualSearchActivity.class));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_installation_c;
    }

    @Override
    protected void setup() {

    }

    private void showScanDialog() {
        new ScanDialog()
                .installationType(ScanDialog.INSTALLATION_TYPE_AUTO_SEARCH)
                .setOnScanSearchListener(v -> {
                    Intent intent = new Intent(InstallationCActivity.this,
                            ScanTVandRadioActivity.class);
                    intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX,
                            Constants.SatIndex.CABLE);
                    intent.putExtra(Constants.IntentKey.INTENT_SEARCH_TYPE,
                            Constants.IntentValue.SEARCH_TYPE_CAUTO);
                    startActivity(intent);
                    finish();
                }).show(getSupportFragmentManager(), ScanDialog.TAG);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_DPAD_DOWN && rlInstallationCManualSearch.isFocused()){
            rlInstallationCAutoSearch.requestFocus();
            return true;
        }

        if(keyCode == KeyEvent.KEYCODE_DPAD_UP && rlInstallationCAutoSearch.isFocused()){
            rlInstallationCManualSearch.requestFocus();
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }
}
