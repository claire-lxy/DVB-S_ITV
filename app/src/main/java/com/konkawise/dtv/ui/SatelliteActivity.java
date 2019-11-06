package com.konkawise.dtv.ui;

import android.content.Intent;
import android.view.KeyEvent;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.SatelliteListAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.ScanDialog;
import com.konkawise.dtv.dialog.SearchProgramDialog;
import com.konkawise.dtv.rx.RxTransformer;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.view.TVListView;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
import io.reactivex.Observable;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_TP;
import vendor.konka.hardware.dtvmanager.V1_0.HProg_Struct_SatInfo;

public class SatelliteActivity extends BaseActivity {
    private static final String TAG = "SatelliteActivity";
    private static final int REQUEST_CODE_SATELLITE_EDIT = 0;
    private static final int REQUEST_CODE_TP_EDIT = 1;

    @BindView(R.id.lv_satellite)
    TVListView mListView;

    @BindView(R.id.tv_lnb)
    TextView mTvLnb;

    @BindView(R.id.tv_lnb_power)
    TextView mTvLnbPower;

    @BindView(R.id.tv_freq)
    TextView mTvFreq;

    @BindView(R.id.tv_diseqc)
    TextView mTvDiSEqC;

    @BindView(R.id.tv_motor_type)
    TextView mTvMotorType;

    @BindView(R.id.tv_bottom_bar_blue)
    TextView mTvBottomBarBlue;

    @OnItemSelected(R.id.lv_satellite)
    void selectSatelliteItem(int position) {
        mCurrPosition = position;
        updateUI();
    }

    @OnItemClick(R.id.lv_satellite)
    void clickSatelliteItem(int position) {
        if (mAdapter.isSatelliteCheck(position)) {
            if (satList.size() > 0) {
                Iterator<HProg_Struct_SatInfo> it = satList.iterator();
                while (it.hasNext()) {
                    HProg_Struct_SatInfo satInfo = it.next();
                    if (satInfo.SatIndex == position) {
                        it.remove();
                    }
                }
            }
        } else {
            satList.add(mAdapter.getItem(position));
        }
        mAdapter.setSatelliteCheck(position);
    }

    private SatelliteListAdapter mAdapter;
    public static List<HProg_Struct_SatInfo> satList = new ArrayList(); // ScanTVandRadioActivity传递选中的卫星列表
    private int mCurrPosition;

    @Override
    public int getLayoutId() {
        return R.layout.activity_satellite;
    }

    @Override
    protected void setup() {
        mTvBottomBarBlue.setText(R.string.blind_scan);

        mAdapter = new SatelliteListAdapter(this, new ArrayList<>());
        mListView.setAdapter(mAdapter);

        addObservable(Observable.just(DTVProgramManager.getInstance().getSatList())
                .compose(RxTransformer.threadTransformer())
                .subscribe(satList -> {
                    if (satList != null && !satList.isEmpty()) {
                        mAdapter.updateData(satList);
                        mListView.setSelection(0);
                    }
                }));
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_RED) {
            new SearchProgramDialog()
                    .setMessage(getString(satList.size() > 1 ?
                            R.string.multi_satellite_search : R.string.single_satellite_search))
                    .setBtnOnclickLisener(v -> {
                        Intent intent = new Intent(SatelliteActivity.this, ScanTVandRadioActivity.class);
                        intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mAdapter.getItem(mCurrPosition).SatIndex);
                        intent.putExtra(Constants.IntentKey.INTENT_SEARCH_TYPE, Constants.IntentValue.SEARCH_TYPE_SATELLITE);
                        startActivity(intent);
                        finish();
                    }).show(getSupportFragmentManager(), SearchProgramDialog.TAG);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_PROG_BLUE) {
            new ScanDialog().setOnScanSearchListener(v -> {
                Intent intent = new Intent(SatelliteActivity.this, TpBlindActivity.class);
                intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mAdapter.getItem(mCurrPosition).SatIndex);
                startActivity(intent);
                finish();
            }).show(getSupportFragmentManager(), ScanDialog.TAG);
        }

        return super.onKeyUp(keyCode, event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_GREEN) {
            Intent intent = new Intent(this, EditManualActivity.class);
            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mAdapter.getItem(mCurrPosition).SatIndex);
            startActivityForResult(intent, REQUEST_CODE_SATELLITE_EDIT);
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_YELLOW) {
            Intent intent = new Intent(this, TpListingActivity.class);
            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mAdapter.getItem(mCurrPosition).SatIndex);
            intent.putExtra(Constants.IntentKey.INTENT_LNB, mTvLnb.getText().toString());
            intent.putExtra(Constants.IntentKey.ITENT_DISEQC, mTvDiSEqC.getText().toString());
            intent.putExtra(Constants.IntentKey.INTENT_MOTOR_TYPE, mTvMotorType.getText().toString());
            startActivityForResult(intent, REQUEST_CODE_TP_EDIT);
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN) {
            if (mCurrPosition == mAdapter.getCount() - 1) {
                mListView.setSelection(0);
            }
            return true;
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP) {
            if (mCurrPosition == 0) {
                mListView.setSelection(mAdapter.getCount() - 1);
            }
            return true;
        }

        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == REQUEST_CODE_SATELLITE_EDIT || requestCode == REQUEST_CODE_TP_EDIT) && resultCode == RESULT_OK) {
            mAdapter.updateData(DTVProgramManager.getInstance().getSatList());
            if (data != null) {
                int position = data.getIntExtra(Constants.IntentKey.INTENT_SATELLITE_POSITION, -1);
                if (position != -1) {
                    mCurrPosition = position;
                    mListView.setSelection(mCurrPosition);
                }
            }
            updateUI();
        }
    }

    private void updateUI() {
        addObservable(Observable.just(DTVProgramManager.getInstance().getSatList())
                .map(satList -> {
                    if (satList != null && !satList.isEmpty() && mCurrPosition < satList.size()) {
                        HProg_Struct_SatInfo satInfo = satList.get(mCurrPosition);
                        HProg_Struct_TP channelInfo = DTVProgramManager.getInstance().getTPInfoBySat(satInfo.SatIndex, 0);
                        return new SatParamModel(satInfo, channelInfo);
                    }
                    return null;
                })
                .compose(RxTransformer.threadTransformer())
                .subscribe(satParamModel -> {
                    if (satParamModel != null) {
                        mTvLnb.setText(Utils.getLnb(satParamModel.satInfo));
                        mTvLnbPower.setText(satParamModel.satInfo.LnbPower == 0 ? R.string.off : R.string.on);
                        mTvDiSEqC.setText(Utils.getDiSEqC(this, satParamModel.satInfo));
                        mTvMotorType.setText(Utils.getMotorType(this, satParamModel.satInfo));

                        if (satParamModel.channelInfo != null && satParamModel.channelInfo.Freq > 0) {
                            String tpName = satParamModel.channelInfo.Freq
                                    + Utils.getVorH(this, satParamModel.channelInfo.Qam)
                                    + satParamModel.channelInfo.Symbol;
                            mTvFreq.setText(tpName);
                        } else {
                            mTvFreq.setText("");
                        }
                    }
                })
        );
    }

    private static class SatParamModel {
        HProg_Struct_SatInfo satInfo;
        HProg_Struct_TP channelInfo;

        SatParamModel(HProg_Struct_SatInfo satInfo, HProg_Struct_TP channelInfo) {
            this.satInfo = satInfo;
            this.channelInfo = channelInfo;
        }
    }
}
