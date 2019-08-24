package com.konkawise.dtv.dialog;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.utils.ToastUtils;
import com.konkawise.dtv.view.LastInputEditText;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * PID对话框
 */
public class PIDDialog extends BaseDialogFragment {
    public static final String TAG = "PIDDialog";

    @BindView(R.id.et_video_pid)
    LastInputEditText mEtVideoPid;

    @BindView(R.id.et_audio_pid)
    LastInputEditText mEtAudioPid;

    @BindView(R.id.et_pcr_pid)
    LastInputEditText mEtPcrPid;

    @BindView(R.id.tv_pid_cancel)
    TextView mBtnCancel;

    @OnClick(R.id.tv_pid_sure)
    void ok() {
        vPid = mEtVideoPid.getText().toString().trim();
        aPid = mEtAudioPid.getText().toString().trim();
        pcrPid = mEtPcrPid.getText().toString().trim();
        if (TextUtils.isEmpty(vPid) || TextUtils.isEmpty(aPid) || TextUtils.isEmpty(pcrPid)) {
            ToastUtils.showToast(R.string.toast_pid_not_empty);
            return;
        }
        if (mOnEditPidListener != null) {
            int[] pids = new int[3];
            pids[0] = Integer.parseInt(vPid);
            pids[1] = Integer.parseInt(aPid);
            pids[2] = Integer.parseInt(pcrPid);
            mOnEditPidListener.onPidEdit(pids);
        }
        dismiss();
    }

    @OnClick(R.id.tv_pid_cancel)
    void cancels() {
        dismiss();
    }

    private String vPid, aPid, pcrPid;
    private OnEditPidListener mOnEditPidListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_pid_layout;
    }

    @Override
    protected void setup(View view) {
        mEtVideoPid.setText(vPid);
        mEtAudioPid.setText(aPid);
        mEtPcrPid.setText(pcrPid);

        mBtnCancel.requestFocus();

        mEtVideoPid.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                    mEtVideoPid.setText(EditUtils.getEditSubstring(mEtVideoPid));
                    return true;
                }
                return false;
            }
        });

        mEtAudioPid.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEtAudioPid.setText(EditUtils.getEditSubstring(mEtAudioPid));
                            return true;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            mEtVideoPid.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtVideoPid.requestFocus();
                                }
                            },100);
                            return true;
                    }
                }
                return false;
            }
        });

        mEtPcrPid.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            mEtPcrPid.setText(EditUtils.getEditSubstring(mEtPcrPid));
                            return true;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            mEtAudioPid.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    mEtAudioPid.setFocusable(true);
                                    mEtAudioPid.requestFocus();
                                }
                            }, 100);
                            return true;
                    }
                }
                return false;
            }
        });
    }

    public PIDDialog setPids(int[] pids) {
        if (pids.length >= 3) {
            this.vPid = String.valueOf(pids[0]);
            this.aPid = String.valueOf(pids[1]);
            this.pcrPid = String.valueOf(pids[2]);
        }
        return this;
    }

    public PIDDialog setOnEditPidListener(OnEditPidListener listener) {
        this.mOnEditPidListener = listener;
        return this;
    }

    public interface OnEditPidListener {
        void onPidEdit(int[] pids);
    }
}
