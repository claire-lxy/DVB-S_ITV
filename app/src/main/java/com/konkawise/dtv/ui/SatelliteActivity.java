package com.konkawise.dtv.ui;

import android.content.Intent;
import android.util.Log;
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
import com.konkawise.dtv.event.MotorTypeChangeEvent;
import com.konkawise.dtv.utils.Utils;
import com.konkawise.dtv.view.TVListView;
import com.konkawise.dtv.weaktool.WeakAsyncTask;
import com.konkawise.dtv.weaktool.WeakRunnable;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

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
    private int mCurrPosition;

    private UpdateSatParamRunnable mUpdateSatParamRunnable;

    @Override
    public int getLayoutId() {
        return R.layout.activity_satellite;
    }

    @Override
    protected void setup() {
        EventBus.getDefault().register(this);

        mTvBottomBarBlue.setVisibility(View.GONE);

        mAdapter = new SatelliteListAdapter(this, new ArrayList<>());
        mListView.setAdapter(mAdapter);

        mUpdateSatParamRunnable = new UpdateSatParamRunnable(this);
        new LoadSatelliteTask(this).execute();
    }

    @Override
    protected void onDestroy() {
        EventBus.getDefault().unregister(this);
        super.onDestroy();
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
                context.mListView.setSelection(0);
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
                            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mAdapter.getItem(mCurrPosition).SatIndex);
                            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_ACTIVITY, 1);
                            startActivity(intent);
                            finish();
                        }
                    }).show(getSupportFragmentManager(), SearchProgramDialog.TAG);
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_GREEN) {
            Intent intent = new Intent(this, EditManualActivity.class);
            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mAdapter.getItem(mCurrPosition).SatIndex);
            startActivityForResult(intent, REQUEST_CODE_SATELLITE_EDIT);
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_PROG_YELLOW) {
            Intent intent = new Intent(this, TpListingActivity.class);
            intent.putExtra(Constants.IntentKey.INTENT_SATELLITE_INDEX, mAdapter.getItem(mCurrPosition).SatIndex);
            intent.putExtra(Constants.IntentKey.INTENT_LNB, mTvLnb.getText().toString());
            intent.putExtra(Constants.IntentKey.ITENT_DISEQC, mTvDiSEqC.getText().toString());
            intent.putExtra(Constants.IntentKey.INTENT_MOTOR_TYPE, mTvMotorType.getText().toString());
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

            List<SatInfo_t> satList = SWPDBaseManager.getInstance().getSatList();
            if (satList != null && !satList.isEmpty() && context.mCurrPosition < satList.size()) {
                SatInfo_t satInfo_t = satList.get(context.mCurrPosition);
                ChannelNew_t channel_t1 = SWPDBaseManager.getInstance().getChannelInfoBySat(satInfo_t.SatIndex, 0);

                context.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        context.mTvLnb.setText(Utils.getLNB(satInfo_t));
                        context.mTvLnbPower.setText(Utils.getOnorOff(context, satInfo_t.LnbPower));
                        context.mTvDiSEqC.setText(Utils.getDiseqc(satInfo_t, context.getResources().getStringArray(R.array.DISEQC)));
                        context.mTvMotorType.setText(Utils.getMotorType(context, satInfo_t));

                        if (channel_t1 != null && channel_t1.Freq > 0) {
                            String tpName = channel_t1.Freq + Utils.getVorH(context, channel_t1.Qam) + channel_t1.Symbol;
                            context.mTvFreq.setText(tpName);
                        } else {
                            context.mTvFreq.setText("");
                        }
                    }
                });
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onReceiveMotorTypeChange(MotorTypeChangeEvent event) {
        if (event.isMotorTypeChange) {
            updateUI();
        }
    }
}
