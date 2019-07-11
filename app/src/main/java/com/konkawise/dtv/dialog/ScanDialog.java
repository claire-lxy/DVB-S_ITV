package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.sw.dvblib.SWFta;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;

public class ScanDialog extends BaseDialogFragment {
    public  static final String TAG = "ScanDialog";

    // S2
    private static final int ITEM_SCAN_MODE = 1;
    private static final int ITEM_NETWORK = 2;
    private static final int ITEM_CAS_SYSTEM = 3;

    // T2
    private static final int ITEM_CHANNEL_TYPE = 2;
    private static final int ITEM_SCAN_TYPE = 3;

    public static final int INSTALLATION_TYPE_S2_SEARCH = 1 << 1;
    public static final int INSTALLATION_TYPE_AUTO_SEARCH = 1 << 2;
    public static final int INSTALLATION_TYPE_MANUAL_SEARCH = 1 << 3;

    @BindView(R.id.item_scan_mode)
    LinearLayout mItemScanMode;

    @BindView(R.id.iv_scan_mode_left)
    ImageView mIvScanModeLeft;

    @BindView(R.id.tv_scan_mode)
    TextView mTvScanMode;

    @BindView(R.id.iv_scan_mode_right)
    ImageView mIvScanModeRight;

    @BindView(R.id.item_channel_type)
    LinearLayout mItemChannelType;

    @BindView(R.id.iv_channel_type_left)
    ImageView mIvChannelTypeLeft;

    @BindView(R.id.tv_channel_type)
    TextView mTvChannelType;

    @BindView(R.id.iv_channel_type_right)
    ImageView mIvChannelTypeRight;

    @BindView(R.id.item_scan_type)
    LinearLayout mItemScanType;

    @BindView(R.id.iv_scan_type_left)
    ImageView mIvScanTypeLeft;

    @BindView(R.id.tv_scan_type)
    TextView mTvScanType;

    @BindView(R.id.iv_scan_type_right)
    ImageView mIvScanTypeRight;

    @BindView(R.id.item_network)
    LinearLayout mItemNetwork;

    @BindView(R.id.iv_network_left)
    ImageView mIvNetworkLeft;

    @BindView(R.id.tv_network)
    TextView mTvNetwork;

    @BindView(R.id.iv_network_right)
    ImageView mIvNetworkRight;

    @BindView(R.id.item_cas_system)
    LinearLayout mItemCasSystem;

    @BindView(R.id.iv_cas_system_left)
    ImageView mIvCasSystemLeft;

    @BindView(R.id.tv_cas_system)
    TextView mTvCasSystem;

    @BindView(R.id.iv_cas_system_right)
    ImageView mIvCasSystemRight;

    @BindArray(R.array.scanMode)
    String[] mScanModeArray;

    @BindArray(R.array.network)
    String[] mNetworkArray;

    @BindArray(R.array.casSystem)
    String[] mCasSystemArray;

    @BindArray(R.array.channelType)
    String[] mChannelTypeArray;

    @BindArray(R.array.scanType)
    String[] mScanTypeArray;

    @OnClick({R.id.item_scan_mode, R.id.item_network, R.id.item_cas_system})
    void scan(View view) {
        dismiss();
        if (mOnScanSearchListener != null) {
            SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_ScanMode.ordinal(), mCurrScanMode);
            if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_Network.ordinal(), mCurrNetwork);
                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_CAS.ordinal(), mCurrCAS);
            }
            mOnScanSearchListener.onClick(view);
        }
    }

    private int mCurrSelectItem = ITEM_SCAN_MODE;

    private int mCurrScanMode;
    private int mCurrChannelType;
    private int mCurrScanType;
    private int mCurrNetwork;
    private int mCurrCAS;

    private int mInstallationType = INSTALLATION_TYPE_S2_SEARCH;

    private View.OnClickListener mOnScanSearchListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_scan;
    }

    @Override
    protected void setup(View view) {
        refreshUI();
    }

    @Override
    protected int resizeDialogWidth() {
        return (int) (getResources().getDisplayMetrics().widthPixels * 0.7);
    }

    public ScanDialog installationType(int installationType) {
        this.mInstallationType = installationType;
        return this;
    }

    private void refreshUI() {
        switch (mInstallationType) {
            case INSTALLATION_TYPE_S2_SEARCH:
                showS2InstallationItem();
                break;
            case INSTALLATION_TYPE_AUTO_SEARCH:
                showAutoSearchInstallationItem();
                break;
            case INSTALLATION_TYPE_MANUAL_SEARCH:
                showManualSearchInstallationItem();
                break;
        }
    }

    private void showS2InstallationItem() {
        mCurrScanMode = SWFtaManager.getInstance().getCurrScanMode();
        mTvScanMode.setText(mScanModeArray[mCurrScanMode]);

        mCurrNetwork = SWFtaManager.getInstance().getCurrNetwork();
        mTvNetwork.setText(mNetworkArray[mCurrNetwork]);

        mCurrCAS = SWFtaManager.getInstance().getCurrCAS();
        mTvCasSystem.setText(mCasSystemArray[mCurrCAS]);
    }

    private void showAutoSearchInstallationItem() {
        mItemNetwork.setVisibility(View.GONE);
        mItemCasSystem.setVisibility(View.GONE);
        mItemChannelType.setVisibility(View.VISIBLE);

        mCurrScanMode = SWFtaManager.getInstance().getCurrScanMode();
        mTvScanMode.setText(mScanModeArray[mCurrScanMode]);

        // channelType
    }

    private void showManualSearchInstallationItem() {
        mItemNetwork.setVisibility(View.GONE);
        mItemCasSystem.setVisibility(View.GONE);
        mItemChannelType.setVisibility(View.VISIBLE);
        mItemScanType.setVisibility(View.VISIBLE);

        mCurrScanMode = SWFtaManager.getInstance().getCurrScanMode();
        mTvScanMode.setText(mScanModeArray[mCurrScanMode]);

        // channelType

        // scanType
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            dismiss();
            if (mOnScanSearchListener != null) {
                SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_ScanMode.ordinal(), mCurrScanMode);
                if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_Network.ordinal(), mCurrNetwork);
                    SWFtaManager.getInstance().setCommE2PInfo(SWFta.E_E2PP.E2P_CAS.ordinal(), mCurrCAS);
                }
                mOnScanSearchListener.onClick(null);
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_CAS_SYSTEM:
                    case ITEM_NETWORK:
                        mCurrSelectItem--;
                        break;
                }
            }

            if (mInstallationType == INSTALLATION_TYPE_AUTO_SEARCH && mCurrSelectItem == ITEM_CHANNEL_TYPE) {
                mCurrSelectItem--;
            }

            if (mInstallationType == INSTALLATION_TYPE_MANUAL_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_TYPE:
                    case ITEM_CHANNEL_TYPE:
                        mCurrSelectItem--;
                        break;
                }
            }
            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_MODE:
                    case ITEM_NETWORK:
                        mCurrSelectItem++;
                        break;
                }
            }

            if (mInstallationType == INSTALLATION_TYPE_AUTO_SEARCH && mCurrSelectItem == ITEM_SCAN_MODE) {
                mCurrSelectItem++;
            }

            if (mInstallationType == INSTALLATION_TYPE_MANUAL_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_MODE:
                    case ITEM_CHANNEL_TYPE:
                        mCurrSelectItem++;
                        break;
                }
            }

            itemFocusChange();
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_MODE:
                        if (--mCurrScanMode < 0) mCurrScanMode = mScanModeArray.length - 1;
                        mTvScanMode.setText(mScanModeArray[mCurrScanMode]);
                        break;

                    case ITEM_NETWORK:
                        if (--mCurrNetwork < 0) mCurrNetwork = mNetworkArray.length - 1;
                        mTvNetwork.setText(mNetworkArray[mCurrNetwork]);
                        break;

                    case ITEM_CAS_SYSTEM:
                        if (--mCurrCAS < 0) mCurrCAS = mCasSystemArray.length - 1;
                        mTvCasSystem.setText(mCasSystemArray[mCurrCAS]);
                        break;
                }
            }

            if (mInstallationType == INSTALLATION_TYPE_AUTO_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_MODE:
                        if (--mCurrScanMode < 0) mCurrScanMode = mScanModeArray.length - 1;
                        mTvScanMode.setText(mScanModeArray[mCurrScanMode]);
                        break;

                    case ITEM_CHANNEL_TYPE:
                        if (--mCurrChannelType < 0) mCurrChannelType = mChannelTypeArray.length - 1;
                        mTvChannelType.setText(mChannelTypeArray[mCurrChannelType]);
                        break;
                }
            }

            if (mInstallationType == INSTALLATION_TYPE_MANUAL_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_MODE:
                        if (--mCurrScanMode < 0) mCurrScanMode = mScanModeArray.length - 1;
                        mTvScanMode.setText(mScanModeArray[mCurrScanMode]);
                        break;

                    case ITEM_CHANNEL_TYPE:
                        if (--mCurrChannelType < 0) mCurrChannelType = mChannelTypeArray.length - 1;
                        mTvChannelType.setText(mChannelTypeArray[mCurrChannelType]);
                        break;

                    case ITEM_SCAN_TYPE:
                        if (--mCurrScanType < 0) mCurrScanType = mScanTypeArray.length - 1;
                        mTvScanType.setText(mScanTypeArray[mCurrScanType]);
                        break;
                }
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT && event.getAction() == KeyEvent.ACTION_DOWN) {
            if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_MODE:
                        if (++mCurrScanMode >= mScanModeArray.length) mCurrScanMode = 0;
                        mTvScanMode.setText(mScanModeArray[mCurrScanMode]);
                        break;

                    case ITEM_NETWORK:
                        if (++mCurrNetwork >= mNetworkArray.length) mCurrNetwork = 0;
                        mTvNetwork.setText(mNetworkArray[mCurrNetwork]);
                        break;

                    case ITEM_CAS_SYSTEM:
                        if (++mCurrCAS >= mCasSystemArray.length) mCurrCAS = 0;
                        mTvCasSystem.setText(mCasSystemArray[mCurrCAS]);
                        break;
                }
            }

            if (mInstallationType == INSTALLATION_TYPE_AUTO_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_MODE:
                        if (++mCurrScanMode >= mScanModeArray.length) mCurrScanMode = 0;
                        mTvScanMode.setText(mScanModeArray[mCurrScanMode]);
                        break;

                    case ITEM_CHANNEL_TYPE:
                        if (++mCurrChannelType >= mChannelTypeArray.length) mCurrChannelType = 0;
                        mTvChannelType.setText(mChannelTypeArray[mCurrChannelType]);
                        break;
                }
            }

            if (mInstallationType == INSTALLATION_TYPE_MANUAL_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_MODE:
                        if (++mCurrScanMode >= mScanModeArray.length) mCurrScanMode = 0;
                        mTvScanMode.setText(mScanModeArray[mCurrScanMode]);
                        break;

                    case ITEM_CHANNEL_TYPE:
                        if (++mCurrChannelType >= mChannelTypeArray.length) mCurrChannelType = 0;
                        mTvChannelType.setText(mChannelTypeArray[mCurrChannelType]);
                        break;

                    case ITEM_SCAN_TYPE:
                        if (++mCurrScanType >= mScanTypeArray.length) mCurrScanType = 0;
                        mTvScanType.setText(mScanTypeArray[mCurrScanType]);
                        break;
                }
            }
        }

        return super.onKeyListener(dialog, keyCode, event);
    }

    private void itemFocusChange() {
        scanModeItemFocusChange();
        channelTypeItemFocusChange();
        scanTypeItemFocusChange();
        networkItemFocusChange();
        casSystemItemFocusChange();
    }

    private void scanModeItemFocusChange() {
        mItemScanMode.setBackgroundResource(mCurrSelectItem == ITEM_SCAN_MODE ? R.drawable.btn_translate_bg_select_shape : 0);
        mIvScanModeLeft.setVisibility(mCurrSelectItem == ITEM_SCAN_MODE ? View.VISIBLE : View.INVISIBLE);
        mTvScanMode.setBackgroundResource(mCurrSelectItem == ITEM_SCAN_MODE ? R.drawable.btn_red_bg_shape : 0);
        mIvScanModeRight.setVisibility(mCurrSelectItem == ITEM_SCAN_MODE ? View.VISIBLE : View.INVISIBLE);
    }

    private void channelTypeItemFocusChange() {
        if (mInstallationType == INSTALLATION_TYPE_AUTO_SEARCH || mInstallationType == INSTALLATION_TYPE_MANUAL_SEARCH) {
            mItemChannelType.setBackgroundResource(mCurrSelectItem == ITEM_CHANNEL_TYPE ? R.drawable.btn_translate_bg_select_shape : 0);
            mIvChannelTypeLeft.setVisibility(mCurrSelectItem == ITEM_CHANNEL_TYPE ? View.VISIBLE : View.INVISIBLE);
            mTvChannelType.setBackgroundResource(mCurrSelectItem == ITEM_CHANNEL_TYPE ? R.drawable.btn_red_bg_shape : 0);
            mIvChannelTypeRight.setVisibility(mCurrSelectItem == ITEM_CHANNEL_TYPE ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void scanTypeItemFocusChange() {
        if (mInstallationType == INSTALLATION_TYPE_MANUAL_SEARCH) {
            mItemScanType.setBackgroundResource(mCurrSelectItem == ITEM_SCAN_TYPE ? R.drawable.btn_translate_bg_select_shape : 0);
            mIvScanTypeLeft.setVisibility(mCurrSelectItem == ITEM_SCAN_TYPE ? View.VISIBLE : View.INVISIBLE);
            mTvScanType.setBackgroundResource(mCurrSelectItem == ITEM_SCAN_TYPE ? R.drawable.btn_red_bg_shape : 0);
            mIvScanTypeRight.setVisibility(mCurrSelectItem == ITEM_SCAN_TYPE ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void networkItemFocusChange() {
        if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
            mItemNetwork.setBackgroundResource(mCurrSelectItem == ITEM_NETWORK ? R.drawable.btn_translate_bg_select_shape : 0);
            mIvNetworkLeft.setVisibility(mCurrSelectItem == ITEM_NETWORK ? View.VISIBLE : View.INVISIBLE);
            mTvNetwork.setBackgroundResource(mCurrSelectItem == ITEM_NETWORK ? R.drawable.btn_red_bg_shape : 0);
            mIvNetworkRight.setVisibility(mCurrSelectItem == ITEM_NETWORK ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void casSystemItemFocusChange() {
        if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
            mItemCasSystem.setBackgroundResource(mCurrSelectItem == ITEM_CAS_SYSTEM ? R.drawable.btn_translate_bg_select_shape : 0);
            mIvCasSystemLeft.setVisibility(mCurrSelectItem == ITEM_CAS_SYSTEM ? View.VISIBLE : View.INVISIBLE);
            mTvCasSystem.setBackgroundResource(mCurrSelectItem == ITEM_CAS_SYSTEM ? R.drawable.btn_red_bg_shape : 0);
            mIvCasSystemRight.setVisibility(mCurrSelectItem == ITEM_CAS_SYSTEM ? View.VISIBLE : View.INVISIBLE);
        }
    }

    public ScanDialog setOnScanSearchListener(View.OnClickListener listener) {
        this.mOnScanSearchListener = listener;
        return this;
    }
}
