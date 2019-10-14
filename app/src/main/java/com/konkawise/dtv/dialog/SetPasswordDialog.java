package com.konkawise.dtv.dialog;

import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.SWFtaManager;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.view.LastInputEditText;

import butterknife.BindView;
import butterknife.OnClick;
import vendor.konka.hardware.dtvmanager.V1_0.HSetting_Enum_Property;

public class SetPasswordDialog extends BaseDialogFragment {
    public static final String TAG = "SetPasswordDialog";

    @BindView(R.id.et_confirm_password)
    LastInputEditText mEtConfirmPassword;

    @BindView(R.id.et_new_password)
    LastInputEditText mEtNewPassword;

    @BindView(R.id.et_current_password)
    LastInputEditText mEtCurrentPassword;

    @BindView(R.id.tv_set_password_cancel)
    TextView mBtnCancel;

    @BindView(R.id.tv_set_password_sure)
    TextView mBtnSave;

    private OnSavePasswordListener mOnSavePasswordListener;

    @OnClick(R.id.tv_set_password_sure)
    void savePassword() {
        if (isPasswordValid()) {
            dismiss();
            String currentPassword = mEtCurrentPassword.getText().toString();
            String newPassword = mEtNewPassword.getText().toString();
            if (mOnSavePasswordListener != null) {
                mOnSavePasswordListener.onSavePassword(currentPassword, newPassword);
            }
        }
    }

    private boolean isPasswordValid() {
        String currentPassword = mEtCurrentPassword.getText().toString();
        String newPassword = mEtNewPassword.getText().toString();
        String confirmPassword = mEtConfirmPassword.getText().toString();
        String password = SWFtaManager.getInstance().getCommPWDInfo(HSetting_Enum_Property.Password);
        if (!currentPassword.equals(password)) {
            ToastUtils.showToast(R.string.toast_current_password_error);
            return false;
        } else if (!newPassword.equals(confirmPassword)) {
           ToastUtils.showToast(R.string.toast_confirm_password_invalid);
            return false;
        }

        return true;
    }

    @OnClick(R.id.tv_set_password_cancel)
    void cancelPassword() {
        dismiss();
        if (mOnSavePasswordListener != null) {
            mOnSavePasswordListener.onCancel();
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_set_password_layout;
    }

    @Override
    protected void setup(View view) {
        mBtnCancel.requestFocus();

        mEtCurrentPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keycode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEtCurrentPassword.setText(EditUtils.getEditSubstring(mEtCurrentPassword));
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            mEtNewPassword.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtNewPassword.requestFocus();
                                }
                            }, 100);
                            break;
                    }
                }
                return false;
            }
        });
        mEtNewPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keycode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEtNewPassword.setText(EditUtils.getEditSubstring(mEtNewPassword));
                            break;
                        case KeyEvent.KEYCODE_DPAD_DOWN:
                            mEtConfirmPassword.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtConfirmPassword.requestFocus();
                                }
                            }, 100);
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            mEtCurrentPassword.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtCurrentPassword.requestFocus();
                                }
                            }, 100);
                            break;
                    }
                }
                return false;
            }
        });
        mEtConfirmPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keycode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (keycode) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEtConfirmPassword.setText(EditUtils.getEditSubstring(mEtConfirmPassword));
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
                            mEtNewPassword.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtNewPassword.requestFocus();
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
