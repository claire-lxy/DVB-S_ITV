package com.konkawise.dtv.dialog;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.text.TextUtils;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialog;

import butterknife.BindView;
import butterknife.OnClick;

public class BookStandbyDialog extends BaseDialog {
    @BindView(R.id.tv_book_content)
    TextView mTvContent;

    @BindView(R.id.tv_book_channel_name)
    TextView mTvChannelName;

    @BindView(R.id.tv_book_mode)
    TextView mTvMode;

    @BindView(R.id.btn_book_positive)
    Button mBtnPositive;

    @BindView(R.id.btn_book_negative)
    Button mBtnNegative;

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

    public BookStandbyDialog(Context context) {
        super(context);
        Window window = getWindow();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.width = (int) (context.getResources().getDisplayMetrics().widthPixels * 0.5);
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
        setCanceledOnTouchOutside(false);
        setCancelable(false);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_book_standby_layout;
    }

    public BookStandbyDialog content(String content) {
        mTvContent.setText(TextUtils.isEmpty(content) ? "" : content);
        return this;
    }

    public BookStandbyDialog channelName(String channelName) {
        mTvChannelName.setVisibility(TextUtils.isEmpty(channelName) ? View.GONE : View.VISIBLE);
        mTvChannelName.setText(TextUtils.isEmpty(channelName) ? "" : channelName);
        return this;
    }

    public BookStandbyDialog mode(String mode) {
        mTvMode.setText(TextUtils.isEmpty(mode) ? "" : mode);
        return this;
    }

    public void updateContent(String content) {
        mTvContent.setText(content);
    }

    public BookStandbyDialog setOnPositiveListener(String positive, OnCommPositiveListener listener) {
        mBtnPositive.setText(TextUtils.isEmpty(positive) ? getContext().getString(R.string.yes) : positive);
        this.mOnPositiveListener = listener;
        return this;
    }

    public BookStandbyDialog setOnNegativeListener(String negative, OnCommNegativeListener listener) {
        mBtnNegative.setText(TextUtils.isEmpty(negative) ? getContext().getString(R.string.cancel) : negative);
        this.mOnNegativeListener = listener;
        return this;
    }
}
