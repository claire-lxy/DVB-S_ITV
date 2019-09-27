package com.konkawise.dtv.dialog;

import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;

import java.text.MessageFormat;

import butterknife.BindView;
import butterknife.OnClick;

public class SearchResultDialog extends BaseDialogFragment {
    public static final String TAG = "SearchResultDialog";

    @BindView(R.id.tv_search_no_program)
    TextView mTvSearchNoProgram;

    @BindView(R.id.ll_search_result)
    ViewGroup mSearchResultLayout;

    @BindView(R.id.tv_search_tv_size)
    TextView mTvSearchTvSize;

    @BindView(R.id.tv_search_radio_size)
    TextView mTvSearchRadioSize;

    @OnClick(R.id.btn_confirm_search_result)
    void confirmSearchResult(View view) {
        dismiss();
        if (mOnConfirmResultListener != null) {
            mOnConfirmResultListener.onClick(view);
        }
    }

    private int mTvSize;
    private int mRadioSize;
    private String mSearchNoProgramContent;
    private View.OnClickListener mOnConfirmResultListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_search_result_layout;
    }

    @Override
    protected void setup(View view) {
        if (mTvSize != 0 || mRadioSize != 0) {
            mTvSearchNoProgram.setVisibility(View.GONE);
            mSearchResultLayout.setVisibility(View.VISIBLE);
            mTvSearchTvSize.setText(MessageFormat.format(getStrings(R.string.dialog_search_result_tv), String.valueOf(mTvSize)));
            mTvSearchRadioSize.setText(MessageFormat.format(getStrings(R.string.dialog_search_result_radio), String.valueOf(mRadioSize)));
        } else {
            if (!TextUtils.isEmpty(mSearchNoProgramContent)) {
                mTvSearchNoProgram.setText(mSearchNoProgramContent);
            }
            mTvSearchNoProgram.setVisibility(View.VISIBLE);
            mSearchResultLayout.setVisibility(View.GONE);
        }
    }

    public SearchResultDialog tvSize(int size) {
        this.mTvSize = size;
        return this;
    }

    public SearchResultDialog radioSize(int size) {
        this.mRadioSize = size;
        return this;
    }

    public SearchResultDialog searchNoProgramContent(String content) {
        if (!TextUtils.isEmpty(content)) this.mSearchNoProgramContent = content;
        return this;
    }

    public SearchResultDialog setOnConfirmResultListener(View.OnClickListener listener) {
        this.mOnConfirmResultListener = listener;
        return this;
    }
}
