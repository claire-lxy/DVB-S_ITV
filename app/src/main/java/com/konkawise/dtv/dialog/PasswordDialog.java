package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.support.annotation.IntDef;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;

import com.konkawise.dtv.DTVSettingManager;
import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.view.LastInputEditText;

import butterknife.BindView;
import vendor.konka.hardware.dtvmanager.V1_0.HSetting_Enum_Property;

/**
 * 密码对话框
 */
public class PasswordDialog extends BaseDialogFragment implements TextWatcher {
    public static final String TAG = "PasswordDialog";

    private static final int PARENTAL_PASSWORD_MAX_LENGTH = 4;
    public static final int CONTROL_ARROW_CURRENT_PROG = 1 << 1;
    public static final int CONTROL_ARROW_NEXT_PROG = 1 << 2;
    public static final int CONTROL_ARROW_LAST_PROG = 1 << 3;

    @BindView(R.id.et_password)
    LastInputEditText mEtPassword;

    @IntDef(flag = true, value = {
            CONTROL_ARROW_CURRENT_PROG, CONTROL_ARROW_NEXT_PROG, CONTROL_ARROW_LAST_PROG
    })
    private @interface ControlArrowPlayProgType {
    }

    private OnPasswordInputListener mOnPasswordInputListener;
    private OnControlArrowKeyListener mOnControlArrowKeyListener;
    private OnKeyListener mOnKeyListener;

    private String mCurrentPassword;
    private int mInputPasswordInvalidCount;
    private boolean mInvalidClose; // 是否输入密码三次后失败自动关闭弹框

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_password_layout;
    }

    @Override
    protected void setup(View view) {
        // 防止软键盘弹出
        mEtPassword.postDelayed(new Runnable() {
            @Override
            public void run() {
                mEtPassword.setFocusable(true);
                mEtPassword.requestFocus();
            }
        }, 500);

        mEtPassword.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            mEtPassword.setText(EditUtils.getEditSubstring(mEtPassword));
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        return true;
                }
                return false;
            }
        });
        mEtPassword.addTextChangedListener(this);
    }

    @Override
    public void dismiss() {
        if (mEtPassword != null) {
            mEtPassword.removeTextChangedListener(this);
        }
        super.dismiss();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        if (s.length() < PARENTAL_PASSWORD_MAX_LENGTH) return;

        if (mCurrentPassword == null) {
            mCurrentPassword = DTVSettingManager.getInstance().getPasswd(HSetting_Enum_Property.Password);
        }

        boolean isPasswordValid;
        if (TextUtils.equals(s.toString(), mCurrentPassword)) {
            isPasswordValid = true;
            reset();
            dismiss();
        } else {
            isPasswordValid = false;
            mEtPassword.setText("");
            mInputPasswordInvalidCount++;
            if (mInputPasswordInvalidCount >= 3 && mInvalidClose) {
                reset();
                dismiss();
            }
        }

        if (mOnPasswordInputListener != null) {
            mOnPasswordInputListener.onPasswordInput(s.toString(), mCurrentPassword, isPasswordValid);
        }
        if (mOnControlArrowKeyListener != null) {
            mOnControlArrowKeyListener.onControlArrowKey(CONTROL_ARROW_CURRENT_PROG);
        }
    }

    private void reset() {
        mCurrentPassword = null;
        mEtPassword.setText("");
        mInputPasswordInvalidCount = 0;
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (mOnKeyListener != null) {
            return mOnKeyListener.onKeyListener(this, keyCode, event);
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnControlArrowKeyListener != null) {
                mOnControlArrowKeyListener.onControlArrowKey(CONTROL_ARROW_NEXT_PROG);
                return true;
            }
        }

        if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN && event.getAction() == KeyEvent.ACTION_UP) {
            if (mOnControlArrowKeyListener != null) {
                mOnControlArrowKeyListener.onControlArrowKey(CONTROL_ARROW_LAST_PROG);
                return true;
            }
        }

        return super.onKeyListener(dialog, keyCode, event);
    }

    public PasswordDialog setInvalidClose(boolean invalidClose) {
        this.mInvalidClose = invalidClose;
        return this;
    }

    public PasswordDialog setOnPasswordInputListener(OnPasswordInputListener listener) {
        this.mOnPasswordInputListener = listener;
        return this;
    }

    public PasswordDialog setOnControlArrowKeyListener(OnControlArrowKeyListener listener) {
        this.mOnControlArrowKeyListener = listener;
        return this;
    }

    public PasswordDialog setOnKeyListener(OnKeyListener listener) {
        this.mOnKeyListener = listener;
        return this;
    }

    public interface OnPasswordInputListener {
        void onPasswordInput(String inputPassword, String currentPassword, boolean isValid);
    }

    public interface OnControlArrowKeyListener {
        void onControlArrowKey(@ControlArrowPlayProgType int playProgType);
    }

    public interface OnKeyListener {
        boolean onKeyListener(PasswordDialog dialog, int keyCode, KeyEvent event);
    }
}
