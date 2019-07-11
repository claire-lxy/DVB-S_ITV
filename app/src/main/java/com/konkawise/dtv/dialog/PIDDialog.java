package com.konkawise.dtv.dialog;

import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.konkawise.dtv.R;
import com.konkawise.dtv.base.BaseDialogFragment;
import com.konkawise.dtv.utils.EditUtils;
import com.konkawise.dtv.view.LastInputEditText;

import butterknife.BindView;
import butterknife.OnClick;

/**
 * PID对话框
 */
public class PIDDialog extends BaseDialogFragment {
    public static final String TAG = "PIDDialog";

    @BindView(R.id.rl_video_type)
    RelativeLayout rl_video_type;

    @BindView(R.id.rl_auto_type)
    RelativeLayout rl_auto_type;

    @BindView(R.id.eidt_text_video_pid)
    LastInputEditText eidt_text_video_pid;

    @BindView(R.id.edit_text_audio_pid)
    LastInputEditText edit_text_audio_pid;

    @BindView(R.id.edit_text_pdr_pid)
    LastInputEditText edit_text_pdr_pid;

    @BindView(R.id.tv_pid_canncle)
    TextView tv_pid_canncle;

    @OnClick(R.id.tv_pid_sure)
    void ok() {
        vPid = eidt_text_video_pid.getText().toString().trim();
        aPid = edit_text_audio_pid.getText().toString().trim();
        pcrPid = edit_text_pdr_pid.getText().toString().trim();
        if (TextUtils.isEmpty(vPid) || TextUtils.isEmpty(aPid) || TextUtils.isEmpty(pcrPid)) {
            Toast.makeText(getContext(), getStrings(R.string.toast_pid_not_empty), Toast.LENGTH_SHORT).show();
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

    @OnClick(R.id.tv_pid_canncle)
    void cancels() {
        dismiss();
    }

    private String vPid, aPid, pcrPid;
    private OnEditPidListener mOnEditPidListener;

    @Override
    protected int getLayoutId() {
        return R.layout.dialog_pid;
    }

    @Override
    protected void setup(View view) {
        eidt_text_video_pid.setText(vPid);
        edit_text_audio_pid.setText(aPid);
        edit_text_pdr_pid.setText(pcrPid);

        tv_pid_canncle.requestFocus();

        rl_video_type.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            break;
                        case KeyEvent.KEYCODE_ENTER:
                            break;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        rl_auto_type.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            break;
                        case KeyEvent.KEYCODE_DPAD_RIGHT:
                            break;
                        case KeyEvent.KEYCODE_ENTER:
                            break;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        eidt_text_video_pid.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                if (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT && event.getAction() == KeyEvent.ACTION_DOWN) {
                    eidt_text_video_pid.setText(EditUtils.getEditSubstring(eidt_text_video_pid));
                }
                return false;
            }
        });

        edit_text_audio_pid.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            edit_text_audio_pid.setText(EditUtils.getEditSubstring(edit_text_audio_pid));
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            eidt_text_video_pid.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    eidt_text_video_pid.requestFocus();
                                }
                            },100);
                            break;
                        default:
                            break;
                    }
                }
                return false;
            }
        });

        edit_text_pdr_pid.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int i, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN) {
                    switch (event.getKeyCode()) {
                        case KeyEvent.KEYCODE_DPAD_LEFT:
                            edit_text_pdr_pid.setText(EditUtils.getEditSubstring(edit_text_pdr_pid));
                            break;
                        case KeyEvent.KEYCODE_DPAD_UP:
                            edit_text_audio_pid.postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    edit_text_audio_pid.setFocusable(true);
                                    edit_text_audio_pid.requestFocus();
                                }
                            }, 100);
                            break;

                        default:
                            break;
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
