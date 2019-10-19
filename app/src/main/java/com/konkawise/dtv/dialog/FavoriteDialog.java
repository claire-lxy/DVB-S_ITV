package com.konkawise.dtv.dialog;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.konkawise.dtv.DTVProgramManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.adapter.CheckGroupAdapter;
import com.konkawise.dtv.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import vendor.konka.hardware.dtvmanager.V1_0. HProg_Struct_ProgInfo;

public class FavoriteDialog extends BaseDialogFragment {
    public static final String TAG = "FavoriteDialog";

    @BindView(R.id.tv_check_group_title)
    TextView mTvTitle;

    @BindView(R.id.lv_check_group)
    ListView mListView;

    @BindView(R.id.tv_sure)
    TextView mBtnSure;

    @OnClick(R.id.tv_sure)
    void saveFavorite() {
        dismiss();
        if (mOnCheckGroupCallback != null) {
            mOnCheckGroupCallback.callback(mAdapter.getCheckMap());
        }
    }

    @OnClick(R.id.tv_canncle)
    void cancels() {
        dismiss();
        if (mOnCheckGroupCallback != null) {
            mOnCheckGroupCallback.cancel();
        }
    }

    @OnItemClick(R.id.lv_check_group)
    void onItemClick(int position) {
        mAdapter.setCheck(position);
    }

    private CheckGroupAdapter mAdapter;
    private  HProg_Struct_ProgInfo mCurrChannelInfo;
    private OnCheckGroupCallback mOnCheckGroupCallback;
    private String mTitle;
    private boolean mMulti;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_check_group_layout;
    }

    @Override
    protected void setup(View view) {
        mTvTitle.setText(mTitle);

        mAdapter = new CheckGroupAdapter(getContext(), DTVProgramManager.getInstance().getFavoriteGroupNameList(DTVProgramManager.getInstance().getFavIndexArray().length));
        mListView.setAdapter(mAdapter);
        mListView.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                        if (mListView.getSelectedItemPosition() == mAdapter.getCount() - 1) {
                            mBtnSure.requestFocus();
                            return true;
                        }
                    }

                    if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                        if (mListView.getSelectedItemPosition() == 0) {
                            mListView.setSelection(mAdapter.getCount() - 1);
                            return true;
                        }
                    }
                }

                return false;
            }
        });

        if (mCurrChannelInfo != null) {
            char[] favGroupArray = DTVProgramManager.getInstance().getProgInfoFavGroupArray(mCurrChannelInfo);
            if (!mMulti) {
                for (int i = 0; i < favGroupArray.length; i++) {
                    if (favGroupArray[i] == '1') {
                        mAdapter.setCheck(i);
                    }
                }
            }
        }
    }

    public FavoriteDialog title(String title) {
        this.mTitle = TextUtils.isEmpty(title) ? "" : title;
        return this;
    }

    public FavoriteDialog multi(boolean isMulti) {
        this.mMulti = isMulti;
        return this;
    }

    public FavoriteDialog setData( HProg_Struct_ProgInfo channelInfo) {
        this.mCurrChannelInfo = channelInfo;
        return this;
    }

    public FavoriteDialog setOnCheckGroupCallback(OnCheckGroupCallback callback) {
        this.mOnCheckGroupCallback = callback;
        return this;
    }
}
