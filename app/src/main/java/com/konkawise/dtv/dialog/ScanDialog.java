package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.base.BaseItemFocusChangeDialogFragment;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.OnClick;
import vendor.konka.hardware.dtvmanager.V1_0.HSetting_Enum_Property;

public class ScanDialog extends BaseItemFocusChangeDialogFragment {
    public static final String TAG = "ScanDialog";

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
            SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.ScanMode, mCurrScanMode);
            if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.Network, mCurrNetwork);
                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.CAS, mCurrCAS);
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
        return R.layout.dialog_scan_layout;
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

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mItemChannelType.getLayoutParams();
        if (lp != null) {
            lp.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            mItemChannelType.setLayoutParams(lp);
        }
    }

    private void showManualSearchInstallationItem() {
        mItemNetwork.setVisibility(View.GONE);
        mItemCasSystem.setVisibility(View.GONE);
        mItemChannelType.setVisibility(View.VISIBLE);
        mItemScanType.setVisibility(View.VISIBLE);

        mCurrScanMode = SWFtaManager.getInstance().getCurrScanMode();
        mTvScanMode.setText(mScanModeArray[mCurrScanMode]);

        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) mItemScanType.getLayoutParams();
        if (lp != null) {
            lp.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 20, getResources().getDisplayMetrics());
            mItemScanType.setLayoutParams(lp);
        }
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER && event.getAction() == KeyEvent.ACTION_DOWN) {
            dismiss();
            if (mOnScanSearchListener != null) {

                SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.ScanMode, mCurrScanMode);
                if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.Network, mCurrNetwork);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.CAS, mCurrCAS);
                } else if (mInstallationType == INSTALLATION_TYPE_MANUAL_SEARCH) {
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.CAS, mCurrChannelType);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.ScanMode, mCurrScanMode);
                } else if (mInstallationType == INSTALLATION_TYPE_AUTO_SEARCH) {
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.CAS, mCurrChannelType);
                    SWFtaManager.getInstance().setCommE2PInfo(HSetting_Enum_Property.ScanMode, mCurrScanMode);
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

                    case ITEM_SCAN_MODE:
                        mCurrSelectItem = ITEM_CAS_SYSTEM;
                        mItemCasSystem.requestFocus();
                        itemFocusChange();
                        return true;
                }
            }

            if (mInstallationType == INSTALLATION_TYPE_AUTO_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_CHANNEL_TYPE:
                        mCurrSelectItem--;
                        break;

                    case ITEM_SCAN_MODE:
                        mCurrSelectItem = ITEM_CHANNEL_TYPE;
                        mItemChannelType.requestFocus();
                        itemFocusChange();
                        return true;
                }
            }

            if (mInstallationType == INSTALLATION_TYPE_MANUAL_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_TYPE:
                    case ITEM_CHANNEL_TYPE:
                        mCurrSelectItem--;
                        break;

                    case ITEM_SCAN_MODE:
                        mCurrSelectItem = ITEM_SCAN_TYPE;
                        mItemScanType.requestFocus();
                        itemFocusChange();
                        return true;
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

                    case ITEM_CAS_SYSTEM:
                        mCurrSelectItem = ITEM_SCAN_MODE;
                        mItemScanMode.requestFocus();
                        itemFocusChange();
                        return true;
                }
            }

            if (mInstallationType == INSTALLATION_TYPE_AUTO_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_MODE:
                        mCurrSelectItem++;
                        break;

                    case ITEM_CHANNEL_TYPE:
                        mCurrSelectItem = ITEM_SCAN_MODE;
                        mItemScanMode.requestFocus();
                        itemFocusChange();
                        return true;
                }
            }

            if (mInstallationType == INSTALLATION_TYPE_MANUAL_SEARCH) {
                switch (mCurrSelectItem) {
                    case ITEM_SCAN_MODE:
                    case ITEM_CHANNEL_TYPE:
                        mCurrSelectItem++;
                        break;

                    case ITEM_SCAN_TYPE:
                        mCurrSelectItem = ITEM_SCAN_MODE;
                        mItemScanMode.requestFocus();
                        itemFocusChange();
                        return true;
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
        itemChange(mCurrSelectItem, ITEM_SCAN_MODE, mItemScanMode, mIvScanModeLeft, mIvScanModeRight, mTvScanMode);
        channelTypeItemFocusChange();
        scanTypeItemFocusChange();
        networkItemFocusChange();
        casSystemItemFocusChange();
    }

    private void channelTypeItemFocusChange() {
        if (mInstallationType == INSTALLATION_TYPE_AUTO_SEARCH || mInstallationType == INSTALLATION_TYPE_MANUAL_SEARCH) {
            itemChange(mCurrSelectItem, ITEM_CHANNEL_TYPE, mItemChannelType, mIvChannelTypeLeft, mIvChannelTypeRight, mTvChannelType);
        }
    }

    private void scanTypeItemFocusChange() {
        if (mInstallationType == INSTALLATION_TYPE_MANUAL_SEARCH) {
            itemChange(mCurrSelectItem, ITEM_SCAN_TYPE, mItemScanType, mIvScanTypeLeft, mIvScanTypeRight, mTvScanType);
        }
    }

    private void networkItemFocusChange() {
        if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
            itemChange(mCurrSelectItem, ITEM_NETWORK, mItemNetwork, mIvNetworkLeft, mIvNetworkRight, mTvNetwork);
        }
    }

    private void casSystemItemFocusChange() {
        if (mInstallationType == INSTALLATION_TYPE_S2_SEARCH) {
            itemChange(mCurrSelectItem, ITEM_CAS_SYSTEM, mItemCasSystem, mIvCasSystemLeft, mIvCasSystemRight, mTvCasSystem);
        }
    }

    public ScanDialog setOnScanSearchListener(View.OnClickListener listener) {
        this.mOnScanSearchListener = listener;
        return this;
    }
}
