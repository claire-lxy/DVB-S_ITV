package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;

import java.text.MessageFormat;

import butterknife.BindView;

public class PfDetailDialog extends BaseDialogFragment {
    public static final String TAG = "PfDetailDialog";

    @BindView(R.id.tv_pf_detail_information)
    TextView mTvInformation;

    @BindView(R.id.tv_pf_detail_satellite_name)
    TextView mTvSatelliteName;

    @BindView(R.id.tv_pf_detail_channel_name)
    TextView mTvChannelName;

    @BindView(R.id.tv_pf_detail_vpid)
    TextView mTvVPID;

    @BindView(R.id.tv_pf_detail_apid)
    TextView mTvAPID;

    @BindView(R.id.tv_pf_detail_ppid)
    TextView mTvPPID;

    @BindView(R.id.tv_pf_detail_freq)
    TextView mTvFreq;

    @BindView(R.id.tv_pf_detail_symbol)
    TextView mTvSymbol;

    @BindView(R.id.tv_pf_detail_pol)
    TextView mTvPol;

    private String mInformation;
    private String mSatelliteName;
    private String mChannelName;
    private String mVPID;
    private String mAPID;
    private String mPPID;
    private String mFreq;
    private String mSymbol;
    private String mPol;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_pf_detail_layout;
    }

    @Override
    protected void setup(View view) {
        mTvInformation.setText(TextUtils.isEmpty(mInformation) ? getStrings(R.string.dialog_pf_no_detail) : mInformation);
        mTvSatelliteName.setText(getFormatString(R.string.dialog_pf_satellite_name, TextUtils.isEmpty(mSatelliteName) ? "" : mSatelliteName));
        mTvChannelName.setText(getFormatString(R.string.dialog_pf_channel_name, TextUtils.isEmpty(mChannelName) ? "" : mChannelName));
        mTvVPID.setText(getFormatString(R.string.dialog_pf_vpid, TextUtils.isEmpty(mVPID) ? "0" : mVPID));
        mTvAPID.setText(getFormatString(R.string.dialog_pf_apid, TextUtils.isEmpty(mAPID) ? "0" : mAPID));
        mTvPPID.setText(getFormatString(R.string.dialog_pf_ppid, TextUtils.isEmpty(mPPID) ? "0" : mPPID));
        mTvFreq.setText(getFormatString(R.string.dialog_pf_freq, TextUtils.isEmpty(mFreq) ? "0" : mFreq));
        mTvSymbol.setText(getFormatString(R.string.dialog_pf_symbol, TextUtils.isEmpty(mSymbol) ? "0" : mSymbol));
        mTvPol.setText(getFormatString(R.string.dialog_pf_pol, TextUtils.isEmpty(mPol) ? "0" : mPol));
    }

    private String getFormatString(@StringRes int resId, String value) {
        return MessageFormat.format(getStrings(resId), value);
    }

    public PfDetailDialog information(String information) {
        this.mInformation = information;
        return this;
    }

    public PfDetailDialog satelliteName(String satelliteName) {
        this.mSatelliteName = satelliteName;
        return this;
    }

    public PfDetailDialog channelName(String channelName) {
        this.mChannelName = channelName;
        return this;
    }

    public PfDetailDialog vpid(String vpid) {
        this.mVPID = vpid;
        return this;
    }

    public PfDetailDialog apid(String apid) {
        this.mAPID = apid;
        return this;
    }

    public PfDetailDialog ppid(String ppid) {
        this.mPPID = ppid;
        return this;
    }

    public PfDetailDialog freq(String freq) {
        this.mFreq = freq;
        return this;
    }

    public PfDetailDialog symbol(String symbol) {
        this.mSymbol = symbol;
        return this;
    }

    public PfDetailDialog pol(String pol) {
        this.mPol = pol;
        return this;
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (getDialog() != null && getDialog().isShowing()) {
                getDialog().dismiss();
                return true;
            }
        }
        return super.onKeyListener(dialog, keyCode, event);
    }
}
