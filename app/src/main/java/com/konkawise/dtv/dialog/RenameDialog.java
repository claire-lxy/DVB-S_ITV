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

    @BindView(R.id.eidt_rename)
    LastInputEditText eidt_rename;

    @BindView(R.id.tv_rename_canncle)
    TextView tv_rename_canncle;

    @BindView(R.id.tv_name_num)
    TextView tv_name_num;

    @BindView(R.id.tv_nametype)
    TextView tv_name_type;

    @OnClick(R.id.tv_rename_sure)
    void ok() {
        dismiss();
        if (eidt_rename.getText().toString().length() > 0) {
            mEditTextLisener.setEdit(eidt_rename.getText().toString());
        }
    }

    @OnClick(R.id.tv_rename_canncle)
    void cancels() {
        dismiss();
    }

    private String mNum;
    private String mProgNo;
    private String mNameType;
    private String mOldName;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_rename;
    }

    @Override
    protected void setup(View view) {
        if (!TextUtils.isEmpty(mNum)) {
            tv_name_num.setText(mNum);
        }
        if (!TextUtils.isEmpty(mProgNo)) {
            tv_name_num.setText(mProgNo);
        }
        tv_name_type.setText(TextUtils.isEmpty(mNameType) ? getStrings(R.string.channel_name) : mNameType);
        eidt_rename.setHint(mOldName);
        eidt_rename.setText(mOldName);

        tv_rename_canncle.requestFocus();
        eidt_rename.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keyCode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            eidt_rename.setText(EditUtils.getEditSubstring(eidt_rename));
                            return true;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            tv_rename_canncle.requestFocus();
                            return true;
                    }
                }
                return false;
            }
        });
    }

    public RenameDialog setTv_name_num(String num) {
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

    public RenameDialog setOldName(String name) {
        this.mOldName = TextUtils.isEmpty(name) ? "" : name;
        return this;
    }

    private EditTextLisener mEditTextLisener;

    public RenameDialog setEditLisener(EditTextLisener lisener) {
        mEditTextLisener = lisener;
        return this;
    }

    public interface EditTextLisener {
        void setEdit(String name);
    }
}
