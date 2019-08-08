package com.konkawise.dtv.dialog;

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

public class RecordMinuteDialog extends BaseDialogFragment {
    public static final String TAG = "RecordMinuteDialog";

    @BindView(R.id.et_record_minute)
    LastInputEditText mEtRecordMinute;

    @OnClick(R.id.btn_confirm_record_minute)
    void confirmRecordMinute() {
        String recordMinutes = mEtRecordMinute.getText().toString();
        if (TextUtils.isEmpty(recordMinutes)) {
            ToastUtils.showToast(R.string.toast_record_minute_empty);
            mEtRecordMinute.requestFocus();
            return;
        }
        if (Integer.valueOf(recordMinutes) <= 0) {
            ToastUtils.showToast(R.string.toast_record_minute_invalid);
            mEtRecordMinute.requestFocus();
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
        return R.layout.dialog_record_minute_layout;
    }

    @Override
    protected void setup(View view) {
        if (getDialog() != null) getDialog().setCancelable(false);
        mEtRecordMinute.postDelayed(new Runnable() {
            @Override
            public void run() {
                mEtRecordMinute.setFocusable(true);
                mEtRecordMinute.requestFocus();
            }
        }, 500);

        mEtRecordMinute.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            mEtRecordMinute.setText(EditUtils.getEditSubstring(mEtRecordMinute));
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        return true;
                }
                return false;
            }
        });
    }

    public RecordMinuteDialog setOnInputRecordMinuteCallback(OnCommCallback callback) {
        this.mCallback = callback;
        return this;
    }
}
