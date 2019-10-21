package com.konkawise.dtv.ui;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.ThreadPoolManager;
import com.konkawise.dtv.adapter.SatelliteListAdapter;
import com.konkawise.dtv.base.BaseActivity;
import com.konkawise.dtv.dialog.SearchProgramDialog;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.view.TVListView;
import com.konkawise.dtv.weaktool.WeakAsyncTask;
import com.konkawise.dtv.weaktool.WeakRunnable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import butterknife.BindView;
import butterknife.OnItemClick;
import butterknife.OnItemSelected;
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

    private UpdateSatParamRunnable mUpdateSatParamRunnable;

    @Override
    public int getLayoutId() {
        return R.layout.activity_satellite;
    }

    @Override
    protected void setup() {
        mTvBottomBarBlue.setVisibility(View.GONE);

        mAdapter = new SatelliteListAdapter(this, new ArrayList<>());
        mListView.setAdapter(mAdapter);

        mUpdateSatParamRunnable = new UpdateSatParamRunnable(this);
        new LoadSatelliteTask(this).execute();
    }

    private static class LoadSatelliteTask extends WeakAsyncTask<SatelliteActivity, Void, List<HProg_Struct_SatInfo>> {

        LoadSatelliteTask(SatelliteActivity view) {
            super(view);
        }

        @Override
        protected List<HProg_Struct_SatInfo> backgroundExecute(Void... param) {
            return DTVProgramManager.getInstance().getSatList();
        }

        @Override
        protected void postExecute(List<HProg_Struct_SatInfo> satList) {
            if (satList != null && !satList.isEmpty()) {
                SatelliteActivity context = mWeakReference.get();

                context.mAdapter.updateData(satList);
                context.mListView.setSelection(0);
            }
        }
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_RED) {
            new SearchProgramDialog()
                    .setMessage(getString(satList.size() > 1 ?
                            R.string.multi_satellite_search : R.string.single_satellite_search))
                    .setBtnOnclickLisener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(SatelliteActivity.this, ScanTVandRadioActivity.class);
                            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mAdapter.getItem(mCurrPosition).SatIndex);
                            intent.putExtra(Constants.IntentKey.INTENT_SEARCH_TYPE, Constants.IntentValue.SEARCH_TYPE_SATELLITE);
                            startActivity(intent);
                            finish();
                        }
                    }).show(getSupportFragmentManager(), SearchProgramDialog.TAG);
            return true;
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
        if (mUpdateSatParamRunnable != null) {
            ThreadPoolManager.getInstance().remove(mUpdateSatParamRunnable);
            ThreadPoolManager.getInstance().execute(mUpdateSatParamRunnable);
        }
    }

    private static class UpdateSatParamRunnable extends WeakRunnable<SatelliteActivity> {

        UpdateSatParamRunnable(SatelliteActivity view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            SatelliteActivity context = mWeakReference.get();

            List<HProg_Struct_SatInfo> satList = DTVProgramManager.getInstance().getSatList();
            if (satList != null && !satList.isEmpty() && context.mCurrPosition < satList.size()) {
                HProg_Struct_SatInfo satInfo = satList.get(context.mCurrPosition);
                HProg_Struct_TP channelInfo = DTVProgramManager.getInstance().getTPInfoBySat(satInfo.SatIndex, 0);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.mTvLnb.setText(Utils.getLnb(satInfo));
                        context.mTvLnbPower.setText(satInfo.LnbPower == 0 ?  R.string.off : R.string.on);
                        context.mTvDiSEqC.setText(Utils.getDiSEqC(context, satInfo));
                        context.mTvMotorType.setText(Utils.getMotorType(context, satInfo));

                        if (channelInfo != null && channelInfo.Freq > 0) {
                            String tpName = channelInfo.Freq + Utils.getVorH(context, channelInfo.Qam) + channelInfo.Symbol;
                            context.mTvFreq.setText(tpName);
                        } else {
                            context.mTvFreq.setText("");
                        }
                    }
                });
            }
        }
    }
}
