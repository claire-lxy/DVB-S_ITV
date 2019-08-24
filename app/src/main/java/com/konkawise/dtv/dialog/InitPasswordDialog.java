package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.text.Editable;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.view.LastInputEditText;

import butterknife.BindView;
import butterknife.OnClick;
import butterknife.OnTextChanged;

public class InitPasswordDialog extends BaseDialogFragment {
    public static final String TAG = "InitPasswordDialog";

    @BindView(R.id.et_init_password)
    LastInputEditText mEtInitPassword;

    @BindView(R.id.et_confirm_init_password)
    LastInputEditText mEtConfirmInitPassword;

    @BindView(R.id.tv_init_pwd_ok)
    TextView mBtnSave;

    @OnClick(R.id.tv_init_pwd_ok)
    void savePassword() {
        if (isPasswordValid()) {
            dismiss();
            if (mOnSavePasswordListener != null) {
                mOnSavePasswordListener.onSavePassword(mEtInitPassword.getText().toString());
            }
        }
    }

    @OnTextChanged(value = R.id.et_init_password, callback = OnTextChanged.Callback.AFTER_TEXT_CHANGED)
    void initPasswordTextChange(Editable editable) {
        if (editable.length() >= 4) {
            mEtConfirmInitPassword.requestFocus();
        }
    }

    private OnSavePasswordListener mOnSavePasswordListener;
    private OnKeyListener mOnKeyListener;

    private boolean isPasswordValid() {
        String initPassword = mEtInitPassword.getText().toString();
        if(initPassword.length() < 4) {
            ToastUtils.showToast(R.string.toast_invalid_password_length);
            return false;
        }

        String confirmPassword = mEtConfirmInitPassword.getText().toString();
        if (!initPassword.equals(confirmPassword)) {
            ToastUtils.showToast(R.string.toast_confirm_password_invalid);
            return false;
        }

        return true;
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_init_password_layout;
    }

    @Override
    protected void setup(View view) {
        mEtInitPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keycode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEtInitPassword.setText(EditUtils.getEditSubstring(mEtInitPassword));
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            mEtConfirmInitPassword.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtConfirmInitPassword.requestFocus();
                                }
                            }, 100);
                            break;
                    }
                }
                return false;
            }
        });
        mEtConfirmInitPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keycode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEtConfirmInitPassword.setText(EditUtils.getEditSubstring(mEtConfirmInitPassword));
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            mBtnSave.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mBtnSave.requestFocus();
                                }
                            }, 100);
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            mEtInitPassword.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtInitPassword.requestFocus();
                                }
                            }, 100);
                            break;
                    }
                }
                return false;
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null && getDialog().getWindow() != null) {
            getDialog().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_NOTHING);
        }
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if(mOnKeyListener != null)
            return mOnKeyListener.onKeyListener(this, keyCode, event);

        return super.onKeyListener(dialog, keyCode, event);
    }

    public InitPasswordDialog setOnSavePasswordListener(OnSavePasswordListener listener) {
        this.mOnSavePasswordListener = listener;
        return this;
    }

    public InitPasswordDialog setOnKeyListener(OnKeyListener listener) {
        this.mOnKeyListener = listener;
        return this;
    }

    public interface OnSavePasswordListener {
        void onSavePassword(String password);
    }

    public interface OnKeyListener {
        boolean onKeyListener(InitPasswordDialog dialog, int keyCode, KeyEvent event);
    }
}
