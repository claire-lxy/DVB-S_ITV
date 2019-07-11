package com.konkawise.dtv.dialog;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class SearchProgramDialog extends BaseDialogFragment {
    public static final String TAG = "SearchProgramDialog";

    @BindView(R.id.tv_text)
    TextView mTextView;

    @OnClick(R.id.btn_search)
    void search(View view) {
        dismiss();
        if (mBtnSearchlickListener != null) {
            mBtnSearchlickListener.onClick(view);
        }
    }

    private String mMessage;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_search_channel;
    }

    @Override
    protected void setup(View view) {
        mTextView.setText(TextUtils.isEmpty(mMessage) ? getStrings(R.string.single_satellite_search) : mMessage);
    }

    public SearchProgramDialog setMessage(String message) {
        this.mMessage = TextUtils.isEmpty(message) ? "" : message;
        return this;
    }

    private View.OnClickListener mBtnSearchlickListener;

    public SearchProgramDialog setBtnOnclickLisener(View.OnClickListener listener) {
        mBtnSearchlickListener = listener;
        return this;
    }
}
