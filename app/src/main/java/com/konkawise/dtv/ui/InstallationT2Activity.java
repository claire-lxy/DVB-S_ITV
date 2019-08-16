package com.konkawise.dtv.ui;

import android.content.Intent;
import android.view.View;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.ScanDialog;

import butterknife.OnClick;

public class InstallationT2Activity extends BaseActivity {
    private static final int T2_SatIndex = 0;

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
                        //T2??????
                        Intent intent = new Intent(InstallationT2Activity.this, ScanTVandRadioActivity.class);
                        intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, T2_SatIndex);//mCurrentSatellite == 0
                        intent.putExtra(Constants.IntentKey.INTENT_T2_AUTO_SEARCH, 5);
                        startActivity(intent);
                        finish();
                    }
                }).show(getSupportFragmentManager(), ScanDialog.TAG);
    }
}
