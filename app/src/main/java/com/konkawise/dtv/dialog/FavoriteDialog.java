package com.konkawise.dtv.dialog;

import android.text.TextUtils;
import android.util.SparseArray;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWPDBaseManager;
import com.konkawise.dtv.adapter.CheckGroupAdapter;
import com.konkawise.dtv.base.BaseDialogFragment;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnItemClick;
import vendor.konka.hardware.dtvmanager.V1_0.PDPMInfo_t;

public class FavoriteDialog extends BaseDialogFragment {
    public static final String TAG = "FavoriteDialog";

    @BindView(R.id.tv_check_group_title)
    TextView mTvTitle;

    @BindView(R.id.lv_check_group)
    ListView mListView;

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
    private PDPMInfo_t mCurrChannelInfo;
    private OnCheckGroupCallback mOnCheckGroupCallback;
    private String mTitle;
    private boolean mMulti;

    private SparseArray<List<PDPMInfo_t>> mFavoriteChannelsMap;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_check_group_layout;
    }

    @Override
    protected void setup(View view) {
        mTvTitle.setText(mTitle);

        if (mFavoriteChannelsMap != null && mFavoriteChannelsMap.size() > 0) {
            mAdapter = new CheckGroupAdapter(getContext(), SWPDBaseManager.getInstance().getFavoriteGroupNameList(mFavoriteChannelsMap.size()));
            mListView.setAdapter(mAdapter);
        }

        // 如果是多选，都不选中
        if (!mMulti) {
            for (int checkIndex : findChannelFavoriteIndexs()) {
                mAdapter.setCheck(checkIndex);
            }
        }
    }

    private List<Integer> findChannelFavoriteIndexs() {
        List<Integer> checkIndexs = new ArrayList<>();
        if (mFavoriteChannelsMap == null || mFavoriteChannelsMap.size() == 0) return checkIndexs;

        for (int i = 0; i < mFavoriteChannelsMap.size(); i++ ) {
            if (isChannelFavorite(mFavoriteChannelsMap.get(i))) {
                checkIndexs.add(i);
            }
        }
        return checkIndexs;
    }

    private boolean isChannelFavorite(List<PDPMInfo_t> favList) {
        if (favList == null || favList.isEmpty() || mCurrChannelInfo == null) return false;
        for (PDPMInfo_t channelInfo : favList) {
            if (channelInfo.ProgIndex == mCurrChannelInfo.ProgIndex) {
                return true;
            }
        }
        return false;
    }

    public FavoriteDialog title(String title) {
        this.mTitle = TextUtils.isEmpty(title) ? "" : title;
        return this;
    }

    public FavoriteDialog multi(boolean isMulti) {
        this.mMulti = isMulti;
        return this;
    }

    public FavoriteDialog setData(SparseArray<List<PDPMInfo_t>> map, PDPMInfo_t channelInfo) {
        this.mFavoriteChannelsMap = map;
        this.mCurrChannelInfo = channelInfo;
        return this;
    }

    public FavoriteDialog setOnCheckGroupCallback(OnCheckGroupCallback callback) {
        this.mOnCheckGroupCallback = callback;
        return this;
    }
}
