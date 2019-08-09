package com.konkawise.dtv.dialog;

import android.content.DialogInterface;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.view.LastInputEditText;

import butterknife.BindView;
import butterknife.OnClick;

public class InputPvrMinuteDialog extends BaseDialogFragment {
    public static final String TAG = "InputPvrMinuteDialog";

    @BindView(R.id.et_pvr_content)
    LastInputEditText mEtPvrMinute;

    @OnClick(R.id.btn_pvr_confirm)
    void confirmRecordMinute() {
        String recordMinutes = mEtPvrMinute.getText().toString();
        if (TextUtils.isEmpty(recordMinutes)) {
            ToastUtils.showToast(R.string.toast_pvr_minute_empty);
            mEtPvrMinute.requestFocus();
            return;
        }
        if (Integer.valueOf(recordMinutes) <= 0) {
            ToastUtils.showToast(R.string.toast_pvr_minute_invalid);
            mEtPvrMinute.requestFocus();
            return;
        }

        if (mCallback != null) {
            mCallback.callback(Integer.valueOf(recordMinutes));
        }

        dismiss();
    }

    private OnCommCallback mCallback;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_input_pvr_minute_layout;
    }

    @Override
    protected void setup(View view) {
        mEtPvrMinute.postDelayed(new Runnable() {
            @Override
            public void run() {
                mEtPvrMinute.setFocusable(true);
                mEtPvrMinute.requestFocus();
            }
        }, 500);

        mEtPvrMinute.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            mEtPvrMinute.setText(EditUtils.getEditSubstring(mEtPvrMinute));
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        return true;
                }
                return false;
            }
        });
    }

    public InputPvrMinuteDialog setOnInputPVRContentCallback(OnCommCallback callback) {
        this.mCallback = callback;
        return this;
    }

    @Override
    protected boolean onKeyListener(DialogInterface dialog, int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            return true;
        }
        return super.onKeyListener(dialog, keyCode, event);
    }
}
