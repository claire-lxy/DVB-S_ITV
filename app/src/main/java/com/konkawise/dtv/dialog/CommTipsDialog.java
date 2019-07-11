package com.konkawise.dtv.dialog;

import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class CommTipsDialog extends BaseDialogFragment {
    public static final String TAG = "CommTipsDialog";

    @BindView(R.id.tv_comm_dialog_title)
    TextView mTvTitle;

    @BindView(R.id.tv_comm_dialog_content)
    TextView mTvContent;

    @BindView(R.id.btn_comm_dialog_positive)
    Button mBtnPositive;

    @BindView(R.id.btn_comm_dialog_negative)
    Button mBtnNegative;

    private String mTitle;
    private String mContent;
    private String mPositive;
    private String mNegative;

    private OnCommPositiveListener mOnPositiveListener;
    private OnCommNegativeListener mOnNegativeListener;

    @OnClick(R.id.btn_comm_dialog_positive)
    void positive() {
        dismiss();
        if (mOnPositiveListener != null) {
            mOnPositiveListener.onPositiveListener();
        }
    }

    @OnClick(R.id.btn_comm_dialog_negative)
    void negative() {
        dismiss();
        if (mOnNegativeListener != null) {
            mOnNegativeListener.onNegativeListener();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_comm_tips_layout;
    }

    @Override
    protected void setup(View view) {
        mTvTitle.setText(mTitle);
        mTvContent.setText(mContent);
        mBtnPositive.setText(TextUtils.isEmpty(mPositive) ? getStrings(R.string.yes) : mPositive);
        mBtnNegative.setText(TextUtils.isEmpty(mNegative) ? getStrings(R.string.cancel) : mNegative);
    }

    public CommTipsDialog title(String title) {
        this.mTitle = TextUtils.isEmpty(mTitle) ? "" : title;
        return this;
    }

    public CommTipsDialog content(String content) {
        this.mContent = TextUtils.isEmpty(content) ? "" : content;
        return this;
    }

    public void updateContent(String content) {
        mTvContent.setText(content);
    }

    public CommTipsDialog setOnPositiveListener(String positive, OnCommPositiveListener listener) {
        if (!TextUtils.isEmpty(positive)) this.mPositive = positive;
        this.mOnPositiveListener = listener;
        return this;
    }

    public CommTipsDialog setOnNegativeListener(String negative, OnCommNegativeListener listener) {
        if (!TextUtils.isEmpty(negative)) this.mNegative = negative;
        this.mOnNegativeListener = listener;
        return this;
    }
}
