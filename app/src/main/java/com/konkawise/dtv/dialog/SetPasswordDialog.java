package com.konkawise.dtv.dialog;

import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.view.LastInputEditText;
import com.sw.dvblib.SWFta;

import butterknife.BindView;
import butterknife.OnClick;

public class SetPasswordDialog extends BaseDialogFragment {
    public static final String TAG = "SetPasswordDialog";

    @BindView(R.id.et_confirm_password)
    LastInputEditText mEt_confirm_Password;

    @BindView(R.id.et_new_password)
    LastInputEditText mEt_new_Password;

    @BindView(R.id.et_current_password)
    LastInputEditText mEt_curr_password;

    @BindView(R.id.tv_set_pwd_cancel)
    TextView mBtnCancel;

    @BindView(R.id.tv_set_pwd_ok)
    TextView mBtnSave;

    private OnSavePasswordListener mOnSavePasswordListener;

    @OnClick(R.id.tv_set_pwd_ok)
    void savePassword() {
        if (isPasswordValid()) {
            dismiss();
            String currentPassword = mEt_curr_password.getText().toString();
            String newPassword = mEt_new_Password.getText().toString();
            if (mOnSavePasswordListener != null) {
                mOnSavePasswordListener.onSavePassword(currentPassword, newPassword);
            }
        }
    }

    private boolean isPasswordValid() {
        String currentPassword = mEt_curr_password.getText().toString();
        String newPassword = mEt_new_Password.getText().toString();
        String confirmPassword = mEt_confirm_Password.getText().toString();
        String password = SWFtaManager.getInstance().getCommPWDInfo(SWFta.E_E2PP.E2P_Password.ordinal());
        if (!currentPassword.equals(password)) {
            Toast.makeText(getContext(), getStrings(R.string.toast_current_password_error), Toast.LENGTH_LONG).show();
            return false;
        } else if (!newPassword.equals(confirmPassword)) {
            Toast.makeText(getContext(), getStrings(R.string.toast_confirm_password_invalid), Toast.LENGTH_LONG).show();
            return false;
        }

        return true;
    }

    @OnClick(R.id.tv_set_pwd_cancel)
    void cancelPassword() {
        dismiss();
        if (mOnSavePasswordListener != null) {
            mOnSavePasswordListener.onCancel();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_set_password;
    }

    @Override
    protected void setup(View view) {
        mBtnCancel.requestFocus();

        mEt_curr_password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keycode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEt_curr_password.setText(EditUtils.getEditSubstring(mEt_curr_password));
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            mEt_new_Password.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEt_new_Password.requestFocus();
                                }
                            }, 100);
                            break;
                    }
                }
                return false;
            }
        });
        mEt_new_Password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keycode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEt_new_Password.setText(EditUtils.getEditSubstring(mEt_new_Password));
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            mEt_confirm_Password.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEt_confirm_Password.requestFocus();
                                }
                            }, 100);
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            mEt_curr_password.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEt_curr_password.requestFocus();
                                }
                            }, 100);
                            break;
                    }
                }
                return false;
            }
        });
        mEt_confirm_Password.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keycode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEt_confirm_Password.setText(EditUtils.getEditSubstring(mEt_confirm_Password));
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            mBtnCancel.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mBtnSave.requestFocus();
                                }
                            }, 100);
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            mEt_new_Password.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEt_new_Password.requestFocus();
                                }
                            }, 100);
                            break;
                    }
                }
                return false;
            }
        });
    }

    public SetPasswordDialog setOnSavePasswordListener(OnSavePasswordListener listener) {
        this.mOnSavePasswordListener = listener;
        return this;
    }

    public interface OnSavePasswordListener {
        void onSavePassword(String currentPassword, String newPassword);

        void onCancel();
    }
}
