package com.konkawise.dtv.dialog;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.view.LastInputEditText;

import butterknife.BindView;
import butterknife.OnClick;

public class RenameDialog extends BaseDialogFragment {
    public static final String TAG = "RenameDialog";

    @BindView(R.id.et_rename)
    LastInputEditText mEtRename;

    @BindView(R.id.tv_rename_cancel)
    TextView mBtnCancel;

    @BindView(R.id.tv_number)
    TextView mTvNumber;

    @BindView(R.id.tv_name_type)
    TextView mTvNameType;

    @OnClick(R.id.tv_rename_sure)
    void ok() {
        dismiss();
        if (mEtRename.getText().toString().length() > 0) {
            mEditTextListener.onRenameEdit(mEtRename.getText().toString());
        }
    }

    @OnClick(R.id.tv_rename_cancel)
    void cancels() {
        dismiss();
    }

    private String mNum;
    private String mProgNo;
    private String mNameType;
    private String mName;
    private String mNameHint;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_rename_layout;
    }

    @Override
    protected void setup(View view) {
        if (!TextUtils.isEmpty(mNum)) mTvNumber.setText(mNum);
        if (!TextUtils.isEmpty(mProgNo)) mTvNumber.setText(mProgNo);
        mTvNameType.setText(TextUtils.isEmpty(mNameType) ? getStrings(R.string.channel_name) : mNameType);
        mEtRename.setHint(mNameHint);
        mEtRename.setText(mName);

        mBtnCancel.requestFocus();
        mEtRename.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEtRename.setText(EditUtils.getEditSubstring(mEtRename));
                            return true;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            mBtnCancel.requestFocus();
                            return true;
                    }
                }
                return false;
            }
        });
    }

    public RenameDialog setNum(String num) {
        this.mNum = TextUtils.isEmpty(num) ? "" : num;
        return this;
    }

    public RenameDialog setProgNo(int proNo) {
        this.mProgNo = String.valueOf(proNo);
        return this;
    }

    public RenameDialog setNameType(String name) {
        this.mNameType = TextUtils.isEmpty(name) ? "" : name;
        return this;
    }

    public RenameDialog setName(String name) {
        this.mName = TextUtils.isEmpty(name) ? "" : name;
        return this;
    }

    public RenameDialog setNameHint(String hint) {
        this.mNameHint = TextUtils.isEmpty(hint) ? mName : hint;
        return this;
    }

    private onRenameEditListener mEditTextListener;

    public RenameDialog setOnRenameEditListener(onRenameEditListener listener) {
        mEditTextListener = listener;
        return this;
    }

    public interface onRenameEditListener {
        void onRenameEdit(String newName);
    }
}
