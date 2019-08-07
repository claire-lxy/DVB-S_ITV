package com.konkawise.dtv.dialog;

import android.view.KeyEvent;
import android.view.View;
import android.widget.Button;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.view.LastInputEditText;

import butterknife.BindView;

public class SetPVRTimeDialog extends BaseDialogFragment {
    public static final String TAG = "SetPVRTimeDialog";

    @BindView(R.id.edit_time)
    LastInputEditText etTime;

    private OnTimeInputListener timeInputListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_set_pvr_time;
    }

    @Override
    protected void setup(View view) {
        // 防止软键盘弹出
        etTime.postDelayed(new Runnable() {
            @Override
            public void run() {
                etTime.setFocusable(true);
                etTime.requestFocus();
            }
        }, 500);

        etTime.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                switch (event.getKeyCode()) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                        if (event.getAction() == KeyEvent.ACTION_DOWN) {
                            etTime.setText(EditUtils.getEditSubstring(etTime));
                        }
                        return true;
                    case KeyEvent.KEYCODE_DPAD_CENTER:
                        String minuteStr = etTime.getText().toString();
                        timeInputListener.onPasswordInput(Integer.parseInt(minuteStr));
                        dismiss();
                        return true;
                }
                return false;
            }
        });
    }



    public SetPVRTimeDialog setOnPasswordInputListener(OnTimeInputListener listener) {
        this.timeInputListener = listener;
        return this;
    }

    public interface OnTimeInputListener {
        void onPasswordInput(int minute);
    }
}
