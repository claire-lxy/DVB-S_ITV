package com.konkawise.dtv.ui;

import android.content.Intent;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.konkawise.dtv.Constants;
import com.konkawise.dtv.R;
import com.konkawise.dtv.SWPDBaseManager;
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
import vendor.konka.hardware.dtvmanager.V1_0.ChannelNew_t;
import vendor.konka.hardware.dtvmanager.V1_0.SatInfo_t;

/**
 * 卫星搜索
 */
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

    @BindView(R.id.tv_lnb_power_freq)
    TextView mTvLnbPowerFreq;

    @BindView(R.id.tv_lnb_power_diseqc)
    TextView mTvLnbPowerDiseqc;

    @BindView(R.id.tv_lnb_power_motor)
    TextView mTvLnbPowerMotor;

    @BindView(R.id.tv_bottom_bar_blue)
    TextView mTvBottomBarBlue;

    @OnItemSelected(R.id.lv_satellite)
    void selectSatelliteItem(int position) {
        updateUI(position);
    }

    @OnItemClick(R.id.lv_satellite)
    void clickSatelliteItem(int position) {
        if (mAdapter.isSatelliteCheck(position)) {
            if (satList.size() > 0) {
                Iterator<SatInfo_t> it = satList.iterator();
                while (it.hasNext()) {
                    SatInfo_t satInfo = it.next();
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
    public static List<SatInfo_t> satList = new ArrayList(); // ScanTVandRadioActivity传递选中的卫星列表
    private int mCurrPosition = 0;

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

    private static class LoadSatelliteTask extends WeakAsyncTask<SatelliteActivity, Void, List<SatInfo_t>> {

        LoadSatelliteTask(SatelliteActivity view) {
            super(view);
        }

        @Override
        protected List<SatInfo_t> backgroundExecute(Void... param) {
            return SWPDBaseManager.getInstance().getSatList();
        }

        @Override
        protected void postExecute(List<SatInfo_t> satList) {
            if (satList != null && !satList.isEmpty()) {
                SatelliteActivity context = mWeakReference.get();

                context.mAdapter.updateData(satList);
                context.mListView.setSelection(context.mCurrPosition);
            }
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_RED) {
            new SearchProgramDialog()
                    .setMessage(getString(satList.size() > 1 ?
                            R.string.multi_satellite_search : R.string.single_satellite_search))
                    .setBtnOnclickLisener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(SatelliteActivity.this, ScanTVandRadioActivity.class);
                            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mCurrPosition);
                            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_ACTIVITY, 1);
                            intent.putExtra(Constants.IntentKey.INTENT_TP_NAME, mTvLnbPowerFreq.getText().toString().trim());
                            startActivity(intent);
                            finish();
                        }
                    }).show(getSupportFragmentManager(), SearchProgramDialog.TAG);
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_GREEN) {
            Intent intent = new Intent(this, EditManualActivity.class);
            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mCurrPosition);
            intent.putExtra(Constants.IntentKey.INTENT_LNB, mTvLnb.getText().toString().trim());
            intent.putExtra(Constants.IntentKey.INTENT_DISEQC, mTvLnbPowerDiseqc.getText().toString());
            startActivityForResult(intent, REQUEST_CODE_SATELLITE_EDIT);
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_YELLOW) {
            Intent intent = new Intent(this, TpListingActivity.class);
            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mCurrPosition);
            intent.putExtra(Constants.IntentKey.INTENT_LNB, mTvLnb.getText().toString().trim());
            intent.putExtra(Constants.IntentKey.INTENT_DISEQC, mTvLnbPowerDiseqc.getText().toString());
            startActivityForResult(intent, REQUEST_CODE_TP_EDIT);
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
            mAdapter.updateData(SWPDBaseManager.getInstance().getSatList());
            if (data != null) {
                int index = data.getIntExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, -1);
                if (index != -1) {
                    mCurrPosition = index;
                    mListView.setSelection(mCurrPosition);
                }
            }
            updateUI(mCurrPosition);
        }
    }

    private void updateUI(int position) {
        if (mUpdateSatParamRunnable != null) {
            ThreadPoolManager.getInstance().remove(mUpdateSatParamRunnable);
            mUpdateSatParamRunnable.position = position;
            ThreadPoolManager.getInstance().execute(mUpdateSatParamRunnable);
        }
    }

    private static class UpdateSatParamRunnable extends WeakRunnable<SatelliteActivity> {
        int position;

        UpdateSatParamRunnable(SatelliteActivity view) {
            super(view);
        }

        @Override
        protected void loadBackground() {
            SatelliteActivity context = mWeakReference.get();

            List<SatInfo_t> satList = SWPDBaseManager.getInstance().getSatList();
            if (satList != null && !satList.isEmpty() && position < satList.size()) {
                SatInfo_t satInfo_t = satList.get(position);
                ChannelNew_t channel_t1 = SWPDBaseManager.getInstance().getChannelInfoBySat(satInfo_t.SatIndex, 0);

                context.mCurrPosition = satInfo_t.SatIndex;

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.mTvLnbPower.setText(Utils.getOnorOff(context, satInfo_t.LnbPower));
                        context.mTvLnb.setText(Utils.getLNB(satInfo_t));
                        context.mTvLnbPowerDiseqc.setText(Utils.getDiseqc(satInfo_t, context.getResources().getStringArray(R.array.DISEQC)));

                        if (channel_t1 != null) {
                            String tpName = channel_t1.Freq + Utils.getVorH(context, channel_t1.Qam) + channel_t1.Symbol;
                            context.mTvLnbPowerFreq.setText(tpName);
                        } else {
                            context.mTvLnbPowerFreq.setText("");
                        }
                    }
                });
            }
        }
    }
}
