package com.konkawise.dtv.dialog;

import android.content.Context;
import android.text.TextUtils;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialog;

import butterknife.BindView;
import butterknife.OnClick;

public class CommTipsSystemDialog extends BaseDialog {

    @BindView(R.id.tv_comm_dialog_title)
    TextView mTvTitle;

    @BindView(R.id.tv_comm_dialog_content)
    TextView mTvContent;

    @BindView(R.id.btn_comm_dialog_positive)
    Button mBtnPositive;

    @BindView(R.id.btn_comm_dialog_negative)
    Button mBtnNegative;

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

    private OnCommPositiveListener mOnPositiveListener;
    private OnCommNegativeListener mOnNegativeListener;

    public CommTipsSystemDialog(Context context) {
        super(context);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.5);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_comm_tips_layout;
    }

    public CommTipsSystemDialog title(String title) {
        mTvTitle.setText(TextUtils.isEmpty(title) ? "" : title);
        return this;
    }

    public CommTipsSystemDialog content(String content) {
        mTvContent.setText(TextUtils.isEmpty(content) ? "" : content);
        return this;
    }

    public CommTipsSystemDialog setOnPositiveListener(String positive, OnCommPositiveListener listener) {
        if (!TextUtils.isEmpty(positive)) mBtnPositive.setText(positive);
        this.mOnPositiveListener = listener;
        return this;
    }

    public CommTipsSystemDialog setOnNegativeListener(String negative, OnCommNegativeListener listener) {
        if (!TextUtils.isEmpty(negative)) mBtnNegative.setText(negative);
        this.mOnNegativeListener = listener;
        return this;
    }
}
